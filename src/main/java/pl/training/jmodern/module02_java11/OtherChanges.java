package pl.training.jmodern.module02_java11;

import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wnioskowanie typów zmiennych lokalnych (var)
// ============================================================

/*
## Wnioskowanie typów zmiennych lokalnych (var)

- `var` (Java 10) pozwala kompilatorowi wnioskować typ zmiennej lokalnej
  z wyrażenia inicjalizującego po prawej stronie.
  Jest to **lukier składniowy** — zmienna jest nadal statycznie typowana
  w czasie kompilacji, dokładnie tak, jakbyś napisał typ jawnie.
- `var` **nie jest słowem kluczowym** — to „zarezerwowana nazwa typu". Można
  nadal używać `var` jako nazwy zmiennej, metody lub pakietu
  (ale nie jako nazwy klasy ani interfejsu). Zrobiono tak dla zachowania
  kompatybilności wstecznej z istniejącym kodem używającym `var` jako identyfikatora.
- **Gdzie MOŻNA używać `var`**:
    - Zmienne lokalne z inicjalizatorem: `var list = new ArrayList<String>()`
    - Zmienne w pętli for-each: `for (var item : collection)`
    - Indeks w tradycyjnej pętli for: `for (var i = 0; i < 10; i++)`
    - Zmienne w try-with-resources: `try (var stream = Files.lines(path))`
- **Gdzie NIE MOŻNA używać `var`**:
    - Pola (instancji ani statyczne)
    - Parametry metod
    - Typy zwracane metod
    - Parametry konstruktorów
    - Parametry catch
    - Zmienne bez inicjalizatora: `var x;` — błąd kompilacji
    - Inicjalizatory tablic: `var arr = {1, 2, 3};` — błąd kompilacji
    - Inicjalizatory `null`: `var x = null;` — błąd kompilacji
- **Kiedy `var` poprawia czytelność**:
    - Złożone typy generyczne: `var map = new HashMap<String, List<Optional<String>>>()`
      zamiast powtarzania pełnego typu po obu stronach.
    - Klasy anonimowe, gdzie typ jest oczywisty z kontekstu.
    - Wzorce iteratorów/pętli, gdzie typ jest jasny z kolekcji.
- **Kiedy `var` pogarsza czytelność**:
    - Literały numeryczne: `var count = 1` — czy to `int`, `long`, `byte`?
    - Wywołania metod z nieoczywistym typem zwracanym: `var result = process()`
      — czytający musi sprawdzić sygnaturę metody.
    - Operator diamond z `var` traci informację o typie:
      `var list = new ArrayList<>()` wnioskuje `ArrayList<Object>`, a nie
      `ArrayList<String>`. Zawsze podawaj argument typu z `var`.
- **`final var`** — łączy wnioskowanie typów z niemutowalnością:
  `final var name = "Alice"` jest równoważne z `final String name = "Alice"`.
*/

// ============================================================
// Sekcja 2: var w parametrach lambda (Java 11)
// ============================================================

/*
## var w parametrach lambda (Java 11)

- Java 11 (JEP 323) pozwala na `var` w formalnych parametrach lambda:
  `(var x, var y) -> x + y`.
- **Dlaczego to dodano?** Nie dla zwięzłości (parametry niejawne są
  już krótsze), ale aby umożliwić **adnotacje** na parametrach lambda.
  Przed Java 11 można było adnotować parametry lambda tylko przy użyciu
  jawnych typów: `(@NotNull String x) -> ...`. Teraz można
  napisać `(@NotNull var x) -> ...` i pozwolić kompilatorowi wnioskować typ.
- **Zasady**:
    - Trzeba użyć `var` dla **wszystkich** parametrów lub **żadnego** — mieszanie
      `var` z jawnymi typami jest niedozwolone:
      `(var x, String y) -> ...` — błąd kompilacji.
    - Nie można mieszać `var` z niejawnymi (bez typu) parametrami:
      `(var x, y) -> ...` — błąd kompilacji.
    - Nie można używać `var` dla parametrów bez nawiasów:
      `var x -> ...` — błąd kompilacji (nawiasy wymagane).
- **Trzy style parametrów lambda** (porównanie):
    - Niejawne:  `(x, y) -> x + y` — najkrótsze, adnotacje niemożliwe
    - Jawne:     `(String x, String y) -> x + y` — rozwlekłe, adnotacje OK
    - var:       `(var x, var y) -> x + y` — jak niejawne, ale pozwala na adnotacje
- **Praktyczne zastosowanie**: nakładanie adnotacji takich jak `@Nullable`, `@NonNull`,
  `@SuppressWarnings` lub niestandardowych adnotacji na poszczególne parametry
  lambda bez podawania pełnego typu.
*/

