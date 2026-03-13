package pl.training.jmodern.module02_java25;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie -- Dlaczego structured concurrency
// ============================================================

/*
## Wprowadzenie -- Dlaczego structured concurrency

- **Problem z niestrukturalną współbieżnością** (ExecutorService):
    - Zadania mogą przeżyć zakres nadrzędny, który je utworzył.
    - Jeśli jedno zadanie się nie powiedzie, zadania siostrzane dalej działają (wyciek zasobów).
    - Ręczne anulowanie jest podatne na błędy i często pomijane.
    - Zrzuty wątków nie pokazują relacji rodzic-dziecko.
- **Analogia**: Structured concurrency jest dla współbieżności tym,
  czym programowanie strukturalne (if/while) było dla goto. Tak jak
  przestaliśmy używać goto i zyskaliśmy lokalne rozumowanie o przepływie
  sterowania, structured concurrency pozwala nam lokalnie rozumować
  o czasie życia współbieżnych operacji.
*/

// ============================================================
// Sekcja 2: Strategie Joiner -- allSuccessfulOrThrow i anySuccessfulResultOrThrow
// ============================================================

/*
## Strategie Joiner -- allSuccessfulOrThrow i anySuccessfulResultOrThrow

- `StructuredTaskScope<T, R>` jest parametryzowany przez Joiner:
    - T = wspólny typ rozwidlonych podzadań
    - R = typ wyniku zwracany przez `join()`
- **`Joiner.allSuccessfulOrThrow()`**:
    - Czeka na zakończenie WSZYSTKICH podzadań z powodzeniem.
    - `join()` zwraca `Stream<Subtask<T>>` zawierający wyniki.
    - Jeśli JAKIEKOLWIEK podzadanie się nie powiedzie → zakres się zamyka,
      pozostałe zadania są anulowane, `join()` rzuca `FailedException`.
    - Zastępuje stary wzorzec `ShutdownOnFailure`.
- **`Joiner.anySuccessfulResultOrThrow()`**:
    - Zwraca PIERWSZY udany wynik `T` z `join()`.
    - Pozostałe zadania są natychmiast anulowane.
    - Jeśli WSZYSTKIE zadania się nie powiodą → `join()` rzuca `FailedException`.
    - Zastępuje stary wzorzec `ShutdownOnSuccess`.
- **`Subtask<T>`** ma trzy stany:
    - SUCCESS → `get()` zwraca wynik
    - FAILED → `exception()` zwraca wyjątek
    - UNAVAILABLE → zadanie zostało anulowane lub jeszcze się nie zakończyło
- **`FailedException`** to niechecked RuntimeException, który
  opakowuje pierwszą awarię podzadania jako swoją przyczynę. Dodatkowe
  awarie mogą pojawić się jako wyjątki stłumione.
*/

// ============================================================
// Sekcja 3: Strategie Joiner -- awaitAll i allUntil
// ============================================================

/*
## Strategie Joiner -- awaitAll i allUntil

- **`Joiner.awaitAllSuccessfulOrThrow()`**:
    - Czeka na wszystkie podzadania, zwraca Void.
    - Po sukcesie → sprawdź referencje Subtask z `fork()`.
    - Jakakolwiek awaria → `FailedException` (jak allSuccessfulOrThrow,
      ale zachowujesz referencje do poszczególnych Subtask).
- **`Joiner.awaitAll()`**:
    - Najbardziej łagodny joiner — czeka na WSZYSTKIE podzadania
      niezależnie od sukcesu czy awarii. Nigdy nie rzuca przy awarii podzadania.
    - `join()` zwraca Void. Wywołujący sprawdza stany Subtask
      ręcznie przez `state()`, `get()`, `exception()`.
    - Używaj, gdy częściowa awaria jest akceptowalna.
- **`Joiner.allUntil(Predicate)`**:
    - Czeka, aż predykat zwróci true dla jakiegokolwiek ukończonego
      Subtask, następnie zamyka pozostałe zadania.
    - `join()` zwraca `Stream<Subtask<T>>`.
    - Umożliwia własną logikę krótkiego spięcia.
- **Przewodnik wyboru Joiner**:
    - Wszystko albo nic → `allSuccessfulOrThrow()`
    - Pierwszy wygrywa / wyścig → `anySuccessfulResultOrThrow()`
    - Potrzebne referencje do poszczególnych podzadań → `awaitAllSuccessfulOrThrow()`
    - Częściowe awarie OK → `awaitAll()`
    - Własny warunek zatrzymania → `allUntil(Predicate)`
*/

