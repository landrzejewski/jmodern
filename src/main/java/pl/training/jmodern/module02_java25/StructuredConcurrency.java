package pl.training.jmodern.module02_java25;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction -- Why Structured Concurrency
// ============================================================

/*
## Introduction -- Why Structured Concurrency

- **Problem with unstructured concurrency** (ExecutorService):
    - Tasks can outlive the parent scope that created them.
    - If one task fails, sibling tasks keep running (resource leak).
    - Manual cancellation is error-prone and often forgotten.
    - Thread dumps show no parent-child relationship.
- **Analogy**: Structured concurrency is to concurrency what
  structured programming (if/while) was to goto. Just as we
  stopped using goto and gained local reasoning about control
  flow, structured concurrency lets us reason locally about
  concurrent lifetimes.
- **Origin**: "Notes on structured concurrency, or: Go statement
  considered harmful" (Nathaniel J. Smith, 2018). The idea was
  adopted by Kotlin (coroutineScope), Swift (TaskGroup), and
  now Java.
- **JEP timeline**:
    - JEP 428: Incubator in Java 19
    - JEP 437: Second incubator in Java 20
    - JEP 453: Preview in Java 21
    - JEP 462: Preview in Java 22
    - JEP 480: Preview in Java 23
    - JEP 499: Preview in Java 24
    - JEP 505: Preview in Java 25
- **Key invariant**: child tasks cannot outlive the scope that
  created them. When the scope's try-with-resources block ends,
  all child tasks are guaranteed to be finished (or cancelled).
- **Builds on virtual threads** (see module02_java21): scopes
  create virtual threads for each forked task. The combination
  of cheap threads + scope-bounded lifetimes is the foundation.
- **`StructuredTaskScope`** is an interface (not a class) with
  static `open(joiner)` factory methods. The Joiner determines
  the completion policy (all succeed, first wins, etc.).
- **Lifecycle**: open → fork → join → close (always in this order).
*/

// ============================================================
// Section 2: Joiner Strategies -- allSuccessfulOrThrow and anySuccessfulResultOrThrow
// ============================================================

/*
## Joiner Strategies -- allSuccessfulOrThrow and anySuccessfulResultOrThrow

- `StructuredTaskScope<T, R>` is parameterized by a Joiner:
    - T = the common type of forked subtasks
    - R = the result type returned by `join()`
- **`Joiner.allSuccessfulOrThrow()`**:
    - Waits for ALL subtasks to complete successfully.
    - `join()` returns `Stream<Subtask<T>>` containing results.
    - If ANY subtask fails → scope shuts down, remaining tasks
      are cancelled, `join()` throws `FailedException`.
    - Replaces the old `ShutdownOnFailure` pattern.
- **`Joiner.anySuccessfulResultOrThrow()`**:
    - Returns the FIRST successful result `T` from `join()`.
    - Remaining tasks are cancelled immediately.
    - If ALL tasks fail → `join()` throws `FailedException`.
    - Replaces the old `ShutdownOnSuccess` pattern.
- **`Subtask<T>`** has three states:
    - SUCCESS → `get()` returns the result
    - FAILED → `exception()` returns the throwable
    - UNAVAILABLE → task was cancelled or not yet complete
- **`FailedException`** is an unchecked RuntimeException that
  wraps the first subtask failure as its cause. Additional
  failures may appear as suppressed exceptions.
*/

// ============================================================
// Section 3: Joiner Strategies -- awaitAll and allUntil
// ============================================================

/*
## Joiner Strategies -- awaitAll and allUntil

- **`Joiner.awaitAllSuccessfulOrThrow()`**:
    - Waits for all subtasks, returns Void.
    - On success → inspect Subtask references from `fork()`.
    - Any failure → `FailedException` (like allSuccessfulOrThrow
      but you keep references to individual Subtasks).
- **`Joiner.awaitAll()`**:
    - Most lenient joiner — waits for ALL subtasks regardless
      of success or failure. Never throws on subtask failure.
    - `join()` returns Void. Caller inspects Subtask states
      manually via `state()`, `get()`, `exception()`.
    - Use when partial failure is acceptable.
- **`Joiner.allUntil(Predicate)`**:
    - Waits until the predicate returns true for any completed
      Subtask, then shuts down remaining tasks.
    - `join()` returns `Stream<Subtask<T>>`.
    - Enables custom short-circuit logic.
- **Joiner selection guide**:
    - All-or-nothing → `allSuccessfulOrThrow()`
    - First wins / racing → `anySuccessfulResultOrThrow()`
    - Need individual subtask refs → `awaitAllSuccessfulOrThrow()`
    - Partial failures OK → `awaitAll()`
    - Custom stop condition → `allUntil(Predicate)`
*/

