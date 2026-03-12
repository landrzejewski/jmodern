package pl.training.jmodern.module02_java8;

import javax.script.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

// ============================================================
// Sekcja 1: Metody domyślne w interfejsach
// ============================================================

/*
## Metody domyślne w interfejsach

- Przed Java 8 dodanie nowej metody do interfejsu **łamało wszystkie
  istniejące implementacje**. Nie było sposobu na ewolucję interfejsów
  bez zmuszania każdego implementora do aktualizacji.
- **Metody domyślne** rozwiązują to, pozwalając interfejsom dostarczać
  ciało metody za pomocą słowa kluczowego `default`. Klasy implementujące
  dziedziczą domyślne zachowanie, ale mogą je **nadpisać**.
- Umożliwiło to kompatybilną wstecznie ewolucję podstawowych API — na
  przykład `Collection.forEach()`, `List.sort()`, `Map.getOrDefault()`
  zostały dodane jako metody domyślne bez łamania istniejącego kodu.
- **Metody statyczne** w interfejsach są również dozwolone od Java 8.
  Dostarczają metody narzędziowe bezpośrednio na typie interfejsu
  (np. `Comparator.comparing()`, `Predicate.not()`).
- **Problem diamentu**: jeśli klasa implementuje dwa interfejsy, które
  oba dostarczają metodę domyślną o tej samej sygnaturze,
  kompilator zmusza klasę do **jawnego nadpisania** metody
  i rozwiązania niejednoznaczności. Reguły rozwiązywania:
    1. **Klasa wygrywa**: metoda zdefiniowana w klasie (lub nadklasie)
       ma priorytet nad każdą metodą domyślną.
    2. **Najbardziej specyficzny interfejs wygrywa**: jeśli jeden interfejs
       rozszerza inny, wygrywa metoda domyślna podinterfejsu.
    3. **Wymagane jawne rozwiązanie**: jeśli żadna reguła nie ma zastosowania,
       klasa musi nadpisać i wybrać za pomocą
       `InterfaceName.super.method()`.
- **Interfejs vs klasa abstrakcyjna po Java 8**:
    - Interfejsy nadal nie mogą mieć pól instancji (stanu).
    - Interfejsy wspierają wielokrotne dziedziczenie zachowania.
    - Klasy abstrakcyjne mogą mieć konstruktory, pola i metody niepubliczne.
    - Używaj interfejsów do definiowania typów/kontraktów; klas abstrakcyjnych
      do współdzielenia stanu i częściowej implementacji między pokrewnymi klasami.
*/

// ============================================================
// Sekcja 2: Nowe API daty i czasu (java.time)
// ============================================================

/*
## Nowe API daty i czasu (java.time)

- Starsze klasy `java.util.Date` i `java.util.Calendar` miały
  poważne problemy projektowe:
    - **Mutowalne** — obiekty `Date` mogą być zmieniane po utworzeniu,
      prowadząc do błędów w kodzie wielowątkowym.
    - **Niebezpieczne wątkowo** — `SimpleDateFormat` nie jest bezpieczny wątkowo.
    - **Słaby projekt API** — miesiące numerowane od 0, rok przesunięty od 1900,
      niespójne nazewnictwo, brak wyraźnego rozdzielenia daty i czasu.
- Java 8 wprowadziła pakiet `java.time` (oparty na Joda-Time)
  z czystym, **niezmiennym**, **bezpiecznym wątkowo** API daty/czasu.
- **Główne klasy**:
    - `LocalDate` — data bez czasu i strefy czasowej (np. 2024-03-15).
    - `LocalTime` — czas bez daty i strefy czasowej (np. 14:30:00).
    - `LocalDateTime` — data + czas bez strefy czasowej.
    - `ZonedDateTime` — data + czas + strefa czasowa.
    - `Instant` — znacznik czasu maszyny (sekundy + nanosekundy od epoki).
- **Tworzenie instancji**: `now()`, `of(...)`, `parse("...")`.
- **Manipulowanie**: `plusDays()`, `minusHours()`, `withMonth()` — wszystkie
  zwracają **nową** instancję (niemutowalność).
- **`Period`** — ilość oparta na dacie (lata, miesiące, dni).
  **`Duration`** — ilość oparta na czasie (godziny, minuty, sekundy, nanosekundy).
- **`DateTimeFormatter`** — bezpieczny wątkowo zamiennik `SimpleDateFormat`.
  Użyj `ofPattern()` dla niestandardowych formatów lub predefiniowanych stałych
  jak `ISO_LOCAL_DATE`.
- **Strefy czasowe**: `ZoneId` reprezentuje strefę czasową (np. "Europe/Warsaw").
  `ZoneOffset` to stałe przesunięcie od UTC (np. "+02:00").
- **Konwersja z legacy**: `Date.toInstant()`, `Instant.atZone(zone)`,
  `Date.from(instant)` łączą stare i nowe API.
- **Dostosowania temporalne**: `TemporalAdjusters` dostarcza typowe
  manipulacje datami jak `firstDayOfMonth()`, `nextOrSame(DayOfWeek.MONDAY)`,
  `lastDayOfYear()`.
*/

