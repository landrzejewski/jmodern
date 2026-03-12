package pl.training.jmodern.module02_java25;

import module java.base;           // Demo sekcji 5 — zastępuje szczegółowe importy
import java.lang.classfile.*;       // Sekcja 8 — Class-File API
import java.lang.classfile.attribute.*;

// ============================================================
// Sekcja 1: String Templates (wycofane)
// ============================================================

/*
## String Templates (wycofane)

- **JEP 430** (preview Java 21) i **JEP 459** (preview Java 22)
  wprowadzały String Templates z procesorami szablonów.
- **Składnia**: STR."Hello \{name}" — wyrażenia osadzone wewnątrz
  literałów tekstowych, przetwarzane przez procesor szablonów.
- **Procesory szablonów**: STR (interpolacja), FMT (formatowanie),
  RAW (surowy dostęp do szablonu). Można było definiować własne
  procesory dla SQL, JSON itp.
- **Dlaczego wycofano**: Projekt miał kilka problemów:
    - Niejawny import STR budził kontrowersje
    - Model procesora był zbyt złożony dla typowych przypadków
    - Kompromisy dotyczące bezpieczeństwa typów z typem zwracanym procesora
    - Niejasna interakcja z surowymi literałami tekstowymi
- **Status**: Usunięte po preview w Java 22. Mogą wrócić
  w zupełnie innej formie w przyszłych wersjach JDK.
- **Na razie**: Używaj String.formatted(), MessageFormat lub
  StringBuilder do interpolacji łańcuchów znaków.
*/

// ============================================================
// Sekcja 2: Niejawnie deklarowane klasy i instancyjne metody main
// ============================================================

/*
## Niejawnie deklarowane klasy i instancyjne metody main

- **JEP 463** (preview Java 21) → **JEP 477** (sfinalizowane Java 23)
- **Wcześniej**: Każdy program Java wymagał:
    public class Main {
        public static void main(String[] args) { ... }
    }
- **Teraz**: Plik może zawierać po prostu:
    void main() { ... }
  Bez deklaracji klasy, bez public, bez static, bez String[] args.
- **Instancyjne metody main**: void main() działa jako metoda
  instancji. JVM tworzy instancję niejawnej klasy i wywołuje
  na niej main().
- **Kompaktowe pliki źródłowe**: Programy jednoplikowe można
  uruchamiać bezpośrednio: java MyApp.java — bez osobnej kompilacji.
- **Klasa java.io.IO** (planowana): API konsolowe z println(),
  print(), readln() — zaprojektowane dla początkujących. Jeszcze
  niedostępne w obecnych wersjach JDK.
- **Priorytet protokołu uruchamiania**:
    1. static void main(String[] args)
    2. static void main()
    3. void main(String[] args) (instancyjna)
    4. void main() (instancyjna)
- **Zastosowanie**: Nauczanie Javy początkujących, skryptowanie,
  małe narzędzia. Usuwa ceremonialny boilerplate z prostych programów.
- **Nie można zademonstrować wewnątrz zwykłej klasy** — funkcja
  wymaga pliku bez jawnej deklaracji klasy.
*/

// ============================================================
// Sekcja 3: Typy prymitywne w dopasowywaniu wzorców
// ============================================================

/*
## Typy prymitywne w dopasowywaniu wzorców

- **JEP 455** (preview Java 23, kontynuowane w Java 25)
  rozszerza dopasowywanie wzorców o obsługę typów prymitywnych.
- **Wcześniej**: switch na typach prymitywnych obsługiwał tylko
  przypadki ze stałymi dokładnego typu (case 1, case 2, itp.).
  Wzorce typów takie jak case int i nie były dozwolone.
- **Teraz**: Wzorce typów z prymitywami działają zarówno w switch
  jak i w wyrażeniach instanceof:
    - case int i — dopasowuje i wiąże wartość int
    - case double d — dopasowuje i wiąże wartość double
    - obj instanceof int i — wyodrębnia int z Object
- **Konwersje we wzorcach**:
    - Poszerzanie: int dopasowany jako long (zawsze bezpieczne)
    - Zawężanie: long dopasowany jako int (tylko jeśli wartość się mieści)
    - Unboxing: Integer dopasowany jako int
    - Boxing: int dopasowany jako Integer
- **Strażnicy**: słowo kluczowe when działa ze wzorcami prymitywnymi:
    case int i when i > 0 -> "positive"
- **Praktyczna korzyść**: Umożliwia jednolity switch-on-Object
  obsługujący zarówno typy referencyjne jak i prymitywne, bez
  ręcznego unboxingu czy sprawdzania typów.
*/