// ============================================================
// Section 4: Configuration, Timeouts, and Exception Handling
// ============================================================

/*
## Configuration, Timeouts, and Exception Handling

- **Configuration** is supplied as a second parameter to `open()`:
    - `withName(String)` — names the scope, visible in thread
      dumps for diagnostics.
    - `withTimeout(Duration)` — sets a deadline for the scope.
    - `withThreadFactory(ThreadFactory)` — customize thread
      creation (e.g., named virtual threads).
- **Timeout**: If the deadline expires before `join()` completes,
  `join()` throws `StructuredTaskScope.TimeoutException` (a
  nested class, NOT java.util.concurrent.TimeoutException).
  The scope shuts down and remaining tasks are cancelled.
- **FailedException** wraps the first subtask failure as its
  cause. Use `getCause()` to inspect the original exception.
  Additional failures may be suppressed.
- **Lifecycle rules** (violating these throws IllegalStateException):
    - `close()` before `join()` → error
    - `fork()` after `join()` → error
    - `fork()` after scope is cancelled → error
- **`isCancelled()`** — returns true if the scope was shut
  down (e.g., due to timeout or a joiner policy decision).
*/

// ============================================================
// Section 5: Practical Patterns and Comparison
// ============================================================

/*
## Practical Patterns and Comparison

- **Structured concurrency vs ExecutorService**:
    - Scope-bounded lifetime (tasks can't escape)
    - Automatic cancellation on failure
    - Thread dump observability (parent-child hierarchy)
    - Virtual threads by default (no pool sizing)
- **Fan-out pattern**: fork N tasks from a collection, collect
  all results via allSuccessfulOrThrow stream.
- **Nested scopes**: An inner scope inside a forked task. Errors
  in inner scopes propagate up to the outer scope naturally.
- **ScopedValue integration** (preview): Context values bound
  in the parent thread are automatically inherited by forked
  tasks via ScopedValue, enabling safe context propagation
  without ThreadLocal.
- **Best practices**:
    - Always use try-with-resources for scopes
    - Prefer the most restrictive joiner that fits your use case
    - Keep forked tasks I/O-bound (structured concurrency shines
      with blocking operations on virtual threads)
    - Use withTimeout to prevent indefinite blocking
    - Use withName for debuggability
*/

public class StructuredConcurrency {

    // ---- Inner types ----

    record User(long id, String name, String email) {}
    record Order(long id, long userId, String product, double price) {}
    record Review(long id, long userId, String text, int rating) {}
    record UserProfile(User user, List<Order> orders, List<Review> reviews) {}
    record WeatherData(String city, double temperature, String condition) {}
    record SearchResult(String source, List<String> results) {}

    // ---- Helper methods (simulate I/O) ----

    static User simulateFetchUser(long id) throws InterruptedException {
        Thread.sleep(100);
        return new User(id, "User-" + id, "user" + id + "@example.com");
    }

    static List<Order> simulateFetchOrders(long userId) throws InterruptedException {
        Thread.sleep(150);
        return List.of(
                new Order(1, userId, "Laptop", 999.99),
                new Order(2, userId, "Mouse", 29.99)
        );
    }

    static List<Review> simulateFetchReviews(long userId) throws InterruptedException {
        Thread.sleep(200);
        return List.of(
                new Review(1, userId, "Great product!", 5),
                new Review(2, userId, "Good value", 4)
        );
    }

    static WeatherData simulateFetchWeather(String city, long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        var temps = Map.of("London", 15.0, "Paris", 18.0, "Tokyo", 22.0, "New York", 20.0, "Sydney", 25.0);
        var conditions = Map.of("London", "Cloudy", "Paris", "Sunny", "Tokyo", "Rainy", "New York", "Windy", "Sydney", "Clear");
        return new WeatherData(city, temps.getOrDefault(city, 20.0), conditions.getOrDefault(city, "Unknown"));
    }

    static String simulateSlowOperation(long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        return "Completed after " + delayMs + "ms";
    }