// ============================================================
// Sekcja 3: StringJoiner
// ============================================================

/*
## StringJoiner

- `java.util.StringJoiner` (Java 8) konstruuje sekwencję
  znaków oddzielonych separatorem, z opcjonalnym prefiksem i sufiksem.
- **Konstruktor**: `new StringJoiner(delimiter)` lub
  `new StringJoiner(delimiter, prefix, suffix)`.
- **`add(CharSequence)`** — dodaje element.
- **`toString()`** — zwraca połączony łańcuch znaków.
- **`setEmptyValue(CharSequence)`** — definiuje łańcuch zwracany,
  gdy żadne elementy nie zostały dodane (domyślnie `prefix + suffix`).
- **`merge(StringJoiner)`** — scala zawartość innego joinera
  (bez jego prefiksu/sufiksu) do tego. Przydatne dla operacji
  równoległych.
- **Powiązane narzędzia**:
    - `String.join(delimiter, elements)` — statyczna metoda wygody,
      która wewnętrznie używa `StringJoiner`. Najlepsza dla prostych przypadków.
    - `Collectors.joining(delimiter, prefix, suffix)` — wariant
      kolektora strumieniowego. Najlepszy przy pracy ze strumieniami.
- **Kiedy używać którego**:
    - `String.join()` — szybki jednoliniowiec dla tablic/iterowalnych.
    - `StringJoiner` — przy budowaniu przyrostowym, potrzebie prefiksu/sufiksu
      lub potrzebie `merge()` do łączenia wyników.
    - `Collectors.joining()` — wewnątrz potoków strumieniowych.
    - `StringBuilder` — gdy potrzebujesz pełnej kontroli (brak wzorca
      separatora, złożona logika warunkowa).
*/

// ============================================================
// Sekcja 4: Silnik JavaScript Nashorn
// ============================================================

/*
## Silnik JavaScript Nashorn

- Java 8 wprowadziła **Nashorn**, wysokowydajny silnik JavaScript,
  który zastąpił starszy silnik Rhino. Kompilował JavaScript do
  bajtkodu Javy dla lepszej wydajności.
- **Deprecjacja**: Nashorn został zdeprecjonowany w **Java 11** (JEP 335)
  i **usunięty w Java 15** (JEP 372). Na nowoczesnych JVM silnik
  skryptowy może nie być dostępny.
- **`ScriptEngineManager`** — fabryka do uzyskiwania silników skryptowych
  po nazwie ("nashorn", "javascript"), typie MIME lub rozszerzeniu pliku.
- **`ScriptEngine.eval(String)`** — ewaluuje wyrażenie JavaScript
  i zwraca wynik jako obiekt Java.
- **`Bindings`** — `Map<String, Object>` używana do przekazywania obiektów Java
  do skryptu jako zmiennych globalnych. Użyj `engine.put(key, value)`
  lub utwórz instancję `Bindings`.
- **`Invocable`** — interfejs do wywoływania funkcji JavaScript z
  Javy. Rzutuj `ScriptEngine` na `Invocable` i użyj
  `invokeFunction(name, args...)`.
- **Elegancka obsługa**: zawsze sprawdzaj, czy silnik nie jest `null` przed
  użyciem, ponieważ nie będzie dostępny na Java 15+. Opakowuj wywołania
  w try-catch dla `ScriptException`.
*/

