package pl.training.jmodern.module02_java8;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Exercises {

    // ---- Helper types ----

    record Transaction(String id, String category, double amount, LocalDate date) {}

    record Employee(String name, Set<DayOfWeek> workingDays, LocalTime startHour, LocalTime endHour) {}

    @FunctionalInterface
    interface Mapper<T, R> {
        R apply(T input);

        default <V> Mapper<T, V> andThen(Mapper<R, V> after) {
            return input -> after.apply(this.apply(input));
        }
    }

    // ============================================================
    // Exercise 1: Transaction Analytics Pipeline
    // ============================================================

    /**
     * Summarize total spending by category for transactions in a given month.
     *
     * <p>Given a list of transactions and a target {@link YearMonth}, return a
     * {@code Map<String, Double>} where each key is a category and each value
     * is the total amount spent in that category during the target month.</p>
     *
     * <p><b>Hints:</b> Use {@code Stream.filter} to select the month,
     * {@code Collectors.groupingBy} with {@code Collectors.summingDouble}
     * to aggregate, and lambdas / method references where appropriate.</p>
     */
    static Map<String, Double> spendingByCategory(List<Transaction> transactions, YearMonth month) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 2: Composable Log Processor
    // ============================================================

    /**
     * Build a reusable log-processing pipeline that extracts log levels from raw lines.
     *
     * <p>Each raw log line may look like {@code "2024-01-15 ERROR Something failed"}.
     * Using the custom {@link Mapper} functional interface:</p>
     * <ol>
     *   <li>Create a {@code Mapper<String, Optional<String>>} that returns the second
     *       token (the log level) wrapped in an {@code Optional}, or empty if the line
     *       has fewer than two tokens.</li>
     *   <li>Compose it with another mapper that uppercases the value (use {@code andThen}).</li>
     *   <li>Apply the composed pipeline to a stream of log lines and collect distinct,
     *       non-empty log levels into a sorted {@code List<String>}.</li>
     * </ol>
     *
     * <p><b>Hints:</b> Use {@code Stream.map}, {@code Optional.stream} (or
     * {@code flatMap}), and the custom {@code Mapper.andThen} default method.</p>
     */
    static List<String> extractLogLevels(List<String> rawLines) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 3: Employee Meeting Scheduler
    // ============================================================

    /**
     * Find employees available for a meeting at a given day and time range.
     *
     * <p>An employee is available if:</p>
     * <ul>
     *   <li>The meeting day's {@link DayOfWeek} is in their {@code workingDays} set.</li>
     *   <li>The meeting's {@code startTime} and {@code endTime} fall within their
     *       working hours ({@code startHour} to {@code endHour}).</li>
     * </ul>
     *
     * <p>Return a single {@code String} listing available employee names separated
     * by {@code ", "} (e.g. {@code "Alice, Bob, Carol"}).</p>
     *
     * <p><b>Hints:</b> Use {@code Predicate} composition ({@code and}),
     * {@code Stream.filter}, and {@code Collectors.joining} or {@link StringJoiner}.</p>
     */
    static String findAvailableEmployees(List<Employee> employees,
                                         LocalDate meetingDay,
                                         LocalTime startTime,
                                         LocalTime endTime) {
        throw new UnsupportedOperationException();
    }
}