// ============================================================
// Sekcja 4: Elastyczne ciała konstruktorów
// ============================================================

/*
## Elastyczne ciała konstruktorów

- **JEP 482** (preview Java 24, kontynuowane w Java 25) pozwala
  na instrukcje przed wywołaniem super() lub this() w konstruktorach.
- **Wcześniej**: super(...) lub this(...) MUSIAŁO być pierwszą
  instrukcją w konstruktorze. Brak walidacji, obliczeń czy
  logowania przed wywołaniem super.
- **Teraz**: Kod przed super() jest dozwolony, umożliwiając:
    - Walidację argumentów (wczesne odrzucenie nieprawidłowych)
    - Obliczanie wartości (wyprowadzanie wartości do przekazania do super)
    - Logowanie / diagnostykę (śledzenie konstrukcji)
- **Ograniczenie**: Nie można uzyskać dostępu do `this` (pól
  instancji ani metod) przed zakończeniem super(). Dostępne są
  tylko składowe statyczne, parametry i zmienne lokalne.
- **Uzasadnienie**: Eliminuje kłopotliwe obejścia, takie jak
  statyczne metody pomocnicze tylko po to, by walidować lub
  obliczać przed super().
*/

// ============================================================
// Sekcja 5: Deklaracje importu modułów
// ============================================================

/*
## Deklaracje importu modułów

- **JEP 476** (preview Java 23, kontynuowane w Java 25) dodaje
  nową formę importu: import module <nazwa-modułu>;
- **import module java.base;** importuje WSZYSTKIE publiczne typy
  najwyższego poziomu eksportowane przez moduł java.base. Obejmuje to:
    - java.util.* (List, Map, Set, itp.)
    - java.util.stream.* (Stream, Collectors, Gatherers)
    - java.util.function.* (Function, Predicate, itp.)
    - java.io.* (InputStream, OutputStream, itp.)
    - java.nio.file.* (Path, Files, itp.)
    - java.time.* (Instant, Duration, LocalDate, itp.)
    - java.util.regex.* (Pattern, Matcher)
    - java.util.concurrent.* (CompletableFuture, itp.)
    - ... i wiele więcej pakietów z java.base
- **Ten plik tego używa**: import module java.base; na górze
  zastępuje to, co byłoby dziesiątkami indywidualnych importów.
- **Działa obok konkretnych importów**: np. import java.lang.classfile.*
  może współistnieć z import module java.base;
- **Rozwiązywanie niejednoznaczności**: Jeśli dwa moduły eksportują
  tę samą prostą nazwę typu, trzeba dodać jawny import, aby rozwiązać.
*/

// ============================================================
// Sekcja 6: Stream Gatherers (JEP 485, Java 24)
// ============================================================

/*
## Stream Gatherers

- Stream API (Java 8) dostarczało stały zestaw operacji pośrednich
  (map, filter, flatMap, itp.) bez możliwości definiowania własnych.
  Podczas gdy Collector rozwiązywał problem własnych operacji terminalnych,
  nie było odpowiednika dla operacji pośrednich.
- **JEP 485 (Java 24)** finalizuje Stream Gatherers (podgląd w
  Java 22 przez JEP 461 i Java 23 przez JEP 473).
- **`stream.gather(Gatherer)`** to nowa operacja pośrednia, która
  transformuje elementy strumienia za pomocą strategii zdefiniowanej
  przez użytkownika.
- **Komponenty Gatherer** (podobne do struktury Collector):
    - initializer: dostarcza początkowy stan prywatny (opcjonalny)
    - integrator: przetwarza każdy element, może przekazywać dalej
    - combiner: łączy stan dla strumieni równoległych (opcjonalny)
    - finisher: uruchamiany po przetworzeniu wszystkich elementów (opcjonalny)
- **Wbudowane Gatherers** w java.util.stream.Gatherers:
    - windowFixed(int size) -- nienakładające się okna o stałym rozmiarze
    - windowSliding(int size) -- nakładające się okna przesuwne
    - fold(Supplier, BiFunction) -- redukcja do pojedynczego elementu
    - scan(Supplier, BiFunction) -- bieżąca akumulacja
    - mapConcurrent(int, Function) -- mapowanie z ograniczoną współbieżnością
- **Kompozycja** przez gatherer.andThen(anotherGatherer) do łączenia
  własnych operacji pośrednich.
- **Własne Gatherers**: Użyj Gatherer.ofSequential(initializer,
  integrator) lub Gatherer.of(...) dla wersji obsługujących
  równoległość. Dzięki temu Stream API jest rozszerzalne po raz pierwszy.
*/