// ============================================================
// Sekcja 5: Adnotacje typów
// ============================================================

/*
## Adnotacje typów

- Przed Java 8 adnotacje mogły pojawiać się tylko na **deklaracjach**
  (klasach, metodach, polach, parametrach, itp.).
- Java 8 rozszerzyła meta-adnotację `@Target` o dwa nowe
  typy elementów: **`ElementType.TYPE_USE`** i
  **`ElementType.TYPE_PARAMETER`**.
- Z `TYPE_USE` adnotacje mogą pojawiać się wszędzie tam, gdzie używany jest **typ**:
    - Rzutowania typów: `(@NonNull String) obj`
    - `instanceof`: `obj instanceof @NonNull String`
    - Argumenty typów generycznych: `List<@NonNull String>`
    - `extends`/`implements`: `class Foo extends @Audited Bar`
    - Klauzule `throws`: `void m() throws @Critical IOException`
    - Tworzenie obiektów: `new @Interned String("hello")`
    - Typy tablicowe: `@NonNull String @Nullable []`
- Z `TYPE_PARAMETER` adnotacje mogą pojawiać się na parametrach typów:
  `class Box<@NonEmpty T>`.
- **Cel**: adnotacje typów umożliwiają **podłączalne systemy typów**
  i narzędzia **analizy statycznej** (jak Checker Framework) do
  wykrywania błędów w czasie kompilacji — wyjątki wskaźnika null,
  błędy współbieżności, skażone dane, itp.
- Adnotacje typów **same w sobie nie mają efektu w czasie wykonania** — są
  metadanymi konsumowanymi przez procesory adnotacji i analizatory statyczne.
- **Różnica od adnotacji deklaracji**: adnotacje deklaracji
  opisują sam element (np. `@Override` na metodzie);
  adnotacje typów opisują użycie typu (np. `@NonNull` na
  typie zwracanym).
*/

// ============================================================
// Sekcja 6: Powtarzalne adnotacje
// ============================================================

/*
## Powtarzalne adnotacje

- Przed Java 8 stosowanie **tej samej adnotacji** wielokrotnie
  na pojedynczym elemencie nie było dozwolone:
  ```
  @Schedule(day = "Mon")
  @Schedule(day = "Fri")  // błąd kompilacji przed Java 8!
  void backup() {}
  ```
- Obejściem była **adnotacja kontenera** przechowująca tablicę:
  `@Schedules({@Schedule(day="Mon"), @Schedule(day="Fri")})`.
- Java 8 wprowadziła **`@Repeatable`** — meta-adnotację, która
  deklaruje, która adnotacja kontenera opakowuje powtarzane wartości.
- **Definiowanie powtarzalnej adnotacji**:
    1. Utwórz powtarzalną adnotację z `@Repeatable(Container.class)`.
    2. Utwórz adnotację kontenera z metodą `value()` zwracającą
       tablicę powtarzalnej adnotacji.
- **Pobieranie w czasie wykonania**:
    - `getAnnotationsByType(RepeatableAnnotation.class)` — zwraca
      wszystkie instancje (automatycznie rozpakowuje kontener).
    - `getAnnotation(Container.class)` — zwraca kontener,
      jeśli jest obecny.
    - `getDeclaredAnnotationsByType(...)` — to samo, ale ignoruje
      adnotacje odziedziczone.
- Ta funkcja upraszcza API, które naturalnie pozwalają na wielokrotne
  zastosowania: reguły harmonogramowania, role bezpieczeństwa, ograniczenia
  walidacji, nasłuchiwacze zdarzeń, itp.
*/

public class OtherChanges {

    // ---- Pomocnicze interfejsy dla Sekcji 1 (Metody domyślne) ----

