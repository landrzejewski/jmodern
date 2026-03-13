package pl.training.jmodern.transactions;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;

public class TransactionProcessing {

    record Transaction(
            String id, String timestamp, BigDecimal amount, String currency,
            String status, String description, String cardType, String maskedNumber,
            String cardHolder, String cardExpiry
    ) {}

    record TransactionSummary(
            long totalCount,
            Map<String, BigDecimal> sumByCurrency,
            Map<String, Long> countByStatus,
            Map<String, Long> countByCardType,
            Duration parseTime,
            Duration summaryTime,
            Duration dbTime,
            Duration totalTime
    ) {}

    interface TransactionProcessor {
        TransactionSummary process(Path xmlFile, String jdbcUrl);
    }

    static class DatabaseHelper {

        static void createSchema(Connection conn) throws SQLException {
            try (var stmt = conn.createStatement()) {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS transactions (
                            id VARCHAR(36) PRIMARY KEY,
                            timestamp VARCHAR(30),
                            amount DECIMAL(12,2),
                            currency VARCHAR(3),
                            status VARCHAR(10),
                            description VARCHAR(255),
                            card_type VARCHAR(12),
                            card_masked VARCHAR(19),
                            card_holder VARCHAR(100),
                            card_expiry VARCHAR(7)
                        )
                        """);
            }
        }

        static void cleanTable(Connection conn) throws SQLException {
            try (var stmt = conn.createStatement()) {
                stmt.execute("TRUNCATE TABLE transactions");
            }
        }

        static void printSummary(TransactionSummary summary) {
            System.out.println("=== Transaction Processing Summary ===");
            System.out.printf("Total transactions: %,d%n", summary.totalCount());
            System.out.println("Sum by currency:");
            summary.sumByCurrency().forEach((k, v) -> System.out.printf("  %s: %,.2f%n", k, v));
            System.out.println("Count by status:");
            summary.countByStatus().forEach((k, v) -> System.out.printf("  %s: %,d%n", k, v));
            System.out.println("Count by card type:");
            summary.countByCardType().forEach((k, v) -> System.out.printf("  %s: %,d%n", k, v));
            System.out.println("--- Timing ---");
            System.out.printf("Parse:   %s%n", formatDuration(summary.parseTime()));
            System.out.printf("Summary: %s%n", formatDuration(summary.summaryTime()));
            System.out.printf("DB:      %s%n", formatDuration(summary.dbTime()));
            System.out.printf("Total:   %s%n", formatDuration(summary.totalTime()));
        }

        private static String formatDuration(Duration d) {
            long millis = d.toMillis();
            if (millis < 1000) return millis + " ms";
            return String.format("%.2f s", millis / 1000.0);
        }
    }
}
