package pl.training.jmodern.module02_java11;

import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.*;

// ============================================================
// Section 1: Local Variable Type Inference (var)
// ============================================================

/*
## Local Variable Type Inference (var)

- `var` (Java 10) lets the compiler infer the type of a local
  variable from the initializer expression on the right-hand side.
  It is **syntactic sugar** — the variable is still statically typed
  at compile time, exactly as if you had written the type explicitly.
- `var` is **not a keyword** — it is a "reserved type name". You can
  still use `var` as a variable name, method name, or package name
  (but not as a class or interface name). This was done for backward
  compatibility with existing code that used `var` as an identifier.
- **Where you CAN use `var`**:
    - Local variables with initializers: `var list = new ArrayList<String>()`
    - Enhanced for-loop variables: `for (var item : collection)`
    - Traditional for-loop index: `for (var i = 0; i < 10; i++)`
    - Try-with-resources variables: `try (var stream = Files.lines(path))`
- **Where you CANNOT use `var`**:
    - Fields (instance or static)
    - Method parameters
    - Method return types
    - Constructor parameters
    - Catch parameters
    - Variables without an initializer: `var x;` — compile error
    - Array initializers: `var arr = {1, 2, 3};` — compile error
    - `null` initializers: `var x = null;` — compile error
- **When `var` improves readability**:
    - Complex generic types: `var map = new HashMap<String, List<Optional<String>>>()`
      instead of repeating the full type on both sides.
    - Anonymous classes where the type is obvious from context.
    - Iterator/loop patterns where the type is clear from the collection.
- **When `var` hurts readability**:
    - Numeric literals: `var count = 1` — is it `int`, `long`, `byte`?
    - Method calls with non-obvious return types: `var result = process()`
      — the reader must look up the method signature.
    - Diamond operator with `var` loses type info:
      `var list = new ArrayList<>()` infers `ArrayList<Object>`, not
      `ArrayList<String>`. Always specify the type argument with `var`.
- **`final var`** — combines type inference with immutability:
  `final var name = "Alice"` is equivalent to `final String name = "Alice"`.
*/

// ============================================================
// Section 2: var in Lambda Parameters (Java 11)
// ============================================================

/*
## var in Lambda Parameters (Java 11)

- Java 11 (JEP 323) allows `var` in lambda formal parameters:
  `(var x, var y) -> x + y`.
- **Why was this added?** Not for brevity (implicit parameters are
  already shorter), but to allow **annotations** on lambda parameters.
  Before Java 11, you could only annotate lambda parameters if you
  used explicit types: `(@NotNull String x) -> ...`. Now you can
  write `(@NotNull var x) -> ...` and let the compiler infer the type.
- **Rules**:
    - Must use `var` for **all** parameters or **none** — mixing
      `var` with explicit types is not allowed:
      `(var x, String y) -> ...` — compile error.
    - Cannot mix `var` with implicit (no-type) parameters:
      `(var x, y) -> ...` — compile error.
    - Cannot use `var` for parameters with no parentheses:
      `var x -> ...` — compile error (parentheses required).
- **Three lambda parameter styles** (comparison):
    - Implicit:  `(x, y) -> x + y` — shortest, no annotations possible
    - Explicit:  `(String x, String y) -> x + y` — verbose, annotations OK
    - var:       `(var x, var y) -> x + y` — same as implicit but allows annotations
- **Practical use**: applying annotations like `@Nullable`, `@NonNull`,
  `@SuppressWarnings`, or custom annotations to individual lambda
  parameters without specifying the full type.
*/

// ============================================================
// Section 3: New String Methods (Java 11)
// ============================================================

