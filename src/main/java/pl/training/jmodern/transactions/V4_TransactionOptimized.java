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
import java.util.stream.Collectors;

/*
 * V4 — Change from V3: PostgreSQL tuning — disables synchronous_commit, increases work_mem
 *      to 256MB, drops PK before inserts and re-adds after, wraps all inserts in a single
 *      transaction (autocommit off).
 * Why: synchronous_commit=OFF skips waiting for WAL flush (safe for bulk loads). Dropping PK
 *      avoids index maintenance during inserts. Single transaction reduces WAL overhead.
 * Unchanged: StAX parsing + batch inserts same as V3.
 */
public class V4_TransactionOptimized implements TransactionProcessor {

    private static final int BATCH_SIZE = 10_000;

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        var parseStart = Instant.now();
        List<Transaction> transactions = parseStax(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        var summaryStart = Instant.now();
        var sumByCurrency = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — single transaction, drop PK, tuned settings
        var dbStart = Instant.now();
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            DatabaseHelper.createSchema(conn);
            DatabaseHelper.cleanTable(conn);

            conn.setAutoCommit(false); // V4: Single transaction wrapping all inserts — reduces WAL overhead vs V3's autocommit
            try (var stmt = conn.createStatement()) {
                stmt.execute("SET synchronous_commit = OFF"); // V4: Skip waiting for WAL flush — safe for bulk loads, significant speedup
                stmt.execute("SET work_mem = '256MB'"); // V4: More memory for sort/aggregate operations
                stmt.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_pkey"); // V4: Drop PK before bulk insert — avoids index maintenance per row
            }

            insertBatch(conn, transactions);

            try (var stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE transactions ADD PRIMARY KEY (id)"); // V4: Restore PK after all inserts — single index build is faster than incremental
            }
            conn.commit();
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

    public static void main(String[] args) {
        var file = args.length > 0 ? args[0] : "transactions-1_000_000.xml";
        var jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/transactions";
        var processor = new V4_TransactionOptimized();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