    static String simulateFailingOperation() {
        throw new RuntimeException("Simulated failure");
    }

    // ============================================================
    // Section 1: Introduction -- Why Structured Concurrency
    // ============================================================

    static void introductionToStructuredConcurrency() throws Exception {
        System.out.println("=== Section 1: Introduction -- Why Structured Concurrency ===");

        // ---- Demo 1: Unstructured approach (ExecutorService + Futures) ----
        System.out.println("\n--- Demo 1: Unstructured approach (ExecutorService) ---");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var userFuture = executor.submit(() -> simulateFetchUser(1));
            var ordersFuture = executor.submit(() -> simulateFetchOrders(1));
            var reviewsFuture = executor.submit(() -> simulateFetchReviews(1));

            // Must manually handle each future — no automatic cancellation
            try {
                var user = userFuture.get();
                var orders = ordersFuture.get();
                var reviews = reviewsFuture.get();
                var profile = new UserProfile(user, orders, reviews);
                System.out.println("  User: " + profile.user().name());
                System.out.println("  Orders: " + profile.orders().size());
                System.out.println("  Reviews: " + profile.reviews().size());
            } catch (ExecutionException e) {
                // If one fails, others keep running — must cancel manually
                userFuture.cancel(true);
                ordersFuture.cancel(true);
                reviewsFuture.cancel(true);
                System.out.println("  Failed: " + e.getCause());
            }
        }
        System.out.println("  Problem: if one task fails, siblings continue running unless manually cancelled");

