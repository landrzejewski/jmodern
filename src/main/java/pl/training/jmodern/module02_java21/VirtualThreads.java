package pl.training.jmodern.module02_java21;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie -- Dlaczego virtual threads
// ============================================================

/*
## Wprowadzenie -- Dlaczego virtual threads

- **Problem**: Wątki systemu operacyjnego (platformowe) są kosztowne.
    - Każdy wątek zazwyczaj rezerwuje ~1 MB pamięci stosu.
    - Tworzenie wątku wymaga wywołania jądra i narzutu na planowanie.
    - W praktyce JVM może obsłużyć **~2 000--10 000 wątków platformowych**
      zanim osiągnie limity systemu operacyjnego, presję pamięciową lub
      degradację planowania.
- **Jak inne języki rozwiązały ten problem**:
    - Go: **goroutines** (lekkie, planowane przez runtime)
    - Kotlin: **coroutines** (funkcje suspend, structured concurrency)
    - JavaScript: **async/await** (jednowątkowa pętla zdarzeń)
    - Wszystkie te rozwiązania wymagają **innego modelu programowania** --
      nie można po prostu wywoływać metod blokujących i oczekiwać, że wszystko zadziała.
- **Podejście Javy**: zachowanie znanego **kodu w stylu blokującym**, ale
  uczynienie samych wątków tanimi. Brak kolorowanych funkcji, brak adnotacji
  async, brak nowej składni.
- **Harmonogram JEP**:
    - JEP 425: Podgląd w Java 19
    - JEP 436: Drugi podgląd w Java 20
    - JEP 444: Wersja finalna w **Java 21**
- **Virtual threads** to wątki w trybie użytkownika planowane przez JVM,
  a nie przez system operacyjny. JVM mapuje wiele virtual threads na małą
  pulę **wątków nośnych** (carrier threads, ForkJoinPool). Gdy virtual thread
  blokuje się (sleep, I/O, blokada), JVM **odmontowuje** go z wątku nośnego
  i montuje inny virtual thread -- dzięki czemu wątek nośny nigdy nie jest bezczynny.
- Rezultat: można tworzyć **miliony** virtual threads, z których każdy
  może swobodnie się blokować, a JVM transparentnie multipleksuje je
  na garść wątków systemu operacyjnego.
*/

// ============================================================
// Sekcja 2: Tworzenie virtual threads
// ============================================================

/*
## Tworzenie virtual threads

- **Thread.ofVirtual()** zwraca builder dla virtual threads:
      Thread.ofVirtual().name("worker").start(() -> { ... });
- **Thread.startVirtualThread(Runnable)** to wygodny skrót:
      Thread.startVirtualThread(() -> System.out.println("hello"));
  Tworzy, uruchamia i zwraca virtual thread w jednym wywołaniu.
- **Nazewnictwo**: Builder obsługuje .name("prefix-", startIndex) dla
  automatycznie numerowanych nazw: worker-0, worker-1, worker-2, ...
- **Thread.ofPlatform()** to symetryczne API dla wątków platformowych:
      Thread.ofPlatform().name("os-thread").start(() -> { ... });
- **Thread.isVirtual()** zwraca true dla virtual threads.
- Virtual threads są **zawsze wątkami daemon** -- nie zapobiegają
  zamknięciu JVM. Wywołanie setDaemon(false) rzuca
  IllegalArgumentException.
- **Priorytet wątku nie ma wpływu** na virtual threads -- planista JVM
  go ignoruje (wszystkie virtual threads mają NORM_PRIORITY).
- **ThreadFactory**: Oba buildery udostępniają .factory() do tworzenia
  ThreadFactory, co jest przydatne z ExecutorService i innymi
  narzędziami współbieżności.
*/

// ============================================================
// Sekcja 3: Virtual threads z Executors
// ============================================================

