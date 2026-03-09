package pl.training.jmodern.module02_java17;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

// ============================================================
// Section 1: Text Blocks -- Introduction
// ============================================================

/*
## Text Blocks -- Introduction

- Before text blocks, embedding multi-line strings in Java was
  painful: manual \n escapes, string concatenation with +, and
  poor readability. A simple JSON snippet could take 5+ lines of
  concatenation with escaped quotes everywhere.
- Other languages solved this long ago:
    - Python: triple-quoted strings (""" or ''')
    - Kotlin: trimMargin() / trimIndent() on raw strings
    - JavaScript: template literals with backticks
    - C#: verbatim strings with @"..."
- **JEP timeline**:
    - JEP 355: Preview in Java 13
    - JEP 368: Second preview in Java 14
    - JEP 378: Finalized in Java 15
    (Taught here with Java 17 LTS)
- **Basic syntax**: opening """ followed by a line terminator,
  then the content, closed by """. The opening """ MUST be
  followed by a line terminator -- you cannot put content on the
  same line as the opening delimiter.
- Text blocks produce ordinary `String` instances -- they are
  NOT a new type. You can call any String method on them, use
  them in concatenation, pass them anywhere a String is expected.
- **Three-step compile-time processing**:
    1. Line terminators are normalized to \n (LF)
    2. Incidental whitespace is removed (re-indentation)
    3. Escape sequences are interpreted
  This happens at compile time, so there is no runtime overhead.
*/

// ============================================================
// Section 2: Text Blocks -- Indentation and Whitespace
// ============================================================

/*
## Text Blocks -- Indentation and Whitespace

- The key concept is **incidental vs essential whitespace**:
    - Incidental whitespace = indentation added purely for code
      formatting (to align with surrounding Java code). This is
      automatically removed by the compiler.
    - Essential whitespace = indentation that is part of the
      actual content (e.g., indentation in HTML or JSON). This
      is preserved in the resulting string.
- **Re-indentation algorithm**: The compiler finds the leftmost
  non-whitespace character across ALL content lines AND the
  closing """ position. That column becomes the left margin, and
  all whitespace to the left of it is stripped.
- **Closing """ position controls indentation**:
    - Closing """ aligned with content -- no extra indentation
    - Closing """ moved left -- adds indentation to all lines
    - Closing """ moved right -- no effect (content lines
      determine the margin)
- **String.stripIndent()** (Java 15) -- applies the same
  re-indentation algorithm to a regular string at runtime.
  Useful when text blocks are not available (e.g., reading from
  a file or database).
- Trailing whitespace on each line is stripped by default. Use
  the \s escape (Java 15) to preserve trailing spaces when needed.
- Empty lines within a text block are preserved.
*/

// ============================================================
// Section 3: Text Blocks -- Escape Sequences and Formatting
// ============================================================

/*
## Text Blocks -- Escape Sequences and Formatting

- **\s escape** (Java 15) -- translates to a single space (U+0020).
  Its purpose is to prevent trailing whitespace stripping. Any
  spaces before \s are also preserved.
- **\ (backslash-newline)** -- line continuation (Java 15). Joins
  the current line with the next, suppressing the line terminator.
  Useful for long lines that you want to wrap in source code but
  appear as a single line in the resulting string.
- **Traditional escape sequences** work inside text blocks:
  \n, \t, \\, \", unicode escapes, etc. They are processed in step 3
  of the compile-time pipeline (after re-indentation).
- **Quoting rules**: A single " or two "" are fine inside a text
  block. Three consecutive quotes must be escaped: use \"\"\" or
  ""\". The rule is: any sequence of 3+ unescaped quotes would
  close the text block.
- **String.formatted()** (Java 15) -- instance method equivalent
  to String.format(). Reads more naturally with text blocks:
      var json = """
          {"name": "%s", "age": %d}
          """.formatted(name, age);
  instead of String.format(textBlock, name, age).
- **String.translateEscapes()** (Java 15) -- interprets Java
  escape sequences in a string at runtime. Converts literal
  backslash-n to a newline character, backslash-t to a tab, etc.
  Useful when processing user input or config files that contain
  escape sequences as literal text.
*/

// ============================================================
// Section 4: Text Blocks -- Practical Patterns
// ============================================================

