package pl.training.jmodern.module02_java21;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Exercises {

    // ---- Helper types for Exercise 1: Financial Ledger ----

    record Money(String currency, double amount) {}

    sealed interface FinancialEvent permits FinancialEvent.Payment, FinancialEvent.Refund {
        record Payment(Money money, String merchant) implements FinancialEvent {}
        record Refund(Money money, String reason) implements FinancialEvent {}
    }

    // ---- Helper types for Exercise 2: Event Stream Classifier ----

    sealed interface AppEvent permits AppEvent.UserLogin, AppEvent.UserLogout,
            AppEvent.PageView, AppEvent.ApiCall {
        record UserLogin(String userId, String ipAddress, boolean suspicious) implements AppEvent {}
        record UserLogout(String userId) implements AppEvent {}
        record PageView(String userId, String page, int durationMs) implements AppEvent {}
        record ApiCall(String endpoint, int statusCode, long latencyMs) implements AppEvent {}
    }

    // ============================================================
    // Exercise 1: Financial Ledger Reconciliation
    // ============================================================

    /**
     * Reconcile a list of financial events into per-currency balances.
     *
     * <p>Given a {@link SequencedCollection} of {@link FinancialEvent}s:</p>
     * <ul>
     *   <li>Payments <b>add</b> to the balance for that currency.</li>
     *   <li>Refunds <b>subtract</b> from the balance.</li>
     * </ul>
     *
     * <p>Return a {@link SequencedMap} (e.g. {@link LinkedHashMap}) of
     * {@code currency -> net balance}, ordered by first appearance of each
     * currency. Also print the first and last events processed using
     * {@code getFirst()} / {@code getLast()}.</p>
     *
     * <p><b>Hints:</b> Use record patterns in switch to destructure
     * {@code Payment(Money(currency, amount), _)} with unnamed variables
     * for fields you don't need. Use sequenced collection methods
     * ({@code getFirst}, {@code getLast}).</p>
     */
    static SequencedMap<String, Double> reconcile(SequencedCollection<FinancialEvent> events) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 2: Event Stream Classifier
    // ============================================================

    /**
     * Classify application events into severity categories using fine-grained rules.
     *
     * <p>Classification rules:</p>
     * <ul>
     *   <li>{@code UserLogin} with {@code suspicious == true} → {@code "ALERT"}</li>
     *   <li>{@code UserLogin} (normal) → {@code "INFO"}</li>
     *   <li>{@code UserLogout} → {@code "INFO"}</li>
     *   <li>{@code PageView} with {@code durationMs > 30_000} → {@code "WARNING"} (slow page)</li>
     *   <li>{@code PageView} (normal) → {@code "INFO"}</li>
     *   <li>{@code ApiCall} with {@code statusCode >= 500} → {@code "ALERT"}</li>
     *   <li>{@code ApiCall} with {@code statusCode >= 400} → {@code "WARNING"}</li>
     *   <li>{@code ApiCall} with {@code latencyMs > 5000} → {@code "WARNING"} (slow API)</li>
     *   <li>{@code ApiCall} (normal) → {@code "INFO"}</li>
     * </ul>
     *
     * <p>Return a {@code Map<String, List<AppEvent>>} grouping events by their
     * severity category.</p>
     *
     * <p><b>Hints:</b> Use pattern matching for switch with guarded patterns
     * ({@code when}), record patterns for destructuring, unnamed variables
     * ({@code _}) for unused components, and {@code Collectors.groupingBy}.</p>
     */
    static Map<String, List<AppEvent>> classifyEvents(List<AppEvent> events) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 3: Parallel Sensor Aggregation
    // ============================================================

    /**
     * Compute per-sensor average readings concurrently using virtual threads.
     *
     * <p>Given a {@code Map<String, List<Double>>} where each key is a sensor
     * ID and each value is a list of raw readings, compute the average for each
     * sensor. Each sensor's computation should run on its own virtual thread.</p>
     *
     * <p>Return a {@link SequencedMap} of {@code sensorId -> average}, ordered
     * alphabetically by sensor ID. Use {@code getFirst()} and {@code getLast()}
     * on the result to print the first and last sensor entries.</p>
     *
     * <p><b>Hints:</b> Use {@code Thread.ofVirtual().start()},
     * {@link ConcurrentHashMap} for thread-safe accumulation,
     * and sequenced collection methods on the final sorted result.</p>
     */
    static SequencedMap<String, Double> aggregateSensors(Map<String, List<Double>> sensorData)
            throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