// ============================================================
// Sekcja 4: Konfiguracja, limity czasowe i obsługa wyjątków
// ============================================================

/*
## Konfiguracja, limity czasowe i obsługa wyjątków

- **Konfiguracja** jest przekazywana jako drugi parametr do `open()`:
    - `withName(String)` — nazywa zakres, widoczna w zrzutach
      wątków do celów diagnostycznych.
    - `withTimeout(Duration)` — ustawia termin dla zakresu.
    - `withThreadFactory(ThreadFactory)` — dostosowanie tworzenia
      wątków (np. nazwane virtual threads).
- **Limit czasowy**: Jeśli termin upłynie przed zakończeniem `join()`,
  `join()` rzuca `StructuredTaskScope.TimeoutException` (klasa
  zagnieżdżona, NIE java.util.concurrent.TimeoutException).
  Zakres się zamyka i pozostałe zadania są anulowane.
- **FailedException** opakowuje pierwszą awarię podzadania jako swoją
  przyczynę. Użyj `getCause()` do zbadania oryginalnego wyjątku.
  Dodatkowe awarie mogą być stłumione.
- **Reguły cyklu życia** (naruszenie tych reguł rzuca IllegalStateException):
    - `close()` przed `join()` → błąd
    - `fork()` po `join()` → błąd
    - `fork()` po anulowaniu zakresu → błąd
- **`isCancelled()`** — zwraca true jeśli zakres został zamknięty
  (np. z powodu limitu czasowego lub decyzji polityki joinera).
*/

// ============================================================
// Sekcja 5: Praktyczne wzorce i porównanie
// ============================================================

/*
## Praktyczne wzorce i porównanie

- **Structured concurrency vs ExecutorService**:
    - Czas życia ograniczony zakresem (zadania nie mogą uciec)
    - Automatyczne anulowanie przy awarii
    - Obserwowalność zrzutów wątków (hierarchia rodzic-dziecko)
    - Virtual threads domyślnie (bez wymiarowania puli)
- **Wzorzec fan-out**: rozwidl N zadań z kolekcji, zbierz
  wszystkie wyniki przez strumień allSuccessfulOrThrow.
- **Zagnieżdżone zakresy**: Wewnętrzny zakres w rozwidlonym zadaniu.
  Błędy w wewnętrznych zakresach propagują się naturalnie do zakresu zewnętrznego.
- **Integracja z ScopedValue** (preview): Wartości kontekstu związane
  w wątku nadrzędnym są automatycznie dziedziczone przez rozwidlone
  zadania przez ScopedValue, umożliwiając bezpieczną propagację
  kontekstu bez ThreadLocal.
- **Najlepsze praktyki**:
    - Zawsze używaj try-with-resources dla zakresów
    - Preferuj najbardziej restrykcyjny joiner pasujący do przypadku użycia
    - Utrzymuj rozwidlone zadania jako I/O-bound (structured concurrency
      błyszczy przy operacjach blokujących na virtual threads)
    - Używaj withTimeout aby zapobiec nieskończonemu blokowaniu
    - Używaj withName dla łatwości debugowania
*/

public class StructuredConcurrency {

    // ---- Typy wewnętrzne ----

    record User(long id, String name, String email) {}
    record Order(long id, long userId, String product, double price) {}
    record Review(long id, long userId, String text, int rating) {}
    record UserProfile(User user, List<Order> orders, List<Review> reviews) {}
    record WeatherData(String city, double temperature, String condition) {}
    record SearchResult(String source, List<String> results) {}