    interface Greeter {
        String greet(String name);

        default String greetLoudly(String name) {
            return greet(name).toUpperCase() + "!";
        }

        static String defaultGreeting() {
            return "Hello, stranger!";
        }
    }

    interface Logging {
        default void log(String message) {
            System.out.println("[LOG] " + message);
        }
    }

    interface Auditing {
        default void log(String message) {
            System.out.println("[AUDIT] " + message);
        }
    }

    // Problem diamentu: zarówno Logging jak i Auditing mają domyślną log()
    // Klasa MUSI nadpisać i rozwiązać konflikt
    static class AuditedLogger implements Logging, Auditing {
        @Override
        public void log(String message) {
            // Jawny wybór, do której domyślnej delegować
            Logging.super.log(message);
            Auditing.super.log(message);
        }
    }

    interface Drawable {
        default String draw() {
            return "Drawing shape";
        }
    }

    interface Resizable extends Drawable {
        // Bardziej specyficzny interfejs — jego domyślna wygrywa z Drawable
        @Override
        default String draw() {
            return "Drawing resizable shape";
        }

        default String resize(int factor) {
            return "Resized by " + factor + "x";
        }
    }

    // ---- Pomocnicze adnotacje dla Sekcji 5 (Adnotacje typów) ----

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NonNull {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Validated {}

    @Target(ElementType.TYPE_PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NonEmpty {}

    // ---- Pomocnicze adnotacje dla Sekcji 6 (Powtarzalne adnotacje) ----

    @Repeatable(Schedules.class)
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Schedule {
        String day();
        String task() default "backup";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Schedules {
        Schedule[] value();
    }

    @Repeatable(Roles.class)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Role {
        String value();
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Roles {
        Role[] value();
    }

    // Zaadnotowane metody do demonstracji Sekcji 6
    @Schedule(day = "Monday", task = "full backup")
    @Schedule(day = "Wednesday", task = "incremental backup")
    @Schedule(day = "Friday", task = "full backup")
    static void performBackup() {
        System.out.println("  performing backup...");
    }

    @Role("ADMIN")
    @Role("DBA")
    static void manageDatabase() {
        System.out.println("  managing database...");
    }

    // ============================================================
    // Sekcja 1: Metody domyślne w interfejsach
    // ============================================================

    static void defaultMethodsInInterfaces() {
        System.out.println("=== Default Methods in Interfaces ===");

        // Implementacja interfejsu z metodą domyślną
        Greeter politeGreeter = name -> "Good day, " + name;
        System.out.println(politeGreeter.greet("Alice"));
        // Użycie metody domyślnej — nie trzeba jej implementować
        System.out.println(politeGreeter.greetLoudly("Alice"));

        // Nadpisanie metody domyślnej
        Greeter casualGreeter = new Greeter() {
            @Override
            public String greet(String name) {
                return "Hey, " + name;
            }

            @Override
            public String greetLoudly(String name) {
                return greet(name) + "!!!"; // niestandardowe nadpisanie
            }
        };
        System.out.println(casualGreeter.greet("Bob"));
        System.out.println(casualGreeter.greetLoudly("Bob"));

        // Metody statyczne w interfejsach
        System.out.println(Greeter.defaultGreeting());

        // Rozwiązanie problemu diamentu — AuditedLogger implementuje zarówno Logging jak i Auditing
        AuditedLogger logger = new AuditedLogger();
        logger.log("user login"); // wywołuje zarówno Logging.super.log jak i Auditing.super.log

        // Bardziej specyficzny interfejs wygrywa — Resizable rozszerza Drawable
        Resizable shape = new Resizable() {};
        System.out.println(shape.draw());    // "Drawing resizable shape" — wygrywa podinterfejs
        System.out.println(shape.resize(3));

        // Rzeczywiste metody domyślne: Collection.forEach, Comparator.comparing
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
        names.forEach(n -> System.out.print("  " + n)); // forEach to metoda domyślna na Iterable
        System.out.println();

        // Comparator.comparing — metoda statyczna + domyślna thenComparing
        names.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("sorted by length then alphabetically: " + names);

        // Map.getOrDefault, Map.putIfAbsent — metody domyślne dodane w Java 8
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 95);
        System.out.println("getOrDefault (Alice): " + scores.getOrDefault("Alice", 0));
        System.out.println("getOrDefault (Dave): " + scores.getOrDefault("Dave", 0));
    }

    // ============================================================
    // Sekcja 2: Nowe API daty i czasu (java.time)
    // ============================================================

    static void dateTimeApi() {
        System.out.println("\n=== New Date/Time API (java.time) ===");

        // LocalDate — data bez czasu i strefy czasowej
        LocalDate today = LocalDate.now();
        LocalDate specificDate = LocalDate.of(2024, 3, 15);
        LocalDate parsedDate = LocalDate.parse("2024-12-25");
        System.out.println("today: " + today);
        System.out.println("specific date: " + specificDate);
        System.out.println("parsed date: " + parsedDate);

        // LocalTime — czas bez daty i strefy czasowej
        LocalTime now = LocalTime.now();
        LocalTime specificTime = LocalTime.of(14, 30, 0);
        LocalTime parsedTime = LocalTime.parse("09:15:30");
        System.out.println("current time: " + now);
        System.out.println("specific time: " + specificTime);
        System.out.println("parsed time: " + parsedTime);

        // LocalDateTime — data + czas bez strefy czasowej
        LocalDateTime dateTime = LocalDateTime.of(specificDate, specificTime);
        System.out.println("date + time: " + dateTime);

        // Manipulowanie datami — niezmienne, zwracają nowe instancje
        LocalDate tomorrow = today.plusDays(1);
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate withDifferentDay = today.withDayOfMonth(1);
        System.out.println("tomorrow: " + tomorrow);
        System.out.println("last month: " + lastMonth);
        System.out.println("first of this month: " + withDifferentDay);

        // Manipulowanie czasem
        LocalTime later = specificTime.plusHours(2).plusMinutes(30);
        System.out.println("14:30 + 2h30m: " + later);

        // Period — ilość oparta na dacie (lata, miesiące, dni)
        Period period = Period.between(specificDate, parsedDate);
        System.out.println("period from " + specificDate + " to " + parsedDate + ": " + period);
        System.out.println("  = " + period.getMonths() + " months and " + period.getDays() + " days");

        Period twoWeeks = Period.ofWeeks(2);
        System.out.println("two weeks from today: " + today.plus(twoWeeks));

        // Duration — ilość oparta na czasie (godziny, minuty, sekundy, nanosekundy)
        Duration duration = Duration.ofHours(2).plusMinutes(30);
        System.out.println("duration: " + duration);
        System.out.println("duration in minutes: " + duration.toMinutes());

        Duration between = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.println("work day duration: " + between);

        // Instant — znacznik czasu maszyny (oparty na epoce)
        Instant instant = Instant.now();
        System.out.println("instant (epoch seconds): " + instant.getEpochSecond());
        System.out.println("instant: " + instant);

        // DateTimeFormatter — formatowanie i parsowanie
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formatted = dateTime.format(customFormatter);
        System.out.println("formatted: " + formatted);

        LocalDateTime reparsed = LocalDateTime.parse(formatted, customFormatter);
        System.out.println("reparsed: " + reparsed);

        // Predefiniowane formattery
        System.out.println("ISO_LOCAL_DATE: " + today.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // ZonedDateTime — data + czas + strefa czasowa
        ZonedDateTime warsawTime = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime tokyoTime = warsawTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        System.out.println("Warsaw: " + warsawTime.format(DateTimeFormatter.ofPattern("HH:mm z")));
        System.out.println("Tokyo:  " + tokyoTime.format(DateTimeFormatter.ofPattern("HH:mm z")));

        // ZoneId — listowanie dostępnych stref
        System.out.println("available zones (sample): " + ZoneId.getAvailableZoneIds().stream()
                .filter(z -> z.startsWith("Europe/"))
                .sorted()
                .limit(5)
                .collect(Collectors.joining(", ")));

        // Konwersja ze starszego Date
        java.util.Date legacyDate = new java.util.Date();
        Instant fromLegacy = legacyDate.toInstant();
        LocalDateTime converted = fromLegacy.atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println("legacy Date -> LocalDateTime: " + converted);

        // Konwersja z powrotem do starszego Date
        java.util.Date backToLegacy = java.util.Date.from(instant);
        System.out.println("Instant -> legacy Date: " + backToLegacy);

        // Dostosowania temporalne — typowe manipulacje datami
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate nextMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate firstDayOfNextYear = today.with(TemporalAdjusters.firstDayOfNextYear());
        System.out.println("first day of month: " + firstDayOfMonth);
        System.out.println("last day of month: " + lastDayOfMonth);
        System.out.println("next Monday: " + nextMonday);
        System.out.println("first day of next year: " + firstDayOfNextYear);

        // ChronoUnit — mierzenie odległości między obiektami temporalnymi
        long daysBetween = ChronoUnit.DAYS.between(specificDate, parsedDate);
        System.out.println("days between " + specificDate + " and " + parsedDate + ": " + daysBetween);
    }

    // ============================================================
    // Sekcja 3: StringJoiner
    // ============================================================

    static void stringJoiner() {
        System.out.println("\n=== StringJoiner ===");

        // Podstawowy StringJoiner z separatorem
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("apple");
        joiner.add("banana");
        joiner.add("cherry");
        System.out.println("basic joiner: " + joiner);

        // StringJoiner z separatorem, prefiksem i sufiksem
        StringJoiner jsonArray = new StringJoiner(", ", "[", "]");
        jsonArray.add("\"one\"");
        jsonArray.add("\"two\"");
        jsonArray.add("\"three\"");
        System.out.println("with prefix/suffix: " + jsonArray);

        // Pusty joiner — domyślnie zwraca prefix + suffix
        StringJoiner empty = new StringJoiner(", ", "(", ")");
        System.out.println("empty joiner: " + empty); // "()"

        // setEmptyValue — niestandardowy łańcuch dla pustego joinera
        StringJoiner emptyWithDefault = new StringJoiner(", ", "[", "]");
        emptyWithDefault.setEmptyValue("[]  (no elements)");
        System.out.println("empty with setEmptyValue: " + emptyWithDefault);

        // Po dodaniu elementów setEmptyValue nie ma efektu
        emptyWithDefault.add("item");
        System.out.println("after add: " + emptyWithDefault);

        // merge — łączenie dwóch joinerów
        StringJoiner fruits = new StringJoiner(", ");
        fruits.add("apple");
        fruits.add("banana");

        StringJoiner vegs = new StringJoiner(", ");
        vegs.add("carrot");
        vegs.add("pea");

        fruits.merge(vegs); // scala zawartość, nie prefiks/sufiks drugiego joinera
        System.out.println("after merge: " + fruits);

        // String.join — statyczna metoda wygody (wewnętrznie używa StringJoiner)
        String joined = String.join(" | ", "alpha", "beta", "gamma");
        System.out.println("String.join: " + joined);

        // String.join z kolekcją
        List<String> items = List.of("one", "two", "three");
        String joinedList = String.join(", ", items);
        System.out.println("String.join (list): " + joinedList);

        // Collectors.joining — wariant kolektora strumieniowego
        String streamJoined = items.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(" - ", "<<", ">>"));
        System.out.println("Collectors.joining: " + streamJoined);

        // Praktyczny przykład: budowanie klauzuli SQL IN
        List<String> ids = List.of("101", "102", "103", "104");
        StringJoiner inClause = new StringJoiner(", ", "WHERE id IN (", ")");
        ids.forEach(inClause::add);
        System.out.println("SQL IN clause: " + inClause);

        // Praktyczny przykład: budowanie linii CSV
        StringJoiner csv = new StringJoiner(",");
        csv.add("John");
        csv.add("Doe");
        csv.add("30");
        csv.add("Warsaw");
        System.out.println("CSV line: " + csv);
    }

    // ============================================================
    // Sekcja 4: Silnik JavaScript Nashorn
    // ============================================================

    static void nashornJavaScriptEngine() {
        System.out.println("\n=== Nashorn JavaScript Engine ===");

        // Nashorn został wprowadzony w Java 8, zdeprecjonowany w Java 11, usunięty w Java 15.
        // Na nowoczesnych JVM silnik może nie być dostępny.
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        // Zapasowo: spróbuj "javascript" jeśli "nashorn" nie zostanie znaleziony
        if (engine == null) {
            engine = manager.getEngineByName("javascript");
        }

        if (engine == null) {
            System.out.println("  No JavaScript engine available (expected on Java 15+).");
            System.out.println("  Nashorn was deprecated in Java 11 and removed in Java 15.");
            System.out.println("  Alternatives: GraalJS (GraalVM), or standalone JS runtimes.");

            // Wyświetl dostępne silniki
            System.out.print("  Available engines: ");
            List<ScriptEngineFactory> factories = manager.getEngineFactories();
            if (factories.isEmpty()) {
                System.out.println("none");
            } else {
                factories.forEach(f -> System.out.print(f.getEngineName() + " "));
                System.out.println();
            }
            return;
        }

        System.out.println("  Engine found: " + engine.getFactory().getEngineName()
                + " " + engine.getFactory().getEngineVersion());

        try {
            // Ewaluacja prostych wyrażeń JavaScript
            Object result = engine.eval("1 + 2");
            System.out.println("  eval('1 + 2'): " + result);

            result = engine.eval("'Hello'.length");
            System.out.println("  eval(\"'Hello'.length\"): " + result);

            // Przekazywanie obiektów Java do JavaScript przez Bindings
            engine.put("name", "Java");
            engine.put("version", 8);
            result = engine.eval("'Hello from ' + name + ' ' + version");
            System.out.println("  with bindings: " + result);

            // Ewaluacja wieloliniowego skryptu
            String script = """
                    var items = ['apple', 'banana', 'cherry'];
                    var result = '';
                    for (var i = 0; i < items.length; i++) {
                        result += items[i].toUpperCase();
                        if (i < items.length - 1) result += ', ';
                    }
                    result;
                    """;
            result = engine.eval(script);
            System.out.println("  multi-line script: " + result);

            // Wywoływanie funkcji JavaScript z Javy za pomocą Invocable
            engine.eval("function add(a, b) { return a + b; }");
            engine.eval("function greet(name) { return 'Hello, ' + name + '!'; }");

            if (engine instanceof Invocable invocable) {
                Object sum = invocable.invokeFunction("add", 10, 20);
                System.out.println("  invokeFunction('add', 10, 20): " + sum);

                Object greeting = invocable.invokeFunction("greet", "World");
                System.out.println("  invokeFunction('greet', 'World'): " + greeting);
            }

        } catch (ScriptException e) {
            System.out.println("  Script error: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            System.out.println("  Function not found: " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 5: Adnotacje typów
    // ============================================================

    // Przykład: metoda z adnotacjami typów
    static @NonNull String getGreeting(@NonNull String name) {
        return "Hello, " + name;
    }

    // Przykład: klasa generyczna z adnotacją parametru typu
    static class Box<@NonEmpty T> {
        private final T value;

        Box(T value) {
            this.value = value;
        }

        T getValue() {
            return value;
        }
    }

    static void typeAnnotations() {
        System.out.println("\n=== Type Annotations ===");

        // Adnotacja typu na deklaracji zmiennej
        @NonNull String message = "This is annotated as non-null";
        System.out.println("annotated variable: " + message);

        // Adnotacja typu na typie zwracanym metody (zobacz getGreeting powyżej)
        System.out.println("annotated method: " + getGreeting("Alice"));

        // Adnotacja typu na argumencie typu generycznego
        List<@NonNull String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        System.out.println("annotated generics: " + names);

        // Adnotacja typu na parametrze typu (zobacz klasę Box powyżej)
        Box<@NonNull String> box = new Box<>("contents");
        System.out.println("annotated type parameter: " + box.getValue());

        // Adnotacja typu na rzutowaniu
        Object obj = "hello";
        String casted = (@NonNull String) obj;
        System.out.println("annotated cast: " + casted);

        // Adnotacja typu na tworzeniu tablicy
        @NonNull String @Validated [] array = new @NonNull String[3];
        array[0] = "first";
        array[1] = "second";
        array[2] = "third";
        System.out.println("annotated array: " + Arrays.toString(array));

        // Inspekcja adnotacji typów za pomocą refleksji
        try {
            Method method = OtherChanges.class.getDeclaredMethod("getGreeting", String.class);
            Annotation[] returnAnnotations = method.getAnnotatedReturnType().getAnnotations();
            System.out.println("annotations on return type of getGreeting():");
            for (Annotation a : returnAnnotations) {
                System.out.println("  " + a);
            }

            // Sprawdzanie adnotacji typów parametrów
            var paramAnnotations = method.getAnnotatedParameterTypes();
            for (var param : paramAnnotations) {
                System.out.println("annotations on parameter type: ");
                for (Annotation a : param.getAnnotations()) {
                    System.out.println("  " + a);
                }
            }
        } catch (NoSuchMethodException e) {
            System.out.println("  reflection error: " + e.getMessage());
        }

        // Definiowanie i używanie niestandardowych adnotacji typów do analizy statycznej
        System.out.println("  (Type annotations are metadata — they enable tools like");
        System.out.println("   the Checker Framework to detect null pointer errors,");
        System.out.println("   concurrency bugs, and tainted data at compile time.)");
    }

    // ============================================================
    // Sekcja 6: Powtarzalne adnotacje
    // ============================================================

    static void repeatingAnnotations() {
        System.out.println("\n=== Repeating Annotations ===");

        // Pobieranie powtarzalnych adnotacji @Schedule z performBackup()
        try {
            Method backupMethod = OtherChanges.class.getDeclaredMethod("performBackup");

            // getAnnotationsByType — automatycznie rozpakowuje kontener
            Schedule[] schedules = backupMethod.getAnnotationsByType(Schedule.class);
            System.out.println("@Schedule annotations on performBackup():");
            for (Schedule s : schedules) {
                System.out.println("  day=" + s.day() + ", task=" + s.task());
            }

            // getAnnotation z typem kontenera
            Schedules container = backupMethod.getAnnotation(Schedules.class);
            if (container != null) {
                System.out.println("container annotation present: @Schedules with "
                        + container.value().length + " entries");
            }

            // Pobieranie powtarzalnych adnotacji @Role z manageDatabase()
            Method dbMethod = OtherChanges.class.getDeclaredMethod("manageDatabase");
            Role[] roles = dbMethod.getAnnotationsByType(Role.class);
            System.out.println("@Role annotations on manageDatabase():");
            for (Role r : roles) {
                System.out.println("  role=" + r.value());
            }

            // Sprawdzanie czy kontener jest obecny
            Roles rolesContainer = dbMethod.getAnnotation(Roles.class);
            if (rolesContainer != null) {
                System.out.println("container annotation present: @Roles with "
                        + rolesContainer.value().length + " entries");
            }

        } catch (NoSuchMethodException e) {
            System.out.println("  reflection error: " + e.getMessage());
        }

        // Praktyczne zastosowanie: symulacja harmonogramu odczytującego adnotacje
        System.out.println("simulated scheduler:");
        try {
            Method backupMethod = OtherChanges.class.getDeclaredMethod("performBackup");
            for (Schedule s : backupMethod.getAnnotationsByType(Schedule.class)) {
                System.out.println("  scheduling '" + s.task() + "' for " + s.day());
            }
        } catch (NoSuchMethodException e) {
            System.out.println("  error: " + e.getMessage());
        }
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        defaultMethodsInInterfaces();
        dateTimeApi();
        stringJoiner();
        nashornJavaScriptEngine();
        typeAnnotations();
        repeatingAnnotations();
    }
}
