package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/*
 * V6 — Change from V5: StAX → memory-mapped I/O (MappedByteBuffer) + custom string-scanning parser.
 * Why: Memory-mapped files bypass Java's I/O buffering layers; OS handles paging. Custom parser
 *      avoids XML library overhead (no SAX/StAX event dispatch).
 * Unchanged: Parallel DB inserts same as V5.
 */
public class V6_MemoryMappedParser implements TransactionProcessor {

    private static final int BATCH_SIZE = 10_000;

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        // Parse — memory-mapped I/O + custom byte-scanning parser
        var parseStart = Instant.now();
        List<Transaction> transactions = parseMapped(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        // Summary — parallel
        var summaryStart = Instant.now();
        var sumByCurrency = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — concurrent inserts (same as V5)
        var dbStart = Instant.now();
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            DatabaseHelper.createSchema(conn);
            DatabaseHelper.cleanTable(conn);
            try (var stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_pkey");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int threads = Runtime.getRuntime().availableProcessors();
        var partitions = partition(transactions, threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (var partition : partitions) {
                futures.add(executor.submit(() -> {
                    try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
                        conn.setAutoCommit(false);
                        try (var stmt = conn.createStatement()) {
                            stmt.execute("SET synchronous_commit = OFF");
                        }
                        insertBatch(conn, partition);
                        conn.commit();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
            for (var f : futures) f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }

        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            try (var stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE transactions ADD PRIMARY KEY (id)");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var dbTime = Duration.between(dbStart, Instant.now());

        return new TransactionSummary(
                transactions.size(), sumByCurrency, countByStatus, countByCardType,
                parseTime, summaryTime, dbTime, Duration.between(totalStart, Instant.now())
        );
    }

    // V6: Memory-mapped I/O — OS handles paging, bypasses Java's I/O buffering layers
    private List<Transaction> parseMapped(Path xmlFile) {
        try (var raf = new RandomAccessFile(xmlFile.toFile(), "r");
             var channel = raf.getChannel()) {

            long fileSize = channel.size();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            byte[] bytes = new byte[(int) fileSize];
            buffer.get(bytes);
            var content = new String(bytes, StandardCharsets.UTF_8);

            var transactions = new ArrayList<Transaction>();
            var txTag = "<transaction ";
            var txEnd = "</transaction>";
            int pos = 0;

            while (true) {
                int start = content.indexOf(txTag, pos);
                if (start == -1) break;
                int end = content.indexOf(txEnd, start);
                if (end == -1) break;
                end += txEnd.length();

                var block = content.substring(start, end);
                transactions.add(parseTransaction(block));
                pos = end;
            }
            return transactions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // V6: Custom string-scanning parser — no SAX/StAX event dispatch overhead
    private Transaction parseTransaction(String block) {
        var id = extractAttr(block, "id=\"");
        var timestamp = extractAttr(block, "timestamp=\"");
        var status = extractAttr(block, "status=\"");
        var currency = extractAttr(block, "currency=\"");
        var amountStr = extractTagContent(block, "<amount", "</amount>");
        var amount = new BigDecimal(amountStr);
        var description = extractTagContent(block, "<description>", "</description>");
        var cardType = extractTagContent(block, "<type>", "</type>");
        var maskedNumber = extractTagContent(block, "<masked-number>", "</masked-number>");
        var cardHolder = extractTagContent(block, "<holder>", "</holder>");
        var cardExpiry = extractTagContent(block, "<expiry>", "</expiry>");

        return new Transaction(id, timestamp, amount, currency, status,
                description, cardType, maskedNumber, cardHolder, cardExpiry);
    }

    private String extractAttr(String block, String prefix) {
        int start = block.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = block.indexOf('"', start);
        return block.substring(start, end);
    }

    private String extractTagContent(String block, String openTag, String closeTag) {
        int start = block.indexOf(openTag);
        if (start == -1) return "";
        start = block.indexOf('>', start) + 1;
        int end = block.indexOf(closeTag, start);
        return block.substring(start, end).trim();
    }

    private void insertBatch(Connection conn, List<Transaction> transactions) throws Exception {
        var sql = "INSERT INTO transactions VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var ps = conn.prepareStatement(sql)) {
            int count = 0;
            for (var tx : transactions) {
                V3_BatchInserts.bindTransaction(ps, tx);
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int parts) {
        var partitions = new ArrayList<List<T>>();
        int size = list.size();
        int chunkSize = (size + parts - 1) / parts;
        for (int i = 0; i < size; i += chunkSize) {
            partitions.add(list.subList(i, Math.min(i + chunkSize, size)));
        }
        return partitions;
    }

    public static void main(String[] args) {
        var file = args.length > 0 ? args[0] : "transactions-1_000_000.xml";
        var jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/transactions";
        var processor = new V6_MemoryMappedParser();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
