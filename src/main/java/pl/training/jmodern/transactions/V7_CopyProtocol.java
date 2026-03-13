package pl.training.jmodern.transactions;

import pl.training.jmodern.transactions.TransactionProcessing.*;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import java.io.FileInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * V7 — Change from V6: Batch INSERT → PostgreSQL COPY protocol via CopyManager.
 * Why: COPY is PostgreSQL's bulk-load pathway — sends raw CSV over the wire, bypasses SQL
 *      parsing/planning entirely. Typically 5-10x faster than batch INSERTs for large datasets.
 * Note: Reverts to StAX parsing (simpler than V6's custom parser), builds full CSV in memory.
 */
public class V7_CopyProtocol implements TransactionProcessor {

    @Override
    public TransactionSummary process(Path xmlFile, String jdbcUrl) {
        var totalStart = Instant.now();

        // Parse — StAX
        var parseStart = Instant.now();
        List<Transaction> transactions = parseStax(xmlFile);
        var parseTime = Duration.between(parseStart, Instant.now());

        // Summary — parallelStream
        var summaryStart = Instant.now();
        var sumByCurrency = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::currency,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));
        var countByStatus = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::status, Collectors.counting()));
        var countByCardType = transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::cardType, Collectors.counting()));
        var summaryTime = Duration.between(summaryStart, Instant.now());

        // DB — COPY protocol
        var dbStart = Instant.now();
        try (var conn = DriverManager.getConnection(jdbcUrl, "bench", "bench")) {
            DatabaseHelper.createSchema(conn);
            DatabaseHelper.cleanTable(conn);
            conn.setAutoCommit(false);

            try (var stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE transactions DROP CONSTRAINT IF EXISTS transactions_pkey");
                stmt.execute("SET synchronous_commit = OFF");
                stmt.execute("SET work_mem = '256MB'");
            }

            // V7: Build CSV in memory for PostgreSQL COPY — bypasses SQL parsing/planning entirely
            var csv = new StringBuilder(transactions.size() * 200);
            for (var tx : transactions) {
                csv.append(escapeCsv(tx.id())).append(',');
                csv.append(escapeCsv(tx.timestamp())).append(',');
                csv.append(tx.amount()).append(',');
                csv.append(escapeCsv(tx.currency())).append(',');
                csv.append(escapeCsv(tx.status())).append(',');
                csv.append(escapeCsv(tx.description())).append(',');
                csv.append(escapeCsv(tx.cardType())).append(',');
                csv.append(escapeCsv(tx.maskedNumber())).append(',');
                csv.append(escapeCsv(tx.cardHolder())).append(',');
                csv.append(escapeCsv(tx.cardExpiry())).append('\n');
            }

            // V7: COPY protocol — sends raw CSV over the wire, typically 5-10x faster than batch INSERTs
            CopyManager copyManager = conn.unwrap(PGConnection.class).getCopyAPI();
            copyManager.copyIn(
                    "COPY transactions FROM STDIN WITH (FORMAT csv)",
                    new StringReader(csv.toString())
            );

            try (var stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE transactions ADD PRIMARY KEY (id)");
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

    // V7: CSV escaping for COPY protocol — handles commas, quotes, newlines
    static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // V7: Reverted to StAX from V6's custom parser — simpler, COPY is now the bottleneck
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

    public static void main(String[] args) {
        var file = args.length > 0 ? args[0] : "transactions-1_000_000.xml";
        var jdbcUrl = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/transactions";
        var processor = new V7_CopyProtocol();
        var summary = processor.process(Path.of(file), jdbcUrl);
        DatabaseHelper.printSummary(summary);
    }
}
