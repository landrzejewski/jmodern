package pl.training.jmodern.module02_java8;

import java.util.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do Optional
// ============================================================

/*
## Wprowadzenie do Optional

- `Optional<T>` to obiekt kontenerowy, który może, ale nie musi, przechowywać
  wartość różną od null. Wprowadzony w Java 8 w celu dostarczenia rozwiązania
  na **poziomie typów** do reprezentowania braku wartości.
- **Dlaczego został wprowadzony**: `NullPointerException` to najczęstszy
  wyjątek czasu wykonania w Javie. Gdy metoda zwraca `null`,
  wywołujący nie ma sposobu — patrząc na samą sygnaturę —
  aby wiedzieć, czy `null` jest poprawnym zwracanym wynikiem. `Optional` sprawia,
  że "brak" jest **jawny** w systemie typów.
- **`Optional` jako typ zwracany vs zwracanie `null`**:
    - Zwracanie `null` zmusza każdego wywołującego do defensywnego sprawdzania
      `null`, a zapomnienie o sprawdzeniu prowadzi do NPE.
    - Zwracanie `Optional<T>` komunikuje *intencję*: "ta metoda
      może legalnie nie wytworzyć wyniku." Kompilator i IDE
      mogą kierować wywołującego do obsługi obu przypadków.
- **`Optional` nie jest ogólnym `Maybe`**: został zaprojektowany
  dla **typów zwracanych metod**, nie dla:
    - **Pól** — dodaje pośredniość, psuje serializację.
    - **Parametrów metod** — użyj przeciążania lub `@Nullable`.
    - **Kolekcji** — zamiast tego użyj pustej kolekcji.
- **Relacja ze wzorcem null-object**: oba podejścia
  eliminują jawne sprawdzanie null, ale Optional jest kontenerem
  ogólnego przeznaczenia, podczas gdy wzorzec null-object dostarcza domenowo-specyficzną
  implementację "nic nie rób" interfejsu.
*/

// ============================================================
// Sekcja 2: Tworzenie obiektów Optional
// ============================================================

/*
## Tworzenie obiektów Optional

- `Optional.of(value)` — opakowuje wartość różną od null. Rzuca
  `NullPointerException` natychmiast, jeśli `value` jest null.
  Używaj, gdy masz pewność, że wartość nie jest null.
- `Optional.ofNullable(value)` — opakowuje wartość, jeśli jest różna od null,
  zwraca `Optional.empty()`, jeśli jest null. Używaj, gdy wartość może
  legalnie być null (np. z legacy API).
- `Optional.empty()` — zwraca pusty Optional. Używaj jako
  wartość zwracaną, gdy nie ma wyniku do dostarczenia.
- **Kiedy używać której metody fabrycznej**:
    - `of()` → szybkie zgłoszenie błędu jeśli null (błąd programistyczny przy przekazaniu null).
    - `ofNullable()` → elegancka obsługa wartości nullable.
    - `empty()` → jawne "brak wyniku" w warunkowych zwracaniach.
- **Warianty prymitywne**: `OptionalInt`, `OptionalLong`,
  `OptionalDouble` — unikanie narzutu autoboxingu. Odzwierciedlają
  `Optional<T>`, ale z metodami specyficznymi dla prymitywów, takimi jak
  `getAsInt()`, `orElse(int)`, itp.
*/

// ============================================================
// Sekcja 3: Sprawdzanie i wyodrębnianie wartości
// ============================================================