// ============================================================
// Sekcja 7: Class-File API (JEP 484, Java 24)
// ============================================================

/*
## Class-File API (programowe parsowanie plików klas)

- **JEP 457** (preview Java 22), **JEP 466** (preview Java 23),
  **JEP 484** (sfinalizowane Java 24).
- **java.lang.classfile.ClassFile** — standardowe API do odczytywania,
  transformowania i generowania plików .class programowo.
- **Zamiennik dla ASM**: Przed tym API manipulacja kodem bajtowym
  wymagała bibliotek zewnętrznych (ASM, Javassist, ByteBuddy).
  Teraz samo JDK dostarcza standardowe, utrzymywane API.
- **Kluczowe typy**:
    - ClassFile — punkt wejścia do parsowania i generowania
    - ClassModel — reprezentuje sparsowany plik .class
    - MethodModel, FieldModel — metody i pola
    - Attributes — dostęp do atrybutów pliku klasy
- **Przypadki użycia**:
    - Generowanie kodu bajtowego we frameworkach (proxy, AOP)
    - Analiza narzędzi budowania (skanowanie zależności)
    - Wsparcie IDE (inspekcja struktury klas)
    - Narzędzia edukacyjne (eksploracja kodu bajtowego)
- **Przewaga nad ASM**: Wersjonowane razem z JDK, zawsze obsługuje
  najnowszy format pliku klasy. Brak opóźnień wersji ani problemów
  z kompatybilnością z nowymi wersjami JDK.
*/

// ============================================================
// Sekcja 8: Wyprzedzające ładowanie i linkowanie klas (Project Leyden)
// ============================================================

/*
## Wyprzedzające ładowanie i linkowanie klas (Project Leyden)

- **JEP 483 (Java 24)**: AOT Cache — wstępnie przygotowane decyzje
  dotyczące ładowania i linkowania klas przechowywane w archiwum współdzielonym.
- **Trzystopniowy przepływ pracy**:
    1. java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.example.Main
       → Rejestruje decyzje ładowania/linkowania klas podczas przebiegu treningowego
    2. java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp app.jar
       → Tworzy cache AOT z zarejestrowanych danych
    3. java -XX:AOTMode=on -XX:AOTCache=app.aot -cp app.jar com.example.Main
       → Uruchamia z wstępnie zbudowanym cache dla szybszego startu
- **Poprawa startu**: Klasy są ładowane z archiwum współdzielonego
  zamiast skanowania classpath i weryfikacji przy każdym starcie.
  Może to znacząco skrócić czas uruchamiania.
- **Szersza wizja Project Leyden**: Przeniesienie pracy z czasu
  wykonania na wcześniejsze fazy (czas budowania, pierwszy start,
  kompilacja AOT). AOT Cache to jeden krok; przyszłe JEP mogą
  dodać bardziej agresywne optymalizacje wyprzedzające.
- **Powiązane**: Bazuje na technologii CDS (Class Data Sharing),
  która jest w JVM od lat, ale czyni ją znacznie bardziej
  praktyczną i zautomatyzowaną.
- **To nie jest API** — wyłącznie flagi JVM i narzędzia. Brak kodu
  do zademonstrowania; to kwestia wdrożenia/operacji.
*/

// ============================================================
// Sekcja 9: Pisanie prostych skryptów
// ============================================================