/*
## Text Blocks -- Practical Patterns

- **Embedding structured text**: Text blocks excel at embedding
  JSON, HTML, SQL, YAML, XML, and other structured formats
  directly in Java code with proper formatting preserved.
- **Parameterized templates**: Combine text blocks with
  .formatted() (or String.format()) to create reusable templates
  with placeholders (%s, %d, %.2f, etc.).
- **Regex readability**: Complex regular expressions can be split
  across multiple lines using the \ line continuation escape,
  making them much more readable than single-line regex strings.
- **Code generation**: Text blocks are ideal for generating source
  code, configuration files, or other structured text where
  indentation matters.
- **Testing**: Text blocks make expected values in assertions much
  more readable -- you can write the expected output exactly as it
  should appear, with proper formatting and line breaks.
*/

// ============================================================
// Section 5: The @Serial Annotation
// ============================================================

/*
## The @Serial Annotation

- Introduced in Java 14 (JEP 367), @Serial is a compile-time
  annotation for serialization-related members.
- **Purpose**: Provides compile-time checking that serialization
  members have the correct signatures. Without @Serial, a typo
  in a method name (e.g., writeObjct instead of writeObject) or
  wrong parameter types would silently fail -- the JVM would
  just ignore the misspelled method during serialization.
- **Applies to these members**:
    - serialVersionUID (must be private static final long)
    - writeObject(ObjectOutputStream) (must be private void)
    - readObject(ObjectInputStream) (must be private void)
    - readObjectNoData() (must be private void)
    - readResolve() (must return Object)
    - writeReplace() (must return Object)
    - serialPersistentFields (must be private static final
      ObjectStreamField[])
- **Comparison with @Override**: Just as @Override catches method
  signature mismatches with superclass methods, @Serial catches
  signature mismatches with the serialization protocol. Both are
  optional but strongly recommended for safety.
- The annotation is in the java.io package and is retained only
  at compile time (SOURCE retention) -- it has no runtime cost.
*/

// ============================================================
// Section 6: Other Notable Java 12-17 Additions
// ============================================================

/*
## Other Notable Java 12-17 Additions

- **Helpful NullPointerExceptions** (JEP 358, Java 14) -- when a
  NullPointerException occurs, the JVM now includes a detailed
  message describing exactly which variable or expression was
  null. For example: "Cannot invoke String.length() because
  this.name is null". This dramatically reduces debugging time
  for chained method calls like a.getB().getC().doStuff().
- **Stream.toList()** (Java 16) -- a convenient terminal
  operation that returns an **unmodifiable** List. Important
  difference from Collectors.toList(): the latter returns a
  **mutable** ArrayList, while .toList() returns an unmodifiable
  list (similar to List.of()). Use .toList() when you do not
  need to modify the result; use Collectors.toList() or
  Collectors.toCollection(ArrayList::new) when you do.
- **String.indent(int n)** (Java 12) -- adjusts indentation:
    - Positive n: prepends n spaces to each line
    - Negative n: removes up to n leading spaces from each line
    - Also normalizes line terminators and ensures trailing \n
- **String.transform(Function)** (Java 12) -- applies a function
  to the string and returns the result. Enables fluent chaining:
    "hello".transform(String::toUpperCase).transform(s -> s + "!")
- **CompactNumberFormat** (Java 12) -- formats numbers in a
  locale-aware compact form: 1000 becomes "1K", 1000000 becomes
  "1M", etc. Supports SHORT and LONG styles.
- **Files.mismatch()** (Java 12) -- compares two files and
  returns the position of the first mismatched byte, or -1 if
  the files are identical. Much more efficient than reading both
  files into memory and comparing.
*/

public class OtherChanges {

    // ---- Serializable classes for @Serial demonstration ----

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
    // Section 1: Text Blocks -- Introduction
    // ============================================================