/*
## Sprawdzanie i wyodrębnianie wartości

- `isPresent()` — zwraca `true`, jeśli wartość jest obecna.
- `isEmpty()` (Java 11) — zwraca `true`, jeśli wartość nie jest obecna;
  logiczna negacja `isPresent()`.
- `get()` — zwraca wartość, jeśli jest obecna, w przeciwnym razie rzuca
  `NoSuchElementException`. **Unikaj w kodzie produkcyjnym** — niweczy
  cel Optional, wprowadzając potencjalny niekontrolowany wyjątek.
- `orElse(defaultValue)` — zwraca wartość, jeśli jest obecna, lub
  podaną wartość domyślną. Wartość domyślna jest **zawsze ewaluowana**,
  nawet gdy Optional zawiera wartość.
- `orElseGet(Supplier)` — zwraca wartość, jeśli jest obecna, lub
  **leniwie** oblicza wartość domyślną za pomocą Supplier. Supplier
  jest wywoływany **tylko gdy Optional jest pusty**.
- `orElseThrow()` (Java 10) — to samo co `get()`, ale z
  jaśniejszą, bardziej intencjonalną nazwą.
- `orElseThrow(Supplier<Exception>)` — rzuca niestandardowy
  wyjątek, jeśli jest pusty. Preferowane dla błędów domenowych.
- **Ważne**: `orElse` vs `orElseGet` — `orElse` zawsze
  ewaluuje swój argument (co może być kosztowne), podczas gdy
  `orElseGet` odracza ewaluację. Używaj `orElseGet`, gdy
  wartość domyślna jest kosztowna do obliczenia.
*/

// ============================================================
// Sekcja 4: Transformacja Optional (map, flatMap, filter)
// ============================================================

/*
## Transformacja Optional (`map`, `flatMap`, `filter`)

- `map(Function)` — jeśli wartość jest obecna, stosuje funkcję
  i opakowuje wynik w nowy Optional. Jeśli pusty, zwraca pusty.
  Sygnatura: `Optional<T>.map(T -> U)` → `Optional<U>`.
- `flatMap(Function)` — jak `map`, ale funkcja mapująca
  sama zwraca `Optional<U>`. Unika `Optional<Optional<T>>`.
  Używaj, gdy transformacja może również wytworzyć brak wyniku.
- `filter(Predicate)` — jeśli wartość jest obecna **i** pasuje do
  predykatu, zwraca ten sam Optional; w przeciwnym razie zwraca pusty.
- **Łączenie w łańcuchy**: `map`, `filter` i `flatMap` mogą być łączone
  w łańcuchy, aby budować płynne potoki zastępujące zagnieżdżone sprawdzanie null:
  ```
  optional.map(User::getAddress)
          .map(Address::getCity)
          .filter(city -> city.startsWith("W"))
          .orElse("Unknown")
  ```
- **`map` vs `flatMap`** — to samo rozróżnienie co w Stream
  API: używaj `map`, gdy funkcja zwraca zwykłą wartość, używaj
  `flatMap`, gdy funkcja zwraca `Optional`.
*/

// ============================================================
// Sekcja 5: Akcje warunkowe (ifPresent, ifPresentOrElse)
// ============================================================

/*
## Akcje warunkowe (`ifPresent`, `ifPresentOrElse`)

- `ifPresent(Consumer)` — wykonuje podaną akcję tylko jeśli
  wartość jest obecna. Zwraca `void`. To odpowiednik "zrób coś"
  dla `map` (który jest "przekształć coś").
- `ifPresentOrElse(Consumer, Runnable)` (Java 9) — wykonuje
  Consumer, jeśli wartość jest obecna, lub Runnable, jeśli jest pusty. Obsługuje oba
  przypadki w jednym wywołaniu.
- **Zastępowanie wzorców `if (x != null)`**: zamiast:
  ```
  if (opt.isPresent()) {
      process(opt.get());
  }
  ```
  użyj:
  ```
  opt.ifPresent(this::process);
  ```
- **`ifPresent` vs `isPresent` + `get`**: `ifPresent` jest bezpieczniejsze,
  ponieważ eliminuje możliwość wywołania `get()` na
  pustym Optional. Wyraża intencję jaśniej i jest
  bardziej zwięzłe.
*/

// ============================================================
// Sekcja 6: Łączenie Optional (or, stream)
// ============================================================