        // ---- Demo 2: Structured approach ----
        System.out.println("\n--- Demo 2: Structured approach (StructuredTaskScope) ---");
        try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())) {
            // Fork tasks — each runs in its own virtual thread
            var userTask = scope.fork(() -> simulateFetchUser(1));
            var ordersTask = scope.fork(() -> simulateFetchOrders(1));
            var reviewsTask = scope.fork(() -> simulateFetchReviews(1));

            // Join waits for all tasks; if any fails, others are cancelled automatically
            var subtasks = scope.join();

            // Build result from completed subtasks
            var results = subtasks.toList();
            System.out.println("  Completed " + results.size() + " subtasks");
            System.out.println("  User: " + userTask.get());
            System.out.println("  Orders: " + ordersTask.get());
            System.out.println("  Reviews: " + reviewsTask.get());
        }
        System.out.println("  Benefit: automatic cancellation, scope-bounded lifetime, clean code");

        // ---- Demo 3: Lifetime guarantee — timeout cancels tasks ----
        System.out.println("\n--- Demo 3: Lifetime guarantee (timeout cancels tasks) ---");
        try (var scope = StructuredTaskScope.open(
                Joiner.allSuccessfulOrThrow(),
                cf -> cf.withTimeout(Duration.ofMillis(500)))) {

            scope.fork(() -> simulateSlowOperation(3000)); // 3 seconds — will be cancelled
            scope.fork(() -> simulateSlowOperation(100));   // 100ms — fast enough

            scope.join();
            System.out.println("  Should not reach here");
        } catch (StructuredTaskScope.TimeoutException e) {
            System.out.println("  TimeoutException caught — scope timed out after 500ms");
            System.out.println("  Slow task (3s) was automatically cancelled");
        }
    }

    // ============================================================
    // Section 2: Joiner Strategies -- allSuccessfulOrThrow and anySuccessfulResultOrThrow
    // ============================================================

    static void joinerAllAndAny() throws Exception {
        System.out.println("\n=== Section 2: Joiner Strategies -- allSuccessfulOrThrow and anySuccessfulResultOrThrow ===");

        // ---- Demo 1: allSuccessfulOrThrow — happy path ----
        System.out.println("\n--- Demo 1: allSuccessfulOrThrow -- happy path ---");
        try (var scope = StructuredTaskScope.open(Joiner.<Object>allSuccessfulOrThrow())) {
            var userTask = scope.fork(() -> simulateFetchUser(42));
            var ordersTask = scope.fork(() -> simulateFetchOrders(42));
            var reviewsTask = scope.fork(() -> simulateFetchReviews(42));

            var subtasks = scope.join().toList();
            System.out.println("  join() returned " + subtasks.size() + " subtasks");

            // Build UserProfile from individual subtask results
            var profile = new UserProfile(
                    userTask.get(),
                    ordersTask.get(),
                    reviewsTask.get()
            );
            System.out.println("  UserProfile: " + profile.user().name()
                    + ", " + profile.orders().size() + " orders"
                    + ", " + profile.reviews().size() + " reviews");
        }

        // ---- Demo 2: allSuccessfulOrThrow — failure ----
        System.out.println("\n--- Demo 2: allSuccessfulOrThrow -- failure ---");
        try (var scope = StructuredTaskScope.open(Joiner.<Object>allSuccessfulOrThrow())) {
            var goodTask = scope.fork(() -> simulateFetchUser(1));
            var failTask = scope.fork(() -> simulateFailingOperation());
            var slowTask = scope.fork(() -> simulateSlowOperation(2000));

            scope.join();
            System.out.println("  Should not reach here");
        } catch (FailedException e) {
            System.out.println("  FailedException caught!");
            System.out.println("  Cause: " + e.getCause());
            System.out.println("  FailedException is a RuntimeException: " + (e instanceof RuntimeException));
        }

        // ---- Demo 3: anySuccessfulResultOrThrow — racing ----
        System.out.println("\n--- Demo 3: anySuccessfulResultOrThrow -- racing mirrors ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(() -> { Thread.sleep(200); return "Mirror-A (200ms)"; });
            scope.fork(() -> { Thread.sleep(50);  return "Mirror-B (50ms)"; });
            scope.fork(() -> { Thread.sleep(500); return "Mirror-C (500ms)"; });

            String fastest = scope.join();
            System.out.println("  Fastest result: " + fastest);
            System.out.println("  Other tasks were cancelled automatically");
        }

        // ---- Demo 4: anySuccessfulResultOrThrow — all fail ----
        System.out.println("\n--- Demo 4: anySuccessfulResultOrThrow -- all fail ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(() -> { throw new RuntimeException("Error from source A"); });
            scope.fork(() -> { throw new RuntimeException("Error from source B"); });
            scope.fork(() -> { throw new RuntimeException("Error from source C"); });

            scope.join();
            System.out.println("  Should not reach here");
        } catch (FailedException e) {
            System.out.println("  FailedException: all tasks failed");
            System.out.println("  Primary cause: " + e.getCause().getMessage());
            System.out.println("  Suppressed exceptions: " + e.getSuppressed().length);
            for (var suppressed : e.getSuppressed()) {
                System.out.println("    - " + suppressed.getMessage());
            }
        }
    }

    // ============================================================
    // Section 3: Joiner Strategies -- awaitAll and allUntil
    // ============================================================

    static void joinerAwaitAllAndAllUntil() throws Exception {
        System.out.println("\n=== Section 3: Joiner Strategies -- awaitAll and allUntil ===");

        // ---- Demo 1: awaitAll — partial failure tolerance ----
        System.out.println("\n--- Demo 1: awaitAll -- partial failure tolerance ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>awaitAll())) {
            var tasks = new ArrayList<Subtask<String>>();
            tasks.add(scope.fork(() -> { Thread.sleep(50);  return "Task-1: OK"; }));
            tasks.add(scope.fork(() -> { Thread.sleep(80);  throw new RuntimeException("Task-2: DB error"); }));
            tasks.add(scope.fork(() -> { Thread.sleep(100); return "Task-3: OK"; }));
            tasks.add(scope.fork(() -> { Thread.sleep(120); throw new RuntimeException("Task-4: Timeout"); }));
            tasks.add(scope.fork(() -> { Thread.sleep(60);  return "Task-5: OK"; }));

            scope.join(); // Does NOT throw even though some tasks failed

            System.out.println("  join() completed without throwing (awaitAll is lenient)");
            var successes = tasks.stream().filter(t -> t.state() == Subtask.State.SUCCESS).toList();
            var failures = tasks.stream().filter(t -> t.state() == Subtask.State.FAILED).toList();

            System.out.println("  Successes (" + successes.size() + "):");
            for (var t : successes) {
                System.out.println("    " + t.get());
            }
            System.out.println("  Failures (" + failures.size() + "):");
            for (var t : failures) {
                System.out.println("    " + t.exception().getMessage());
            }
        }

        // ---- Demo 2: awaitAll — resilient search ----
        System.out.println("\n--- Demo 2: awaitAll -- resilient search ---");
        try (var scope = StructuredTaskScope.open(Joiner.<SearchResult>awaitAll())) {
            var tasks = new ArrayList<Subtask<SearchResult>>();
            tasks.add(scope.fork(() -> {
                Thread.sleep(80);
                return new SearchResult("Google", List.of("result-g1", "result-g2"));
            }));
            tasks.add(scope.fork(() -> {
                Thread.sleep(50);
                throw new RuntimeException("Bing is down");
            }));
            tasks.add(scope.fork(() -> {
                Thread.sleep(120);
                return new SearchResult("DuckDuckGo", List.of("result-d1"));
            }));
            tasks.add(scope.fork(() -> {
                Thread.sleep(60);
                throw new RuntimeException("Yahoo timeout");
            }));

            scope.join();

            var allResults = tasks.stream()
                    .filter(t -> t.state() == Subtask.State.SUCCESS)
                    .map(Subtask::get)
                    .toList();

            System.out.println("  Successful sources: " + allResults.size() + " / " + tasks.size());
            for (var result : allResults) {
                System.out.println("    " + result.source() + ": " + result.results());
            }
            var merged = allResults.stream()
                    .flatMap(r -> r.results().stream())
                    .toList();
            System.out.println("  Merged results: " + merged);
        }

        // ---- Demo 3: allUntil — custom short-circuit ----
        System.out.println("\n--- Demo 3: allUntil -- custom short-circuit ---");
        try (var scope = StructuredTaskScope.open(
                Joiner.<Integer>allUntil(subtask ->
                        subtask.state() == Subtask.State.SUCCESS && subtask.get() > 50))) {

            scope.fork(() -> { Thread.sleep(50);  return 10; });
            scope.fork(() -> { Thread.sleep(100); return 25; });
            scope.fork(() -> { Thread.sleep(150); return 75; }); // This one triggers the predicate
            scope.fork(() -> { Thread.sleep(200); return 30; }); // Should be cancelled
            scope.fork(() -> { Thread.sleep(250); return 90; }); // Should be cancelled

            var subtasks = scope.join().toList();
            System.out.println("  Completed subtasks: " + subtasks.size());
            for (var t : subtasks) {
                System.out.println("    State: " + t.state()
                        + (t.state() == Subtask.State.SUCCESS ? ", value: " + t.get() : ""));
            }
            System.out.println("  Scope stopped early when a value > 50 was found");
        }
    }

    // ============================================================
    // Section 4: Configuration, Timeouts, and Exception Handling
    // ============================================================

    static void configurationTimeoutsAndExceptions() throws Exception {
        System.out.println("\n=== Section 4: Configuration, Timeouts, and Exception Handling ===");

        // ---- Demo 1: Named scope + timeout ----
        System.out.println("\n--- Demo 1: Named scope + timeout ---");
        try (var scope = StructuredTaskScope.open(
                Joiner.<String>allSuccessfulOrThrow(),
                cf -> cf.withName("fetch-scope").withTimeout(Duration.ofMillis(500)))) {

            scope.fork(() -> simulateSlowOperation(2000)); // Will exceed timeout
            scope.fork(() -> { Thread.sleep(100); return "Fast task done"; });

            scope.join();
            System.out.println("  Should not reach here");
        } catch (StructuredTaskScope.TimeoutException e) {
            System.out.println("  TimeoutException caught!");
            System.out.println("  The scope 'fetch-scope' timed out after 500ms");
            System.out.println("  Note: this is StructuredTaskScope.TimeoutException, not java.util.concurrent.TimeoutException");
        }

        // ---- Demo 2: FailedException inspection ----
        System.out.println("\n--- Demo 2: FailedException inspection ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
            scope.fork(() -> {
                // Simulate a checked exception wrapped in the callable
                throw new Exception("Database connection refused");
            });
            scope.fork(() -> { Thread.sleep(100); return "Other task"; });

            scope.join();
        } catch (FailedException e) {
            System.out.println("  FailedException caught");
            System.out.println("  Type: " + e.getClass().getName());
            System.out.println("  Is RuntimeException: " + (e instanceof RuntimeException));
            System.out.println("  Cause type: " + e.getCause().getClass().getName());
            System.out.println("  Cause message: " + e.getCause().getMessage());
        }

        // ---- Demo 3: Scope lifecycle rules ----
        System.out.println("\n--- Demo 3: Scope lifecycle rules ---");
        System.out.println("  Correct order: open -> fork -> join -> close");

        // Demonstrate fork after join throws IllegalStateException
        try (var scope = StructuredTaskScope.open(Joiner.<String>awaitAll())) {
            scope.fork(() -> "first task");
            scope.join();

            // Fork after join should throw
            scope.fork(() -> "too late");
            System.out.println("  Should not reach here");
        } catch (IllegalStateException e) {
            System.out.println("  fork() after join() -> IllegalStateException: " + e.getMessage());
        }

        // Demonstrate isCancelled
        System.out.println("\n  Checking isCancelled() after timeout:");
        try (var scope = StructuredTaskScope.open(
                Joiner.<String>allSuccessfulOrThrow(),
                cf -> cf.withTimeout(Duration.ofMillis(100)))) {

            scope.fork(() -> simulateSlowOperation(5000));
            scope.join();
        } catch (StructuredTaskScope.TimeoutException e) {
            System.out.println("  Scope timed out (as expected)");
        }
    }

    // ============================================================
    // Section 5: Practical Patterns and Comparison
    // ============================================================

    static void practicalPatternsAndComparison() throws Exception {
        System.out.println("\n=== Section 5: Practical Patterns and Comparison ===");

        // ---- Demo 1: Fan-out with dynamic count ----
        System.out.println("\n--- Demo 1: Fan-out -- weather for multiple cities ---");
        var cities = List.of("London", "Paris", "Tokyo", "New York", "Sydney");

        try (var scope = StructuredTaskScope.open(Joiner.<WeatherData>allSuccessfulOrThrow())) {
            for (var city : cities) {
                scope.fork(() -> simulateFetchWeather(city, 100 + (long) (Math.random() * 100)));
            }

            var results = scope.join()
                    .map(Subtask::get)
                    .toList();

            System.out.println("  Fetched weather for " + results.size() + " cities:");
            for (var weather : results) {
                System.out.printf("    %-10s %.1f°C  %s%n",
                        weather.city(), weather.temperature(), weather.condition());
            }
        }

        // ---- Demo 2: Nested scopes ----
        System.out.println("\n--- Demo 2: Nested scopes ---");
        try (var outerScope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {

            outerScope.fork(() -> {
                // Inner scope for user data
                try (var innerScope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                    innerScope.fork(() -> { Thread.sleep(50); return "user-name"; });
                    innerScope.fork(() -> { Thread.sleep(80); return "user-email"; });
                    var innerResults = innerScope.join().map(Subtask::get).toList();
                    return "UserData: " + innerResults;
                }
            });

            outerScope.fork(() -> {
                // Inner scope for product data
                try (var innerScope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                    innerScope.fork(() -> { Thread.sleep(60); return "product-name"; });
                    innerScope.fork(() -> { Thread.sleep(70); return "product-price"; });
                    var innerResults = innerScope.join().map(Subtask::get).toList();
                    return "ProductData: " + innerResults;
                }
            });

            var results = outerScope.join().map(Subtask::get).toList();
            System.out.println("  Outer scope collected " + results.size() + " results:");
            for (var result : results) {
                System.out.println("    " + result);
            }
            System.out.println("  Inner scopes completed before outer scope — hierarchical completion");
        }

        // ---- Demo 3: Best practices summary ----
        System.out.println("\n--- Demo 3: Best practices summary ---");
        System.out.println("  1. Always use try-with-resources for StructuredTaskScope");
        System.out.println("  2. Prefer the most restrictive Joiner that fits your use case:");
        System.out.println("     - allSuccessfulOrThrow() for all-or-nothing operations");
        System.out.println("     - anySuccessfulResultOrThrow() for racing / first-wins");
        System.out.println("     - awaitAll() for partial failure tolerance");
        System.out.println("  3. Keep forked tasks I/O-bound — structured concurrency shines");
        System.out.println("     with blocking operations on virtual threads");
        System.out.println("  4. Use withTimeout() to prevent indefinite blocking");
        System.out.println("  5. Use withName() for debuggability in thread dumps");
        System.out.println("  6. Prefer ScopedValue over ThreadLocal for context propagation");
        System.out.println("  7. Remember: StructuredTaskScope is still preview in Java 25 (JEP 505)");
    }

    // ============================================================
    // Main -- run all sections
    // ============================================================

    public static void main(String[] args) throws Exception {
        introductionToStructuredConcurrency();
        joinerAllAndAny();
        joinerAwaitAllAndAllUntil();
        configurationTimeoutsAndExceptions();
        practicalPatternsAndComparison();
    }
}
