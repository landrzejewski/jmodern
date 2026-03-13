package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import java.io.FileInputStream;
import java.math.BigDecimal;
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
 * V5 — Change from V4: Parallel DB inserts using ExecutorService thread pool + parallelStream
 *      for summary aggregation.
 * Why: Partitions transactions across N connections (one per CPU core), each doing batch inserts
 *      concurrently. Saturates multiple PostgreSQL backend processes simultaneously.
 * Unchanged: StAX parsing still sequential (single-threaded).
 */
public class V5_ParallelProcessing implements TransactionProcessor {

    private static final int BATCH_SIZE = 10_000;

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        var parseStart = Instant.now();
        List<Transaction> transactions = parseStax(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        // Summary — parallelStream for aggregation
        var summaryStart = Instant.now();
        var sumByCurrency = transactions.parallelStream() // V5: parallelStream replaces V4's sequential stream for concurrent aggregation
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — concurrent batch inserts across multiple connections
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

        // V5: Partition data across N threads (one per CPU core), each with its own connection
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

    private List<Transaction> parseStax(Path xmlFile) {
        try (var input = new FileInputStream(xmlFile.toFile())) {
            var factory = XMLInputFactory.newInstance();
            var reader = factory.createXMLStreamReader(input);
            var transactions = new ArrayList<Transaction>();

            String id = null, timestamp = null, status = null, currency = null;
            String description = null, cardType = null, maskedNumber = null, cardHolder = null, cardExpiry = null;
            BigDecimal amount = null;
            String currentElement = null;

            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        var name = reader.getLocalName();
                        currentElement = name;
                        if ("transaction".equals(name)) {
                            id = reader.getAttributeValue(null, "id");
                            timestamp = reader.getAttributeValue(null, "timestamp");
                            status = reader.getAttributeValue(null, "status");
                        } else if ("amount".equals(name)) {
                            currency = reader.getAttributeValue(null, "currency");
                        }
                    }
                    case XMLStreamConstants.CHARACTERS -> {
                        var text = reader.getText().trim();
                        if (text.isEmpty()) continue;
                        switch (currentElement) {
                            case "amount" -> amount = new BigDecimal(text);
                            case "description" -> description = text;
                            case "type" -> cardType = text;
                            case "masked-number" -> maskedNumber = text;
                            case "holder" -> cardHolder = text;
                            case "expiry" -> cardExpiry = text;
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        if ("transaction".equals(reader.getLocalName())) {
                            transactions.add(new Transaction(
                                    id, timestamp, amount, currency, status,
                                    description, cardType, maskedNumber, cardHolder, cardExpiry
                            ));
                        }
                        currentElement = null;
                    }
                }
            }
            reader.close();
            return transactions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    // V5: Split list into roughly equal chunks for parallel processing
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
        var processor = new V5_ParallelProcessing();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
