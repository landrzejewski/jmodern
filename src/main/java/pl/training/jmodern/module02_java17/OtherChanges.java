package pl.training.jmodern.module02_java17;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Bloki tekstowe — Wprowadzenie
// ============================================================

/*
## Bloki tekstowe — Wprowadzenie

- Przed blokami tekstowymi osadzanie wieloliniowych ciągów znaków
  w Javie było uciążliwe: ręczne znaki ucieczki \n, konkatenacja
  ciągów z + i słaba czytelność. Prosty fragment JSON mógł zająć
  5+ linii konkatenacji z wszędzie uciekającymi cudzysłowami.
- Inne języki rozwiązały to dawno temu:
    - Python: ciągi z potrójnym cudzysłowem (""" lub ''')
    - Kotlin: trimMargin() / trimIndent() na surowych ciągach
    - JavaScript: literały szablonowe z backtickami
    - C#: ciągi dosłowne z @"..."
- **Harmonogram JEP**:
    - JEP 355: Podgląd w Javie 13
    - JEP 368: Drugi podgląd w Javie 14
    - JEP 378: Sfinalizowano w Javie 15
    (Omawiane tutaj z Java 17 LTS)
- **Podstawowa składnia**: otwierające """ po którym następuje
  terminator linii, potem zawartość, zamknięte przez """.
  Otwierające """ MUSZĄ być zakończone terminatorem linii
  — nie można umieścić zawartości w tej samej linii co
  otwierający delimiter.
- Bloki tekstowe produkują zwykłe instancje `String` — NIE
  są nowym typem. Możesz wywoływać na nich dowolne metody String,
  używać ich w konkatenacji, przekazywać wszędzie gdzie oczekiwany
  jest String.
- **Trzyetapowe przetwarzanie w czasie kompilacji**:
    1. Terminatory linii są normalizowane do \n (LF)
    2. Zbędne białe znaki są usuwane (ponowne wcięcie)
    3. Sekwencje ucieczki są interpretowane
  Dzieje się to w czasie kompilacji, więc nie ma narzutu w czasie
  wykonania.
*/

// ============================================================
// Sekcja 2: Bloki tekstowe — Wcięcia i białe znaki
// ============================================================

/*
## Bloki tekstowe — Wcięcia i białe znaki

- Kluczowym pojęciem jest **zbędne vs istotne białe znaki**:
    - Zbędne białe znaki = wcięcie dodane wyłącznie na potrzeby
      formatowania kodu (do wyrównania z otaczającym kodem Java).
      Są automatycznie usuwane przez kompilator.
    - Istotne białe znaki = wcięcie będące częścią
      faktycznej zawartości (np. wcięcie w HTML lub JSON). Są
      zachowywane w wynikowym ciągu.
- **Algorytm ponownego wcięcia**: Kompilator znajduje najbardziej
  na lewo położony znak niebędący białym znakiem we WSZYSTKICH
  liniach zawartości ORAZ pozycję zamykającego """. Ta kolumna
  staje się lewym marginesem, a wszystkie białe znaki na lewo
  od niej są usuwane.
- **Pozycja zamykającego """ kontroluje wcięcie**:
    - Zamykające """ wyrównane z zawartością — brak dodatkowego wcięcia
    - Zamykające """ przesunięte w lewo — dodaje wcięcie do wszystkich linii
    - Zamykające """ przesunięte w prawo — bez efektu (linie zawartości
      określają margines)
- **String.stripIndent()** (Java 15) — stosuje ten sam
  algorytm ponownego wcięcia do zwykłego ciągu w czasie wykonania.
  Przydatne gdy bloki tekstowe nie są dostępne (np. odczyt z
  pliku lub bazy danych).
- Końcowe białe znaki w każdej linii są domyślnie usuwane. Użyj
  sekwencji ucieczki \s (Java 15) aby zachować końcowe spacje gdy potrzeba.
- Puste linie wewnątrz bloku tekstowego są zachowywane.
*/

// ============================================================
// Sekcja 3: Bloki tekstowe — Sekwencje ucieczki i formatowanie
// ============================================================

