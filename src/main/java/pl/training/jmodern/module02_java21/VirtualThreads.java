package pl.training.jmodern.module02_java21;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.stream.*;

public class VirtualThreads {

    // ============================================================
    // Sekcja 1: Tworzenie wątków wirtualnych
    // ============================================================

    /*
     * Virtual threads = lekkie wątki planowane przez JVM na małej puli
     * carrier threads (ForkJoinPool). Koszt: ~200 bajtów vs ~1 MB dla platformowego.
     * Zawsze daemon, priorytet ignorowany. Kluczowe API:
     *   - Thread.ofVirtual().name("prefix-", startIndex).start(Runnable)
     *   - Thread.startVirtualThread(Runnable) — skrót jednoliniowy
     *   - thread.isVirtual() — sprawdzenie typu
     */

    static void creatingVirtualThreads() throws Exception {
        System.out.println("=== Sekcja 1: Tworzenie wątków wirtualnych ===\n");

        // Platform vs Virtual — porównanie
        var platform = Thread.ofPlatform().name("my-platform").start(() -> {
            var t = Thread.currentThread();
            System.out.println("  Platform: isVirtual=" + t.isVirtual() + ", daemon=" + t.isDaemon() + ", name=" + t.getName());
        });
        platform.join();

        var virtual = Thread.ofVirtual().name("my-virtual").start(() -> {
            var t = Thread.currentThread();
            System.out.println("  Virtual:  isVirtual=" + t.isVirtual() + ", daemon=" + t.isDaemon() + ", name=" + t.getName());
        });
        virtual.join();

        // Skrót jednoliniowy
        Thread.startVirtualThread(() ->
                System.out.println("  startVirtualThread: " + Thread.currentThread())
        ).join();

        // Builder z automatyczną numeracją nazw
        System.out.println("\n  Named builder (3 wątki):");
        var builder = Thread.ofVirtual().name("worker-", 0);
        var threads = new ArrayList<Thread>();
        for (int i = 0; i < 3; i++) {
            threads.add(builder.start(() ->
                    System.out.println("    " + Thread.currentThread().getName() + " running")));
        }
        for (var t : threads) t.join();
    }

    // ============================================================
    // Sekcja 2: Executor — newVirtualThreadPerTaskExecutor()
    // ============================================================

    /*
     * Executors.newVirtualThreadPerTaskExecutor() — nowy virtual thread na każde
     * zadanie. Pooling niepotrzebny, bo koszt tworzenia jest znikomy.
     * ExecutorService jest AutoCloseable (Java 19+) — close() czeka na zakończenie
     * wszystkich zadań (jak shutdown + awaitTermination).
     */

    static void executorDemo() throws Exception {
        System.out.println("\n=== Sekcja 2: Executor ===\n");

        int taskCount = 20;
        int sleepMs = 100;
        System.out.println("--- " + taskCount + " tasks x " + sleepMs + "ms sleep ---\n");

        // Virtual thread executor — wszystkie zadania równolegle
        var startVirtual = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> {
                        Thread.sleep(sleepMs);
                        return "task-" + i;
                    }))
                    .toList();
            for (var f : futures) f.get(); // czekamy na wyniki
        } // close() = shutdown + awaitTermination
        var durationVirtual = Duration.between(startVirtual, Instant.now()).toMillis();

        // Fixed pool(4) — ograniczona współbieżność
        var startFixed = Instant.now();
        try (var executor = Executors.newFixedThreadPool(4)) {
            IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> { Thread.sleep(sleepMs); return null; }))
                    .toList()
                    .forEach(f -> { try { f.get(); } catch (Exception e) { throw new RuntimeException(e); } });
        }
        var durationFixed = Duration.between(startFixed, Instant.now()).toMillis();

        System.out.println("  Virtual executor:  " + durationVirtual + "ms (expected ~" + sleepMs + "ms)");
        System.out.println("  Fixed pool(4):     " + durationFixed + "ms (expected ~" + (taskCount / 4 * sleepMs) + "ms)");
        System.out.println("  Speedup:           ~" + (durationFixed / Math.max(1, durationVirtual)) + "x");
    }

    // ============================================================
    // Sekcja 3: Skalowalność — miliony wątków
    // ============================================================

    /*
     * Virtual thread zaczyna z ~200 bajtów stosu. JVM obsługuje miliony
     * jednocześnie. 100k wątków śpiących 1s kończy się w ~1-2s.
     * 100k platform threads wymagałoby ~100 GB pamięci na stos.
     */

    static void scalabilityDemo() throws Exception {
        System.out.println("\n=== Sekcja 3: Skalowalność ===\n");

        int count = 100_000;
        var completed = new AtomicInteger(0);

        System.out.println("--- " + count + " virtual threads x 1s sleep ---");
        var start = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    completed.incrementAndGet();
                    return null;
                });
            }
        }
        var duration = Duration.between(start, Instant.now()).toMillis();

        System.out.println("  Completed: " + completed.get() + " tasks in " + duration + "ms");
        System.out.println("  Note: 100k platform threads = ~100 GB stack memory!");
    }

    // ============================================================
    // Sekcja 4: Pinning — synchronized vs ReentrantLock
    // ============================================================

    /*
     * synchronized przypina (pins) virtual thread do carrier thread — JVM nie może
     * go odmontować. Zmniejsza to współbieżność do liczby carrier threads.
     * ReentrantLock pozwala na odmontowanie — virtual thread zwalnia carrier.
     *
     * Diagnostyka: -Djdk.tracePinnedThreads=short  (wypisuje stos przy pinning)
     */

    static class SynchronizedCounter {
        private int count = 0;
        synchronized void increment() {
            count++;
        }
        synchronized int getCount() {
            return count;
        }
    }

    static class LockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;
        void increment() {
            lock.lock();
            try { count++; } finally { lock.unlock(); }
        }
        int getCount() {
            lock.lock();
            try { return count; } finally { lock.unlock(); }
        }
    }

    static void pinningDemo() throws Exception {
        System.out.println("\n=== Sekcja 4: Pinning ===\n");

        int threads = 1_000;
        int increments = 100;
        System.out.println("--- " + threads + " virtual threads x " + increments + " increments ---\n");

        // ReentrantLock — pozwala na odmontowanie
        var lockCounter = new LockCounter();
        var startLock = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < increments; j++) lockCounter.increment();
                    return null;
                });
            }
        }
        var durationLock = Duration.between(startLock, Instant.now()).toMillis();

        // synchronized — przypina carrier thread
        var syncCounter = new SynchronizedCounter();
        var startSync = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < increments; j++) syncCounter.increment();
                    return null;
                });
            }
        }
        var durationSync = Duration.between(startSync, Instant.now()).toMillis();

        System.out.println("  ReentrantLock: " + lockCounter.getCount() + " count, " + durationLock + "ms");
        System.out.println("  synchronized:  " + syncCounter.getCount() + " count, " + durationSync + "ms");
        System.out.println("  Tip: -Djdk.tracePinnedThreads=short to detect pinning");
    }

    // ============================================================
    // Main
    // ============================================================

    public static void main(String[] args) throws Exception {
        creatingVirtualThreads();
        executorDemo();
        scalabilityDemo();
        pinningDemo();
    }
}