/*
## New String Methods (Java 11)

- **`isBlank()`** — returns `true` if the string is empty or contains
  only whitespace characters. Unlike `isEmpty()` which only checks
  `length() == 0`, `isBlank()` is **Unicode-aware** and recognizes
  all Unicode whitespace (e.g., non-breaking space `\u00A0`,
  ideographic space `\u3000`, etc.).
- **`strip()`** — removes leading and trailing whitespace. Similar
  to `trim()`, but **Unicode-aware**:
    - `trim()` removes characters with code points ≤ U+0020 (ASCII
      control characters and space).
    - `strip()` uses `Character.isWhitespace()`, which includes
      Unicode whitespace like `\u2003` (em space), `\u3000`
      (ideographic space), etc.
    - In most practical cases they behave the same, but `strip()` is
      the correct choice for internationalized text.
- **`stripLeading()`** — removes whitespace from the beginning only.
- **`stripTrailing()`** — removes whitespace from the end only.
- **`lines()`** — returns a `Stream<String>` of lines, split by
  line terminators: `\n` (LF), `\r` (CR), or `\r\n` (CRLF).
  The stream is **lazy** — efficient for processing large multi-line
  strings without loading all lines into memory at once.
  Empty trailing lines are not included if the string ends with a
  line terminator.
- **`repeat(int count)`** — returns a string that is this string
  repeated `count` times. `"ab".repeat(3)` → `"ababab"`.
  Returns an empty string when `count` is 0. Throws
  `IllegalArgumentException` if `count` is negative.
*/

// ============================================================
// Section 4: Files.readString() and Files.writeString() (Java 11)
// ============================================================

/*
## Files.readString() and Files.writeString() (Java 11)

- Before Java 11, reading an entire file into a `String` required
  multi-step boilerplate:
    - `new String(Files.readAllBytes(path), StandardCharsets.UTF_8)`
    - or wrapping a `BufferedReader` in try-with-resources.
- Java 11 added convenience methods on `java.nio.file.Files`:
    - **`Files.readString(Path)`** — reads the entire file as a
      `String` using UTF-8 encoding (default).
    - **`Files.readString(Path, Charset)`** — reads with the
      specified charset.
    - **`Files.writeString(Path, CharSequence, OpenOption...)`** —
      writes a string to a file. Default options are `CREATE` and
      `TRUNCATE_EXISTING` (creates the file if it doesn't exist,
      overwrites if it does).
    - **`Files.writeString(Path, CharSequence, Charset, OpenOption...)`**
      — writes with specified charset.
- **Common `OpenOption` values** (from `StandardOpenOption`):
    - `CREATE` — create the file if it doesn't exist (default).
    - `TRUNCATE_EXISTING` — truncate the file to zero length (default).
    - `APPEND` — append to the end of the file.
    - `CREATE_NEW` — create a new file, fail if it already exists.
    - `WRITE` — open for writing (implied by `writeString`).
- **Caution**: these methods read/write the **entire** file into
  memory. For large files, use streaming APIs like `Files.lines()`,
  `BufferedReader`, or `BufferedWriter`.
*/

// ============================================================
// Section 5: Other Notable Java 11 Additions
// ============================================================

/*
## Other Notable Java 11 Additions

- **`Optional.isEmpty()`** (Java 11) — the counterpart to
  `isPresent()`. Returns `true` if no value is present.
  Before Java 11, you had to write `!optional.isPresent()`.
- **`Collection.toArray(IntFunction)`** (Java 11) — type-safe
  array conversion: `list.toArray(String[]::new)` replaces the
  older `list.toArray(new String[0])` pattern. The `IntFunction`
  receives the collection size and returns an array of that size.
- **`Predicate.not()`** (Java 11) — static method for negating
  predicates: `lines.filter(Predicate.not(String::isBlank))`
  instead of `lines.filter(s -> !s.isBlank())`. Works with
  method references, unlike the lambda negation pattern.
- **`Pattern.asMatchPredicate()`** (Java 11) — returns a
  `Predicate<String>` that tests if the **entire** input matches
  the pattern (like `matches()`). Compare with Java 8's
  `asPredicate()` which uses `find()` (partial match).
- **`Character.toString(int)`** (Java 11) — converts a Unicode
  code point to a `String`. Replaces the two-step
  `new String(Character.toChars(codePoint))` pattern.
- **Single-file source-code programs** (Java 11, JEP 330) — you
  can run `java MyProgram.java` directly without a separate
  `javac` compilation step. The JVM compiles and runs in one
  command. Useful for scripts, prototyping, and learning.
*/

public class OtherChanges {