/*
## Łączenie Optional (`or`, `stream`)

- `or(Supplier<Optional>)` (Java 9) — jeśli wartość jest obecna,
  zwraca `this`; w przeciwnym razie zwraca Optional wytworzony przez
  Supplier. W odróżnieniu od `orElse`/`orElseGet`, które rozpakowują do surowej
  wartości, `or()` pozostaje w świecie Optional.
- `stream()` (Java 9) — konwertuje Optional na Stream
  o zero lub jednym elemencie. Zwraca `Stream.of(value)`, jeśli obecny, lub
  `Stream.empty()`, jeśli nie.
- **Użycie `stream()` z `flatMap` do filtrowania pustych Optional**:
  mając `List<Optional<T>>`, możesz wyodrębnić wszystkie obecne wartości:
  ```
  optionals.stream()
           .flatMap(Optional::stream)
           .collect(toList())
  ```
- **Łańcuchy zapasowe**: wiele wywołań `or()` może być łączonych w łańcuchy:
  ```
  findInCache(key)
      .or(() -> findInDatabase(key))
      .or(() -> findInRemoteService(key))
  ```
*/

// ============================================================
// Sekcja 7: Optional ze strumieniami
// ============================================================

/*
## Optional ze strumieniami

- Wiele operacji terminalnych Stream zwraca Optional:
    - `findFirst()` / `findAny()` — pierwszy/dowolny pasujący element.
    - `min(Comparator)` / `max(Comparator)` — najmniejszy/największy.
    - `reduce(BinaryOperator)` — zakumulowany wynik (dwuargumentowe
      `reduce(identity, op)` **nie** zwraca Optional, ponieważ zawsze
      ma tożsamość jako wartość zapasową).
- `Optional.stream()` (Java 9) łączy Optional z powrotem ze światem
  Stream, umożliwiając integrację w większych potokach.
- **Wzorzec: `Stream<Optional<T>>` → `Stream<T>`**:
  ```
  stream.flatMap(Optional::stream)
  ```
  To zastępuje idiom sprzed Java 9:
  ```
  stream.filter(Optional::isPresent).map(Optional::get)
  ```
- **Prymitywne warianty Optional**: strumienie prymitywne (`IntStream`,
  `LongStream`, `DoubleStream`) zwracają `OptionalInt`,
  `OptionalLong`, `OptionalDouble` z `min()`, `max()`,
  `findFirst()`, `average()`, `reduce()`.
*/

// ============================================================
// Sekcja 8: Typowe wzorce i antywzorce
// ============================================================

/*
## Typowe wzorce i antywzorce

- **Antywzorzec**: `if (opt.isPresent()) opt.get()` — zamiast tego użyj
  `orElse`, `map` lub `ifPresent`. Użycie `get()` po
  `isPresent()` jest rozwlekłe i podatne na błędy.
- **Antywzorzec**: `Optional` jako parametr metody — zmusza
  wywołujących do niepotrzebnego opakowywania wartości. Zamiast tego użyj
  przeciążania metod lub adnotacji `@Nullable`.
- **Antywzorzec**: `Optional` jako typ pola — dodaje narzut
  pamięciowy, psuje frameworki serializacji. Używaj `null` wewnętrznie
  i udostępniaj Optional poprzez getter, jeśli potrzeba:
  ```
  private String email; // nullable
  public Optional<String> getEmail() { return Optional.ofNullable(email); }
  ```
- **Antywzorzec**: `Optional.of(collection)` — pusta kolekcja
  już reprezentuje "brak elementów". Opakowywanie jej nic nie dodaje.
  Preferuj zwracanie pustej `List`/`Set`/`Map`.
- **Wzorzec**: zastępowanie zagnieżdżonych sprawdzeń null za pomocą `map`/`flatMap`:
  ```
  // Przed: if (user != null && user.getAddress() != null && ...)
  // Po:
  Optional.ofNullable(user)
          .flatMap(User::optionalAddress)
          .map(Address::city)
          .orElse("unknown")
  ```
- **Wzorzec**: Optional w typach zwracanych dla metod repozytorium/wyszukiwania:
  `Optional<User> findByEmail(String email)`.
- **Wzorzec**: konwersja legacy nullable API na Optional:
  `Optional.ofNullable(legacyMap.get(key))`.
- **Ostrzeżenie dotyczące serializacji**: `Optional` **nie** implementuje
  `Serializable`. Nie powinien być używany jako pole w klasach,
  które muszą być serializowane (DTO, encje, itp.).
*/

public class Optionals {

    // --- Typy pomocnicze do demonstracji ---

    record Address(String city, String zip) {}

    record User(String name, String email, Address address) {

        Optional<String> optionalEmail() {
            return Optional.ofNullable(email);
        }