/*
## Virtual threads z Executors

- **Executors.newVirtualThreadPerTaskExecutor()** tworzy
  ExecutorService, który uruchamia nowy virtual thread dla każdego
  przesłanego zadania. Jest to zalecany sposób użycia virtual
  threads w aplikacjach serwerowych.
- **Dlaczego pooling jest nieproduktywny**: Virtual threads są tak
  tanie, że tworzenie z nich puli (jak fixed thread pool) dodaje
  narzut bez korzyści. Każde zadanie dostaje własny wątek -- nie ma
  potrzeby ponownego użycia, ponieważ koszt tworzenia jest znikomy.
- **AutoCloseable**: W Java 19+, ExecutorService rozszerza
  AutoCloseable. Użycie try-with-resources wywołuje close(), które
  czeka na zakończenie wszystkich przesłanych zadań (jak wywołanie
  shutdown() + awaitTermination()).
- **ExecutorService.close()** blokuje do momentu zakończenia wszystkich zadań.
  Dzięki temu wzorce structured concurrency są proste -- przesyłasz pracę,
  zamykasz executor i wszystkie wyniki są gotowe.
- **Porównanie wydajności**: 10 zadań, każde śpiące 100ms:
    - Executor virtual threads: ~100ms (wszystkie zadania działają współbieżnie)
    - Fixed thread pool(4): ~300ms (tylko 4 zadania naraz)
*/

// ============================================================
// Sekcja 4: Demonstracja skalowalności
// ============================================================

/*
## Demonstracja skalowalności

- Virtual thread rozpoczyna z **początkowym stosem ~200 bajtów**
  (vs ~1 MB dla wątku platformowego). Stos jest przechowywany na
  stercie i dynamicznie rośnie/kurczy się w miarę potrzeb.
- JVM może obsłużyć **miliony** virtual threads jednocześnie.
  Wąskie gardło przesuwa się z liczby wątków na rzeczywistą pracę
  i pamięć na ramki stosu.
- Gdy virtual thread **blokuje się** (sleep, I/O, pozyskanie blokady),
  JVM odmontowuje jego kontynuację z wątku nośnego. Wątek nośny
  jest natychmiast wolny do uruchomienia innego virtual thread.
- **Uśpienie jest prawie darmowe**: Uśpiony virtual thread zużywa
  tylko pamięć sterty na swój zamrożony stos -- żaden wątek OS
  nie jest zajęty. Oznacza to, że 100 000 wątków, każdy śpiący
  1 sekundę, kończy pracę w około 1 sekundę łącznie, a nie 100 000 sekund.
- Próba tego samego z wątkami platformowymi wymagałaby ~100 GB
  pamięci stosu i prawdopodobnie spowodowałaby awarię JVM lub systemu operacyjnego.
*/

// ============================================================
// Sekcja 5: Virtual threads i blokujące I/O
// ============================================================

/*
## Virtual threads i blokujące I/O

- JVM przechwytuje wywołania blokujące i **automatycznie odmontowuje**
  virtual thread z jego wątku nośnego. Programista nie musi nic
  robić -- kod blokujący po prostu działa.
- **Obsługiwane punkty blokowania** (gdzie następuje odmontowanie):
    - Thread.sleep()
    - BlockingQueue.take() / put()
    - Lock.lock() (ReentrantLock)
    - Odczyt/zapis gniazda (java.net, kanały java.nio w trybie blokującym)
    - Future.get()
    - CountDownLatch.await()
    - Operacje Selector
- **Model kontynuacji**: Gdy virtual thread blokuje się, JVM
  zapisuje cały jego stos na stercie ("kontynuacja"). Gdy warunek
  blokowania zostanie rozwiązany, kontynuacja jest ponownie montowana
  na wątku nośnym -- potencjalnie na **innym** wątku nośnym
  niż pierwotny.
- Jest to **transparentne dla programisty**: kod wygląda jak
  zwykły sekwencyjny kod blokujący, ale pod spodem JVM
  efektywnie multipleksuje tysiące virtual threads na
  kilku wątkach OS.
- **Symulowane I/O**: W tym demo symulujemy zapytania bazodanowe i
  wywołania API za pomocą Thread.sleep(). W prawdziwych aplikacjach każda
  blokująca operacja I/O (JDBC, klienty HTTP, I/O plików) korzysta w ten sam sposób.
*/

