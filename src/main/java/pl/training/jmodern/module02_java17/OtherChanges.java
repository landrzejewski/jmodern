package pl.training.jmodern.module02_java17;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Bloki tekstowe
// ============================================================

/*
## Bloki tekstowe (JEP 378, Java 15, omawiane z Java 17 LTS)

- **Podstawowa składnia**: otwierające """ zakończone terminatorem
  linii, potem zawartość, zamknięte przez """. Produkują zwykłe
  instancje String — nie są nowym typem.
- **Trzyetapowe przetwarzanie w czasie kompilacji**:
    1. Terminatory linii normalizowane do \n (LF)
    2. Zbędne białe znaki usuwane (ponowne wcięcie)
    3. Sekwencje ucieczki interpretowane
- **Pozycja zamykającego """ kontroluje wcięcie**:
    - Wyrównane z zawartością → brak dodatkowego wcięcia
    - Przesunięte w lewo → dodaje wcięcie do wszystkich linii
- **Nowe sekwencje ucieczki** (Java 15):
    - \s — zachowuje końcowe spacje (zapobiega ich usuwaniu)
    - \ (backslash-nowa-linia) — kontynuacja linii (łączy
      bieżącą linię z następną)
- **Nowe metody String** (Java 15):
    - .formatted() — odpowiednik String.format() jako metoda instancji
    - .stripIndent() — algorytm ponownego wcięcia w czasie wykonania
    - .translateEscapes() — interpretuje sekwencje ucieczki (\n, \t)
      w czasie wykonania
- **Zasady cytowania**: " i "" dopuszczalne wewnątrz bloku;
  """ wymaga ucieczki (\""" lub ""\").
*/

// ============================================================
// Sekcja 2: Adnotacja @Serial
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
// Sekcja 3: Inne istotne dodatki Javy 12-17
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
    // Sekcja 1: Bloki tekstowe
    // ============================================================

    static void textBlocks() {
        System.out.println("=== Section 1: Text Blocks ===");

        // ---- Porównanie: stary vs nowy sposób (JSON) ----
        System.out.println("--- Old way: JSON with string concatenation ---");
        String oldJson = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30,\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";
        System.out.println(oldJson);

        System.out.println("\n--- New way: text block ---");
        String newJson = """
                {
                  "name": "Alice",
                  "age": 30,
                  "email": "alice@example.com"
                }""";
        System.out.println(newJson);
        System.out.println("old equals new: " + oldJson.equals(newJson));

        // ---- Pozycja zamykającego """ kontroluje wcięcie ----
        System.out.println("\n--- Closing delimiter position ---");

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

        // ---- \s: zachowanie końcowych spacji ----
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

        // ---- \ kontynuacja linii ----
        System.out.println("\n--- Backslash-newline (line continuation) ---");
        String singleLine = """
                This is a very long line that we want to \
                break across multiple lines in the source \
                code but appears as a single line.""";
        System.out.println("Line continuation result:");
        System.out.println("  " + singleLine);
        System.out.println("  Line count: " + singleLine.lines().count());

        // ---- .formatted() z szablonem ----
        System.out.println("\n--- .formatted() with text blocks ---");
        String json = """
                {
                  "user": {
                    "name": "%s",
                    "age": %d,
                    "active": %b,
                    "balance": %.2f
                  }
                }
                """.formatted("Charlie", 28, true, 1234.56);
        System.out.println("JSON with .formatted():");
        System.out.println(json);

        // ---- translateEscapes() ----
        System.out.println("--- translateEscapes() ---");
        String rawInput = "Hello\\nWorld\\tJava";
        System.out.println("Raw input:        [" + rawInput + "]");
        System.out.println("translateEscapes: [" + rawInput.translateEscapes() + "]");
    }

    // ============================================================
    // Sekcja 2: Adnotacja @Serial
    // ============================================================

    static void serialAnnotation() {
        System.out.println("\n=== Section 2: The @Serial Annotation ===");

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
    // Sekcja 3: Inne istotne dodatki Javy 12-17
    // ============================================================

    static void otherJava12to17Additions() {
        System.out.println("\n=== Section 3: Other Notable Java 12-17 Additions ===");

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
        textBlocks();
        serialAnnotation();
        otherJava12to17Additions();
    }
}