        Optional<Address> optionalAddress() {
            return Optional.ofNullable(address);
        }
    }

    // --- Pomocnicze metody repozytorium ---

    private static final Map<String, User> USER_DB = Map.of(
            "alice", new User("Alice", "alice@example.com", new Address("Warsaw", "00-001")),
            "bob", new User("Bob", null, new Address("Krakow", "30-001")),
            "charlie", new User("Charlie", "charlie@example.com", null)
    );

    static Optional<User> findUserByName(String name) {
        return Optional.ofNullable(USER_DB.get(name.toLowerCase()));
    }

    static Optional<String> findEmailByName(String name) {
        return findUserByName(name).flatMap(User::optionalEmail);
    }

    // Symuluje kosztowne obliczanie wartości domyślnej
    static String computeExpensiveDefault() {
        System.out.println("  (computing expensive default...)");
        return "default@example.com";
    }

    // ============================================================
    // Sekcja 1: Wprowadzenie do Optional
    // ============================================================

    static void introductionToOptional() {
        System.out.println("=== Introduction to Optional ===");

        // Problem: metoda zwraca null — wywołujący nie ma sygnału na poziomie typów
        Map<String, String> config = Map.of("host", "localhost", "port", "8080");
        String timeout = config.get("timeout"); // zwraca null — brak wpisu
        // Bez Optional musimy pamiętać o sprawdzeniu:
        if (timeout != null) {
            System.out.println("timeout: " + timeout);
        } else {
            System.out.println("timeout not configured (null check)");
        }

        // Z Optional: intencja jest jawna w typie zwracanym
        Optional<String> maybeTimeout = Optional.ofNullable(config.get("timeout"));
        System.out.println("timeout via Optional: " + maybeTimeout.orElse("30s (default)"));

        // Optional komunikuje, że brak wartości jest poprawnym wynikiem
        Optional<User> foundUser = findUserByName("alice");
        Optional<User> missingUser = findUserByName("unknown");
        System.out.println("found user: " + foundUser);
        System.out.println("missing user: " + missingUser);

        // Optional vs wzorzec null-object
        // null-object: konkretna implementacja "nic nie rób" (domenowo-specyficzna)
        // Optional: kontener ogólnego przeznaczenia dla dowolnego typu
        System.out.println("Optional.empty() is a general-purpose 'no value': " + Optional.empty());
    }

    // ============================================================
    // Sekcja 2: Tworzenie obiektów Optional
    // ============================================================

