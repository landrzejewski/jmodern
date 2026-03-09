package pl.training.jmodern.module02_java25;

import module java.base;           // Section 5 demo — replaces granular imports
import java.lang.classfile.*;       // Section 8 — Class-File API
import java.lang.classfile.attribute.*;

// ============================================================
// Section 1: String Templates (Withdrawn)
// ============================================================

/*
## String Templates (Withdrawn)

- **JEP 430** (preview Java 21) and **JEP 459** (preview Java 22)
  introduced String Templates with template processors.
- **Syntax**: STR."Hello \{name}" — embedded expressions inside
  string literals, processed by a template processor.
- **Template processors**: STR (interpolation), FMT (formatting),
  RAW (raw template access). Custom processors could be defined
  for SQL, JSON, etc.
- **Why withdrawn**: The design had several concerns:
    - Implicit STR import was controversial
    - The processor model was overly complex for common cases
    - Type safety trade-offs with the processor return type
    - Interaction with raw string literals was unclear
- **Status**: Removed after Java 22 previews. May return in a
  completely different form in future JDKs.
- **For now**: Use String.formatted(), MessageFormat, or
  StringBuilder for string interpolation needs.
*/

// ============================================================
// Section 2: Implicitly Declared Classes and Instance Main Methods
// ============================================================

/*
## Implicitly Declared Classes and Instance Main Methods

- **JEP 463** (preview Java 21) → **JEP 477** (finalized Java 23)
- **Before**: Every Java program required:
    public class Main {
        public static void main(String[] args) { ... }
    }
- **After**: A file can contain just:
    void main() { ... }
  No class declaration, no public, no static, no String[] args.
- **Instance main methods**: void main() runs as an instance
  method. The JVM creates an instance of the implicit class
  and calls main() on it.
- **Compact Source Files**: Single-file programs can be launched
  directly: java MyApp.java — no separate compilation needed.
- **java.io.IO class** (planned): A console API with println(),
  print(), readln() — designed for beginners. Not yet available
  in current JDK builds.
- **Launch protocol priority**:
    1. static void main(String[] args)
    2. static void main()
    3. void main(String[] args) (instance)
    4. void main() (instance)
- **Use case**: Teaching Java to beginners, scripting, small
  utilities. Removes ceremonial boilerplate for simple programs.
- **Cannot demo inside a regular class** — the feature requires
  a file without an explicit class declaration.
*/

// ============================================================
// Section 3: Primitive Types in Pattern Matching
// ============================================================

/*
## Primitive Types in Pattern Matching

- **JEP 455** (preview Java 23, continuing through Java 25)
  extends pattern matching to support primitive types.
- **Before**: switch on primitives only supported exact-type
  constant cases (case 1, case 2, etc.). Type patterns like
  case int i were not allowed.
- **Now**: Type patterns with primitives work in both switch
  and instanceof expressions:
    - case int i — matches and binds an int value
    - case double d — matches and binds a double value
    - obj instanceof int i — extracts int from Object
- **Conversions in patterns**:
    - Widening: int matched as long (always safe)
    - Narrowing: long matched as int (only if value fits)
    - Unboxing: Integer matched as int
    - Boxing: int matched as Integer
- **Guards**: when keyword works with primitive patterns:
    case int i when i > 0 -> "positive"
- **Practical benefit**: Enables uniform switch-on-Object that
  handles both reference and primitive types together, without
  manual unboxing or type checking.
*/

// ============================================================
// Section 4: Flexible Constructor Bodies
// ============================================================

/*
## Flexible Constructor Bodies

- **JEP 482** (preview Java 24, continuing in Java 25) allows
  statements before the super() or this() call in constructors.
- **Before**: super(...) or this(...) MUST be the very first
  statement in a constructor. No validation, no computation,
  no logging before the super call.
- **Now**: Code before super() is permitted, enabling:
    - Argument validation (throw early if invalid)
    - Value computation (derive values to pass to super)
    - Logging / diagnostics (trace construction)
- **Restriction**: You cannot access `this` (instance fields
  or methods) before super() completes. Only static members,
  parameters, and local variables are accessible.
- **Rationale**: Eliminates awkward workarounds like static
  helper methods just to validate or compute before super().
*/

// ============================================================
// Section 5: Module Import Declarations
// ============================================================