    // ---- Helper annotation for Section 2 (var in lambdas) ----

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NotNull {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Positive {}

    // ============================================================
    // Section 1: Local Variable Type Inference (var)
    // ============================================================

    static void localVariableTypeInference() {
        System.out.println("=== Local Variable Type Inference (var) ===");

        // Basic type inference — compiler infers String from the initializer
        var greeting = "Hello, Java 10!";
        System.out.println("var greeting (String): " + greeting);
        System.out.println("  actual type: " + greeting.getClass().getSimpleName());

        // Inference with numeric types — var infers int (not long, not short)
        var count = 42;
        var price = 19.99;   // infers double
        var initial = 'A';   // infers char
        var flag = true;     // infers boolean
        System.out.println("var count (int): " + count);
        System.out.println("var price (double): " + price);
        System.out.println("var initial (char): " + initial);
        System.out.println("var flag (boolean): " + flag);

        // Inference with collections — the full generic type is inferred
        var names = new ArrayList<String>();
        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");
        System.out.println("var names (ArrayList<String>): " + names);

        // Complex generic types — var shines here
        // Without var: Map<String, List<Optional<String>>> map = new HashMap<String, List<Optional<String>>>();
        var complexMap = new HashMap<String, List<Optional<String>>>();
        complexMap.put("greetings", List.of(Optional.of("hello"), Optional.empty()));
        System.out.println("complex generic map: " + complexMap);

        // var with Map.of — infers Map<String, Integer>
        var scores = Map.of("Alice", 95, "Bob", 87, "Charlie", 92);
        System.out.println("var scores (Map<String, Integer>): " + scores);

        // var in enhanced for-loop — infers the element type from the collection
        System.out.print("for-each with var: ");
        for (var name : names) {
            System.out.print(name + " ");
        }
        System.out.println();

        // var in traditional for-loop
        System.out.print("for-loop with var: ");
        for (var i = 0; i < names.size(); i++) {
            System.out.print(names.get(i) + " ");
        }
        System.out.println();

        // var in try-with-resources
        // (We'll demo with a StringReader since it implements AutoCloseable)
        var content = "line1\nline2\nline3";
        try (var reader = new java.io.StringReader(content);
             var buffered = new java.io.BufferedReader(reader)) {
            var firstLine = buffered.readLine();
            System.out.println("try-with-resources var: first line = " + firstLine);
        } catch (IOException e) {
            System.out.println("  error: " + e.getMessage());
        }

        // final var — combining immutability with type inference
        final var PI = 3.14159;
        final var APP_NAME = "JModern";
        System.out.println("final var PI: " + PI);
        System.out.println("final var APP_NAME: " + APP_NAME);
        // PI = 3.0;  // compile error — final variable

        // var is still statically typed — you CANNOT change the type after declaration
        var text = "hello";
        text = "world";     // OK — same type (String)
        // text = 42;       // compile error — incompatible types: int cannot be converted to String
        System.out.println("var is static: reassigned to \"" + text + "\" (still String)");

        // ---- PITFALLS: where var loses type information ----

        // PITFALL 1: Diamond operator with var — infers Object, not the expected type
        var rawList = new ArrayList<>();     // ArrayList<Object>, NOT ArrayList<String>!
        rawList.add("string");
        rawList.add(42);                     // compiles — because it's ArrayList<Object>
        System.out.println("PITFALL — var + diamond: " + rawList + " (ArrayList<Object>)");
        // Fix: always specify the type argument: var list = new ArrayList<String>()

        // PITFALL 2: Method return types may not be obvious
        var result = processData();          // What type is this? Reader must check the method.
        System.out.println("PITFALL — non-obvious return: " + result);

        // BEST PRACTICE: use var when the type is clear from context
        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        var now = java.time.LocalDate.now();
        System.out.println("clear context: " + now.format(formatter));
    }

    // Helper method for demonstrating non-obvious return types
    private static Map<String, List<Integer>> processData() {
        return Map.of("values", List.of(1, 2, 3));
    }

    // ============================================================
    // Section 2: var in Lambda Parameters (Java 11)
    // ============================================================

    static void varInLambdaParameters() {
        System.out.println("\n=== var in Lambda Parameters (Java 11) ===");

        // Three styles of lambda parameters — comparison
        // Style 1: Implicit types (no types, shortest)
        BinaryOperator<String> implicitConcat = (x, y) -> x + " " + y;
        System.out.println("implicit: " + implicitConcat.apply("Hello", "World"));

        // Style 2: Explicit types (full type names)
        BinaryOperator<String> explicitConcat = (String x, String y) -> x + " " + y;
        System.out.println("explicit: " + explicitConcat.apply("Hello", "World"));

        // Style 3: var types (Java 11 — allows annotations)
        BinaryOperator<String> varConcat = (var x, var y) -> x + " " + y;
        System.out.println("var:      " + varConcat.apply("Hello", "World"));

        // The main reason for var in lambdas: ANNOTATIONS on parameters
        // With var, you can annotate lambda parameters without writing the full type
        Function<String, String> annotatedLambda = (@NotNull var s) -> s.toUpperCase();
        System.out.println("annotated lambda: " + annotatedLambda.apply("hello"));

        // Multiple annotated parameters
        BinaryOperator<Integer> annotatedAdd = (@Positive var a, @Positive var b) -> a + b;
        System.out.println("annotated add: " + annotatedAdd.apply(3, 5));

        // var in lambda with stream operations
        var words = List.of("hello", "world", "java", "eleven");
        var upperWords = words.stream()
                .map((var word) -> word.toUpperCase())
                .collect(Collectors.toList());
        System.out.println("stream with var lambda: " + upperWords);

        // var with BiFunction
        BiFunction<String, Integer, String> repeater = (var text, var times) -> text.repeat(times);
        System.out.println("BiFunction with var: " + repeater.apply("Ha", 3));

        // Practical: filtering with annotated var
        var numbers = List.of(1, -2, 3, -4, 5, 0);
        var positives = numbers.stream()
                .filter((@Positive var n) -> n > 0)
                .collect(Collectors.toList());
        System.out.println("filtered positives: " + positives);

        // RULES — what you CANNOT do:
        System.out.println("\nvar lambda rules:");
        System.out.println("  OK:    (var x, var y) -> x + y");
        System.out.println("  OK:    (@NotNull var x) -> x.length()");
        System.out.println("  ERROR: (var x, String y) -> ...  // cannot mix var with explicit types");
        System.out.println("  ERROR: (var x, y) -> ...         // cannot mix var with implicit");
        System.out.println("  ERROR: var x -> ...              // parentheses required with var");
    }

    // ============================================================
    // Section 3: New String Methods (Java 11)
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
        var unicodeSpaces = "\u2003\u2003";  // em spaces (Unicode whitespace)

        System.out.println("\"\"        — isEmpty: " + empty.isEmpty() + ", isBlank: " + empty.isBlank());
        System.out.println("\"   \"     — isEmpty: " + spaces.isEmpty() + ", isBlank: " + spaces.isBlank());
        System.out.println("\"\\t\\t\"    — isEmpty: " + tabs.isEmpty() + ", isBlank: " + tabs.isBlank());
        System.out.println("\"\\n\\n\"    — isEmpty: " + newlines.isEmpty() + ", isBlank: " + newlines.isBlank());
        System.out.println("\"hello\"   — isEmpty: " + text.isEmpty() + ", isBlank: " + text.isBlank());
        System.out.println("em spaces — isEmpty: " + unicodeSpaces.isEmpty() + ", isBlank: " + unicodeSpaces.isBlank());

        // ---- strip() vs trim() ----
        System.out.println("\n--- strip() vs trim() ---");

        // For ASCII whitespace, they behave the same
        var padded = "  hello  ";
        System.out.println("ASCII — trim():  [" + padded.trim() + "]");
        System.out.println("ASCII — strip(): [" + padded.strip() + "]");

        // For Unicode whitespace, strip() removes it but trim() does not
        var unicodePadded = "\u2003hello\u2003";  // em space (U+2003)
        System.out.println("Unicode — trim():  [" + unicodePadded.trim() + "]");
        System.out.println("Unicode — strip(): [" + unicodePadded.strip() + "]");
        System.out.println("  (\\u2003 is em space — strip() removes it, trim() does not)");

        // Another Unicode example: ideographic space (U+3000, used in CJK text)
        var cjkPadded = "\u3000hello\u3000";
        System.out.println("CJK space — trim():  [" + cjkPadded.trim() + "]");
        System.out.println("CJK space — strip(): [" + cjkPadded.strip() + "]");

        // ---- stripLeading() and stripTrailing() ----
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

        // lines() is lazy — works efficiently with large strings
        var lineCount = multiLine.lines().count();
        System.out.println("line count: " + lineCount);

        // Practical: filtering non-blank lines
        var withBlanks = "hello\n   \nworld\n\n  \njava";
        var nonBlankLines = withBlanks.lines()
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
        System.out.println("non-blank lines: " + nonBlankLines);

        // Practical: using Predicate.not() with lines
        var trimmedLines = withBlanks.lines()
                .filter(Predicate.not(String::isBlank))
                .map(String::strip)
                .collect(Collectors.toList());
        System.out.println("trimmed non-blank: " + trimmedLines);

        // lines() handles different line terminators: \n, \r, \r\n
        var mixedEndings = "unix\nwindows\r\nold-mac\rend";
        System.out.println("mixed line endings: " + mixedEndings.lines().collect(Collectors.toList()));

        // ---- repeat() ----
        System.out.println("\n--- repeat() ---");

        System.out.println("\"ab\".repeat(3):  " + "ab".repeat(3));
        System.out.println("\"ha\".repeat(5):  " + "ha".repeat(5));
        System.out.println("\"x\".repeat(0):   [" + "x".repeat(0) + "]  (empty string)");
        System.out.println("\"x\".repeat(1):   " + "x".repeat(1));

        // Practical: creating separators
        var separator = "-".repeat(40);
        System.out.println(separator);
        System.out.println("  formatted output between separators");
        System.out.println(separator);

        // Practical: indentation
        var indent = "  ".repeat(3);  // 6 spaces
        System.out.println(indent + "indented text (3 levels)");

        // Practical: simple text patterns
        for (var i = 1; i <= 5; i++) {
            System.out.println("*".repeat(i));
        }
    }

