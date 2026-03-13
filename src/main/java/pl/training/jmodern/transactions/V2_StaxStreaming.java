package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
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
 * V2 — Change from V1: DOM → StAX (Streaming API for XML) for parsing.
 * Why: StAX is event-driven, reads one element at a time — drastically lower memory
 *      footprint (no full DOM tree in memory).
 * Unchanged: DB inserts still use string concatenation, one INSERT per row.
 */
public class V2_StaxStreaming implements TransactionProcessor {

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        // Parse — StAX streaming, one transaction at a time
        var parseStart = Instant.now();
        List<Transaction> transactions = parseStax(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        // Summary
        var summaryStart = Instant.now();
        var sumByCurrency = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — still single inserts with string concatenation (unchanged from V1)
        var dbStart = Instant.now();
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            DatabaseHelper.createSchema(conn);
            DatabaseHelper.cleanTable(conn);
            insertAll(conn, transactions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var dbTime = Duration.between(dbStart, Instant.now());

        return new TransactionSummary(
                transactions.size(), sumByCurrency, countByStatus, countByCardType,
                parseTime, summaryTime, dbTime, Duration.between(totalStart, Instant.now())
        );
    }

    // V2: Replaced DOM parsing with StAX — event-driven, one element at a time, no full tree in memory
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

    private void insertAll(Connection conn, List<Transaction> transactions) throws Exception {
        try (var stmt = conn.createStatement()) {
            for (var tx : transactions) {
                stmt.execute("INSERT INTO transactions VALUES ('"
                        + tx.id() + "', '"
                        + tx.timestamp() + "', "
                        + tx.amount() + ", '"
                        + tx.currency() + "', '"
                        + tx.status() + "', '"
                        + tx.description().replace("'", "''") + "', '"
                        + tx.cardType() + "', '"
                        + tx.maskedNumber() + "', '"
                        + tx.cardHolder().replace("'", "''") + "', '"
                        + tx.cardExpiry() + "')");
            }
        }
    }

    public static void main(String[] args) {
        var file = args.length > 0 ? args[0] : "transactions-1_000_000.xml";
        var jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/transactions";
        var processor = new V2_StaxStreaming();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