// ============================================================
// Sekcja 3: Nowe metody String (Java 11)
// ============================================================

/*
## Nowe metody String (Java 11)

- **`isBlank()`** — zwraca `true`, jeśli ciąg jest pusty lub zawiera
  tylko białe znaki. W przeciwieństwie do `isEmpty()`, które sprawdza
  tylko `length() == 0`, `isBlank()` jest **świadome Unicode** i rozpoznaje
  wszystkie białe znaki Unicode (np. spacja niełamliwa `\u00A0`,
  spacja ideograficzna `\u3000` itp.).
- **`strip()`** — usuwa wiodące i końcowe białe znaki. Podobne
  do `trim()`, ale **świadome Unicode**:
    - `trim()` usuwa znaki o punktach kodowych ≤ U+0020 (znaki
      kontrolne ASCII i spacja).
    - `strip()` używa `Character.isWhitespace()`, które obejmuje
      białe znaki Unicode, takie jak `\u2003` (spacja firetowa), `\u3000`
      (spacja ideograficzna) itp.
    - W większości praktycznych przypadków zachowują się tak samo, ale `strip()` jest
      właściwym wyborem dla tekstu międzynarodowego.
- **`stripLeading()`** — usuwa białe znaki tylko z początku.
- **`stripTrailing()`** — usuwa białe znaki tylko z końca.
- **`lines()`** — zwraca `Stream<String>` linii, rozdzielonych
  terminatorami linii: `\n` (LF), `\r` (CR) lub `\r\n` (CRLF).
  Stream jest **leniwy** — wydajny do przetwarzania dużych wieloliniowych
  ciągów bez ładowania wszystkich linii do pamięci naraz.
  Puste końcowe linie nie są uwzględniane, jeśli ciąg kończy się
  terminatorem linii.
- **`repeat(int count)`** — zwraca ciąg będący powtórzeniem tego ciągu
  `count` razy. `"ab".repeat(3)` → `"ababab"`.
  Zwraca pusty ciąg, gdy `count` wynosi 0. Rzuca
  `IllegalArgumentException`, jeśli `count` jest ujemne.
*/

// ============================================================
// Sekcja 4: Files.readString() i Files.writeString() (Java 11)
// ============================================================