    // ---- Metody pomocnicze (symulacja I/O) ----

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
    // Sekcja 1: Wprowadzenie -- Dlaczego structured concurrency
    // ============================================================

    static void introductionToStructuredConcurrency() throws Exception {
        System.out.println("=== Section 1: Introduction -- Why Structured Concurrency ===");

        // ---- Demo 1: Podejście niestrukturalne (ExecutorService + Futures) ----
        System.out.println("\n--- Demo 1: Unstructured approach (ExecutorService) ---");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var userFuture = executor.submit(() -> simulateFetchUser(1));
            var ordersFuture = executor.submit(() -> simulateFetchOrders(1));
            var reviewsFuture = executor.submit(() -> simulateFetchReviews(1));

            // Trzeba ręcznie obsłużyć każdy future — brak automatycznego anulowania
            try {
                var user = userFuture.get();
                var orders = ordersFuture.get();
                var reviews = reviewsFuture.get();
                var profile = new UserProfile(user, orders, reviews);
                System.out.println("  User: " + profile.user().name());
                System.out.println("  Orders: " + profile.orders().size());
                System.out.println("  Reviews: " + profile.reviews().size());
            } catch (ExecutionException e) {
                // Jeśli jedno się nie powiedzie, inne dalej działają — trzeba anulować ręcznie
                userFuture.cancel(true);
                ordersFuture.cancel(true);
                reviewsFuture.cancel(true);
                System.out.println("  Failed: " + e.getCause());
            }
        }
        System.out.println("  Problem: if one task fails, siblings continue running unless manually cancelled");

