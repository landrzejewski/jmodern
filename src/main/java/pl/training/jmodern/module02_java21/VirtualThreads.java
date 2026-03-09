package pl.training.jmodern.module02_java21;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction -- Why Virtual Threads
// ============================================================

/*
## Introduction -- Why Virtual Threads

- The **thread-per-request** model is the simplest server design:
  one incoming request = one thread that handles it from start to
  finish. It is easy to reason about (stack traces, debuggers,
  profilers all work naturally) and requires no callbacks or async
  plumbing.
- **Problem**: OS (platform) threads are expensive.
    - Each thread typically reserves ~1 MB of stack memory.
    - Thread creation involves a kernel call and scheduling overhead.
    - In practice, a JVM can sustain **~2,000--10,000 platform threads**
      before hitting OS limits, memory pressure, or scheduling
      degradation.
    - This means a server using thread-per-request tops out at a few
      thousand concurrent requests -- far fewer than the I/O hardware
      could support.
- **How other languages solved this**:
    - Go: **goroutines** (lightweight, runtime-scheduled)
    - Kotlin: **coroutines** (suspend functions, structured concurrency)
    - JavaScript: **async/await** (single-threaded event loop)
    - All of these require a **different programming model** -- you
      cannot simply call blocking methods and expect things to work.
- **Java's approach**: keep the familiar **blocking-style code** but
  make the threads themselves cheap. No colored functions, no async
  annotations, no new syntax.
- **JEP timeline**:
    - JEP 425: Preview in Java 19
    - JEP 436: Second preview in Java 20
    - JEP 444: Finalized in **Java 21**
- **Virtual threads** are user-mode threads scheduled by the JVM,
  not by the OS. The JVM maps many virtual threads onto a small
  pool of **carrier threads** (a ForkJoinPool). When a virtual thread
  blocks (sleep, I/O, lock), the JVM **unmounts** it from the carrier
  and mounts another virtual thread -- so the carrier is never idle.
- The result: you can create **millions** of virtual threads, each
  one blocking freely, with the JVM transparently multiplexing them
  onto a handful of OS threads.
*/

// ============================================================
// Section 2: Creating Virtual Threads
// ============================================================

/*
## Creating Virtual Threads

- **Thread.ofVirtual()** returns a builder for virtual threads:
      Thread.ofVirtual().name("worker").start(() -> { ... });
- **Thread.startVirtualThread(Runnable)** is a convenience shortcut:
      Thread.startVirtualThread(() -> System.out.println("hello"));
  This creates, starts, and returns a virtual thread in one call.
- **Naming**: The builder supports .name("prefix-", startIndex) for
  auto-numbered names: worker-0, worker-1, worker-2, ...
- **Thread.ofPlatform()** is the symmetric API for platform threads:
      Thread.ofPlatform().name("os-thread").start(() -> { ... });
- **Thread.isVirtual()** returns true for virtual threads.
- Virtual threads are **always daemon threads** -- they do not
  prevent the JVM from exiting. Calling setDaemon(false) throws
  an IllegalArgumentException.
- **Thread priority has no effect** on virtual threads -- the JVM
  scheduler ignores it (all virtual threads have NORM_PRIORITY).
- **ThreadFactory**: Both builders expose .factory() to create a
  ThreadFactory, which is useful with ExecutorService and other
  concurrency utilities.
*/

// ============================================================
// Section 3: Virtual Threads with Executors
// ============================================================

/*
## Virtual Threads with Executors

- **Executors.newVirtualThreadPerTaskExecutor()** creates an
  ExecutorService that starts a new virtual thread for every
  submitted task. This is the recommended way to use virtual
  threads in server applications.
- **Why pooling is counterproductive**: Virtual threads are so
  cheap that pooling them (like a fixed thread pool) adds overhead
  without benefit. Each task gets its own thread -- there is no
  reuse needed because creation cost is negligible.
- **AutoCloseable**: In Java 19+, ExecutorService extends
  AutoCloseable. Using try-with-resources calls close(), which
  waits for all submitted tasks to finish (like invoking
  shutdown() + awaitTermination()).
- **ExecutorService.close()** blocks until all tasks complete.
  This makes structured concurrency patterns easy -- submit work,
  close the executor, and all results are ready.
- **Performance comparison**: 10 tasks each sleeping 100ms:
    - Virtual thread executor: ~100ms (all tasks run concurrently)
    - Fixed thread pool(4): ~300ms (only 4 tasks at a time)
*/

