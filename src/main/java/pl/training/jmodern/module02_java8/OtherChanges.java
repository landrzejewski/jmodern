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
// Section 1: Default Methods in Interfaces
// ============================================================

/*
## Default Methods in Interfaces

- Before Java 8, adding a new method to an interface **broke all
  existing implementations**. There was no way to evolve interfaces
  without forcing every implementor to update.
- **Default methods** solve this by allowing interfaces to provide
  a method body using the `default` keyword. Implementing classes
  inherit the default behavior but may **override** it.
- This enabled backward-compatible evolution of core APIs — for
  example, `Collection.forEach()`, `List.sort()`, `Map.getOrDefault()`
  were all added as default methods without breaking existing code.
- **Static methods** in interfaces are also allowed since Java 8.
  They provide utility methods directly on the interface type
  (e.g., `Comparator.comparing()`, `Predicate.not()`).
- **Diamond problem**: if a class implements two interfaces that
  both provide a default method with the same signature, the
  compiler forces the class to **explicitly override** the method
  and resolve the ambiguity. Resolution rules:
    1. **Class wins**: a method defined in a class (or superclass)
       takes priority over any default method.
    2. **Most specific interface wins**: if one interface extends
       another, the sub-interface's default wins.
    3. **Explicit resolution required**: if neither rule applies,
       the class must override and choose via
       `InterfaceName.super.method()`.
- **Interface vs abstract class after Java 8**:
    - Interfaces still cannot have instance fields (state).
    - Interfaces support multiple inheritance of behavior.
    - Abstract classes can have constructors, fields, and non-public methods.
    - Use interfaces for defining types/contracts; abstract classes
      for sharing state and partial implementation among related classes.
*/

// ============================================================
// Section 2: New Date/Time API (java.time)
// ============================================================

/*
## New Date/Time API (java.time)

- The legacy `java.util.Date` and `java.util.Calendar` classes had
  serious design problems:
    - **Mutable** — `Date` objects can be changed after creation,
      leading to bugs in multi-threaded code.
    - **Thread-unsafe** — `SimpleDateFormat` is not thread-safe.
    - **Poor API design** — months are 0-based, year offset from 1900,
      inconsistent naming, no clear separation of date vs time.
- Java 8 introduced the `java.time` package (based on Joda-Time)
  with a clean, **immutable**, **thread-safe** date/time API.
- **Core classes**:
    - `LocalDate` — date without time or timezone (e.g., 2024-03-15).
    - `LocalTime` — time without date or timezone (e.g., 14:30:00).
    - `LocalDateTime` — date + time without timezone.
    - `ZonedDateTime` — date + time + timezone.
    - `Instant` — machine timestamp (seconds + nanoseconds from epoch).
- **Creating instances**: `now()`, `of(...)`, `parse("...")`.
- **Manipulating**: `plusDays()`, `minusHours()`, `withMonth()` — all
  return a **new** instance (immutability).
- **`Period`** — date-based amount (years, months, days).
  **`Duration`** — time-based amount (hours, minutes, seconds, nanos).
- **`DateTimeFormatter`** — thread-safe replacement for `SimpleDateFormat`.
  Use `ofPattern()` for custom formats, or predefined constants
  like `ISO_LOCAL_DATE`.
- **Time zones**: `ZoneId` represents a time zone (e.g., "Europe/Warsaw").
  `ZoneOffset` is a fixed offset from UTC (e.g., "+02:00").
- **Legacy conversion**: `Date.toInstant()`, `Instant.atZone(zone)`,
  `Date.from(instant)` bridge old and new APIs.
- **Temporal adjusters**: `TemporalAdjusters` provides common
  date manipulations like `firstDayOfMonth()`, `nextOrSame(DayOfWeek.MONDAY)`,
  `lastDayOfYear()`.
*/

// ============================================================
// Section 3: StringJoiner
// ============================================================