/*
## Module Import Declarations

- **JEP 476** (preview Java 23, continuing in Java 25) adds
  a new import form: import module <module-name>;
- **import module java.base;** imports ALL public top-level
  types exported by the java.base module. This includes:
    - java.util.* (List, Map, Set, etc.)
    - java.util.stream.* (Stream, Collectors, Gatherers)
    - java.util.function.* (Function, Predicate, etc.)
    - java.io.* (InputStream, OutputStream, etc.)
    - java.nio.file.* (Path, Files, etc.)
    - java.time.* (Instant, Duration, LocalDate, etc.)
    - java.util.regex.* (Pattern, Matcher)
    - java.util.concurrent.* (CompletableFuture, etc.)
    - ... and many more packages from java.base
- **This file uses it**: The import module java.base; at the
  top replaces what would be dozens of individual imports.
- **Works alongside specific imports**: e.g., import java.lang.classfile.*
  can coexist with import module java.base;
- **Ambiguity resolution**: If two modules export the same
  simple type name, you must add an explicit import to resolve.
*/

// ============================================================
// Section 6: Stream Gatherers (JEP 485, Java 24)
// ============================================================

/*
## Stream Gatherers

- The Stream API (Java 8) provided a fixed set of intermediate
  operations (map, filter, flatMap, etc.) with no way to define
  custom ones. While Collector solved custom terminal operations,
  there was no equivalent for intermediate operations.
- **JEP 485 (Java 24)** finalizes Stream Gatherers (previewed in
  Java 22 via JEP 461 and Java 23 via JEP 473).
- **`stream.gather(Gatherer)`** is a new intermediate operation
  that transforms stream elements using a user-defined strategy.
- **Gatherer components** (similar to Collector's structure):
    - initializer: supplies initial private state (optional)
    - integrator: processes each element, may push to downstream
    - combiner: merges state for parallel streams (optional)
    - finisher: runs after all elements are consumed (optional)
- **Built-in Gatherers** in java.util.stream.Gatherers:
    - windowFixed(int size) -- non-overlapping fixed-size windows
    - windowSliding(int size) -- overlapping sliding windows
    - fold(Supplier, BiFunction) -- reduces to single element
    - scan(Supplier, BiFunction) -- running accumulation
    - mapConcurrent(int, Function) -- bounded-concurrency map
- **Compose** via gatherer.andThen(anotherGatherer) for chaining
  custom intermediate operations.
- **Custom Gatherers**: Use Gatherer.ofSequential(initializer,
  integrator) or Gatherer.of(...) for parallel-capable versions.
  This makes the Stream API extensible for the first time.
*/

// ============================================================
// Section 7: Class-File API (JEP 484, Java 24)
// ============================================================

/*
## Class-File API (Programmatic Class File Parsing)

- **JEP 457** (preview Java 22), **JEP 466** (preview Java 23),
  **JEP 484** (finalized Java 24).
- **java.lang.classfile.ClassFile** — a standard API to read,
  transform, and generate .class files programmatically.
- **Replacement for ASM**: Before this API, bytecode manipulation
  required third-party libraries (ASM, Javassist, ByteBuddy).
  Now the JDK itself provides a standard, maintained API.
- **Key types**:
    - ClassFile — entry point for parsing and generating
    - ClassModel — represents a parsed .class file
    - MethodModel, FieldModel — methods and fields
    - Attributes — access class file attributes
- **Use cases**:
    - Framework bytecode generation (proxies, AOP)
    - Build tool analysis (dependency scanning)
    - IDE support (class structure inspection)
    - Educational tools (exploring bytecode)
- **Advantage over ASM**: Versioned with the JDK, always supports
  the latest class file format. No version lag or compatibility
  issues with new JDK releases.
*/

// ============================================================
// Section 8: Ahead-of-Time Class Loading & Linking (Project Leyden)
// ============================================================

/*
## Ahead-of-Time Class Loading & Linking (Project Leyden)

- **JEP 483 (Java 24)**: AOT Cache — premade class loading and
  linking decisions stored in a shared archive.
- **Three-step workflow**:
    1. java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp app.jar com.example.Main
       → Records class loading/linking decisions during a training run
    2. java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp app.jar
       → Creates the AOT cache from recorded data
    3. java -XX:AOTMode=on -XX:AOTCache=app.aot -cp app.jar com.example.Main
       → Runs with the pre-built cache for faster startup
- **Startup improvement**: Classes are loaded from the shared
  archive instead of classpath scanning and verification at
  each startup. This can significantly reduce startup time.
- **Project Leyden's broader vision**: Shift work from runtime
  to earlier phases (build time, first-run, AOT compilation).
  AOT Cache is one step; future JEPs may add more aggressive
  ahead-of-time optimizations.
- **Related**: Builds on CDS (Class Data Sharing) technology
  that has been in the JVM for years, but makes it much more
  practical and automated.
- **Not an API** — purely JVM flags and tooling. No code to
  demonstrate; it's a deployment/operations concern.
*/