/*
## Bloki tekstowe — Sekwencje ucieczki i formatowanie

- **Sekwencja ucieczki \s** (Java 15) — tłumaczy się na pojedynczą spację
  (U+0020). Jej celem jest zapobieganie usuwaniu końcowych białych znaków.
  Spacje przed \s są również zachowywane.
- **\ (backslash-nowa-linia)** — kontynuacja linii (Java 15). Łączy
  bieżącą linię z następną, pomijając terminator linii.
  Przydatne dla długich linii, które chcesz zawinąć w kodzie źródłowym,
  ale mają pojawiać się jako pojedyncza linia w wynikowym ciągu.
- **Tradycyjne sekwencje ucieczki** działają wewnątrz bloków tekstowych:
  \n, \t, \\, \", ucieczki unicode itp. Są przetwarzane w kroku 3
  potoku kompilacji (po ponownym wcięciu).
- **Zasady cytowania**: Pojedynczy " lub dwa "" są dopuszczalne
  wewnątrz bloku tekstowego. Trzy kolejne cudzysłowy muszą być
  ucieczkowane: użyj \"\"\" lub ""\". Zasada jest taka: każda
  sekwencja 3+ nieucieczkowanych cudzysłowów zamknęłaby blok tekstowy.
- **String.formatted()** (Java 15) — metoda instancji będąca
  odpowiednikiem String.format(). Czyta się bardziej naturalnie
  z blokami tekstowymi:
      var json = """
          {"name": "%s", "age": %d}
          """.formatted(name, age);
  zamiast String.format(textBlock, name, age).
- **String.translateEscapes()** (Java 15) — interpretuje
  sekwencje ucieczki Javy w ciągu w czasie wykonania. Konwertuje
  dosłowny backslash-n na znak nowej linii, backslash-t na tabulator itp.
  Przydatne przy przetwarzaniu danych użytkownika lub plików
  konfiguracyjnych zawierających sekwencje ucieczki jako dosłowny tekst.
*/

// ============================================================
// Sekcja 4: Bloki tekstowe — Wzorce praktyczne
// ============================================================

/*
## Bloki tekstowe — Wzorce praktyczne

- **Osadzanie tekstu strukturalnego**: Bloki tekstowe doskonale
  nadają się do osadzania JSON, HTML, SQL, YAML, XML i innych
  formatów strukturalnych bezpośrednio w kodzie Java z zachowanym
  prawidłowym formatowaniem.
- **Szablony z parametrami**: Połącz bloki tekstowe z
  .formatted() (lub String.format()) aby tworzyć wielokrotnie
  używane szablony z symbolami zastępczymi (%s, %d, %.2f itp.).
- **Czytelność wyrażeń regularnych**: Złożone wyrażenia regularne
  mogą być podzielone na wiele linii przy użyciu sekwencji ucieczki
  \ kontynuacji linii, co czyni je znacznie bardziej czytelnymi
  niż jednoliniowe ciągi regex.
- **Generowanie kodu**: Bloki tekstowe idealnie nadają się do
  generowania kodu źródłowego, plików konfiguracyjnych lub innego
  tekstu strukturalnego, gdzie wcięcia mają znaczenie.
- **Testowanie**: Bloki tekstowe czynią oczekiwane wartości w asercjach
  znacznie bardziej czytelnymi — możesz zapisać oczekiwany wynik
  dokładnie tak, jak powinien wyglądać, z prawidłowym formatowaniem
  i podziałami linii.
*/

// ============================================================
// Sekcja 5: Adnotacja @Serial
// ============================================================

/*
## Adnotacja @Serial

- Wprowadzona w Javie 14 (JEP 367), @Serial jest adnotacją
  czasu kompilacji dla członków związanych z serializacją.
- **Cel**: Zapewnia sprawdzanie w czasie kompilacji, czy członki
  serializacji mają prawidłowe sygnatury. Bez @Serial literówka
  w nazwie metody (np. writeObjct zamiast writeObject) lub
  nieprawidłowe typy parametrów cicho by zawiodły — JVM po prostu
  zignorowałby błędnie nazwaną metodę podczas serializacji.
- **Stosuje się do następujących członków**:
    - serialVersionUID (musi być private static final long)
    - writeObject(ObjectOutputStream) (musi być private void)
    - readObject(ObjectInputStream) (musi być private void)
    - readObjectNoData() (musi być private void)
    - readResolve() (musi zwracać Object)
    - writeReplace() (musi zwracać Object)
    - serialPersistentFields (musi być private static final
      ObjectStreamField[])
- **Porównanie z @Override**: Tak jak @Override wyłapuje
  niezgodności sygnatur metod z metodami nadklasy, @Serial wyłapuje
  niezgodności sygnatur z protokołem serializacji. Obie są
  opcjonalne, ale zdecydowanie zalecane dla bezpieczeństwa.
- Adnotacja jest w pakiecie java.io i jest zachowywana tylko
  w czasie kompilacji (retencja SOURCE) — nie ma kosztu w czasie wykonania.
*/