    // ============================================================
    // Section 4: Files.readString() and Files.writeString() (Java 11)
    // ============================================================

    static void filesReadWriteString() {
        System.out.println("\n=== Files.readString() and Files.writeString() ===");

        try {
            // Create a temporary file for safe demonstration
            var tempFile = Files.createTempFile("jmodern-demo-", ".txt");
            System.out.println("temp file: " + tempFile);

            // ---- Writing a string to a file ----
            var content = "Hello from Java 11!\nThis is line 2.\nAnd line 3.";
            Files.writeString(tempFile, content);
            System.out.println("wrote " + content.length() + " chars to file");

            // ---- Reading the file back ----
            var readBack = Files.readString(tempFile);
            System.out.println("read back:\n" + readBack);

            // Verify round-trip
            System.out.println("round-trip OK: " + content.equals(readBack));

            // ---- Appending to a file ----
            var appendContent = "\nAppended line 4.";
            Files.writeString(tempFile, appendContent, StandardOpenOption.APPEND);
            System.out.println("\nafter APPEND:");
            System.out.println(Files.readString(tempFile));

            // ---- Writing with CREATE_NEW (fails if file exists) ----
            var newFile = tempFile.getParent().resolve("jmodern-new-" + System.nanoTime() + ".txt");
            Files.writeString(newFile, "Brand new file!", StandardOpenOption.CREATE_NEW);
            System.out.println("\nCREATE_NEW file: " + Files.readString(newFile));

            // ---- Comparison: the old way (before Java 11) ----
            // Old way to read:
            //   byte[] bytes = Files.readAllBytes(path);
            //   String content = new String(bytes, StandardCharsets.UTF_8);
            //
            // Old way to write:
            //   Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            //
            // New way (Java 11):
            //   String content = Files.readString(path);
            //   Files.writeString(path, content);
            System.out.println("\n(old way: Files.readAllBytes + new String(bytes, charset))");
            System.out.println("(new way: Files.readString(path) — one line!)");

            // ---- Using with lines() for processing ----
            var fileContent = Files.readString(tempFile);
            var lineList = fileContent.lines()
                    .filter(Predicate.not(String::isBlank))
                    .map(String::strip)
                    .collect(Collectors.toList());
            System.out.println("\nprocessed lines from file: " + lineList);

            // Cleanup temp files
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(newFile);
            System.out.println("temp files cleaned up");

        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }

    // ============================================================
    // Section 5: Other Notable Java 11 Additions
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

        // Practical: cleaner null-checking patterns
        var maybeValue = Optional.ofNullable(System.getProperty("non.existent.property"));
        if (maybeValue.isEmpty()) {
            System.out.println("property not found (checked with isEmpty)");
        }

        // ---- Collection.toArray(IntFunction) ----
        System.out.println("\n--- Collection.toArray(IntFunction) ---");

        var names = List.of("Alice", "Bob", "Charlie");

        // Old way (Java 8):
        String[] oldWay = names.toArray(new String[0]);
        System.out.println("old way: " + Arrays.toString(oldWay));

        // New way (Java 11) — using method reference:
        String[] newWay = names.toArray(String[]::new);
        System.out.println("new way: " + Arrays.toString(newWay));

        // Works with any collection type
        var numbers = Set.of(1, 2, 3, 4, 5);
        Integer[] numArray = numbers.toArray(Integer[]::new);
        System.out.println("set to array: " + Arrays.toString(numArray));

        // ---- Predicate.not() ----
        System.out.println("\n--- Predicate.not() ---");

        var lines = List.of("hello", "", "  ", "world", "\t", "java");

        // Without Predicate.not() — need a lambda
        var withoutNot = lines.stream()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        System.out.println("lambda negation:     " + withoutNot);

        // With Predicate.not() — works with method references
        var withNot = lines.stream()
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
        System.out.println("Predicate.not():     " + withNot);

        // Predicate.not() with other method references
        var mixedNumbers = List.of(1, -2, 3, -4, 5, 0);
        Predicate<Integer> isNegative = n -> n < 0;
        var nonNegative = mixedNumbers.stream()
                .filter(Predicate.not(isNegative))
                .collect(Collectors.toList());
        System.out.println("non-negative numbers: " + nonNegative);

        // Composing with Predicate.not()
        var words = List.of("hello", "Hi", "WORLD", "java", "OK");
        var result = words.stream()
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(s -> s.equals(s.toUpperCase())))
                .collect(Collectors.toList());
        System.out.println("not empty, not all-uppercase: " + result);