/*
## Files.readString() i Files.writeString() (Java 11)

- Przed Java 11 wczytanie całego pliku do `String` wymagało
  wieloetapowego kodu szablonowego:
    - `new String(Files.readAllBytes(path), StandardCharsets.UTF_8)`
    - lub opakowanie `BufferedReader` w try-with-resources.
- Java 11 dodała metody pomocnicze w `java.nio.file.Files`:
    - **`Files.readString(Path)`** — wczytuje cały plik jako
      `String` w kodowaniu UTF-8 (domyślnie).
    - **`Files.readString(Path, Charset)`** — wczytuje z podanym
      zestawem znaków.
    - **`Files.writeString(Path, CharSequence, OpenOption...)`** —
      zapisuje ciąg do pliku. Domyślne opcje to `CREATE` i
      `TRUNCATE_EXISTING` (tworzy plik, jeśli nie istnieje,
      nadpisuje, jeśli istnieje).
    - **`Files.writeString(Path, CharSequence, Charset, OpenOption...)`**
      — zapisuje z podanym zestawem znaków.
- **Typowe wartości `OpenOption`** (z `StandardOpenOption`):
    - `CREATE` — utwórz plik, jeśli nie istnieje (domyślnie).
    - `TRUNCATE_EXISTING` — obetnij plik do zerowej długości (domyślnie).
    - `APPEND` — dopisz na końcu pliku.
    - `CREATE_NEW` — utwórz nowy plik, niepowodzenie jeśli już istnieje.
    - `WRITE` — otwórz do zapisu (implikowane przez `writeString`).
- **Uwaga**: te metody wczytują/zapisują **cały** plik do
  pamięci. Dla dużych plików używaj API strumieniowych jak `Files.lines()`,
  `BufferedReader` lub `BufferedWriter`.
*/

// ============================================================
// Sekcja 5: Inne istotne dodatki Java 11
// ============================================================

/*
## Inne istotne dodatki Java 11

- **`Optional.isEmpty()`** (Java 11) — odpowiednik
  `isPresent()`. Zwraca `true`, jeśli wartość nie jest obecna.
  Przed Java 11 trzeba było pisać `!optional.isPresent()`.
- **`Collection.toArray(IntFunction)`** (Java 11) — bezpieczna typowo
  konwersja na tablicę: `list.toArray(String[]::new)` zastępuje
  starszy wzorzec `list.toArray(new String[0])`. `IntFunction`
  otrzymuje rozmiar kolekcji i zwraca tablicę tego rozmiaru.
- **`Predicate.not()`** (Java 11) — metoda statyczna do negowania
  predykatów: `lines.filter(Predicate.not(String::isBlank))`
  zamiast `lines.filter(s -> !s.isBlank())`. Działa z referencjami
  do metod, w przeciwieństwie do wzorca negacji lambda.
- **`Pattern.asMatchPredicate()`** (Java 11) — zwraca
  `Predicate<String>`, który testuje, czy **całe** wejście pasuje
  do wzorca (jak `matches()`). Porównaj z `asPredicate()` z Java 8,
  które używa `find()` (częściowe dopasowanie).
- **`Character.toString(int)`** (Java 11) — konwertuje punkt kodowy
  Unicode na `String`. Zastępuje dwuetapowy wzorzec
  `new String(Character.toChars(codePoint))`.
- **Jednoplikowe programy źródłowe** (Java 11, JEP 330) — można
  uruchomić `java MyProgram.java` bezpośrednio bez osobnego kroku
  kompilacji `javac`. JVM kompiluje i uruchamia jednym poleceniem.
  Przydatne do skryptów, prototypowania i nauki.
*/

public class OtherChanges {