// ============================================================
// Section 9: Writing Simple Scripts
// ============================================================

/*
## Writing Simple Scripts

- **Compact Source Files**: java MyScript.java — launch a Java
  source file directly without a separate compilation step.
  The JVM compiles it on the fly.
- **Instance main methods** (Section 2 recap): void main() { }
  is all you need in a compact source file. No class, no static,
  no args parameter.
- **Shebang support on Unix**:
    #!/usr/bin/env java --source 25 --enable-preview
    void main() {
        System.out.println("Hello from a script!");
    }
  Make the file executable (chmod +x) and run it directly.
- **Multi-file source programs**: java --source 25 Main.java
  can reference other .java files in the same directory. The
  launcher resolves and compiles them together.
- **Comparison with JShell**:
    - JShell: interactive REPL, great for experimentation
    - Source launcher: runs complete programs as scripts
    - Both avoid explicit compilation, but serve different purposes
- **Comparison with Groovy scripting**: Java's source launcher
  now covers many use cases that previously required Groovy or
  other JVM scripting languages. The gap has narrowed significantly.
*/

public class OtherChanges {

    // ---- Inner types for flexible constructor bodies demo ----

    static class Base {
        final String value;

        Base(String value) {
            this.value = value;
            System.out.println("    Base constructor called with: " + value);
        }
    }

    static class ValidatedSub extends Base {
        ValidatedSub(String value) {
            // Code before super() — JEP 482
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
            // Compute derived value before super()
            var computed = "sum=" + (x + y) + ",product=" + (x * y);
            System.out.println("    Pre-super computation: " + computed);
            super(computed);
        }
    }

    static class LoggedSub extends Base {
        LoggedSub(String value) {
            // Logging / diagnostics before super()
            System.out.println("    [LOG] Constructing LoggedSub with: \"" + value + "\" on thread " + Thread.currentThread().getName());
            super(value);
        }
    }

    // Reused from java21 OtherChanges for Gatherers
    record Person(String name, int age) {}

    // ============================================================
    // Section 1: String Templates (Withdrawn)
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
    // Section 2: Implicitly Declared Classes and Instance Main Methods
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
    // Section 3: Primitive Types in Pattern Matching
    // ============================================================