/*
## Pisanie prostych skryptów

- **Kompaktowe pliki źródłowe**: java MyScript.java — uruchomienie
  pliku źródłowego Java bezpośrednio, bez osobnego kroku kompilacji.
  JVM kompiluje go w locie.
- **Instancyjne metody main** (podsumowanie sekcji 2): void main() { }
  to wszystko, czego potrzebujesz w kompaktowym pliku źródłowym.
  Bez klasy, bez static, bez parametru args.
- **Obsługa shebang na Uniksie**:
    #!/usr/bin/env java --source 25 --enable-preview
    void main() {
        System.out.println("Hello from a script!");
    }
  Uczyń plik wykonywalnym (chmod +x) i uruchom go bezpośrednio.
- **Wieloplikowe programy źródłowe**: java --source 25 Main.java
  może odwoływać się do innych plików .java w tym samym katalogu.
  Launcher rozwiązuje je i kompiluje razem.
- **Porównanie z JShell**:
    - JShell: interaktywny REPL, świetny do eksperymentowania
    - Source launcher: uruchamia kompletne programy jako skrypty
    - Oba unikają jawnej kompilacji, ale służą różnym celom
- **Porównanie ze skryptowaniem Groovy**: Source launcher Javy
  obejmuje teraz wiele przypadków użycia, które wcześniej wymagały
  Groovy lub innych języków skryptowych JVM. Różnica znacząco się zmniejszyła.
*/

public class OtherChanges {

    // ---- Typy wewnętrzne dla demo elastycznych ciał konstruktorów ----

    static class Base {
        final String value;

        Base(String value) {
            this.value = value;
            System.out.println("    Base constructor called with: " + value);
        }
    }