/*
## StringJoiner

- `java.util.StringJoiner` (Java 8) constructs a sequence of
  characters separated by a delimiter, with optional prefix and suffix.
- **Constructor**: `new StringJoiner(delimiter)` or
  `new StringJoiner(delimiter, prefix, suffix)`.
- **`add(CharSequence)`** — appends an element.
- **`toString()`** — returns the joined string.
- **`setEmptyValue(CharSequence)`** — defines the string returned
  when no elements have been added (default is `prefix + suffix`).
- **`merge(StringJoiner)`** — merges the contents of another joiner
  (without its prefix/suffix) into this one. Useful for parallel
  operations.
- **Related utilities**:
    - `String.join(delimiter, elements)` — static convenience method
      that uses `StringJoiner` internally. Best for simple cases.
    - `Collectors.joining(delimiter, prefix, suffix)` — stream
      collector variant. Best when working with streams.
- **When to use which**:
    - `String.join()` — quick one-liner for arrays/iterables.
    - `StringJoiner` — when building incrementally, need prefix/suffix,
      or need `merge()` for combining results.
    - `Collectors.joining()` — inside stream pipelines.
    - `StringBuilder` — when you need full control (no delimiter
      pattern, complex conditional logic).
*/

// ============================================================
// Section 4: Nashorn JavaScript Engine
// ============================================================

/*
## Nashorn JavaScript Engine

- Java 8 introduced **Nashorn**, a high-performance JavaScript engine
  that replaced the older Rhino engine. It compiled JavaScript to
  Java bytecode for better performance.
- **Deprecation**: Nashorn was deprecated in **Java 11** (JEP 335)
  and **removed in Java 15** (JEP 372). On modern JVMs, the script
  engine may not be available.
- **`ScriptEngineManager`** — factory for obtaining script engines
  by name ("nashorn", "javascript"), MIME type, or file extension.
- **`ScriptEngine.eval(String)`** — evaluates a JavaScript expression
  and returns the result as a Java object.
- **`Bindings`** — a `Map<String, Object>` used to pass Java objects
  to the script as global variables. Use `engine.put(key, value)`
  or create a `Bindings` instance.
- **`Invocable`** — interface for calling JavaScript functions from
  Java. Cast `ScriptEngine` to `Invocable` and use
  `invokeFunction(name, args...)`.
- **Graceful handling**: always check if the engine is `null` before
  use, as it won't be available on Java 15+. Wrap calls in try-catch
  for `ScriptException`.
*/

// ============================================================
// Section 5: Type Annotations
// ============================================================

/*
## Type Annotations

- Before Java 8, annotations could only appear on **declarations**
  (classes, methods, fields, parameters, etc.).
- Java 8 expanded the `@Target` meta-annotation to include two new
  element types: **`ElementType.TYPE_USE`** and
  **`ElementType.TYPE_PARAMETER`**.
- With `TYPE_USE`, annotations can appear wherever a **type** is used:
    - Type casts: `(@NonNull String) obj`
    - `instanceof`: `obj instanceof @NonNull String`
    - Generic type arguments: `List<@NonNull String>`
    - `extends`/`implements`: `class Foo extends @Audited Bar`
    - `throws` clauses: `void m() throws @Critical IOException`
    - Object creation: `new @Interned String("hello")`
    - Array types: `@NonNull String @Nullable []`
- With `TYPE_PARAMETER`, annotations can appear on type parameters:
  `class Box<@NonEmpty T>`.
- **Purpose**: type annotations enable **pluggable type systems**
  and **static analysis** tools (like the Checker Framework) to
  detect errors at compile time — null pointer exceptions,
  concurrency bugs, tainted data, etc.
- Type annotations have **no runtime effect by themselves** — they
  are metadata consumed by annotation processors and static analyzers.
- **Difference from declaration annotations**: declaration annotations
  describe the element itself (e.g., `@Override` on a method);
  type annotations describe the type usage (e.g., `@NonNull` on
  a return type).
*/

// ============================================================
// Section 6: Repeating Annotations
// ============================================================