    static void primitiveTypesInPatternMatching() {
        System.out.println("\n=== Section 3: Primitive Types in Pattern Matching (JEP 455) ===");

        // ---- Demo 1: Switch on Object with primitive patterns ----
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

        // ---- Demo 2: Primitive conversions and guards ----
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

        // ---- Demo 3: instanceof with primitives ----
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
    // Section 4: Flexible Constructor Bodies
    // ============================================================

    static void flexibleConstructorBodies() {
        System.out.println("\n=== Section 4: Flexible Constructor Bodies (JEP 482) ===");

        // ---- Demo 1: Validation before super() ----
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

        // ---- Demo 2: Computation before super() ----
        System.out.println("\n--- Demo 2: Computation before super() ---");
        System.out.println("  Creating ComputedSub(3, 7):");
        var computed = new ComputedSub(3, 7);
        System.out.println("  Result: value = \"" + computed.value + "\"");

        System.out.println("\n  Creating ComputedSub(10, 20):");
        var computed2 = new ComputedSub(10, 20);
        System.out.println("  Result: value = \"" + computed2.value + "\"");

        // ---- Demo 3: Logging before super() ----
        System.out.println("\n--- Demo 3: Logging / side-effects before super() ---");
        System.out.println("  Creating LoggedSub(\"test-value\"):");
        var logged = new LoggedSub("test-value");
        System.out.println("  Result: value = \"" + logged.value + "\"");
        System.out.println("  Note: log output appeared BEFORE Base constructor ran");
    }

    // ============================================================
    // Section 5: Module Import Declarations
    // ============================================================

    static void moduleImportDeclarations() {
        System.out.println("\n=== Section 5: Module Import Declarations (JEP 476) ===");

        // ---- Demo 1: Using types without explicit imports ----
        System.out.println("--- Demo 1: Types available via import module java.base ---");
        System.out.println("  This file uses: import module java.base;");
        System.out.println("  All of these types are available without individual imports:");

        // Collections
        List<String> list = List.of("a", "b", "c");
        Map<String, Integer> map = Map.of("x", 1, "y", 2);
        Set<Integer> set = Set.of(1, 2, 3);
        System.out.println("    List:   " + list);
        System.out.println("    Map:    " + map);
        System.out.println("    Set:    " + set);

        // Time
        Instant now = Instant.now();
        Duration duration = Duration.ofMinutes(5);
        System.out.println("    Instant.now():      " + now);
        System.out.println("    Duration.ofMinutes: " + duration);

        // NIO
        Path path = Path.of("src", "main", "java");
        System.out.println("    Path.of():          " + path);

        // Regex
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher("abc123def456");
        List<String> matches = new ArrayList<>();
        while (matcher.find()) matches.add(matcher.group());
        System.out.println("    Pattern/Matcher:    found " + matches + " in \"abc123def456\"");

        // Concurrent
        CompletableFuture<String> future = CompletableFuture.completedFuture("done");
        System.out.println("    CompletableFuture:  " + future.join());

        // Functional
        Function<String, Integer> strlen = String::length;
        Predicate<Integer> isPositive = n -> n > 0;
        System.out.println("    Function<String,Integer>: \"hello\" -> " + strlen.apply("hello"));
        System.out.println("    Predicate<Integer>: 42 -> " + isPositive.test(42));

        // ---- Demo 2: Show available modules ----
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
    // Section 6: Stream Gatherers (JEP 485, Java 24)
    // ============================================================

    static void streamGatherers() {
        System.out.println("\n=== Section 6: Stream Gatherers (JEP 485, Java 24) ===");

        // ---- windowFixed: batch processing ----
        System.out.println("--- windowFixed: batch processing ---");
        var numbers = List.of(1, 2, 3, 4, 5, 6, 7);
        var fixedWindows = numbers.stream()
                .gather(Gatherers.windowFixed(3))
                .toList();
        System.out.println("  Input:  " + numbers);
        System.out.println("  windowFixed(3): " + fixedWindows);

        // ---- windowSliding: overlapping windows ----
        System.out.println("\n--- windowSliding: overlapping windows ---");
        var slidingWindows = numbers.stream()
                .gather(Gatherers.windowSliding(3))
                .toList();
        System.out.println("  Input:  " + numbers);
        System.out.println("  windowSliding(3): " + slidingWindows);

        // ---- Moving average using sliding windows ----
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

        // ---- scan: running totals ----
        System.out.println("\n--- scan: running totals ---");
        var runningTotals = Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.scan(() -> 0, Integer::sum))
                .toList();
        System.out.println("  Input:  [1, 2, 3, 4, 5]");
        System.out.println("  scan:   " + runningTotals);

        // ---- fold: single result ----
        System.out.println("\n--- fold: reduce to single result ---");
        var folded = Stream.of("a", "b", "c")
                .gather(Gatherers.fold(() -> "", (acc, el) -> acc.isEmpty() ? el : acc + "-" + el))
                .toList();
        System.out.println("  Input:  [a, b, c]");
        System.out.println("  fold:   " + folded);

        // ---- Custom Gatherer: distinctBy ----
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

        // ---- mapConcurrent: bounded-concurrency mapping ----
        System.out.println("\n--- mapConcurrent: bounded-concurrency mapping ---");
        var urls = List.of("page-1", "page-2", "page-3", "page-4", "page-5");
        var startTime = System.currentTimeMillis();
        var results = urls.stream()
                .gather(Gatherers.mapConcurrent(3, url -> {
                    try {
                        Thread.sleep(100); // Simulate I/O
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
    // Section 7: Class-File API (JEP 484, Java 24)
    // ============================================================

    static void classFileApi() throws Exception {
        System.out.println("\n=== Section 7: Class-File API (JEP 484, Java 24) ===");

        // ---- Demo 1: Parse String.class ----
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

        // ---- Demo 2: List methods of a parsed class ----
        System.out.println("\n--- Demo 2: List methods of String.class (first 15) ---");
        methods.stream()
                .limit(15)
                .forEach(m -> System.out.println("    " + m.methodName().stringValue()
                        + m.methodType().stringValue()));

        // ---- Demo 3: Inspect own class ----
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

        // List inner classes from the InnerClasses attribute
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
    // Section 8: Ahead-of-Time Class Loading & Linking (Project Leyden)
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
    // Section 9: Writing Simple Scripts
    // ============================================================

    static void writingSimpleScripts() {
        System.out.println("\n=== Section 9: Writing Simple Scripts ===");

        System.out.println("  Compact Source Files: java MyScript.java — no compilation step needed.");
        System.out.println("  Combined with instance main methods (Section 2):");
        System.out.println("    // MyScript.java (entire file)");
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
    // Main -- run all sections
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