// ============================================================
// Sekcja 6: Dobre praktyki i pułapki
// ============================================================

/*
## Dobre praktyki i pułapki

- **Nie twórz puli virtual threads**: Tworzenie stałej puli virtual
  threads niweluje ich cel. Używaj newVirtualThreadPerTaskExecutor()
  lub Thread.startVirtualThread() -- jeden wątek na zadanie.
- **Unikaj bloków/metod `synchronized`**: Virtual thread wewnątrz
  bloku synchronized **przypina** swój wątek nośny -- wątek nośny nie może
  być ponownie użyty przez inne virtual threads dopóki monitor nie zostanie
  zwolniony. Zmniejsza to współbieżność i może powodować degradację wydajności.
- **Używaj ReentrantLock zamiast tego**: ReentrantLock jest przyjazny
  dla virtual threads. Gdy virtual thread blokuje się na lock.lock(),
  prawidłowo odmontowuje się z wątku nośnego.
- **Wyjaśnienie przypinania (pinning)**: Przypinanie występuje, gdy JVM
  nie może odmontować virtual thread. Dwie główne przyczyny:
    - Wewnątrz bloku lub metody `synchronized`
    - Wewnątrz metody natywnej lub funkcji obcej
  Virtual thread nadal działa poprawnie, ale trzyma wątek nośny
  jako zakładnika do momentu zakończenia sekcji przypinającej.
- **Thread-locals są kosztowne**: Ponieważ możesz mieć miliony
  virtual threads, przechowywanie per-wątek (ThreadLocal) może zużywać
  nadmierną ilość pamięci. Java 21 wprowadza **ScopedValue** (podgląd)
  jako lekką, niezmienną alternatywę.
- **Praca CPU-bound nie jest wspomagana**: Virtual threads sprawdzają się,
  gdy zadania spędzają większość czasu na **blokowaniu** (czekaniu na I/O).
  Dla pracy CPU-bound jesteś ograniczony liczbą rdzeni niezależnie
  od typu wątku.
- **Flaga diagnostyczna**: -Djdk.tracePinnedThreads=short (lub =full)
  wypisuje ślad stosu za każdym razem, gdy virtual thread jest przypięty.
  Przydatne podczas programowania i testowania do znajdowania problematycznych
  bloków synchronized.
*/

public class VirtualThreads {

    // ---- Sekcja 6: Implementacje liczników dla demo przypinania ----

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
    // Sekcja 1: Wprowadzenie -- Dlaczego virtual threads
    // ============================================================