// ============================================================
// Sekcja 6: Inne istotne dodatki Javy 12-17
// ============================================================

/*
## Inne istotne dodatki Javy 12-17

- **Pomocne NullPointerException** (JEP 358, Java 14) — gdy
  NullPointerException wystąpi, JVM zawiera teraz szczegółowy
  komunikat opisujący dokładnie, która zmienna lub wyrażenie było
  null. Na przykład: "Cannot invoke String.length() because
  this.name is null". To dramatycznie skraca czas debugowania
  dla łańcuchowych wywołań metod jak a.getB().getC().doStuff().
- **Stream.toList()** (Java 16) — wygodna operacja terminalna
  zwracająca **niemodyfikowalną** listę. Ważna
  różnica od Collectors.toList(): ta ostatnia zwraca
  **mutowalny** ArrayList, natomiast .toList() zwraca niemodyfikowalną
  listę (podobną do List.of()). Użyj .toList() gdy nie potrzebujesz
  modyfikować wyniku; użyj Collectors.toList() lub
  Collectors.toCollection(ArrayList::new) gdy potrzebujesz.
- **String.indent(int n)** (Java 12) — dostosowuje wcięcie:
    - Dodatnie n: dodaje n spacji na początku każdej linii
    - Ujemne n: usuwa do n wiodących spacji z każdej linii
    - Również normalizuje terminatory linii i zapewnia końcowe \n
- **String.transform(Function)** (Java 12) — stosuje funkcję
  do ciągu i zwraca wynik. Umożliwia płynne łańcuchowanie:
    "hello".transform(String::toUpperCase).transform(s -> s + "!")
- **CompactNumberFormat** (Java 12) — formatuje liczby w
  skróconej formie zależnej od lokalizacji: 1000 staje się "1K",
  1000000 staje się "1M" itp. Obsługuje style SHORT i LONG.
- **Files.mismatch()** (Java 12) — porównuje dwa pliki i
  zwraca pozycję pierwszego niezgodnego bajtu, lub -1 jeśli
  pliki są identyczne. Znacznie wydajniejsze niż wczytywanie obu
  plików do pamięci i porównywanie.
*/

public class OtherChanges {

    // ---- Klasy Serializable do demonstracji @Serial ----

    static class SerializableUser implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String name;
        private int age;

        SerializableUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Serial
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            System.out.println("    [custom writeObject called for " + name + "]");
        }

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            System.out.println("    [custom readObject called for " + name + "]");
        }

        @Serial
        private Object readResolve() {
            System.out.println("    [readResolve called for " + name + "]");
            return this;
        }

        @Override
        public String toString() {
            return "SerializableUser[name=" + name + ", age=" + age + "]";
        }
    }

    static class SingletonConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        static final SingletonConfig INSTANCE = new SingletonConfig("default-value");

        private String value;

        private SingletonConfig(String value) {
            this.value = value;
        }

        @Serial
        private Object readResolve() {
            return INSTANCE;
        }

        @Override
        public String toString() {
            return "SingletonConfig[value=" + value + "]";
        }
    }

    // ============================================================
    // Sekcja 1: Bloki tekstowe — Wprowadzenie
    // ============================================================

    static void textBlocksIntroduction() {
        System.out.println("=== Section 1: Text Blocks -- Introduction ===");

        // ---- Stary sposób: JSON z konkatenacją ----
        System.out.println("--- Old way: JSON with string concatenation ---");
        String oldJson = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30,\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";
        System.out.println(oldJson);

        // ---- Nowy sposób: blok tekstowy ----
        System.out.println("\n--- New way: text block ---");
        String newJson = """
                {
                  "name": "Alice",
                  "age": 30,
                  "email": "alice@example.com"
                }""";
        System.out.println(newJson);

        // Oba produkują ten sam String
        System.out.println("old equals new: " + oldJson.equals(newJson));

        // ---- Szablon HTML ----
        System.out.println("\n--- HTML template ---");
        String html = """
                <html>
                    <head>
                        <title>Hello</title>
                    </head>
                    <body>
                        <p>Welcome to Java 17!</p>
                    </body>
                </html>
                """;
        System.out.println(html);

        // ---- Zapytanie SQL ----
        System.out.println("--- SQL query ---");
        String sql = """
                SELECT u.name, u.email, o.total
                FROM users u
                JOIN orders o ON u.id = o.user_id
                WHERE o.total > 100.00
                ORDER BY o.total DESC
                """;
        System.out.println(sql);

        // ---- Bloki tekstowe to po prostu instancje String ----
        System.out.println("--- Text blocks are just String instances ---");
        String textBlock = """
                Hello, World!
                """;
        System.out.println("instanceof String: " + (textBlock instanceof String));
        System.out.println("length: " + textBlock.strip().length());
        System.out.println("contains \"World\": " + textBlock.contains("World"));
        System.out.println("toUpperCase: " + textBlock.strip().toUpperCase());

        // Konkatenacja z blokami tekstowymi
        String prefix = "Greeting: ";
        String combined = prefix + """
                Hello from a text block!""";
        System.out.println("concatenation: " + combined);
    }

    // ============================================================
    // Sekcja 2: Bloki tekstowe — Wcięcia i białe znaki
    // ============================================================

    static void textBlocksIndentation() {
        System.out.println("\n=== Section 2: Text Blocks -- Indentation and Whitespace ===");

        // ---- Pozycja zamykającego """ kontroluje wcięcie ----
        System.out.println("--- Closing delimiter position ---");

        // Wariant 1: zamykające """ wyrównane z zawartością — brak dodatkowego wcięcia
        String variant1 = """
                Line one
                Line two
                Line three
                """;
        System.out.println("Variant 1 (aligned):");
        System.out.println(variant1.replace(" ", "."));

        // Wariant 2: zamykające """ przesunięte w lewo — dodaje wcięcie
        String variant2 = """
                Line one
                Line two
                Line three
""";
        System.out.println("Variant 2 (closing left):");
        System.out.println(variant2.replace(" ", "."));

        // Wariant 3: zamykające """ wcięte dalej — zawartość określa margines
        String variant3 = """
                Line one
                Line two
                Line three
                        """;
        System.out.println("Variant 3 (closing right):");
        System.out.println(variant3.replace(" ", "."));

        // ---- Istotne vs zbędne białe znaki ----
        System.out.println("--- Essential vs incidental whitespace ---");
        String withEssential = """
                No indent
                    4-space indent
                        8-space indent
                    back to 4
                No indent again
                """;
        System.out.println("Essential whitespace preserved:");
        System.out.println(withEssential.replace(" ", "."));

        // ---- stripIndent() na zwykłym ciągu ----
        System.out.println("--- stripIndent() on a regular string ---");
        String regularString = "    Line one\n    Line two\n    Line three\n";
        System.out.println("Before stripIndent():");
        System.out.println(regularString.replace(" ", "."));
        System.out.println("After stripIndent():");
        System.out.println(regularString.stripIndent().replace(" ", "."));

        // ---- Końcowe białe znaki domyślnie usuwane ----
        System.out.println("--- Trailing whitespace stripped by default ---");
        // Końcowe spacje po "Hello" są usuwane
        String trailingDemo = """
                Hello
                World
                """;
        System.out.println("Lines (trailing whitespace stripped):");
        trailingDemo.lines().forEach(line ->
                System.out.println("  [" + line + "] length=" + line.length()));

        // ---- Puste linie zachowywane ----
        System.out.println("\n--- Empty lines preserved ---");
        String withEmpty = """
                First

                Third

                Fifth
                """;
        System.out.println("Empty lines count: " + withEmpty.lines().count());
        withEmpty.lines().forEach(line ->
                System.out.println("  [" + line + "]"));
    }

    // ============================================================
    // Sekcja 3: Bloki tekstowe — Sekwencje ucieczki i formatowanie
    // ============================================================

    static void textBlocksEscapes() {
        System.out.println("\n=== Section 3: Text Blocks -- Escape Sequences and Formatting ===");

        // ---- Sekwencja ucieczki \s: zachowanie końcowych spacji ----
        System.out.println("--- \\s escape (preserve trailing spaces) ---");
        String withoutBackslashS = """
                Name:     Alice
                Age:      30
                """;
        String withBackslashS = """
                Name:     Alice\s
                Age:      30\s\s\s
                """;
        System.out.println("Without \\s (trailing spaces stripped):");
        withoutBackslashS.lines().forEach(line ->
                System.out.println("  [" + line + "] len=" + line.length()));
        System.out.println("With \\s (trailing spaces preserved):");
        withBackslashS.lines().forEach(line ->
                System.out.println("  [" + line + "] len=" + line.length()));

        // ---- Backslash-nowa-linia: kontynuacja linii ----
        System.out.println("\n--- Backslash-newline (line continuation) ---");
        String singleLine = """
                This is a very long line that we want to \
                break across multiple lines in the source \
                code but appears as a single line.""";
        System.out.println("Line continuation result:");
        System.out.println("  " + singleLine);
        System.out.println("  Line count: " + singleLine.lines().count());

        // ---- Zasady cytowania ----
        System.out.println("\n--- Quoting rules ---");
        String quoting = """
                Single quote: "hello"
                Double quote: ""hello""
                Triple quote escaped: \"""hello\"""
                """;
        System.out.println("Quoting examples:");
        System.out.println(quoting);

        // ---- .formatted() z blokami tekstowymi ----
        System.out.println("--- .formatted() with text blocks ---");
        String name = "Bob";
        int age = 25;
        String city = "Berlin";

        String template = """
                INSERT INTO users (name, age, city)
                VALUES ('%s', %d, '%s');
                """.formatted(name, age, city);
        System.out.println("SQL with .formatted():");
        System.out.println(template);

        // ---- translateEscapes() ----
        System.out.println("--- translateEscapes() ---");
        // Wyobraź sobie odczyt "Hello\\nWorld\\tJava" z pliku konfiguracyjnego
        String rawInput = "Hello\\nWorld\\tJava";
        System.out.println("Raw input:        [" + rawInput + "]");
        System.out.println("translateEscapes: [" + rawInput.translateEscapes() + "]");

        String moreEscapes = "Line1\\nLine2\\nLine3\\t\\tindented";
        System.out.println("More escapes:     [" + moreEscapes.translateEscapes() + "]");
    }

    // ============================================================
    // Sekcja 4: Bloki tekstowe — Wzorce praktyczne
    // ============================================================

    static void textBlocksPracticalPatterns() {
        System.out.println("\n=== Section 4: Text Blocks -- Practical Patterns ===");

        // ---- Generowanie JSON z .formatted() ----
        System.out.println("--- JSON generation with .formatted() ---");
        String jsonTemplate = """
                {
                  "user": {
                    "name": "%s",
                    "age": %d,
                    "active": %b,
                    "balance": %.2f
                  }
                }
                """;
        String json = jsonTemplate.formatted("Charlie", 28, true, 1234.56);
        System.out.println(json);

        // ---- Szablon strony HTML ----
        System.out.println("--- HTML page template ---");
        String title = "My Page";
        String heading = "Welcome";
        String content = "This page was generated with text blocks!";
        String htmlPage = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body>
                    <h1>%s</h1>
                    <p>%s</p>
                </body>
                </html>
                """.formatted(title, heading, content);
        System.out.println(htmlPage);

        // ---- Sparametryzowany SQL ----
        System.out.println("--- Parameterized SQL ---");
        String table = "products";
        double minPrice = 50.0;
        int limit = 10;
        String sqlQuery = """
                SELECT p.id, p.name, p.price, c.name AS category
                FROM %s p
                JOIN categories c ON p.category_id = c.id
                WHERE p.price >= %.2f
                  AND p.active = true
                ORDER BY p.price DESC
                LIMIT %d;
                """.formatted(table, minPrice, limit);
        System.out.println(sqlQuery);

        // ---- Złożone wyrażenie regularne z kontynuacją linii ----
        System.out.println("--- Complex regex with line continuation ---");
        String emailRegex = """
                ^[a-zA-Z0-9._%+\\-]+\
                @\
                [a-zA-Z0-9.\\-]+\
                \\.\
                [a-zA-Z]{2,}$""";
        System.out.println("Email regex: " + emailRegex);
        System.out.println("Matches alice@example.com: " +
                "alice@example.com".matches(emailRegex));
        System.out.println("Matches not-an-email: " +
                "not-an-email".matches(emailRegex));

        // ---- Generowanie kodu ----
        System.out.println("\n--- Code generation ---");
        String className = "Person";
        String field1 = "name";
        String type1 = "String";
        String field2 = "age";
        String type2 = "int";
        String generatedClass = """
                public class %s {
                    private %s %s;
                    private %s %s;

                    public %s(%s %s, %s %s) {
                        this.%s = %s;
                        this.%s = %s;
                    }

                    public %s get%s() { return %s; }
                    public %s get%s() { return %s; }
                }
                """.formatted(
                className,
                type1, field1,
                type2, field2,
                className, type1, field1, type2, field2,
                field1, field1,
                field2, field2,
                type1, capitalize(field1), field1,
                type2, capitalize(field2), field2
        );
        System.out.println(generatedClass);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ============================================================
    // Sekcja 5: Adnotacja @Serial
    // ============================================================

    static void serialAnnotation() {
        System.out.println("\n=== Section 5: The @Serial Annotation ===");

        // ---- SerializableUser: serializacja + deserializacja (round-trip) ----
        System.out.println("--- SerializableUser: serialize + deserialize ---");
        var user = new SerializableUser("Alice", 30);
        System.out.println("Original: " + user);

        try {
            // Serializacja
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(user);
            oos.close();
            System.out.println("Serialized to " + baos.size() + " bytes");

            // Deserializacja
            var bais = new ByteArrayInputStream(baos.toByteArray());
            var ois = new ObjectInputStream(bais);
            var deserialized = (SerializableUser) ois.readObject();
            ois.close();

            System.out.println("Deserialized: " + deserialized);
            System.out.println("Round-trip successful: name and age preserved");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // ---- SingletonConfig: readResolve zachowuje tożsamość ----
        System.out.println("\n--- SingletonConfig: readResolve preserves identity ---");
        var config = SingletonConfig.INSTANCE;
        System.out.println("Original instance: " + config);
        System.out.println("Is INSTANCE: " + (config == SingletonConfig.INSTANCE));

        try {
            // Serializacja singletona
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(config);
            oos.close();

            // Deserializacja — readResolve powinno zwrócić INSTANCE
            var bais = new ByteArrayInputStream(baos.toByteArray());
            var ois = new ObjectInputStream(bais);
            var deserialized = (SingletonConfig) ois.readObject();
            ois.close();

            System.out.println("Deserialized: " + deserialized);
            System.out.println("Same instance as INSTANCE: " + (deserialized == SingletonConfig.INSTANCE));
            System.out.println("readResolve() preserved singleton identity!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // ---- Co wyłapuje @Serial ----
        System.out.println("\n--- What @Serial catches (compile-time) ---");
        System.out.println("@Serial on serialVersionUID -- catches wrong type or non-static");
        System.out.println("@Serial on writeObject      -- catches wrong name or wrong params");
        System.out.println("@Serial on readObject       -- catches wrong name or wrong params");
        System.out.println("@Serial on readResolve      -- catches wrong return type");
        System.out.println("@Serial on writeReplace     -- catches wrong return type");
        System.out.println("Without @Serial, a typo like 'wirteObject' would silently fail");
        System.out.println("Compare with @Override: both catch signature mistakes at compile time");
    }

    // ============================================================
    // Sekcja 6: Inne istotne dodatki Javy 12-17
    // ============================================================

    static void otherJava12to17Additions() {
        System.out.println("\n=== Section 6: Other Notable Java 12-17 Additions ===");

        // ---- Pomocne NullPointerException (Java 14) ----
        System.out.println("--- Helpful NullPointerExceptions (Java 14) ---");
        try {
            String[] names = {"Alice", null, "Charlie"};
            int len = names[1].length();  // rzuci NPE z pomocnym komunikatem
            System.out.println(len);
        } catch (NullPointerException e) {
            System.out.println("NPE message: " + e.getMessage());
            System.out.println("(Before Java 14, this would just say 'null')");
        }

        // NPE przy łańcuchowym dostępie
        try {
            Map<String, List<String>> map = new HashMap<>();
            map.put("key", null);
            int size = map.get("key").size();
            System.out.println(size);
        } catch (NullPointerException e) {
            System.out.println("Chained NPE: " + e.getMessage());
        }

        // ---- Stream.toList() (Java 16) vs Collectors.toList() ----
        System.out.println("\n--- Stream.toList() vs Collectors.toList() ---");

        var numbers = List.of(1, 2, 3, 4, 5);

        // toList() — zwraca niemodyfikowalną listę
        List<Integer> unmodifiable = numbers.stream()
                .filter(n -> n > 2)
                .toList();
        System.out.println("toList():           " + unmodifiable);
        System.out.println("toList() class:     " + unmodifiable.getClass().getSimpleName());

        try {
            unmodifiable.add(99);
        } catch (UnsupportedOperationException e) {
            System.out.println("toList() is UNMODIFIABLE: cannot add elements");
        }

        // Collectors.toList() — zwraca mutowalny ArrayList
        List<Integer> mutable = numbers.stream()
                .filter(n -> n > 2)
                .collect(Collectors.toList());
        System.out.println("\nCollectors.toList(): " + mutable);
        mutable.add(99);
        System.out.println("After adding 99:     " + mutable + " (mutable!)");

        // ---- String.indent() i String.transform() (Java 12) ----
        System.out.println("\n--- String.indent() (Java 12) ---");

        String text = "Line one\nLine two\nLine three";
        System.out.println("Original:");
        System.out.println(text);

        System.out.println("\nindent(4):");
        System.out.print(text.indent(4));

        String indented = "        deeply indented\n        lines here";
        System.out.println("indent(-6) (remove up to 6 leading spaces):");
        System.out.print(indented.indent(-6));

        System.out.println("\n--- String.transform() (Java 12) ---");
        String result = "  hello world  "
                .transform(String::strip)
                .transform(String::toUpperCase)
                .transform(s -> s + "!")
                .transform(s -> s.replace(" ", "-"));
        System.out.println("Chained transform: " + result);

        // transform może zmienić typ zwracany
        int wordCount = "one two three four five"
                .transform(s -> s.split(" ").length);
        System.out.println("Word count via transform: " + wordCount);

        // ---- CompactNumberFormat (Java 12) ----
        System.out.println("\n--- CompactNumberFormat (Java 12) ---");

        var usShort = CompactNumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        var usLong = CompactNumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG);
        var deShort = CompactNumberFormat.getCompactNumberInstance(Locale.GERMANY, NumberFormat.Style.SHORT);

        long[] testNumbers = {1, 999, 1_000, 12_500, 1_000_000, 1_500_000_000L};
        System.out.printf("%-15s %-10s %-15s %-10s%n", "Number", "US Short", "US Long", "DE Short");
        System.out.println("-".repeat(50));
        for (long num : testNumbers) {
            System.out.printf("%-15d %-10s %-15s %-10s%n",
                    num, usShort.format(num), usLong.format(num), deShort.format(num));
        }

        // ---- Files.mismatch() (Java 12) ----
        System.out.println("\n--- Files.mismatch() (Java 12) ---");
        Path file1 = null;
        Path file2 = null;
        try {
            // Tworzenie dwóch identycznych plików tymczasowych
            file1 = Files.createTempFile("mismatch-test-1-", ".txt");
            file2 = Files.createTempFile("mismatch-test-2-", ".txt");

            Files.writeString(file1, "Hello, World!\nLine two.\n");
            Files.writeString(file2, "Hello, World!\nLine two.\n");

            long mismatchPos = Files.mismatch(file1, file2);
            System.out.println("Identical files -- mismatch position: " + mismatchPos + " (-1 = identical)");

            // Modyfikacja file2
            Files.writeString(file2, "Hello, World!\nLine TWO.\n");
            mismatchPos = Files.mismatch(file1, file2);
            System.out.println("Different files -- mismatch position: " + mismatchPos);
            System.out.println("  (difference starts at byte " + mismatchPos + ")");

            // Różne długości
            Files.writeString(file2, "Hello, World!\nLine two.\nExtra line.\n");
            mismatchPos = Files.mismatch(file1, file2);
            System.out.println("Different length -- mismatch position: " + mismatchPos);
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } finally {
            try {
                if (file1 != null) Files.deleteIfExists(file1);
                if (file2 != null) Files.deleteIfExists(file2);
            } catch (IOException ignored) {
                // czyszczenie — najlepszy wysiłek
            }
        }
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        textBlocksIntroduction();
        textBlocksIndentation();
        textBlocksEscapes();
        textBlocksPracticalPatterns();
        serialAnnotation();
        otherJava12to17Additions();
    }
}