    static class ValidatedSub extends Base {
        ValidatedSub(String value) {
            // Kod przed super() — JEP 482
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Value must not be null or blank");
            }
            var processed = value.trim().toUpperCase();
            System.out.println("    Pre-super validation passed, processed: " + processed);
            super(processed);
        }
    }

    static class ComputedSub extends Base {
        ComputedSub(int x, int y) {
            // Obliczanie wartości pochodnej przed super()
            var computed = "sum=" + (x + y) + ",product=" + (x * y);
            System.out.println("    Pre-super computation: " + computed);
            super(computed);
        }
    }

    static class LoggedSub extends Base {
        LoggedSub(String value) {
            // Logowanie / diagnostyka przed super()
            System.out.println("    [LOG] Constructing LoggedSub with: \"" + value + "\" on thread " + Thread.currentThread().getName());
            super(value);
        }
    }

    // Ponownie wykorzystane z java21 OtherChanges dla Gatherers
    record Person(String name, int age) {}

    // ============================================================
    // Sekcja 1: String Templates (wycofane)
    // ============================================================

    static void stringTemplatesWithdrawn() {
        System.out.println("=== Section 1: String Templates (Withdrawn) ===");

        System.out.println("  String Templates were previewed in Java 21 (JEP 430) and Java 22 (JEP 459).");
        System.out.println("  Syntax was: STR.\"Hello \\{name}\" with template processor model.");
        System.out.println("  WITHDRAWN after Java 22 — removed from the language.");
        System.out.println("  Reasons: complex processor model, implicit STR import concerns,");
        System.out.println("           type safety trade-offs, interaction with raw strings unclear.");
        System.out.println("  May return in a different form in future JDKs.");
        System.out.println("  For now, use String.formatted() or MessageFormat:");

        var name = "World";
        var age = 25;
        System.out.println("    \"Hello %s, age %d\".formatted(name, age) -> " + "Hello %s, age %d".formatted(name, age));
        System.out.println("    MessageFormat.format(\"Hello {0}\", name) -> " + java.text.MessageFormat.format("Hello {0}", name));
    }

    // ============================================================
    // Sekcja 2: Niejawnie deklarowane klasy i instancyjne metody main
    // ============================================================

    static void implicitlyDeclaredClasses() {
        System.out.println("\n=== Section 2: Implicitly Declared Classes and Instance Main Methods ===");

        System.out.println("  JEP 463 (preview Java 21) -> JEP 477 (finalized Java 23).");
        System.out.println("  Cannot demo inside a regular class — requires a file with no class declaration.");
        System.out.println();
        System.out.println("  Before:");
        System.out.println("    public class Main {");
        System.out.println("        public static void main(String[] args) {");
        System.out.println("            System.out.println(\"Hello\");");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println();
        System.out.println("  After (entire file):");
        System.out.println("    void main() {");
        System.out.println("        System.out.println(\"Hello\");");
        System.out.println("    }");
        System.out.println();
        System.out.println("  Launch protocol priority:");
        System.out.println("    1. static void main(String[] args)");
        System.out.println("    2. static void main()");
        System.out.println("    3. void main(String[] args) — instance method");
        System.out.println("    4. void main() — instance method");
        System.out.println();
        System.out.println("  java.io.IO class (println, print, readln) — planned but not yet available.");
        System.out.println("  Use case: teaching, scripting, small utilities.");
    }

    // ============================================================
    // Sekcja 3: Typy prymitywne w dopasowywaniu wzorców
    // ============================================================

    static void primitiveTypesInPatternMatching() {
        System.out.println("\n=== Section 3: Primitive Types in Pattern Matching (JEP 455) ===");

        // ---- Demo 1: Switch na Object ze wzorcami prymitywnymi ----
        System.out.println("--- Demo 1: Switch on Object with primitive patterns ---");
        Object[] values = {42, 3.14, 100L, "hello", (byte) 7, true};

        for (Object obj : values) {
            String description = switch (obj) {
                case int i -> "int: " + i + " (squared: " + (i * i) + ")";
                case double d -> "double: " + String.format("%.2f", d);
                case long l -> "long: " + l;
                case String s -> "String: \"" + s + "\"";
                default -> "other: " + obj + " (" + obj.getClass().getSimpleName() + ")";
            };
            System.out.println("  " + obj + " -> " + description);
        }

        // ---- Demo 2: Konwersje prymitywne i strażnicy ----
        System.out.println("\n--- Demo 2: Primitive conversions and guards ---");
        Object[] numbers = {42, -10, 0, 255L, 3.14, 100};

        for (Object obj : numbers) {
            String result = switch (obj) {
                case int i when i > 0 && i <= 100 -> "int in range (0,100]: " + i;
                case int i when i > 100 -> "int above 100: " + i;
                case int i when i < 0 -> "negative int: " + i;
                case int i -> "zero";
                case long l -> "long value: " + l;
                case double d when d > 0 -> "positive double: " + d;
                case double d -> "non-positive double: " + d;
                default -> "other: " + obj;
            };
            System.out.println("  " + obj + " -> " + result);
        }

        // ---- Demo 3: instanceof z prymitywami ----
        System.out.println("\n--- Demo 3: instanceof with primitives ---");
        Object[] mixed = {Integer.valueOf(42), Double.valueOf(2.718), "text", Long.valueOf(999L)};

        for (Object obj : mixed) {
            if (obj instanceof int i) {
                System.out.println("  " + obj + " -> extracted as int: " + i + ", doubled: " + (i * 2));
            } else if (obj instanceof double d) {
                System.out.println("  " + obj + " -> extracted as double: " + String.format("%.3f", d));
            } else {
                System.out.println("  " + obj + " -> not a primitive number (" + obj.getClass().getSimpleName() + ")");
            }
        }
    }

    // ============================================================
    // Sekcja 4: Elastyczne ciała konstruktorów
    // ============================================================

    static void flexibleConstructorBodies() {
        System.out.println("\n=== Section 4: Flexible Constructor Bodies (JEP 482) ===");

        // ---- Demo 1: Walidacja przed super() ----
        System.out.println("--- Demo 1: Validation before super() ---");
        System.out.println("  Creating ValidatedSub(\"  hello  \"):");
        var valid = new ValidatedSub("  hello  ");
        System.out.println("  Result: value = \"" + valid.value + "\"");

        System.out.println("\n  Creating ValidatedSub(\"\") — should throw:");
        try {
            new ValidatedSub("");
        } catch (IllegalArgumentException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Validation ran BEFORE super() — object was never partially constructed");
        }

        // ---- Demo 2: Obliczenia przed super() ----
        System.out.println("\n--- Demo 2: Computation before super() ---");
        System.out.println("  Creating ComputedSub(3, 7):");
        var computed = new ComputedSub(3, 7);
        System.out.println("  Result: value = \"" + computed.value + "\"");

        System.out.println("\n  Creating ComputedSub(10, 20):");
        var computed2 = new ComputedSub(10, 20);
        System.out.println("  Result: value = \"" + computed2.value + "\"");

        // ---- Demo 3: Logowanie przed super() ----
        System.out.println("\n--- Demo 3: Logging / side-effects before super() ---");
        System.out.println("  Creating LoggedSub(\"test-value\"):");
        var logged = new LoggedSub("test-value");
        System.out.println("  Result: value = \"" + logged.value + "\"");
        System.out.println("  Note: log output appeared BEFORE Base constructor ran");
    }

    // ============================================================
    // Sekcja 5: Deklaracje importu modułów
    // ============================================================

    static void moduleImportDeclarations() {
        System.out.println("\n=== Section 5: Module Import Declarations (JEP 476) ===");

        // ---- Demo 1: Użycie typów bez jawnych importów ----
        System.out.println("--- Demo 1: Types available via import module java.base ---");
        System.out.println("  This file uses: import module java.base;");
        System.out.println("  All of these types are available without individual imports:");

        // Kolekcje
        List<String> list = List.of("a", "b", "c");
        Map<String, Integer> map = Map.of("x", 1, "y", 2);
        Set<Integer> set = Set.of(1, 2, 3);
        System.out.println("    List:   " + list);
        System.out.println("    Map:    " + map);
        System.out.println("    Set:    " + set);

        // Czas
        Instant now = Instant.now();
        Duration duration = Duration.ofMinutes(5);
        System.out.println("    Instant.now():      " + now);
        System.out.println("    Duration.ofMinutes: " + duration);

        // NIO
        Path path = Path.of("src", "main", "java");
        System.out.println("    Path.of():          " + path);

        // Wyrażenia regularne
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher("abc123def456");
        List<String> matches = new ArrayList<>();
        while (matcher.find()) matches.add(matcher.group());
        System.out.println("    Pattern/Matcher:    found " + matches + " in \"abc123def456\"");

        // Współbieżność
        CompletableFuture<String> future = CompletableFuture.completedFuture("done");
        System.out.println("    CompletableFuture:  " + future.join());

        // Funkcyjne
        Function<String, Integer> strlen = String::length;
        Predicate<Integer> isPositive = n -> n > 0;
        System.out.println("    Function<String,Integer>: \"hello\" -> " + strlen.apply("hello"));
        System.out.println("    Predicate<Integer>: 42 -> " + isPositive.test(42));

        // ---- Demo 2: Wyświetlenie dostępnych modułów ----
        System.out.println("\n--- Demo 2: Available modules in boot layer ---");
        var modules = ModuleLayer.boot().modules().stream()
                .map(Module::getName)
                .sorted()
                .toList();
        System.out.println("  Boot layer has " + modules.size() + " modules.");
        System.out.println("  First 10: " + modules.stream().limit(10).toList());
        System.out.println("  import module <name>; can import from any of these.");
    }

    // ============================================================
    // Sekcja 6: Stream Gatherers (JEP 485, Java 24)
    // ============================================================

    static void streamGatherers() {
        System.out.println("\n=== Section 6: Stream Gatherers (JEP 485, Java 24) ===");

        // ---- windowFixed: przetwarzanie wsadowe ----
        System.out.println("--- windowFixed: batch processing ---");
        var numbers = List.of(1, 2, 3, 4, 5, 6, 7);
        var fixedWindows = numbers.stream()
                .gather(Gatherers.windowFixed(3))
                .toList();
        System.out.println("  Input:  " + numbers);
        System.out.println("  windowFixed(3): " + fixedWindows);

        // ---- windowSliding: nakładające się okna ----
        System.out.println("\n--- windowSliding: overlapping windows ---");
        var slidingWindows = numbers.stream()
                .gather(Gatherers.windowSliding(3))
                .toList();
        System.out.println("  Input:  " + numbers);
        System.out.println("  windowSliding(3): " + slidingWindows);

        // ---- Średnia krocząca z oknami przesuwnymi ----
        System.out.println("\n--- Moving average with windowSliding ---");
        var prices = List.of(100.0, 102.0, 98.0, 104.0, 101.0, 107.0, 103.0);
        var movingAvg = prices.stream()
                .gather(Gatherers.windowSliding(3))
                .map(window -> window.stream().mapToDouble(d -> d).average().orElse(0))
                .toList();
        System.out.println("  Prices:     " + prices);
        System.out.printf("  Moving avg: %s%n", movingAvg.stream()
                .map(d -> String.format("%.1f", d))
                .toList());

        // ---- scan: sumy bieżące ----
        System.out.println("\n--- scan: running totals ---");
        var runningTotals = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        System.out.println("  Input:  [1, 2, 3, 4, 5]");
        System.out.println("  scan:   " + runningTotals);

        // ---- fold: pojedynczy wynik ----
        System.out.println("\n--- fold: reduce to single result ---");
        var folded = Stream.of("a", "b", "c")
                .gather(Gatherers.fold(() -> "", (acc, el) -> acc.isEmpty() ? el : acc + "-" + el))
                .toList();
        System.out.println("  Input:  [a, b, c]");
        System.out.println("  fold:   " + folded);

        // ---- Własny Gatherer: distinctBy ----
        System.out.println("\n--- Custom Gatherer: distinctBy ---");
        var people = List.of(
                new Person("Alice", 30),
                new Person("Bob", 25),
                new Person("Anna", 30),
                new Person("Charlie", 35),
                new Person("Beth", 25)
        );
        var distinctByAge = people.stream()
                .gather(distinctBy(Person::age))
                .toList();
        System.out.println("  Input:       " + people.stream().map(p -> p.name() + "(" + p.age() + ")").toList());
        System.out.println("  distinctBy(age): " + distinctByAge.stream().map(p -> p.name() + "(" + p.age() + ")").toList());

        // ---- mapConcurrent: mapowanie z ograniczoną współbieżnością ----
        System.out.println("\n--- mapConcurrent: bounded-concurrency mapping ---");
        var urls = List.of("page-1", "page-2", "page-3", "page-4", "page-5");
        var startTime = System.currentTimeMillis();
        var results = urls.stream()
                .gather(Gatherers.mapConcurrent(3, url -> {
                    try {
                        Thread.sleep(100); // Symulacja I/O
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return url + " [fetched on " + Thread.currentThread().getName() + "]";
                }))
                .toList();
        var elapsed = System.currentTimeMillis() - startTime;
        System.out.println("  Input: " + urls);
        System.out.println("  mapConcurrent(3) results:");
        for (var r : results) {
            System.out.println("    " + r);
        }
        System.out.println("  Elapsed: ~" + elapsed + "ms (3 concurrent virtual threads, 5 items with 100ms each)");
        System.out.println("  Without concurrency would be ~500ms, with 3 concurrent ~200ms");
    }

    static <T, K> Gatherer<T, ?, T> distinctBy(Function<T, K> keyExtractor) {
        return Gatherer.ofSequential(
                HashSet<K>::new,
                (state, element, downstream) -> {
                    if (state.add(keyExtractor.apply(element))) {
                        return downstream.push(element);
                    }
                    return true;
                }
        );
    }

    // ============================================================
    // Sekcja 7: Class-File API (JEP 484, Java 24)
    // ============================================================

    static void classFileApi() throws Exception {
        System.out.println("\n=== Section 7: Class-File API (JEP 484, Java 24) ===");

        // ---- Demo 1: Parsowanie String.class ----
        System.out.println("--- Demo 1: Parse String.class ---");
        var cf = ClassFile.of();
        byte[] stringClassBytes;
        try (var stream = String.class.getResourceAsStream("String.class")) {
            Objects.requireNonNull(stream, "String.class resource not found");
            stringClassBytes = stream.readAllBytes();
        }
        var stringModel = cf.parse(stringClassBytes);

        System.out.println("  Class name:    " + stringModel.thisClass().asInternalName());
        System.out.println("  Superclass:    " + stringModel.superclass().map(ci -> ci.asInternalName()).orElse("none"));
        System.out.println("  Interfaces:    " + stringModel.interfaces().size());
        var methods = stringModel.methods();
        var fields = stringModel.fields();
        System.out.println("  Methods:       " + methods.size());
        System.out.println("  Fields:        " + fields.size());

        // ---- Demo 2: Lista metod sparsowanej klasy ----
        System.out.println("\n--- Demo 2: List methods of String.class (first 15) ---");
        methods.stream()
                .limit(15)
                .forEach(m -> System.out.println("    " + m.methodName().stringValue()
                        + m.methodType().stringValue()));

        // ---- Demo 3: Inspekcja własnej klasy ----
        System.out.println("\n--- Demo 3: Inspect OtherChanges.class itself ---");
        byte[] ownBytes;
        try (var stream = OtherChanges.class.getResourceAsStream("OtherChanges.class")) {
            Objects.requireNonNull(stream, "OtherChanges.class resource not found");
            ownBytes = stream.readAllBytes();
        }
        var ownModel = cf.parse(ownBytes);

        System.out.println("  Class name:  " + ownModel.thisClass().asInternalName());
        System.out.println("  Superclass:  " + ownModel.superclass().map(ci -> ci.asInternalName()).orElse("none"));
        System.out.println("  Methods (" + ownModel.methods().size() + "):");
        for (var m : ownModel.methods()) {
            System.out.println("    " + m.methodName().stringValue() + m.methodType().stringValue());
        }

        // Lista klas wewnętrznych z atrybutu InnerClasses
        System.out.println("  Inner classes:");
        for (var attr : ownModel.attributes()) {
            if (attr instanceof InnerClassesAttribute innerClasses) {
                for (var ic : innerClasses.classes()) {
                    System.out.println("    " + ic.innerClass().asInternalName());
                }
            }
        }
    }

    // ============================================================
    // Sekcja 8: Wyprzedzające ładowanie i linkowanie klas (Project Leyden)
    // ============================================================

    static void aotClassLoadingAndLinking() {
        System.out.println("\n=== Section 8: Ahead-of-Time Class Loading & Linking (Project Leyden) ===");

        System.out.println("  JEP 483 (Java 24): AOT Cache for faster JVM startup.");
        System.out.println("  Three-step workflow:");
        System.out.println("    1. java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.example.Main");
        System.out.println("       -> Records class loading/linking decisions during a training run");
        System.out.println("    2. java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp app.jar");
        System.out.println("       -> Creates the AOT cache from recorded data");
        System.out.println("    3. java -XX:AOTMode=on -XX:AOTCache=app.aot -cp app.jar com.example.Main");
        System.out.println("       -> Runs with pre-built cache for faster startup");
        System.out.println();
        System.out.println("  Benefits: classes loaded from shared archive instead of classpath scanning.");
        System.out.println("  Part of Project Leyden: shift work from runtime to earlier phases.");
        System.out.println("  Builds on CDS (Class Data Sharing) technology.");
        System.out.println("  Not an API — purely JVM flags and tooling.");
    }

    // ============================================================
    // Sekcja 9: Pisanie prostych skryptów
    // ============================================================

    static void writingSimpleScripts() {
        System.out.println("\n=== Section 9: Writing Simple Scripts ===");

        System.out.println("  Compact Source Files: java MyScript.java — no compilation step needed.");
        System.out.println("  Combined with instance main methods (Section 2):");
        System.out.println("    // MyScript.java (cały plik)");
        System.out.println("    void main() {");
        System.out.println("        System.out.println(\"Hello from a script!\");");
        System.out.println("    }");
        System.out.println();
        System.out.println("  Shebang support on Unix:");
        System.out.println("    #!/usr/bin/env java --source 25 --enable-preview");
        System.out.println("    void main() { System.out.println(\"Executable Java!\"); }");
        System.out.println("    Then: chmod +x script.java && ./script.java");
        System.out.println();
        System.out.println("  Multi-file source programs:");
        System.out.println("    java --source 25 Main.java — can reference other .java files");
        System.out.println();
        System.out.println("  Comparison:");
        System.out.println("    JShell:          Interactive REPL, great for experimentation");
        System.out.println("    Source launcher:  Runs complete programs as scripts");
        System.out.println("    Groovy:          Java's source launcher now covers many Groovy scripting use cases");
    }

    // ============================================================
    // Main -- uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) throws Exception {
        stringTemplatesWithdrawn();
        implicitlyDeclaredClasses();
        primitiveTypesInPatternMatching();
        flexibleConstructorBodies();
        moduleImportDeclarations();
        streamGatherers();
        classFileApi();
        aotClassLoadingAndLinking();
        writingSimpleScripts();
    }
}