    static void introductionWhyVirtualThreads() throws Exception {
        System.out.println("=== Section 1: Introduction -- Why Virtual Threads ===");

        // Dostępne procesory (kontekst rozmiaru puli wątków nośnych)
        System.out.println("Available processors (carrier pool size): " + Runtime.getRuntime().availableProcessors());

        // Tworzenie wątku platformowego
        System.out.println("\n--- Platform thread ---");
        var platformThread = Thread.ofPlatform().name("my-platform-thread").start(() -> {
            var t = Thread.currentThread();
            System.out.println("  Thread: " + t);
            System.out.println("  isVirtual(): " + t.isVirtual());
            System.out.println("  isDaemon(): " + t.isDaemon());
        });
        platformThread.join();

        // Tworzenie virtual thread
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
    // Sekcja 2: Tworzenie virtual threads
    // ============================================================

    static void creatingVirtualThreads() throws Exception {
        System.out.println("\n=== Section 2: Creating Virtual Threads ===");

        // startVirtualThread — metoda wygodna
        System.out.println("--- Thread.startVirtualThread() ---");
        var t1 = Thread.startVirtualThread(() -> {
            System.out.println("  startVirtualThread: " + Thread.currentThread());
        });
        t1.join();

        // Nazwany builder z automatycznie numerowanymi nazwami
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

        // Porównanie właściwości virtual vs platform thread
        System.out.println("\n--- Virtual vs Platform thread comparison ---");
        var virtual = Thread.ofVirtual().name("vt-demo").unstarted(() -> {});
        var platform = Thread.ofPlatform().name("pt-demo").unstarted(() -> {});

        System.out.printf("  %-12s isVirtual=%-5s isDaemon=%-5s name=%s%n",
                "Virtual:", virtual.isVirtual(), virtual.isDaemon(), virtual.getName());
        System.out.printf("  %-12s isVirtual=%-5s isDaemon=%-5s name=%s%n",
                "Platform:", platform.isVirtual(), platform.isDaemon(), platform.getName());
    }

    // ============================================================
    // Sekcja 3: Virtual threads z Executors
    // ============================================================

    static void virtualThreadsWithExecutors() throws Exception {
        System.out.println("\n=== Section 3: Virtual Threads with Executors ===");

        // Executor virtual threads — 10 zadań śpiących po 100ms każde
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

        // Porównanie z fixed thread pool — te same 10 zadań
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
    // Sekcja 4: Demonstracja skalowalności
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
    // Sekcja 5: Virtual threads i blokujące I/O
    // ============================================================

    static void virtualThreadsAndBlockingIO() throws Exception {
        System.out.println("\n=== Section 5: Virtual Threads and Blocking I/O ===");

        // Lokalny rekord dla symulowanego żądania
        record SimulatedRequest(int id, String dbResult, String apiResult, String carrierBefore, String carrierAfter) {}

        System.out.println("--- Simulated request pipeline: DB query (50ms) + API call (100ms) ---");
        System.out.println("  Processing 500 requests concurrently...");

        var start = Instant.now();
        List<Future<SimulatedRequest>> futures;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            futures = IntStream.range(0, 500)
                    .mapToObj(i -> executor.submit(() -> {
                        // Przechwycenie wątku nośnego przed blokowaniem
                        String carrierBefore = Thread.currentThread().toString();

                        // Symulacja zapytania do bazy danych
                        Thread.sleep(50);
                        String dbResult = "db-row-" + i;

                        // Przechwycenie wątku nośnego po pierwszym blokowaniu (może się różnić)
                        String carrierAfter = Thread.currentThread().toString();

                        // Symulacja wywołania API
                        Thread.sleep(100);
                        String apiResult = "api-response-" + i;

                        return new SimulatedRequest(i, dbResult, apiResult, carrierBefore, carrierAfter);
                    }))
                    .toList();
        }

        var duration = Duration.between(start, Instant.now());

        // Wyświetlenie kilku wyników i zmian wątku nośnego
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
    // Sekcja 6: Dobre praktyki i pułapki
    // ============================================================

    static void bestPracticesAndPitfalls() throws Exception {
        System.out.println("\n=== Section 6: Best Practices and Pitfalls ===");

        // Benchmark porównujący ReentrantLock vs synchronized pod obciążeniem
        int threadCount = 1_000;
        int incrementsPerThread = 100;

        // Licznik oparty na ReentrantLock
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

        // Licznik synchronized
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

        // Praca CPU-bound: virtual threads nie pomagają
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

        // Zapowiedź ScopedValue
        System.out.println("\n--- ScopedValue (preview in Java 21) ---");
        System.out.println("  ThreadLocal works but is wasteful with millions of virtual threads.");
        System.out.println("  ScopedValue (JEP 446) provides a lightweight, immutable alternative:");
        System.out.println("    static final ScopedValue<String> USER = ScopedValue.newInstance();");
        System.out.println("    ScopedValue.runWhere(USER, \"alice\", () -> { ... USER.get() ... });");
        System.out.println("  ScopedValues are inherited by child threads and are automatically cleaned up.");
    }

    // Pomocnik: naiwne rekurencyjne Fibonacci dla demo CPU-bound
    static long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    // ============================================================
    // Main -- uruchomienie wszystkich sekcji
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