// ============================================================
// Section 4: Scalability Demonstration
// ============================================================

/*
## Scalability Demonstration

- A virtual thread starts with an **initial stack of ~200 bytes**
  (vs ~1 MB for a platform thread). The stack is stored on the
  heap and grows/shrinks dynamically as needed.
- The JVM can sustain **millions** of virtual threads simultaneously.
  The bottleneck shifts from thread count to actual work and memory
  for stack frames.
- When a virtual thread **blocks** (sleep, I/O, lock acquisition),
  the JVM unmounts its continuation from the carrier thread. The
  carrier is immediately free to run another virtual thread.
- **Sleeping is nearly free**: A sleeping virtual thread consumes
  only heap memory for its frozen stack -- no OS thread is held.
  This means 100,000 threads each sleeping 1 second complete in
  about 1 second total, not 100,000 seconds.
- Attempting the same with platform threads would require ~100 GB
  of stack memory and would likely crash the JVM or the OS.
*/

// ============================================================
// Section 5: Virtual Threads and Blocking I/O
// ============================================================

/*
## Virtual Threads and Blocking I/O

- The JVM intercepts blocking calls and **automatically unmounts**
  the virtual thread from its carrier. The developer does not need
  to do anything -- blocking code just works.
- **Supported blocking points** (where unmounting happens):
    - Thread.sleep()
    - BlockingQueue.take() / put()
    - Lock.lock() (ReentrantLock)
    - Socket read/write (java.net, java.nio channels in blocking mode)
    - Future.get()
    - CountDownLatch.await()
    - Selector operations
- **Continuation model**: When a virtual thread blocks, the JVM
  saves its entire stack to the heap (a "continuation"). When the
  blocking condition is resolved, the continuation is remounted
  onto a carrier thread -- potentially a **different** carrier
  than the original one.
- This is **transparent to the developer**: your code looks like
  normal sequential blocking code, but under the hood the JVM
  is efficiently multiplexing thousands of virtual threads onto
  a few OS threads.
- **Simulated I/O**: In this demo we simulate database queries and
  API calls with Thread.sleep(). In real applications, any blocking
  I/O operation (JDBC, HTTP clients, file I/O) benefits the same way.
*/

// ============================================================
// Section 6: Best Practices and Pitfalls
// ============================================================

/*
## Best Practices and Pitfalls

- **Don't pool virtual threads**: Creating a fixed pool of virtual
  threads defeats their purpose. Use newVirtualThreadPerTaskExecutor()
  or Thread.startVirtualThread() -- one thread per task.
- **Avoid `synchronized` blocks/methods**: A virtual thread inside a
  synchronized block **pins** its carrier thread -- the carrier cannot
  be reused by other virtual threads until the monitor is released.
  This reduces concurrency and can cause performance degradation.
- **Use ReentrantLock instead**: ReentrantLock is virtual-thread-
  friendly. When a virtual thread blocks on lock.lock(), it properly
  unmounts from the carrier.
- **Pinning explained**: Pinning occurs when the JVM cannot unmount
  a virtual thread. Two main causes:
    - Inside a `synchronized` block or method
    - Inside a native method or foreign function
  The virtual thread still works correctly, but it holds the carrier
  hostage until the pinning section completes.
- **Thread-locals are wasteful**: Because you may have millions of
  virtual threads, per-thread storage (ThreadLocal) can consume
  excessive memory. Java 21 introduces **ScopedValue** (preview)
  as a lightweight, immutable alternative.
- **CPU-bound work is not helped**: Virtual threads shine when tasks
  spend most of their time **blocking** (waiting for I/O). For
  CPU-bound work, you are limited by the number of cores regardless
  of thread type.
- **Diagnostic flag**: -Djdk.tracePinnedThreads=short (or =full)
  prints a stack trace whenever a virtual thread is pinned. Useful
  during development and testing to find problematic synchronized
  blocks.
*/