    // ---- Adnotacja pomocnicza dla Sekcji 2 (var w lambdach) ----

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotNull {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Positive {}

    // ============================================================
    // Sekcja 1: Wnioskowanie typów zmiennych lokalnych (var)
    // ============================================================

    static void localVariableTypeInference() {
        System.out.println("=== Local Variable Type Inference (var) ===");

        // Podstawowe wnioskowanie typów — kompilator wnioskuje String z inicjalizatora
        var greeting = "Hello, Java 10!";
        System.out.println("var greeting (String): " + greeting);
        System.out.println("  actual type: " + greeting.getClass().getSimpleName());

        // Wnioskowanie z typami numerycznymi — var wnioskuje int (nie long, nie short)
        var count = 42;
        var price = 19.99;   // wnioskuje double
        var initial = 'A';   // wnioskuje char
        var flag = true;     // wnioskuje boolean
        System.out.println("var count (int): " + count);
        System.out.println("var price (double): " + price);
        System.out.println("var initial (char): " + initial);
        System.out.println("var flag (boolean): " + flag);

        // Wnioskowanie z kolekcjami — pełny typ generyczny jest wnioskowany
        var names = new ArrayList<String>();
        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");
        System.out.println("var names (ArrayList<String>): " + names);

        // Złożone typy generyczne — var sprawdza się tutaj
        // Bez var: Map<String, List<Optional<String>>> map = new HashMap<String, List<Optional<String>>>();
        var complexMap = new HashMap<String, List<Optional<String>>>();
        complexMap.put("greetings", List.of(Optional.of("hello"), Optional.empty()));
        System.out.println("complex generic map: " + complexMap);

        // var z Map.of — wnioskuje Map<String, Integer>
        var scores = Map.of("Alice", 95, "Bob", 87, "Charlie", 92);
        System.out.println("var scores (Map<String, Integer>): " + scores);

        // var w pętli for-each — wnioskuje typ elementu z kolekcji
        System.out.print("for-each with var: ");
        for (var name : names) {
            System.out.print(name + " ");
        }
        System.out.println();

        // var w tradycyjnej pętli for
        System.out.print("for-loop with var: ");
        for (var i = 0; i < names.size(); i++) {
            System.out.print(names.get(i) + " ");
        }
        System.out.println();

        // var w try-with-resources
        // (Demonstracja ze StringReader, ponieważ implementuje AutoCloseable)
        var content = "line1\nline2\nline3";
        try (var reader = new java.io.StringReader(content);
             var buffered = new java.io.BufferedReader(reader)) {
            var firstLine = buffered.readLine();
            System.out.println("try-with-resources var: first line = " + firstLine);
        } catch (IOException e) {
            System.out.println("  error: " + e.getMessage());
        }

        // final var — łączenie niemutowalności z wnioskowaniem typów
        final var PI = 3.14159;
        final var APP_NAME = "JModern";
        System.out.println("final var PI: " + PI);
        System.out.println("final var APP_NAME: " + APP_NAME);
        // PI = 3.0;  // błąd kompilacji — zmienna final

        // var jest nadal statycznie typowane — NIE MOŻNA zmienić typu po deklaracji
        var text = "hello";
        text = "world";     // OK — ten sam typ (String)
        // text = 42;       // błąd kompilacji — niekompatybilne typy: int nie może być przekonwertowany na String
        System.out.println("var is static: reassigned to \"" + text + "\" (still String)");

        // ---- PUŁAPKI: gdzie var traci informację o typie ----

        // PUŁAPKA 1: Operator diamond z var — wnioskuje Object, nie oczekiwany typ
        var rawList = new ArrayList<>();     // ArrayList<Object>, NIE ArrayList<String>!
        rawList.add("string");
        rawList.add(42);                     // kompiluje się — bo to ArrayList<Object>
        System.out.println("PITFALL — var + diamond: " + rawList + " (ArrayList<Object>)");
        // Poprawka: zawsze podawaj argument typu: var list = new ArrayList<String>()

        // PUŁAPKA 2: Typy zwracane metod mogą nie być oczywiste
        var result = processData();          // Jaki to typ? Czytający musi sprawdzić metodę.
        System.out.println("PITFALL — non-obvious return: " + result);

        // DOBRA PRAKTYKA: używaj var, gdy typ jest jasny z kontekstu
        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var now = java.time.LocalDate.now();
        System.out.println("clear context: " + now.format(formatter));
    }

    // Metoda pomocnicza do demonstracji nieoczywistych typów zwracanych
    private static Map<String, List<Integer>> processData() {
        return Map.of("values", List.of(1, 2, 3));
    }

    // ============================================================
    // Sekcja 2: var w parametrach lambda (Java 11)
    // ============================================================

    static void varInLambdaParameters() {
        System.out.println("\n=== var in Lambda Parameters (Java 11) ===");

        // Trzy style parametrów lambda — porównanie
        // Styl 1: Typy niejawne (bez typów, najkrótszy)
        BinaryOperator<String> implicitConcat = (x, y) -> x + " " + y;
        System.out.println("implicit: " + implicitConcat.apply("Hello", "World"));

        // Styl 2: Typy jawne (pełne nazwy typów)
        BinaryOperator<String> explicitConcat = (String x, String y) -> x + " " + y;
        System.out.println("explicit: " + explicitConcat.apply("Hello", "World"));

        // Styl 3: Typy var (Java 11 — pozwala na adnotacje)
        BinaryOperator<String> varConcat = (var x, var y) -> x + " " + y;
        System.out.println("var:      " + varConcat.apply("Hello", "World"));

        // Główny powód var w lambdach: ADNOTACJE na parametrach
        // Z var możesz adnotować parametry lambda bez pisania pełnego typu
        Function<String, String> annotatedLambda = (@NotNull var s) -> s.toUpperCase();
        System.out.println("annotated lambda: " + annotatedLambda.apply("hello"));

        // Wiele adnotowanych parametrów
        BinaryOperator<Integer> annotatedAdd = (@Positive var a, @Positive var b) -> a + b;
        System.out.println("annotated add: " + annotatedAdd.apply(3, 5));

        // var w lambda z operacjami Stream
        var words = List.of("hello", "world", "java", "eleven");
        var upperWords = words.stream()
                .map((var word) -> word.toUpperCase())
                .collect(Collectors.toList());
        System.out.println("stream with var lambda: " + upperWords);

        // var z BiFunction
        BiFunction<String, Integer, String> repeater = (var text, var times) -> text.repeat(times);
        System.out.println("BiFunction with var: " + repeater.apply("Ha", 3));

        // Praktyka: filtrowanie z adnotowanym var
        var numbers = List.of(1, -2, 3, -4, 5, 0);
        var positives = numbers.stream()
                .filter((@Positive var n) -> n > 0)
                .collect(Collectors.toList());
        System.out.println("filtered positives: " + positives);

        // ZASADY — czego NIE MOŻNA robić:
        System.out.println("\nvar lambda rules:");
        System.out.println("  OK:    (var x, var y) -> x + y");
        System.out.println("  OK:    (@NotNull var x) -> x.length()");
        System.out.println("  ERROR: (var x, String y) -> ...  // cannot mix var with explicit types");
        System.out.println("  ERROR: (var x, y) -> ...         // cannot mix var with implicit");
        System.out.println("  ERROR: var x -> ...              // parentheses required with var");
    }

    // ============================================================
    // Sekcja 3: Nowe metody String (Java 11)
    // ============================================================

    static void newStringMethods() {
        System.out.println("\n=== New String Methods (Java 11) ===");

        // ---- isBlank() vs isEmpty() ----
        System.out.println("--- isBlank() vs isEmpty() ---");

        var empty = "";
        var spaces = "   ";
        var tabs = "\t\t";
        var newlines = "\n\n";
        var text = "hello";
        var unicodeSpaces = "\u2003\u2003";  // spacje firetowe (białe znaki Unicode)

        System.out.println("\"\"        — isEmpty: " + empty.isEmpty() + ", isBlank: " + empty.isBlank());
        System.out.println("\"   \"     — isEmpty: " + spaces.isEmpty() + ", isBlank: " + spaces.isBlank());
        System.out.println("\"\\t\\t\"    — isEmpty: " + tabs.isEmpty() + ", isBlank: " + tabs.isBlank());
        System.out.println("\"\\n\\n\"    — isEmpty: " + newlines.isEmpty() + ", isBlank: " + newlines.isBlank());
        System.out.println("\"hello\"   — isEmpty: " + text.isEmpty() + ", isBlank: " + text.isBlank());
        System.out.println("em spaces — isEmpty: " + unicodeSpaces.isEmpty() + ", isBlank: " + unicodeSpaces.isBlank());

        // ---- strip() vs trim() ----
        System.out.println("\n--- strip() vs trim() ---");

        // Dla białych znaków ASCII zachowują się tak samo
        var padded = "  hello  ";
        System.out.println("ASCII — trim():  [" + padded.trim() + "]");
        System.out.println("ASCII — strip(): [" + padded.strip() + "]");

        // Dla białych znaków Unicode, strip() je usuwa, ale trim() nie
        var unicodePadded = "\u2003hello\u2003";  // spacja firetowa (U+2003)
        System.out.println("Unicode — trim():  [" + unicodePadded.trim() + "]");
        System.out.println("Unicode — strip(): [" + unicodePadded.strip() + "]");
        System.out.println("  (\\u2003 is em space — strip() removes it, trim() does not)");

        // Kolejny przykład Unicode: spacja ideograficzna (U+3000, używana w tekście CJK)
        var cjkPadded = "\u3000hello\u3000";
        System.out.println("CJK space — trim():  [" + cjkPadded.trim() + "]");
        System.out.println("CJK space — strip(): [" + cjkPadded.strip() + "]");

        // ---- stripLeading() i stripTrailing() ----
        System.out.println("\n--- stripLeading() / stripTrailing() ---");

        var mixed = "   hello   ";
        System.out.println("original:      [" + mixed + "]");
        System.out.println("stripLeading:  [" + mixed.stripLeading() + "]");
        System.out.println("stripTrailing: [" + mixed.stripTrailing() + "]");
        System.out.println("strip:         [" + mixed.strip() + "]");

        // ---- lines() ----
        System.out.println("\n--- lines() ---");

        var multiLine = "first line\nsecond line\nthird line\n\nfifth line";
        System.out.println("lines from multi-line string:");
        multiLine.lines().forEach(line -> System.out.println("  [" + line + "]"));

        // lines() jest leniwe — działa wydajnie z dużymi ciągami
        var lineCount = multiLine.lines().count();
        System.out.println("line count: " + lineCount);

        // Praktyka: filtrowanie niepustych linii
        var withBlanks = "hello\n   \nworld\n\n  \njava";
        var nonBlankLines = withBlanks.lines()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
        System.out.println("non-blank lines: " + nonBlankLines);

        // Praktyka: użycie Predicate.not() z lines
        var trimmedLines = withBlanks.lines()
                .filter(Predicate.not(String::isBlank))
                .map(String::strip)
                .collect(Collectors.toList());
        System.out.println("trimmed non-blank: " + trimmedLines);

        // lines() obsługuje różne terminatory linii: \n, \r, \r\n
        var mixedEndings = "unix\nwindows\r\nold-mac\rend";
        System.out.println("mixed line endings: " + mixedEndings.lines().collect(Collectors.toList()));

        // ---- repeat() ----
        System.out.println("\n--- repeat() ---");

        System.out.println("\"ab\".repeat(3):  " + "ab".repeat(3));
        System.out.println("\"ha\".repeat(5):  " + "ha".repeat(5));
        System.out.println("\"x\".repeat(0):   [" + "x".repeat(0) + "]  (empty string)");
        System.out.println("\"x\".repeat(1):   " + "x".repeat(1));

        // Praktyka: tworzenie separatorów
        var separator = "-".repeat(40);
        System.out.println(separator);
        System.out.println("  formatted output between separators");
        System.out.println(separator);

        // Praktyka: wcięcia
        var indent = "  ".repeat(3);  // 6 spacji
        System.out.println(indent + "indented text (3 levels)");

        // Praktyka: proste wzorce tekstowe
        for (var i = 1; i <= 5; i++) {
            System.out.println("*".repeat(i));
        }
    }

    // ============================================================
    // Sekcja 4: Files.readString() i Files.writeString() (Java 11)
    // ============================================================

    static void filesReadWriteString() {
        System.out.println("\n=== Files.readString() and Files.writeString() ===");

        try {
            // Tworzenie pliku tymczasowego dla bezpiecznej demonstracji
            var tempFile = Files.createTempFile("jmodern-demo-", ".txt");
            System.out.println("temp file: " + tempFile);

            // ---- Zapis ciągu do pliku ----
            var content = "Hello from Java 11!\nThis is line 2.\nAnd line 3.";
            Files.writeString(tempFile, content);
            System.out.println("wrote " + content.length() + " chars to file");

            // ---- Odczyt pliku z powrotem ----
            var readBack = Files.readString(tempFile);
            System.out.println("read back:\n" + readBack);

            // Weryfikacja cyklu zapis-odczyt
            System.out.println("round-trip OK: " + content.equals(readBack));

            // ---- Dopisywanie do pliku ----
            var appendContent = "\nAppended line 4.";
            Files.writeString(tempFile, appendContent, StandardOpenOption.APPEND);
            System.out.println("\nafter APPEND:");
            System.out.println(Files.readString(tempFile));

            // ---- Zapis z CREATE_NEW (niepowodzenie jeśli plik istnieje) ----
            var newFile = tempFile.getParent().resolve("jmodern-new-" + System.nanoTime() + ".txt");
            Files.writeString(newFile, "Brand new file!", StandardOpenOption.CREATE_NEW);
            System.out.println("\nCREATE_NEW file: " + Files.readString(newFile));

            // ---- Porównanie: stary sposób (przed Java 11) ----
            // Stary sposób odczytu:
            //   byte[] bytes = Files.readAllBytes(path);
            //   String content = new String(bytes, StandardCharsets.UTF_8);
            //
            // Stary sposób zapisu:
            //   Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            //
            // Nowy sposób (Java 11):
            //   String content = Files.readString(path);
            //   Files.writeString(path, content);
            System.out.println("\n(old way: Files.readAllBytes + new String(bytes, charset))");
            System.out.println("(new way: Files.readString(path) — one line!)");

            // ---- Użycie z lines() do przetwarzania ----
            var fileContent = Files.readString(tempFile);
            var lineList = fileContent.lines()
                    .filter(Predicate.not(String::isBlank))
                    .map(String::strip)
                    .collect(Collectors.toList());
            System.out.println("\nprocessed lines from file: " + lineList);

            // Czyszczenie plików tymczasowych
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(newFile);
            System.out.println("temp files cleaned up");

        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 5: Inne istotne dodatki Java 11
    // ============================================================

    static void otherJava11Additions() {
        System.out.println("\n=== Other Notable Java 11 Additions ===");

        // ---- Optional.isEmpty() ----
        System.out.println("--- Optional.isEmpty() ---");

        Optional<String> present = Optional.of("hello");
        Optional<String> absent = Optional.empty();

        System.out.println("present.isEmpty(): " + present.isEmpty());
        System.out.println("absent.isEmpty():  " + absent.isEmpty());
        System.out.println("(before Java 11: !absent.isPresent() = " + !absent.isPresent() + ")");

        // Praktyka: czytelniejsze wzorce sprawdzania null
        var maybeValue = Optional.ofNullable(System.getProperty("non.existent.property"));
        if (maybeValue.isEmpty()) {
            System.out.println("property not found (checked with isEmpty)");
        }

        // ---- Collection.toArray(IntFunction) ----
        System.out.println("\n--- Collection.toArray(IntFunction) ---");

        var names = List.of("Alice", "Bob", "Charlie");

        // Stary sposób (Java 8):
        String[] oldWay = names.toArray(new String[0]);
        System.out.println("old way: " + Arrays.toString(oldWay));

        // Nowy sposób (Java 11) — z użyciem referencji do metody:
        String[] newWay = names.toArray(String[]::new);
        System.out.println("new way: " + Arrays.toString(newWay));

        // Działa z każdym typem kolekcji
        var numbers = Set.of(1, 2, 3, 4, 5);
        Integer[] numArray = numbers.toArray(Integer[]::new);
        System.out.println("set to array: " + Arrays.toString(numArray));

        // ---- Predicate.not() ----
        System.out.println("\n--- Predicate.not() ---");

        var lines = List.of("hello", "", "  ", "world", "\t", "java");

        // Bez Predicate.not() — potrzebna lambda
        var withoutNot = lines.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        System.out.println("lambda negation:     " + withoutNot);

        // Z Predicate.not() — działa z referencjami do metod
        var withNot = lines.stream()
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
        System.out.println("Predicate.not():     " + withNot);

        // Predicate.not() z innymi referencjami do metod
        var mixedNumbers = List.of(1, -2, 3, -4, 5, 0);
        Predicate<Integer> isNegative = n -> n < 0;
        var nonNegative = mixedNumbers.stream()
                .filter(Predicate.not(isNegative))
                .collect(Collectors.toList());
        System.out.println("non-negative numbers: " + nonNegative);

        // Komponowanie z Predicate.not()
        var words = List.of("hello", "Hi", "WORLD", "java", "OK");
        var result = words.stream()
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(s -> s.equals(s.toUpperCase())))
                .collect(Collectors.toList());
        System.out.println("not empty, not all-uppercase: " + result);

        // ---- Pattern.asMatchPredicate() ----
        System.out.println("\n--- Pattern.asMatchPredicate() ---");

        var emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

        // asPredicate() (Java 8) — używa find() (częściowe dopasowanie)
        Predicate<String> partialMatch = emailPattern.asPredicate();
        // asMatchPredicate() (Java 11) — używa matches() (pełne dopasowanie)
        Predicate<String> fullMatch = emailPattern.asMatchPredicate();

        var testInputs = List.of("alice@example.com", "not-an-email", "prefix alice@example.com suffix");
        System.out.println("email pattern — asPredicate (find) vs asMatchPredicate (matches):");
        for (var input : testInputs) {
            System.out.printf("  %-35s  asPredicate: %-5s  asMatchPredicate: %s%n",
                    "\"" + input + "\"", partialMatch.test(input), fullMatch.test(input));
        }

        // Praktyka: filtrowanie prawidłowych adresów email z listy
        var candidates = List.of("alice@example.com", "bob@", "charlie@corp.io", "not-email");
        var validEmails = candidates.stream()
                .filter(emailPattern.asMatchPredicate())
                .collect(Collectors.toList());
        System.out.println("valid emails: " + validEmails);

        // ---- Character.toString(int) ----
        System.out.println("\n--- Character.toString(int) ---");

        // Nowość w Java 11 — konwertuje punkt kodowy bezpośrednio na String
        System.out.println("Character.toString(65):    " + Character.toString(65));     // "A"
        System.out.println("Character.toString(9731):  " + Character.toString(9731));   // bałwan ☃
        System.out.println("Character.toString(128512): " + Character.toString(128512)); // emoji

        // Stary sposób (przed Java 11):
        System.out.println("old way: new String(Character.toChars(9731)): "
                + new String(Character.toChars(9731)));

        // ---- Jednoplikowe programy źródłowe (JEP 330) ----
        System.out.println("\n--- Single-file source-code programs ---");
        System.out.println("Java 11 allows: java MyProgram.java  (no javac step)");
        System.out.println("  - Compiles and runs in a single command");
        System.out.println("  - Great for scripts, prototyping, and learning");
        System.out.println("  - Supports shebang (#!) on Unix: #!/usr/bin/java --source 11");
        System.out.println("  - All classes must be in the single source file");
        System.out.println("  - The first class in the file must have main()");
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        localVariableTypeInference();
        varInLambdaParameters();
        newStringMethods();
        filesReadWriteString();
        otherJava11Additions();
    }
}