    static void textBlocksIntroduction() {
        System.out.println("=== Section 1: Text Blocks -- Introduction ===");

        // ---- The old way: JSON with concatenation ----
        System.out.println("--- Old way: JSON with string concatenation ---");
        String oldJson = "{\n" +
                "  \"name\": \"Alice\",\n" +
                "  \"age\": 30,\n" +
                "  \"email\": \"alice@example.com\"\n" +
                "}";
        System.out.println(oldJson);

        // ---- The new way: text block ----
        System.out.println("\n--- New way: text block ---");
        String newJson = """
                {
                  "name": "Alice",
                  "age": 30,
                  "email": "alice@example.com"
                }""";
        System.out.println(newJson);

        // Both produce the same String
        System.out.println("old equals new: " + oldJson.equals(newJson));

        // ---- HTML template ----
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

        // ---- SQL query ----
        System.out.println("--- SQL query ---");
        String sql = """
                SELECT u.name, u.email, o.total
                FROM users u
                JOIN orders o ON u.id = o.user_id
                WHERE o.total > 100.00
                ORDER BY o.total DESC
                """;
        System.out.println(sql);

        // ---- Text blocks are just String instances ----
        System.out.println("--- Text blocks are just String instances ---");
        String textBlock = """
                Hello, World!
                """;
        System.out.println("instanceof String: " + (textBlock instanceof String));
        System.out.println("length: " + textBlock.strip().length());
        System.out.println("contains \"World\": " + textBlock.contains("World"));
        System.out.println("toUpperCase: " + textBlock.strip().toUpperCase());

        // Concatenation with text blocks
        String prefix = "Greeting: ";
        String combined = prefix + """
                Hello from a text block!""";
        System.out.println("concatenation: " + combined);
    }

    // ============================================================
    // Section 2: Text Blocks -- Indentation and Whitespace
    // ============================================================