public class VirtualThreads {

    // ---- Section 6: Counter implementations for pinning demo ----

    static class SynchronizedCounter {
        private int count = 0;
        synchronized void increment() { count++; }
        synchronized int getCount() { return count; }
    }

    static class LockBasedCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;
        void increment() { lock.lock(); try { count++; } finally { lock.unlock(); } }
        int getCount() { lock.lock(); try { return count; } finally { lock.unlock(); } }
    }

    // ============================================================
    // Section 1: Introduction -- Why Virtual Threads
    // ============================================================

    static void introductionWhyVirtualThreads() throws Exception {
        System.out.println("=== Section 1: Introduction -- Why Virtual Threads ===");

        // Available processors (carrier pool size context)
        System.out.println("Available processors (carrier pool size): " + Runtime.getRuntime().availableProcessors());

        // Create a platform thread
        System.out.println("\n--- Platform thread ---");
        var platformThread = Thread.ofPlatform().name("my-platform-thread").start(() -> {
            var t = Thread.currentThread();
            System.out.println("  Thread: " + t);
            System.out.println("  isVirtual(): " + t.isVirtual());
            System.out.println("  isDaemon(): " + t.isDaemon());
        });
        platformThread.join();

        // Create a virtual thread
        System.out.println("\n--- Virtual thread ---");
        var virtualThread = Thread.ofVirtual().name("my-virtual-thread").start(() -> {
            var t = Thread.currentThread();
            System.out.println("  Thread: " + t);
            System.out.println("  isVirtual(): " + t.isVirtual());
            System.out.println("  isDaemon(): " + t.isDaemon());
        });
        virtualThread.join();
    }

    // ============================================================
    // Section 2: Creating Virtual Threads
    // ============================================================

    static void creatingVirtualThreads() throws Exception {
        System.out.println("\n=== Section 2: Creating Virtual Threads ===");

        // startVirtualThread — convenience method
        System.out.println("--- Thread.startVirtualThread() ---");
        var t1 = Thread.startVirtualThread(() -> {
            System.out.println("  startVirtualThread: " + Thread.currentThread());
        });
        t1.join();

        // Named builder with auto-numbered names
        System.out.println("\n--- Named builder with 5 threads ---");
        var builder = Thread.ofVirtual().name("worker-", 0);
        var threads = new ArrayList<Thread>();
        for (int i = 0; i < 5; i++) {
            var thread = builder.start(() -> {
                System.out.println("  " + Thread.currentThread().getName() + " running");
            });
            threads.add(thread);
        }
        for (var thread : threads) {
            thread.join();
        }

        // ThreadFactory
        System.out.println("\n--- ThreadFactory ---");
        ThreadFactory factory = Thread.ofVirtual().name("factory-thread-", 0).factory();
        var factoryThread = factory.newThread(() -> {
            System.out.println("  Created via factory: " + Thread.currentThread().getName());
        });
        factoryThread.start();
        factoryThread.join();

        // Compare virtual vs platform thread properties
        System.out.println("\n--- Virtual vs Platform thread comparison ---");
        var virtual = Thread.ofVirtual().name("vt-demo").unstarted(() -> {});
        var platform = Thread.ofPlatform().name("pt-demo").unstarted(() -> {});

        System.out.printf("  %-12s isVirtual=%-5s isDaemon=%-5s name=%s%n",
                "Virtual:", virtual.isVirtual(), virtual.isDaemon(), virtual.getName());
        System.out.printf("  %-12s isVirtual=%-5s isDaemon=%-5s name=%s%n",
                "Platform:", platform.isVirtual(), platform.isDaemon(), platform.getName());
    }

    // ============================================================
    // Section 3: Virtual Threads with Executors
    // ============================================================

    static void virtualThreadsWithExecutors() throws Exception {
        System.out.println("\n=== Section 3: Virtual Threads with Executors ===");

        // Virtual thread executor — 10 tasks sleeping 100ms each
        System.out.println("--- newVirtualThreadPerTaskExecutor: 10 tasks x 100ms sleep ---");
        var startVirtual = Instant.now();
        List<Future<String>> futures;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            futures = IntStream.range(0, 10)
                    .mapToObj(i -> executor.submit(() -> {
                        Thread.sleep(100);
                        return "task-" + i + " done by " + Thread.currentThread().getName();
                    }))
                    .toList();
        }

        var durationVirtual = Duration.between(startVirtual, Instant.now());
        System.out.println("  Results:");
        for (var future : futures) {
            System.out.println("    " + future.get());
        }
        System.out.println("  Virtual executor time: " + durationVirtual.toMillis() + "ms (expected ~100ms)");

        // Fixed thread pool comparison — same 10 tasks
        System.out.println("\n--- newFixedThreadPool(4): same 10 tasks x 100ms sleep ---");
        var startFixed = Instant.now();
        List<Future<String>> fixedFutures;

        try (var executor = Executors.newFixedThreadPool(4)) {
            fixedFutures = IntStream.range(0, 10)
                    .mapToObj(i -> executor.submit(() -> {
                        Thread.sleep(100);
                        return "task-" + i + " done";
                    }))
                    .toList();
        }

        var durationFixed = Duration.between(startFixed, Instant.now());
        System.out.println("  Fixed pool time: " + durationFixed.toMillis() + "ms (expected ~300ms)");
        System.out.println("  Virtual threads were ~" + (durationFixed.toMillis() / Math.max(1, durationVirtual.toMillis())) + "x faster");
    }

    // ============================================================
    // Section 4: Scalability Demonstration
    // ============================================================

    static void scalabilityDemonstration() throws Exception {
        System.out.println("\n=== Section 4: Scalability Demonstration ===");

        System.out.println("--- Launching 100,000 virtual threads each sleeping 1 second ---");
        var counter = new AtomicInteger(0);
        var start = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    counter.incrementAndGet();
                    return null;
                });
            }
        }

        var duration = Duration.between(start, Instant.now());
        System.out.println("  Completed: " + counter.get() + " tasks");
        System.out.println("  Time: " + duration.toMillis() + "ms (expected ~1-2 seconds)");
        System.out.println("  Note: 100,000 platform threads would require ~100 GB of stack memory!");
    }

    // ============================================================
    // Section 5: Virtual Threads and Blocking I/O
    // ============================================================

    static void virtualThreadsAndBlockingIO() throws Exception {
        System.out.println("\n=== Section 5: Virtual Threads and Blocking I/O ===");

        // Local record for simulated request
        record SimulatedRequest(int id, String dbResult, String apiResult, String carrierBefore, String carrierAfter) {}

        System.out.println("--- Simulated request pipeline: DB query (50ms) + API call (100ms) ---");
        System.out.println("  Processing 500 requests concurrently...");

        var start = Instant.now();
        List<Future<SimulatedRequest>> futures;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            futures = IntStream.range(0, 500)
                    .mapToObj(i -> executor.submit(() -> {
                        // Capture carrier thread before blocking
                        String carrierBefore = Thread.currentThread().toString();

                        // Simulate DB query
                        Thread.sleep(50);
                        String dbResult = "db-row-" + i;

                        // Capture carrier thread after first block (may differ)
                        String carrierAfter = Thread.currentThread().toString();

                        // Simulate API call
                        Thread.sleep(100);
                        String apiResult = "api-response-" + i;

                        return new SimulatedRequest(i, dbResult, apiResult, carrierBefore, carrierAfter);
                    }))
                    .toList();
        }

        var duration = Duration.between(start, Instant.now());

        // Show a few results and carrier thread changes
        System.out.println("\n  Sample results (first 5):");
        int carrierChanges = 0;
        for (var future : futures) {
            var req = future.get();
            if (req.id() < 5) {
                System.out.println("    Request " + req.id() + ": " + req.dbResult() + " + " + req.apiResult());
                if (!req.carrierBefore().equals(req.carrierAfter())) {
                    System.out.println("      Carrier changed: " + req.carrierBefore().substring(0, Math.min(50, req.carrierBefore().length())) + "...");
                    System.out.println("                    -> " + req.carrierAfter().substring(0, Math.min(50, req.carrierAfter().length())) + "...");
                }
            }
            if (!req.carrierBefore().equals(req.carrierAfter())) {
                carrierChanges++;
            }
        }

        System.out.println("\n  Total requests: " + futures.size());
        System.out.println("  Carrier thread changes observed: " + carrierChanges + " / " + futures.size());
        System.out.println("  Time: " + duration.toMillis() + "ms (expected ~150-200ms, not 75 seconds sequentially)");
    }

    // ============================================================
    // Section 6: Best Practices and Pitfalls
    // ============================================================

    static void bestPracticesAndPitfalls() throws Exception {
        System.out.println("\n=== Section 6: Best Practices and Pitfalls ===");

        // ReentrantLock vs synchronized contention benchmark
        int threadCount = 1_000;
        int incrementsPerThread = 100;

        // ReentrantLock-based counter
        System.out.println("--- ReentrantLock vs synchronized: " + threadCount + " threads x " + incrementsPerThread + " increments ---");

        var lockCounter = new LockBasedCounter();
        var startLock = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        lockCounter.increment();
                    }
                    return null;
                });
            }
        }

        var durationLock = Duration.between(startLock, Instant.now());
        System.out.println("  ReentrantLock counter: " + lockCounter.getCount()
                + " (expected " + (threadCount * incrementsPerThread) + ")");
        System.out.println("  ReentrantLock time: " + durationLock.toMillis() + "ms");

        // Synchronized counter
        var syncCounter = new SynchronizedCounter();
        var startSync = Instant.now();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        syncCounter.increment();
                    }
                    return null;
                });
            }
        }

        var durationSync = Duration.between(startSync, Instant.now());
        System.out.println("  Synchronized counter: " + syncCounter.getCount()
                + " (expected " + (threadCount * incrementsPerThread) + ")");
        System.out.println("  Synchronized time: " + durationSync.toMillis() + "ms");
        System.out.println("  Note: synchronized pins the carrier thread, reducing concurrency");

        // CPU-bound work: virtual threads do not help
        System.out.println("\n--- CPU-bound work: Fibonacci parity (virtual threads don't help) ---");

        var startCpu = Instant.now();
        var evenCount = new AtomicInteger(0);
        var oddCount = new AtomicInteger(0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                final int n = 35 + (i % 5);
                executor.submit(() -> {
                    long fib = fibonacci(n);
                    if (fib % 2 == 0) {
                        evenCount.incrementAndGet();
                    } else {
                        oddCount.incrementAndGet();
                    }
                    return null;
                });
            }
        }

        var durationCpu = Duration.between(startCpu, Instant.now());
        System.out.println("  Computed 20 Fibonacci values: even=" + evenCount.get() + ", odd=" + oddCount.get());
        System.out.println("  Time: " + durationCpu.toMillis() + "ms");
        System.out.println("  CPU-bound work is limited by cores (" + Runtime.getRuntime().availableProcessors()
                + "), not thread count");

        // ScopedValue teaser
        System.out.println("\n--- ScopedValue (preview in Java 21) ---");
        System.out.println("  ThreadLocal works but is wasteful with millions of virtual threads.");
        System.out.println("  ScopedValue (JEP 446) provides a lightweight, immutable alternative:");
        System.out.println("    static final ScopedValue<String> USER = ScopedValue.newInstance();");
        System.out.println("    ScopedValue.runWhere(USER, \"alice\", () -> { ... USER.get() ... });");
        System.out.println("  ScopedValues are inherited by child threads and are automatically cleaned up.");
    }

    // Helper: naive recursive Fibonacci for CPU-bound demo
    static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    // ============================================================
    // Main -- run all sections
    // ============================================================

    public static void main(String[] args) throws Exception {
        introductionWhyVirtualThreads();
        creatingVirtualThreads();
        virtualThreadsWithExecutors();
        scalabilityDemonstration();
        virtualThreadsAndBlockingIO();
        bestPracticesAndPitfalls();
    }
}
