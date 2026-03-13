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
// Sekcja 5: Stream Gatherers (JEP 485, Java 24)
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
// Sekcja 6: Pisanie prostych skryptów
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
    // Sekcja 5: Stream Gatherers (JEP 485, Java 24)
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
    // Sekcja 6: Pisanie prostych skryptów
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
    }

    // ============================================================
    // Main -- uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) throws Exception {
        stringTemplatesWithdrawn();
        implicitlyDeclaredClasses();
        primitiveTypesInPatternMatching();
        flexibleConstructorBodies();
        streamGatherers();
        writingSimpleScripts();
    }
}