        // ---- Demo 2: Podejście strukturalne ----
        System.out.println("\n--- Demo 2: Structured approach (StructuredTaskScope) ---");
        try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())) {
            // Rozwidlenie zadań — każde działa we własnym virtual thread
            var userTask = scope.fork(() -> simulateFetchUser(1));
            var ordersTask = scope.fork(() -> simulateFetchOrders(1));
            var reviewsTask = scope.fork(() -> simulateFetchReviews(1));

            // Join czeka na wszystkie zadania; jeśli którekolwiek się nie powiedzie, inne są anulowane automatycznie
            var subtasks = scope.join();

            // Budowanie wyniku z ukończonych podzadań
            var results = subtasks.toList();
            System.out.println("  Completed " + results.size() + " subtasks");
            System.out.println("  User: " + userTask.get());
            System.out.println("  Orders: " + ordersTask.get());
            System.out.println("  Reviews: " + reviewsTask.get());
        }
        System.out.println("  Benefit: automatic cancellation, scope-bounded lifetime, clean code");

        // ---- Demo 3: Gwarancja czasu życia — limit czasowy anuluje zadania ----
        System.out.println("\n--- Demo 3: Lifetime guarantee (timeout cancels tasks) ---");
        try (var scope = StructuredTaskScope.open(
                Joiner.allSuccessfulOrThrow(),
                cf -> cf.withTimeout(Duration.ofMillis(500)))) {

            scope.fork(() -> simulateSlowOperation(3000)); // 3 sekundy — zostanie anulowane
            scope.fork(() -> simulateSlowOperation(100));   // 100ms — wystarczająco szybkie

            scope.join();
            System.out.println("  Should not reach here");
        } catch (StructuredTaskScope.TimeoutException e) {
            System.out.println("  TimeoutException caught — scope timed out after 500ms");
            System.out.println("  Slow task (3s) was automatically cancelled");
        }
    }

    // ============================================================
    // Sekcja 2: Strategie Joiner -- allSuccessfulOrThrow i anySuccessfulResultOrThrow
    // ============================================================

    static void joinerAllAndAny() throws Exception {
        System.out.println("\n=== Section 2: Joiner Strategies -- allSuccessfulOrThrow and anySuccessfulResultOrThrow ===");

        // ---- Demo 1: allSuccessfulOrThrow — szczęśliwa ścieżka ----
        System.out.println("\n--- Demo 1: allSuccessfulOrThrow -- happy path ---");
        try (var scope = StructuredTaskScope.open(Joiner.<Object>allSuccessfulOrThrow())) {
            var userTask = scope.fork(() -> simulateFetchUser(42));
            var ordersTask = scope.fork(() -> simulateFetchOrders(42));
            var reviewsTask = scope.fork(() -> simulateFetchReviews(42));

            var subtasks = scope.join().toList();
            System.out.println("  join() returned " + subtasks.size() + " subtasks");

            // Budowanie UserProfile z wyników poszczególnych podzadań
            var profile = new UserProfile(
                    userTask.get(),
                    ordersTask.get(),
                    reviewsTask.get()
            );
            System.out.println("  UserProfile: " + profile.user().name()
                    + ", " + profile.orders().size() + " orders"
                    + ", " + profile.reviews().size() + " reviews");
        }

        // ---- Demo 2: allSuccessfulOrThrow — awaria ----
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

        // ---- Demo 3: anySuccessfulResultOrThrow — wyścig ----
        System.out.println("\n--- Demo 3: anySuccessfulResultOrThrow -- racing mirrors ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(() -> { Thread.sleep(200); return "Mirror-A (200ms)"; });
            scope.fork(() -> { Thread.sleep(50);  return "Mirror-B (50ms)"; });
            scope.fork(() -> { Thread.sleep(500); return "Mirror-C (500ms)"; });

            String fastest = scope.join();
            System.out.println("  Fastest result: " + fastest);
            System.out.println("  Other tasks were cancelled automatically");
        }

        // ---- Demo 4: anySuccessfulResultOrThrow — wszystkie się nie powiodły ----
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
    // Sekcja 3: Strategie Joiner -- awaitAll i allUntil
    // ============================================================

    static void joinerAwaitAllAndAllUntil() throws Exception {
        System.out.println("\n=== Section 3: Joiner Strategies -- awaitAll and allUntil ===");

        // ---- Demo 1: awaitAll — tolerancja częściowych awarii ----
        System.out.println("\n--- Demo 1: awaitAll -- partial failure tolerance ---");
        try (var scope = StructuredTaskScope.open(Joiner.<String>awaitAll())) {
            var tasks = new ArrayList<Subtask<String>>();
            tasks.add(scope.fork(() -> { Thread.sleep(50);  return "Task-1: OK"; }));
            tasks.add(scope.fork(() -> { Thread.sleep(80);  throw new RuntimeException("Task-2: DB error"); }));
            tasks.add(scope.fork(() -> { Thread.sleep(100); return "Task-3: OK"; }));
            tasks.add(scope.fork(() -> { Thread.sleep(120); throw new RuntimeException("Task-4: Timeout"); }));
            tasks.add(scope.fork(() -> { Thread.sleep(60);  return "Task-5: OK"; }));

            scope.join(); // NIE rzuca nawet jeśli niektóre zadania się nie powiodły

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

        // ---- Demo 2: allUntil
        System.out.println("\n--- Demo 3: allUntil -- custom short-circuit ---");
        try (var scope = StructuredTaskScope.open(
                Joiner.<Integer>allUntil(subtask ->
                        subtask.state() == Subtask.State.SUCCESS && subtask.get() > 50))) {

            scope.fork(() -> { Thread.sleep(50);  return 10; });
            scope.fork(() -> { Thread.sleep(100); return 25; });
            scope.fork(() -> { Thread.sleep(150); return 75; }); // To wyzwala predykat
            scope.fork(() -> { Thread.sleep(200); return 30; }); // Powinno zostać anulowane
            scope.fork(() -> { Thread.sleep(250); return 90; }); // Powinno zostać anulowane

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
    // Main -- uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) throws Exception {
        introductionToStructuredConcurrency();
        joinerAllAndAny();
        joinerAwaitAllAndAllUntil();
    }
}
