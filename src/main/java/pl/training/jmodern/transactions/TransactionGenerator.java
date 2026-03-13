package pl.training.jmodern.transactions;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class TransactionGenerator {

    enum TransactionStatus { COMPLETED, PENDING, FAILED, REVERSED }

    enum CardType {
        VISA("4", 16),
        MASTERCARD("5", 16),
        AMEX("3", 15),
        DISCOVER("6", 16);

        final String binPrefix;
        final int numberLength;

        CardType(String binPrefix, int numberLength) {
            this.binPrefix = binPrefix;
            this.numberLength = numberLength;
        }
    }

    enum CurrencyCode {
        USD("$"), EUR("€"), GBP("£"), PLN("zł");

        final String symbol;

        CurrencyCode(String symbol) {
            this.symbol = symbol;
        }
    }

    record CardDetails(String maskedNumber, CardType cardType, String holderName, String expiry) {}

    record Transaction(UUID id, Instant timestamp, BigDecimal value, CurrencyCode currency,
                       TransactionStatus status, String description, CardDetails card) {}

    record GeneratorConfig(int count, Path outputPath) {
        GeneratorConfig() {
            this(100, Path.of("transactions.xml"));
        }
    }

    private static final String[] FIRST_NAMES = {
            "John", "Jane", "Michael", "Sarah", "Robert", "Emily", "David", "Anna", "James", "Maria"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Wilson", "Taylor"
    };

    private static final String[] MERCHANTS = {
            "Amazon.com", "Walmart", "Target", "Best Buy", "Apple Store",
            "Netflix", "Spotify", "Uber", "Starbucks", "Home Depot"
    };

    private static final String[] DESCRIPTION_PATTERNS = {
            "Payment to %s", "Purchase at %s", "Online order from %s",
            "Subscription - %s", "Refund from %s"
    };

    private final Random random = new Random();

    public List<Transaction> generate(GeneratorConfig config) {
        return IntStream.range(0, config.count())
                .mapToObj(_ -> generateTransaction())
                .toList();
    }

    public void writeXml(List<Transaction> transactions, Path outputPath) {
        try {
            var docFactory = DocumentBuilderFactory.newInstance();
            var doc = docFactory.newDocumentBuilder().newDocument();

            var root = doc.createElement("transactions");
            root.setAttribute("generated", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
            root.setAttribute("count", String.valueOf(transactions.size()));
            doc.appendChild(root);

            for (var tx : transactions) {
                var txElement = doc.createElement("transaction");
                txElement.setAttribute("id", tx.id().toString());
                txElement.setAttribute("timestamp", tx.timestamp().toString());
                txElement.setAttribute("status", tx.status().name());

                var amount = doc.createElement("amount");
                amount.setAttribute("currency", tx.currency().name());
                amount.setTextContent(tx.value().toPlainString());
                txElement.appendChild(amount);

                var desc = doc.createElement("description");
                desc.setTextContent(tx.description());
                txElement.appendChild(desc);

                var card = doc.createElement("card");

                var type = doc.createElement("type");
                type.setTextContent(tx.card().cardType().name());
                card.appendChild(type);

                var masked = doc.createElement("masked-number");
                masked.setTextContent(tx.card().maskedNumber());
                card.appendChild(masked);

                var holder = doc.createElement("holder");
                holder.setTextContent(tx.card().holderName());
                card.appendChild(holder);

                var expiry = doc.createElement("expiry");
                expiry.setTextContent(tx.card().expiry());
                card.appendChild(expiry);

                txElement.appendChild(card);
                root.appendChild(txElement);
            }

            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.transform(new DOMSource(doc), new StreamResult(outputPath.toFile()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to write XML", e);
        }
    }

    public void generateAndWrite(GeneratorConfig config) {
        var count = config.count();
        var outputPath = config.outputPath();
        try (var writer = new BufferedWriter(
                new OutputStreamWriter(Files.newOutputStream(outputPath), StandardCharsets.UTF_8),
                8 * 1024 * 1024)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            writer.write("<transactions count=\"" + count + "\" generated=\""
                    + Instant.now().truncatedTo(ChronoUnit.SECONDS) + "\">\n");
            for (int i = 0; i < count; i++) {
                var tx = generateTransaction();
                writeTransactionXml(writer, tx);
                if (count >= 1_000_000 && (i + 1) % 1_000_000 == 0) {
                    System.out.printf("  Progress: %,d / %,d transactions written%n", i + 1, count);
                }
            }
            writer.write("</transactions>\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write XML", e);
        }
    }

    private void writeTransactionXml(BufferedWriter writer, Transaction tx) throws IOException {
        writer.write("    <transaction id=\"");
        writer.write(tx.id().toString());
        writer.write("\" status=\"");
        writer.write(tx.status().name());
        writer.write("\" timestamp=\"");
        writer.write(tx.timestamp().toString());
        writer.write("\">\n        <amount currency=\"");
        writer.write(tx.currency().name());
        writer.write("\">");
        writer.write(tx.value().toPlainString());
        writer.write("</amount>\n        <description>");
        writer.write(escapeXml(tx.description()));
        writer.write("</description>\n        <card>\n            <type>");
        writer.write(tx.card().cardType().name());
        writer.write("</type>\n            <masked-number>");
        writer.write(tx.card().maskedNumber());
        writer.write("</masked-number>\n            <holder>");
        writer.write(escapeXml(tx.card().holderName()));
        writer.write("</holder>\n            <expiry>");
        writer.write(tx.card().expiry());
        writer.write("</expiry>\n        </card>\n    </transaction>\n");
    }

    private static String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private Transaction generateTransaction() {
        var id = UUID.randomUUID();
        var timestamp = Instant.now().minus(random.nextLong(30 * 24 * 60), ChronoUnit.MINUTES);
        var value = BigDecimal.valueOf(0.50 + random.nextDouble() * 9999.49).setScale(2, RoundingMode.HALF_UP);
        var currency = randomElement(CurrencyCode.values());
        var status = randomStatus();
        var merchant = randomElement(MERCHANTS);
        var pattern = randomElement(DESCRIPTION_PATTERNS);
        var description = pattern.formatted(merchant) + " - Order #" + (10000 + random.nextInt(90000));
        var card = generateCard();
        return new Transaction(id, timestamp, value, currency, status, description, card);
    }

    private CardDetails generateCard() {
        var cardType = randomElement(CardType.values());
        var suffix = String.format("%04d", random.nextInt(10000));
        var maskedNumber = "****-****-****-" + suffix;
        var holderName = randomElement(FIRST_NAMES) + " " + randomElement(LAST_NAMES);
        var expiryMonth = YearMonth.now().plusMonths(1 + random.nextInt(48));
        var expiry = String.format("%02d/%d", expiryMonth.getMonthValue(), expiryMonth.getYear());
        return new CardDetails(maskedNumber, cardType, holderName, expiry);
    }

    private TransactionStatus randomStatus() {
        var roll = random.nextDouble();
        return switch ((int) (roll * 100)) {
            case int n when n < 60 -> TransactionStatus.COMPLETED;
            case int n when n < 75 -> TransactionStatus.PENDING;
            case int n when n < 90 -> TransactionStatus.FAILED;
            default -> TransactionStatus.REVERSED;
        };
    }

    private <T> T randomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    public static void main(String[] args) {
        var count = args.length > 0 ? Integer.parseInt(args[0]) : 100_000_000;
        var outputPath = args.length > 1 ? Path.of(args[1]) : Path.of("transactions-100_000_000.xml");
        var config = new GeneratorConfig(count, outputPath);

        var generator = new TransactionGenerator();
        generator.generateAndWrite(config);
        System.out.printf("Generated %d transactions to %s%n", count, outputPath);
    }

}