    static void creatingOptionals() {
        System.out.println("\n=== Creating Optionals ===");

        // Optional.of — opakowuje wartość różną od null
        Optional<String> present = Optional.of("Hello");
        System.out.println("Optional.of(\"Hello\"): " + present);

        // Optional.of(null) rzuca NullPointerException natychmiast
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null): NullPointerException — " + e.getMessage());
        }

        // Optional.ofNullable — bezpieczne dla potencjalnie null wartości
        String value = null;
        Optional<String> nullable = Optional.ofNullable(value);
        System.out.println("Optional.ofNullable(null): " + nullable);

        Optional<String> nonNull = Optional.ofNullable("World");
        System.out.println("Optional.ofNullable(\"World\"): " + nonNull);

        // Optional.empty — pusty Optional
        Optional<String> empty = Optional.empty();
        System.out.println("Optional.empty(): " + empty);

        // Kiedy używać którego:
        // of()         → wiesz, że wartość nie jest null (szybkie zgłoszenie błędu, jeśli się mylisz)
        // ofNullable() → wartość może być null (z legacy API, Map.get, itp.)
        // empty()      → jawne zwracanie "brak wyniku"

        // Warianty prymitywne — unikanie autoboxingu
        OptionalInt optInt = OptionalInt.of(42);
        OptionalLong optLong = OptionalLong.of(100_000_000L);
        OptionalDouble optDouble = OptionalDouble.of(3.14);
        System.out.println("OptionalInt: " + optInt);
        System.out.println("OptionalLong: " + optLong);
        System.out.println("OptionalDouble: " + optDouble);

        OptionalInt emptyInt = OptionalInt.empty();
        System.out.println("OptionalInt.empty(): " + emptyInt);
    }

    // ============================================================
    // Sekcja 3: Sprawdzanie i wyodrębnianie wartości
    // ============================================================

    static void checkingAndExtractingValues() {
        System.out.println("\n=== Checking and Extracting Values ===");

        Optional<String> present = Optional.of("Java");
        Optional<String> empty = Optional.empty();

        // isPresent / isEmpty
        System.out.println("present.isPresent(): " + present.isPresent());
        System.out.println("empty.isPresent(): " + empty.isPresent());
        System.out.println("empty.isEmpty(): " + empty.isEmpty()); // Java 11

        // get() — rzuca NoSuchElementException jeśli pusty (unikaj w produkcji)
        System.out.println("present.get(): " + present.get());
        try {
            empty.get();
        } catch (NoSuchElementException e) {
            System.out.println("empty.get(): NoSuchElementException — " + e.getMessage());
        }

        // orElse — zwraca wartość domyślną (zawsze ewaluowana)
        System.out.println("present.orElse(\"default\"): " + present.orElse("default"));
        System.out.println("empty.orElse(\"default\"): " + empty.orElse("default"));

        // orElseGet — leniwie oblicza wartość domyślną (Supplier wywoływany tylko gdy pusty)
        System.out.println("present.orElseGet(() -> ...): " + present.orElseGet(() -> "computed"));
        System.out.println("empty.orElseGet(() -> ...): " + empty.orElseGet(() -> "computed"));

        // orElse vs orElseGet — ważna różnica:
        // orElse ZAWSZE ewaluuje swój argument, nawet gdy wartość jest obecna
        System.out.println("--- orElse vs orElseGet side-effect demo ---");
        System.out.println("present.orElse(expensive): " + present.orElse(computeExpensiveDefault()));
        System.out.println("present.orElseGet(expensive): " + present.orElseGet(Optionals::computeExpensiveDefault));
        // Zwróć uwagę: orElse wypisało "(computing expensive default...)" mimo że wartość była obecna
        // orElseGet NIE obliczyło wartości domyślnej, ponieważ wartość była obecna

        // orElseThrow() — Java 10 — to samo co get(), ale z jaśniejszą nazwą
        System.out.println("present.orElseThrow(): " + present.orElseThrow());

        // orElseThrow(Supplier) — rzuca niestandardowy wyjątek
        try {
            empty.orElseThrow(() -> new IllegalStateException("value is required"));
        } catch (IllegalStateException e) {
            System.out.println("empty.orElseThrow(custom): " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 4: Transformacja Optional (map, flatMap, filter)
    // ============================================================

    static void transformingOptionals() {
        System.out.println("\n=== Transforming Optionals (map, flatMap, filter) ===");

        Optional<String> name = Optional.of("Alice");
        Optional<String> empty = Optional.empty();

        // map — transformacja wartości, jeśli jest obecna
        Optional<Integer> nameLength = name.map(String::length);
        Optional<Integer> emptyLength = empty.map(String::length);
        System.out.println("name.map(length): " + nameLength);
        System.out.println("empty.map(length): " + emptyLength);

        Optional<String> upperName = name.map(String::toUpperCase);
        System.out.println("name.map(toUpperCase): " + upperName);

        // filter — zachowaj wartość tylko jeśli pasuje do predykatu
        Optional<String> startsWithA = name.filter(n -> n.startsWith("A"));
        Optional<String> startsWithB = name.filter(n -> n.startsWith("B"));
        System.out.println("name.filter(startsWith A): " + startsWithA);
        System.out.println("name.filter(startsWith B): " + startsWithB);

        // flatMap — gdy funkcja mapująca sama zwraca Optional
        // Unika Optional<Optional<T>>
        Optional<String> aliceEmail = findUserByName("Alice").flatMap(User::optionalEmail);
        Optional<String> bobEmail = findUserByName("Bob").flatMap(User::optionalEmail);
        Optional<String> unknownEmail = findUserByName("unknown").flatMap(User::optionalEmail);
        System.out.println("Alice's email (flatMap): " + aliceEmail);
        System.out.println("Bob's email (flatMap): " + bobEmail);     // pusty — Bob nie ma emaila
        System.out.println("Unknown's email (flatMap): " + unknownEmail); // pusty — użytkownik nie znaleziony

        // Łączenie map/flatMap/filter w płynne potoki
        // "Znajdź miasto Alice, ale tylko jeśli zaczyna się na 'W'"
        String city = findUserByName("Alice")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .filter(c -> c.startsWith("W"))
                .orElse("unknown");
        System.out.println("Alice's city (starts with W): " + city);

        // Ten sam łańcuch dla Charlie — który nie ma adresu
        String charlieCity = findUserByName("Charlie")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("Charlie's city: " + charlieCity); // unknown — brak adresu

        // Porównanie map vs flatMap
        // map: funkcja zwraca zwykłą wartość → automatycznie opakowywana w Optional
        // flatMap: funkcja zwraca Optional<U> → bez podwójnego opakowywania
        Optional<Optional<String>> doubleWrapped = findUserByName("Alice").map(User::optionalEmail);
        Optional<String> singleWrapped = findUserByName("Alice").flatMap(User::optionalEmail);
        System.out.println("map (double wrapped): " + doubleWrapped);
        System.out.println("flatMap (single wrapped): " + singleWrapped);
    }

    // ============================================================
    // Sekcja 5: Akcje warunkowe (ifPresent, ifPresentOrElse)
    // ============================================================

    static void conditionalActions() {
        System.out.println("\n=== Conditional Actions (ifPresent, ifPresentOrElse) ===");

        Optional<String> present = Optional.of("Java");
        Optional<String> empty = Optional.empty();

        // ifPresent — wykonaj akcję tylko jeśli wartość jest obecna
        System.out.print("present.ifPresent: ");
        present.ifPresent(v -> System.out.println("value is " + v));

        System.out.print("empty.ifPresent: ");
        empty.ifPresent(v -> System.out.println("value is " + v));
        System.out.println("(nothing printed — empty)");

        // ifPresentOrElse — Java 9 — obsługa obu przypadków
        present.ifPresentOrElse(
                v -> System.out.println("ifPresentOrElse (present): " + v),
                () -> System.out.println("ifPresentOrElse (present): no value")
        );
        empty.ifPresentOrElse(
                v -> System.out.println("ifPresentOrElse (empty): " + v),
                () -> System.out.println("ifPresentOrElse (empty): no value")
        );

        // Zastępowanie wzorców if (x != null)
        // Przed:
        Optional<User> user = findUserByName("alice");
        if (user.isPresent()) {
            System.out.println("isPresent+get: " + user.get().name());
        }
        // Po (preferowane):
        findUserByName("alice").ifPresent(u ->
                System.out.println("ifPresent: " + u.name())
        );

        // Praktyczny przykład: wyślij email tylko jeśli adres jest obecny
        findUserByName("alice").flatMap(User::optionalEmail).ifPresentOrElse(
                email -> System.out.println("sending email to: " + email),
                () -> System.out.println("no email address — skipping notification")
        );

        findUserByName("bob").flatMap(User::optionalEmail).ifPresentOrElse(
                email -> System.out.println("sending email to: " + email),
                () -> System.out.println("no email address for Bob — skipping notification")
        );
    }

    // ============================================================
    // Sekcja 6: Łączenie Optional (or, stream)
    // ============================================================

    // Symulowane źródła zapasowe
    static Optional<String> findInCache(String key) {
        System.out.println("  looking in cache...");
        return Optional.empty(); // symulacja braku trafienia w cache
    }

    static Optional<String> findInDatabase(String key) {
        System.out.println("  looking in database...");
        if ("config.timeout".equals(key)) {
            return Optional.of("30s");
        }
        return Optional.empty();
    }

    static Optional<String> findInDefaults(String key) {
        System.out.println("  looking in defaults...");
        return Optional.of("60s");
    }

    static void combiningOptionals() {
        System.out.println("\n=== Combining Optionals (or, stream) ===");

        Optional<String> present = Optional.of("primary");
        Optional<String> empty = Optional.empty();

        // or() — Java 9 — rezerwowe przejście do innego Optional (pozostaje w świecie Optional)
        Optional<String> result1 = present.or(() -> Optional.of("fallback"));
        Optional<String> result2 = empty.or(() -> Optional.of("fallback"));
        System.out.println("present.or(fallback): " + result1);
        System.out.println("empty.or(fallback): " + result2);

        // Łączenie wielu wywołań or() w łańcuchy zapasowe
        System.out.println("--- fallback chain for 'config.timeout' ---");
        String timeout = findInCache("config.timeout")
                .or(() -> findInDatabase("config.timeout"))
                .or(() -> findInDefaults("config.timeout"))
                .orElse("unknown");
        System.out.println("resolved timeout: " + timeout);

        System.out.println("--- fallback chain for 'config.missing' ---");
        String missing = findInCache("config.missing")
                .or(() -> findInDatabase("config.missing"))
                .or(() -> findInDefaults("config.missing"))
                .orElse("unknown");
        System.out.println("resolved missing: " + missing);

        // stream() — Java 9 — konwersja Optional na Stream o zero lub jednym elemencie
        Stream<String> presentStream = present.stream();
        Stream<String> emptyStream = empty.stream();
        System.out.println("present.stream().toList(): " + presentStream.toList());
        System.out.println("empty.stream().toList(): " + emptyStream.toList());

        // Użycie stream() z flatMap do filtrowania pustych Optional z kolekcji
        List<Optional<String>> optionals = List.of(
                Optional.of("alpha"),
                Optional.empty(),
                Optional.of("beta"),
                Optional.empty(),
                Optional.of("gamma")
        );

        List<String> presentValues = optionals.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("filter empty Optionals: " + presentValues);
    }

    // ============================================================
    // Sekcja 7: Optional ze strumieniami
    // ============================================================

    static void optionalWithStreams() {
        System.out.println("\n=== Optional with Streams ===");

        List<Integer> numbers = List.of(5, 3, 8, 1, 9, 2, 7);

        // Operacje terminalne zwracające Optional
        Optional<Integer> first = numbers.stream().filter(n -> n > 6).findFirst();
        Optional<Integer> any = numbers.stream().filter(n -> n > 6).findAny();
        Optional<Integer> min = numbers.stream().min(Comparator.naturalOrder());
        Optional<Integer> max = numbers.stream().max(Comparator.naturalOrder());
        Optional<Integer> sum = numbers.stream().reduce(Integer::sum);

        System.out.println("findFirst (> 6): " + first);
        System.out.println("findAny (> 6): " + any);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("reduce (sum): " + sum);

        // Pusty strumień — operacje terminalne zwracają pusty Optional
        Optional<Integer> emptyFirst = Stream.<Integer>empty().findFirst();
        Optional<Integer> emptyMin = Stream.<Integer>empty().min(Comparator.naturalOrder());
        System.out.println("findFirst on empty stream: " + emptyFirst);
        System.out.println("min on empty stream: " + emptyMin);

        // reduce z tożsamością NIE zwraca Optional (zawsze ma wynik)
        int sumWithIdentity = numbers.stream().reduce(0, Integer::sum);
        System.out.println("reduce with identity: " + sumWithIdentity);

        // Optional.stream() do integracji w potokach strumieniowych
        List<String> userNames = List.of("alice", "bob", "unknown", "charlie", "nobody");
        List<String> emails = userNames.stream()
                .map(Optionals::findEmailByName) // Stream<Optional<String>>
                .flatMap(Optional::stream)          // Stream<String> — puste usunięte
                .toList();
        System.out.println("emails found: " + emails);

        // Prymitywne warianty Optional ze strumieni prymitywnych
        OptionalInt maxInt = IntStream.of(10, 20, 30).max();
        OptionalDouble average = IntStream.rangeClosed(1, 10).average();
        OptionalLong minLong = LongStream.of(100L, 200L, 50L).min();
        System.out.println("IntStream.max(): " + maxInt);
        System.out.println("IntStream.average(): " + average);
        System.out.println("LongStream.min(): " + minLong);

        // Wyodrębnianie wartości prymitywnych Optional
        int maxValue = maxInt.orElse(-1);
        double avgValue = average.orElse(0.0);
        System.out.println("maxInt.orElse(-1): " + maxValue);
        System.out.println("average.orElse(0.0): " + avgValue);
    }

    // ============================================================
    // Sekcja 8: Typowe wzorce i antywzorce
    // ============================================================

    static void commonPatternsAndAntiPatterns() {
        System.out.println("\n=== Common Patterns and Anti-Patterns ===");

        // ANTYWZORZEC 1: isPresent() + get() — rozwlekłe i podatne na błędy
        Optional<User> user = findUserByName("alice");
        // Źle:
        if (user.isPresent()) {
            System.out.println("[anti-pattern] isPresent+get: " + user.get().name());
        }
        // Dobrze:
        user.ifPresent(u -> System.out.println("[pattern] ifPresent: " + u.name()));
        String name = user.map(User::name).orElse("unknown");
        System.out.println("[pattern] map+orElse: " + name);

        // ANTYWZORZEC 2: Optional jako parametr metody
        // Źle: void sendEmail(Optional<String> address) { ... }
        // Dobrze: przeciąż metody lub użyj @Nullable
        // (nie demonstrowane w kodzie, ponieważ to wytyczna projektowa)
        System.out.println("[anti-pattern] Optional as parameter — use overloading instead");

        // ANTYWZORZEC 3: Optional jako typ pola
        // Źle: private Optional<String> email;
        // Dobrze: private String email; // nullable
        //       public Optional<String> getEmail() { return Optional.ofNullable(email); }
        // Nasz rekord User demonstruje poprawny wzorzec:
        User bob = new User("Bob", null, null);
        System.out.println("[pattern] nullable field + Optional getter: " + bob.optionalEmail());

        // ANTYWZORZEC 4: Optional.of(pustaKolekcja)
        // Źle:
        Optional<List<String>> wrappedList = Optional.of(List.of());
        System.out.println("[anti-pattern] Optional<List>: " + wrappedList);
        // Dobrze: po prostu zwróć pustą kolekcję
        List<String> emptyList = List.of();
        System.out.println("[pattern] empty collection: " + emptyList);

        // WZORZEC: zastępowanie zagnieżdżonych sprawdzeń null łańcuchami map/flatMap
        // Przed (imperatywnie):
        //   if (user != null) {
        //     Address addr = user.getAddress();
        //     if (addr != null) {
        //       String city = addr.city();
        //       if (city != null) { ... }
        //     }
        //   }
        // Po (funkcyjnie):
        String aliceCity = findUserByName("alice")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("[pattern] nested null check replacement: " + aliceCity);

        String charlieCity = findUserByName("charlie")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("[pattern] charlie's city (no address): " + charlieCity);

        // WZORZEC: Optional w typach zwracanych dla metod repozytorium/wyszukiwania
        // Nasz findUserByName to demonstruje — zwraca Optional<User>
        findUserByName("alice").ifPresentOrElse(
                u -> System.out.println("[pattern] repository find: " + u.name()),
                () -> System.out.println("[pattern] repository find: not found")
        );
        findUserByName("unknown").ifPresentOrElse(
                u -> System.out.println("[pattern] repository find: " + u.name()),
                () -> System.out.println("[pattern] repository find: not found")
        );

        // WZORZEC: konwersja legacy nullable API na Optional
        Map<String, String> legacyConfig = new HashMap<>();
        legacyConfig.put("host", "localhost");
        // Map.get zwraca null jeśli klucz nie znaleziony — opakuj za pomocą ofNullable
        Optional<String> host = Optional.ofNullable(legacyConfig.get("host"));
        Optional<String> port = Optional.ofNullable(legacyConfig.get("port"));
        System.out.println("[pattern] legacy API wrapping — host: " + host);
        System.out.println("[pattern] legacy API wrapping — port: " + port);

        // Ostrzeżenie dotyczące serializacji
        System.out.println("[warning] Optional does NOT implement Serializable");
        System.out.println("  → do not use Optional as field type in DTOs or entities");
        System.out.println("  → use it only as method return type");
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        introductionToOptional();
        creatingOptionals();
        checkingAndExtractingValues();
        transformingOptionals();
        conditionalActions();
        combiningOptionals();
        optionalWithStreams();
        commonPatternsAndAntiPatterns();
    }
}