        // ---- Pattern.asMatchPredicate() ----
        System.out.println("\n--- Pattern.asMatchPredicate() ---");

        var emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

        // asPredicate() (Java 8) — uses find() (partial match)
        Predicate<String> partialMatch = emailPattern.asPredicate();
        // asMatchPredicate() (Java 11) — uses matches() (full match)
        Predicate<String> fullMatch = emailPattern.asMatchPredicate();

        var testInputs = List.of("alice@example.com", "not-an-email", "prefix alice@example.com suffix");
        System.out.println("email pattern — asPredicate (find) vs asMatchPredicate (matches):");
        for (var input : testInputs) {
            System.out.printf("  %-35s  asPredicate: %-5s  asMatchPredicate: %s%n",
                    "\"" + input + "\"", partialMatch.test(input), fullMatch.test(input));
        }

        // Practical: filtering valid emails from a list
        var candidates = List.of("alice@example.com", "bob@", "charlie@corp.io", "not-email");
        var validEmails = candidates.stream()
                .filter(emailPattern.asMatchPredicate())
                .collect(Collectors.toList());
        System.out.println("valid emails: " + validEmails);

        // ---- Character.toString(int) ----
        System.out.println("\n--- Character.toString(int) ---");

        // New in Java 11 — converts a code point to String directly
        System.out.println("Character.toString(65):    " + Character.toString(65));     // "A"
        System.out.println("Character.toString(9731):  " + Character.toString(9731));   // snowman ☃
        System.out.println("Character.toString(128512): " + Character.toString(128512)); // emoji

        // Old way (before Java 11):
        System.out.println("old way: new String(Character.toChars(9731)): "
                + new String(Character.toChars(9731)));

        // ---- Single-file source-code programs (JEP 330) ----
        System.out.println("\n--- Single-file source-code programs ---");
        System.out.println("Java 11 allows: java MyProgram.java  (no javac step)");
        System.out.println("  - Compiles and runs in a single command");
        System.out.println("  - Great for scripts, prototyping, and learning");
        System.out.println("  - Supports shebang (#!) on Unix: #!/usr/bin/java --source 11");
        System.out.println("  - All classes must be in the single source file");
        System.out.println("  - The first class in the file must have main()");
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        localVariableTypeInference();
        varInLambdaParameters();
        newStringMethods();
        filesReadWriteString();
        otherJava11Additions();
    }
}