/*
## Repeating Annotations

- Before Java 8, applying the **same annotation** multiple times
  to a single element was not allowed:
  ```
  @Schedule(day = "Mon")
  @Schedule(day = "Fri")  // compile error before Java 8!
  void backup() {}
  ```
- The workaround was a **container annotation** holding an array:
  `@Schedules({@Schedule(day="Mon"), @Schedule(day="Fri")})`.
- Java 8 introduced **`@Repeatable`** — a meta-annotation that
  declares which container annotation wraps the repeated values.
- **Defining a repeating annotation**:
    1. Create the repeating annotation with `@Repeatable(Container.class)`.
    2. Create the container annotation with a `value()` method
       returning an array of the repeating annotation.
- **Retrieving at runtime**:
    - `getAnnotationsByType(RepeatableAnnotation.class)` — returns
      all instances (unwraps the container automatically).
    - `getAnnotation(Container.class)` — returns the container
      if present.
    - `getDeclaredAnnotationsByType(...)` — same but ignores
      inherited annotations.
- This feature simplifies APIs that naturally allow multiple
  applications: scheduling rules, security roles, validation
  constraints, event listeners, etc.
*/

public class OtherChanges {

    // ---- Helper interfaces for Section 1 (Default Methods) ----

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

    // Diamond problem: both Logging and Auditing have default log()
    // The class MUST override and resolve the conflict
    static class AuditedLogger implements Logging, Auditing {
        @Override
        public void log(String message) {
            // Explicitly choose which default to delegate to
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
        // More specific interface — its default wins over Drawable's
        @Override
        default String draw() {
            return "Drawing resizable shape";
        }

        default String resize(int factor) {
            return "Resized by " + factor + "x";
        }
    }

