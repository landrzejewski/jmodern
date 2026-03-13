package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import javax.xml.parsers.DocumentBuilderFactory;
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
 * V1 — Baseline implementation.
 * Parsing: DOM — loads entire XML tree into memory (high memory usage, simple API).
 * Summary: Sequential stream with groupingBy.
 * DB: String-concatenated SQL, one INSERT per row, autocommit on
 *     (maximum round-trips, SQL injection risk).
 */
public class V1_DomBaseline implements TransactionProcessor {

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        // Parse — DOM loads entire XML into memory
        var parseStart = Instant.now();
        List<Transaction> transactions = parseDom(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        // Summary — stream + groupingBy
        var summaryStart = Instant.now();
        var sumByCurrency = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — string-concatenated SQL, one INSERT per row, autocommit
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

    private List<Transaction> parseDom(Path xmlFile) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var doc = factory.newDocumentBuilder().parse(xmlFile.toFile());
            var nodes = doc.getElementsByTagName("transaction");
            var transactions = new ArrayList<Transaction>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); i++) {
                var el = (org.w3c.dom.Element) nodes.item(i);
                var amountEl = (org.w3c.dom.Element) el.getElementsByTagName("amount").item(0);
                var cardEl = (org.w3c.dom.Element) el.getElementsByTagName("card").item(0);
                transactions.add(new Transaction(
                        el.getAttribute("id"),
                        el.getAttribute("timestamp"),
                        new BigDecimal(amountEl.getTextContent()),
                        amountEl.getAttribute("currency"),
                        el.getAttribute("status"),
                        el.getElementsByTagName("description").item(0).getTextContent(),
                        cardEl.getElementsByTagName("type").item(0).getTextContent(),
                        cardEl.getElementsByTagName("masked-number").item(0).getTextContent(),
                        cardEl.getElementsByTagName("holder").item(0).getTextContent(),
                        cardEl.getElementsByTagName("expiry").item(0).getTextContent()
                ));
            }
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
        var processor = new V1_DomBaseline();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
