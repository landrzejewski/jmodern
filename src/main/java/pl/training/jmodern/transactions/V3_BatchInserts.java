package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * V3 — Change from V2: String-concatenated SQL → PreparedStatement + batch inserts (batches of 10,000).
 * Why: PreparedStatement avoids SQL re-parsing on every row and prevents SQL injection.
 *      Batch execution sends multiple rows per round-trip, reducing network overhead significantly.
 * Unchanged: StAX parsing same as V2.
 */
public class V3_BatchInserts implements TransactionProcessor {

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

        // DB — PreparedStatement + batch inserts
        var dbStart = Instant.now();
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            DatabaseHelper.createSchema(conn);
            DatabaseHelper.cleanTable(conn);
            insertBatch(conn, transactions);
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

    // V3: PreparedStatement + batch execution (10K/batch) — replaces V2's string-concatenated single-row inserts
    private void insertBatch(Connection conn, List<Transaction> transactions) throws Exception {
        var sql = "INSERT INTO transactions VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var ps = conn.prepareStatement(sql)) {
            int count = 0;
            for (var tx : transactions) {
                bindTransaction(ps, tx);
                ps.addBatch();
                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
    }

    // V3: Parameter binding prevents SQL injection and avoids SQL re-parsing per row
    static void bindTransaction(PreparedStatement ps, Transaction tx) throws Exception {
        ps.setString(1, tx.id());
        ps.setString(2, tx.timestamp());
        ps.setBigDecimal(3, tx.amount());
        ps.setString(4, tx.currency());
        ps.setString(5, tx.status());
        ps.setString(6, tx.description());
        ps.setString(7, tx.cardType());
        ps.setString(8, tx.maskedNumber());
        ps.setString(9, tx.cardHolder());
        ps.setString(10, tx.cardExpiry());
    }

    public static void main(String[] args) {
        var file = args.length > 0 ? args[0] : "transactions-1_000_000.xml";
        var jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/transactions";
        var processor = new V3_BatchInserts();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