    // ---- Helper annotations for Section 5 (Type Annotations) ----

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NonNull {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Validated {}

    @Target(ElementType.TYPE_PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface NonEmpty {}

    // ---- Helper annotations for Section 6 (Repeating Annotations) ----

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

    // Annotated methods for Section 6 demonstration
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
    // Section 1: Default Methods in Interfaces
    // ============================================================

    static void defaultMethodsInInterfaces() {
        System.out.println("=== Default Methods in Interfaces ===");

        // Implementing an interface with a default method
        Greeter politeGreeter = name -> "Good day, " + name;
        System.out.println(politeGreeter.greet("Alice"));
        // Using the default method — no need to implement it
        System.out.println(politeGreeter.greetLoudly("Alice"));

        // Overriding the default method
        Greeter casualGreeter = new Greeter() {
            @Override
            public String greet(String name) {
                return "Hey, " + name;
            }

            @Override
            public String greetLoudly(String name) {
                return greet(name) + "!!!"; // custom override
            }
        };
        System.out.println(casualGreeter.greet("Bob"));
        System.out.println(casualGreeter.greetLoudly("Bob"));

        // Static methods on interfaces
        System.out.println(Greeter.defaultGreeting());

        // Diamond problem resolution — AuditedLogger implements both Logging and Auditing
        AuditedLogger logger = new AuditedLogger();
        logger.log("user login"); // calls both Logging.super.log and Auditing.super.log

        // More specific interface wins — Resizable extends Drawable
        Resizable shape = new Resizable() {};
        System.out.println(shape.draw());    // "Drawing resizable shape" — sub-interface wins
        System.out.println(shape.resize(3));

        // Real-world default methods: Collection.forEach, Comparator.comparing
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob"));
        names.forEach(n -> System.out.print("  " + n)); // forEach is a default method on Iterable
        System.out.println();

        // Comparator.comparing — static method + default thenComparing
        names.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("sorted by length then alphabetically: " + names);

        // Map.getOrDefault, Map.putIfAbsent — default methods added in Java 8
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 95);
        System.out.println("getOrDefault (Alice): " + scores.getOrDefault("Alice", 0));
        System.out.println("getOrDefault (Dave): " + scores.getOrDefault("Dave", 0));
    }

    // ============================================================
    // Section 2: New Date/Time API (java.time)
    // ============================================================

    static void dateTimeApi() {
        System.out.println("\n=== New Date/Time API (java.time) ===");

        // LocalDate — date without time or timezone
        LocalDate today = LocalDate.now();
        LocalDate specificDate = LocalDate.of(2024, 3, 15);
        LocalDate parsedDate = LocalDate.parse("2024-12-25");
        System.out.println("today: " + today);
        System.out.println("specific date: " + specificDate);
        System.out.println("parsed date: " + parsedDate);

        // LocalTime — time without date or timezone
        LocalTime now = LocalTime.now();
        LocalTime specificTime = LocalTime.of(14, 30, 0);
        LocalTime parsedTime = LocalTime.parse("09:15:30");
        System.out.println("current time: " + now);
        System.out.println("specific time: " + specificTime);
        System.out.println("parsed time: " + parsedTime);

        // LocalDateTime — date + time without timezone
        LocalDateTime dateTime = LocalDateTime.of(specificDate, specificTime);
        System.out.println("date + time: " + dateTime);

        // Manipulating dates — immutable, returns new instances
        LocalDate tomorrow = today.plusDays(1);
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate withDifferentDay = today.withDayOfMonth(1);
        System.out.println("tomorrow: " + tomorrow);
        System.out.println("last month: " + lastMonth);
        System.out.println("first of this month: " + withDifferentDay);

        // Manipulating times
        LocalTime later = specificTime.plusHours(2).plusMinutes(30);
        System.out.println("14:30 + 2h30m: " + later);

        // Period — date-based amount (years, months, days)
        Period period = Period.between(specificDate, parsedDate);
        System.out.println("period from " + specificDate + " to " + parsedDate + ": " + period);
        System.out.println("  = " + period.getMonths() + " months and " + period.getDays() + " days");

        Period twoWeeks = Period.ofWeeks(2);
        System.out.println("two weeks from today: " + today.plus(twoWeeks));

        // Duration — time-based amount (hours, minutes, seconds, nanos)
        Duration duration = Duration.ofHours(2).plusMinutes(30);
        System.out.println("duration: " + duration);
        System.out.println("duration in minutes: " + duration.toMinutes());

        Duration between = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.println("work day duration: " + between);

        // Instant — machine timestamp (epoch-based)
        Instant instant = Instant.now();
        System.out.println("instant (epoch seconds): " + instant.getEpochSecond());
        System.out.println("instant: " + instant);

        // DateTimeFormatter — formatting and parsing
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formatted = dateTime.format(customFormatter);
        System.out.println("formatted: " + formatted);

        LocalDateTime reparsed = LocalDateTime.parse(formatted, customFormatter);
        System.out.println("reparsed: " + reparsed);

        // Predefined formatters
        System.out.println("ISO_LOCAL_DATE: " + today.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // ZonedDateTime — date + time + timezone
        ZonedDateTime warsawTime = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime tokyoTime = warsawTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
        System.out.println("Warsaw: " + warsawTime.format(DateTimeFormatter.ofPattern("HH:mm z")));
        System.out.println("Tokyo:  " + tokyoTime.format(DateTimeFormatter.ofPattern("HH:mm z")));

        // ZoneId — listing available zones
        System.out.println("available zones (sample): " + ZoneId.getAvailableZoneIds().stream()
                .filter(z -> z.startsWith("Europe/"))
                .sorted()
                .limit(5)
                .collect(Collectors.joining(", ")));

        // Converting from legacy Date
        java.util.Date legacyDate = new java.util.Date();
        Instant fromLegacy = legacyDate.toInstant();
        LocalDateTime converted = fromLegacy.atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println("legacy Date -> LocalDateTime: " + converted);

        // Converting back to legacy Date
        java.util.Date backToLegacy = java.util.Date.from(instant);
        System.out.println("Instant -> legacy Date: " + backToLegacy);

        // Temporal adjusters — common date manipulations
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate nextMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate firstDayOfNextYear = today.with(TemporalAdjusters.firstDayOfNextYear());
        System.out.println("first day of month: " + firstDayOfMonth);
        System.out.println("last day of month: " + lastDayOfMonth);
        System.out.println("next Monday: " + nextMonday);
        System.out.println("first day of next year: " + firstDayOfNextYear);

        // ChronoUnit — measuring distances between temporal objects
        long daysBetween = ChronoUnit.DAYS.between(specificDate, parsedDate);
        System.out.println("days between " + specificDate + " and " + parsedDate + ": " + daysBetween);
    }

    // ============================================================
    // Section 3: StringJoiner
    // ============================================================

    static void stringJoiner() {
        System.out.println("\n=== StringJoiner ===");

        // Basic StringJoiner with delimiter
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("apple");
        joiner.add("banana");
        joiner.add("cherry");
        System.out.println("basic joiner: " + joiner);

        // StringJoiner with delimiter, prefix, and suffix
        StringJoiner jsonArray = new StringJoiner(", ", "[", "]");
        jsonArray.add("\"one\"");
        jsonArray.add("\"two\"");
        jsonArray.add("\"three\"");
        System.out.println("with prefix/suffix: " + jsonArray);

        // Empty joiner — returns prefix + suffix by default
        StringJoiner empty = new StringJoiner(", ", "(", ")");
        System.out.println("empty joiner: " + empty); // "()"

        // setEmptyValue — custom string for empty joiner
        StringJoiner emptyWithDefault = new StringJoiner(", ", "[", "]");
        emptyWithDefault.setEmptyValue("[]  (no elements)");
        System.out.println("empty with setEmptyValue: " + emptyWithDefault);

        // After adding elements, setEmptyValue has no effect
        emptyWithDefault.add("item");
        System.out.println("after add: " + emptyWithDefault);

        // merge — combining two joiners
        StringJoiner fruits = new StringJoiner(", ");
        fruits.add("apple");
        fruits.add("banana");

        StringJoiner vegs = new StringJoiner(", ");
        vegs.add("carrot");
        vegs.add("pea");

        fruits.merge(vegs); // merges contents, not prefix/suffix of the other joiner
        System.out.println("after merge: " + fruits);

        // String.join — static convenience method (uses StringJoiner internally)
        String joined = String.join(" | ", "alpha", "beta", "gamma");
        System.out.println("String.join: " + joined);

        // String.join with a collection
        List<String> items = List.of("one", "two", "three");
        String joinedList = String.join(", ", items);
        System.out.println("String.join (list): " + joinedList);

        // Collectors.joining — stream collector variant
        String streamJoined = items.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(" - ", "<<", ">>"));
        System.out.println("Collectors.joining: " + streamJoined);

        // Practical example: building a SQL IN clause
        List<String> ids = List.of("101", "102", "103", "104");
        StringJoiner inClause = new StringJoiner(", ", "WHERE id IN (", ")");
        ids.forEach(inClause::add);
        System.out.println("SQL IN clause: " + inClause);

        // Practical example: building CSV line
        StringJoiner csv = new StringJoiner(",");
        csv.add("John");
        csv.add("Doe");
        csv.add("30");
        csv.add("Warsaw");
        System.out.println("CSV line: " + csv);
    }

    // ============================================================
    // Section 4: Nashorn JavaScript Engine
    // ============================================================

    static void nashornJavaScriptEngine() {
        System.out.println("\n=== Nashorn JavaScript Engine ===");

        // Nashorn was introduced in Java 8, deprecated in Java 11, removed in Java 15.
        // On modern JVMs, the engine may not be available.
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        // Fallback: try "javascript" if "nashorn" is not found
        if (engine == null) {
            engine = manager.getEngineByName("javascript");
        }

        if (engine == null) {
            System.out.println("  No JavaScript engine available (expected on Java 15+).");
            System.out.println("  Nashorn was deprecated in Java 11 and removed in Java 15.");
            System.out.println("  Alternatives: GraalJS (GraalVM), or standalone JS runtimes.");

            // Show available engines
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
            // Evaluating simple JavaScript expressions
            Object result = engine.eval("1 + 2");
            System.out.println("  eval('1 + 2'): " + result);

            result = engine.eval("'Hello'.length");
            System.out.println("  eval(\"'Hello'.length\"): " + result);

            // Passing Java objects to JavaScript via Bindings
            engine.put("name", "Java");
            engine.put("version", 8);
            result = engine.eval("'Hello from ' + name + ' ' + version");
            System.out.println("  with bindings: " + result);

            // Evaluating a multi-line script
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

            // Calling JavaScript functions from Java using Invocable
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
    // Section 5: Type Annotations
    // ============================================================

    // Example: method with type annotations
    static @NonNull String getGreeting(@NonNull String name) {
        return "Hello, " + name;
    }

    // Example: generic class with type parameter annotation
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

        // Type annotation on variable declaration
        @NonNull String message = "This is annotated as non-null";
        System.out.println("annotated variable: " + message);

        // Type annotation on method return type (see getGreeting above)
        System.out.println("annotated method: " + getGreeting("Alice"));

        // Type annotation on generic type argument
        List<@NonNull String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        System.out.println("annotated generics: " + names);

        // Type annotation on type parameter (see Box class above)
        Box<@NonNull String> box = new Box<>("contents");
        System.out.println("annotated type parameter: " + box.getValue());

        // Type annotation on cast
        Object obj = "hello";
        String casted = (@NonNull String) obj;
        System.out.println("annotated cast: " + casted);

        // Type annotation on array creation
        @NonNull String @Validated [] array = new @NonNull String[3];
        array[0] = "first";
        array[1] = "second";
        array[2] = "third";
        System.out.println("annotated array: " + Arrays.toString(array));

        // Inspecting type annotations via reflection
        try {
            Method method = OtherChanges.class.getDeclaredMethod("getGreeting", String.class);
            Annotation[] returnAnnotations = method.getAnnotatedReturnType().getAnnotations();
            System.out.println("annotations on return type of getGreeting():");
            for (Annotation a : returnAnnotations) {
                System.out.println("  " + a);
            }

            // Check parameter type annotations
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

        // Defining and using custom type annotations for static analysis
        System.out.println("  (Type annotations are metadata — they enable tools like");
        System.out.println("   the Checker Framework to detect null pointer errors,");
        System.out.println("   concurrency bugs, and tainted data at compile time.)");
    }

    // ============================================================
    // Section 6: Repeating Annotations
    // ============================================================

    static void repeatingAnnotations() {
        System.out.println("\n=== Repeating Annotations ===");

        // Retrieve repeating @Schedule annotations from performBackup()
        try {
            Method backupMethod = OtherChanges.class.getDeclaredMethod("performBackup");

            // getAnnotationsByType — unwraps the container automatically
            Schedule[] schedules = backupMethod.getAnnotationsByType(Schedule.class);
            System.out.println("@Schedule annotations on performBackup():");
            for (Schedule s : schedules) {
                System.out.println("  day=" + s.day() + ", task=" + s.task());
            }

            // getAnnotation with the container type
            Schedules container = backupMethod.getAnnotation(Schedules.class);
            if (container != null) {
                System.out.println("container annotation present: @Schedules with "
                        + container.value().length + " entries");
            }

            // Retrieve repeating @Role annotations from manageDatabase()
            Method dbMethod = OtherChanges.class.getDeclaredMethod("manageDatabase");
            Role[] roles = dbMethod.getAnnotationsByType(Role.class);
            System.out.println("@Role annotations on manageDatabase():");
            for (Role r : roles) {
                System.out.println("  role=" + r.value());
            }

            // Check if the container is present
            Roles rolesContainer = dbMethod.getAnnotation(Roles.class);
            if (rolesContainer != null) {
                System.out.println("container annotation present: @Roles with "
                        + rolesContainer.value().length + " entries");
            }

        } catch (NoSuchMethodException e) {
            System.out.println("  reflection error: " + e.getMessage());
        }

        // Practical usage: simulating a scheduler that reads annotations
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
    // Main — run all sections
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