    static void textBlocksIndentation() {
        System.out.println("\n=== Section 2: Text Blocks -- Indentation and Whitespace ===");

        // ---- Closing """ position controls indentation ----
        System.out.println("--- Closing delimiter position ---");

        // Variant 1: closing """ aligned with content -- no extra indentation
        String variant1 = """
                Line one
                Line two
                Line three
                """;
        System.out.println("Variant 1 (aligned):");
        System.out.println(variant1.replace(" ", "."));

        // Variant 2: closing """ moved left -- adds indentation
        String variant2 = """
                Line one
                Line two
                Line three
""";
        System.out.println("Variant 2 (closing left):");
        System.out.println(variant2.replace(" ", "."));

        // Variant 3: closing """ indented further -- content determines margin
        String variant3 = """
                Line one
                Line two
                Line three
                        """;
        System.out.println("Variant 3 (closing right):");
        System.out.println(variant3.replace(" ", "."));

        // ---- Essential vs incidental whitespace ----
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

        // ---- stripIndent() on a regular string ----
        System.out.println("--- stripIndent() on a regular string ---");
        String regularString = "    Line one\n    Line two\n    Line three\n";
        System.out.println("Before stripIndent():");
        System.out.println(regularString.replace(" ", "."));
        System.out.println("After stripIndent():");
        System.out.println(regularString.stripIndent().replace(" ", "."));

        // ---- Trailing whitespace stripped by default ----
        System.out.println("--- Trailing whitespace stripped by default ---");
        // Trailing spaces after "Hello" are removed
        String trailingDemo = """
                Hello
                World
                """;
        System.out.println("Lines (trailing whitespace stripped):");
        trailingDemo.lines().forEach(line ->
                System.out.println("  [" + line + "] length=" + line.length()));

        // ---- Empty lines preserved ----
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
    // Section 3: Text Blocks -- Escape Sequences and Formatting
    // ============================================================

    static void textBlocksEscapes() {
        System.out.println("\n=== Section 3: Text Blocks -- Escape Sequences and Formatting ===");

        // ---- \s escape: preserve trailing spaces ----
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

        // ---- Backslash-newline: line continuation ----
        System.out.println("\n--- Backslash-newline (line continuation) ---");
        String singleLine = """
                This is a very long line that we want to \
                break across multiple lines in the source \
                code but appears as a single line.""";
        System.out.println("Line continuation result:");
        System.out.println("  " + singleLine);
        System.out.println("  Line count: " + singleLine.lines().count());

        // ---- Quoting rules ----
        System.out.println("\n--- Quoting rules ---");
        String quoting = """
                Single quote: "hello"
                Double quote: ""hello""
                Triple quote escaped: \"""hello\"""
                """;
        System.out.println("Quoting examples:");
        System.out.println(quoting);

        // ---- .formatted() with text blocks ----
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
        // Imagine reading "Hello\\nWorld\\tJava" from a config file
        String rawInput = "Hello\\nWorld\\tJava";
        System.out.println("Raw input:        [" + rawInput + "]");
        System.out.println("translateEscapes: [" + rawInput.translateEscapes() + "]");

        String moreEscapes = "Line1\\nLine2\\nLine3\\t\\tindented";
        System.out.println("More escapes:     [" + moreEscapes.translateEscapes() + "]");
    }

    // ============================================================
    // Section 4: Text Blocks -- Practical Patterns
    // ============================================================

    static void textBlocksPracticalPatterns() {
        System.out.println("\n=== Section 4: Text Blocks -- Practical Patterns ===");

        // ---- JSON generation with .formatted() ----
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

        // ---- HTML page template ----
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

        // ---- Parameterized SQL ----
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

        // ---- Complex regex with line continuation ----
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

        // ---- Code generation ----
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
    // Section 5: The @Serial Annotation
    // ============================================================

    static void serialAnnotation() {
        System.out.println("\n=== Section 5: The @Serial Annotation ===");

        // ---- SerializableUser: serialize + deserialize round-trip ----
        System.out.println("--- SerializableUser: serialize + deserialize ---");
        var user = new SerializableUser("Alice", 30);
        System.out.println("Original: " + user);

        try {
            // Serialize
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(user);
            oos.close();
            System.out.println("Serialized to " + baos.size() + " bytes");

            // Deserialize
            var bais = new ByteArrayInputStream(baos.toByteArray());
            var ois = new ObjectInputStream(bais);
            var deserialized = (SerializableUser) ois.readObject();
            ois.close();

            System.out.println("Deserialized: " + deserialized);
            System.out.println("Round-trip successful: name and age preserved");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // ---- SingletonConfig: readResolve preserves identity ----
        System.out.println("\n--- SingletonConfig: readResolve preserves identity ---");
        var config = SingletonConfig.INSTANCE;
        System.out.println("Original instance: " + config);
        System.out.println("Is INSTANCE: " + (config == SingletonConfig.INSTANCE));

        try {
            // Serialize the singleton
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(config);
            oos.close();

            // Deserialize -- readResolve should return INSTANCE
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

        // ---- What @Serial catches ----
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
    // Section 6: Other Notable Java 12-17 Additions
    // ============================================================

    static void otherJava12to17Additions() {
        System.out.println("\n=== Section 6: Other Notable Java 12-17 Additions ===");

        // ---- Helpful NullPointerExceptions (Java 14) ----
        System.out.println("--- Helpful NullPointerExceptions (Java 14) ---");
        try {
            String[] names = {"Alice", null, "Charlie"};
            int len = names[1].length();  // will throw NPE with helpful message
            System.out.println(len);
        } catch (NullPointerException e) {
            System.out.println("NPE message: " + e.getMessage());
            System.out.println("(Before Java 14, this would just say 'null')");
        }

        // Chained access NPE
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

        // toList() -- returns unmodifiable list
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

        // Collectors.toList() -- returns mutable ArrayList
        List<Integer> mutable = numbers.stream()
                .filter(n -> n > 2)
                .collect(Collectors.toList());
        System.out.println("\nCollectors.toList(): " + mutable);
        mutable.add(99);
        System.out.println("After adding 99:     " + mutable + " (mutable!)");

        // ---- String.indent() and String.transform() (Java 12) ----
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

        // transform can change the return type
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
            // Create two identical temp files
            file1 = Files.createTempFile("mismatch-test-1-", ".txt");
            file2 = Files.createTempFile("mismatch-test-2-", ".txt");

            Files.writeString(file1, "Hello, World!\nLine two.\n");
            Files.writeString(file2, "Hello, World!\nLine two.\n");

            long mismatchPos = Files.mismatch(file1, file2);
            System.out.println("Identical files -- mismatch position: " + mismatchPos + " (-1 = identical)");

            // Modify file2
            Files.writeString(file2, "Hello, World!\nLine TWO.\n");
            mismatchPos = Files.mismatch(file1, file2);
            System.out.println("Different files -- mismatch position: " + mismatchPos);
            System.out.println("  (difference starts at byte " + mismatchPos + ")");

            // Different lengths
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
                // cleanup best-effort
            }
        }
    }

    // ============================================================
    // Main -- run all sections
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
