package pl.training.jmodern.module02_java17;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction to Records
// ============================================================

/*
## Introduction to Records

- In traditional Java, creating a simple data carrier class requires
  a lot of **boilerplate**: constructor, getters, equals(), hashCode(),
  toString(). A simple class with 3 fields can easily be 50+ lines.
- **Other languages solved this long ago**:
    - Kotlin: `data class Point(val x: Double, val y: Double)`
    - Scala: `case class Point(x: Double, y: Double)`
    - C#: `record Point(double X, double Y);`
- **JEP timeline**:
    - JEP 359: Preview in Java 14
    - JEP 384: Second preview in Java 15
    - JEP 395: Finalized in Java 16 (taught with Java 17 LTS)
- **Basic syntax**: `record Point(double x, double y) {}`
    - The parenthesized list is called the **record header** or
      **component list**. Each entry is a **record component**.
    - The compiler automatically generates:
        - A `private final` field for each component
        - A **canonical constructor** that assigns all fields
        - **Accessor methods** named after components (`x()`, `y()`)
          — NOT `getX()` / `getY()`
        - `equals()` based on all components
        - `hashCode()` based on all components
        - `toString()` in the format `Point[x=1.0, y=2.0]`
- Records are **implicitly `final`** — they cannot be extended.
- Records **implicitly extend `java.lang.Record`** — they cannot
  extend any other class (but can implement interfaces).
- Records are **transparent data carriers**: their API is fully
  determined by their state description (the component list).
*/

// ============================================================
// Section 2: Anatomy of a Record
// ============================================================

/*
## Anatomy of a Record

- **Record components** become `private final` fields. You cannot
  add additional instance fields to a record — this is enforced
  by the compiler.
- **Canonical constructor**: The compiler generates a constructor
  that takes all components in order:
      new RGB(255, 128, 0)
- **Compact constructor**: A shorthand syntax where you omit the
  parameter list and the final assignments:
      record RGB(int red, int green, int blue) {
          RGB { // ← compact constructor (no parentheses)
              if (red < 0 || red > 255) throw new IllegalArgumentException();
              // assignments happen automatically at the end
          }
      }
- **Accessor methods** are named after the components:
    - `rgb.red()` — NOT `rgb.getRed()`
    - This follows the pattern established by `Map.Entry.getKey()`
      being renamed to `key()` in newer APIs
- **Auto-generated equals/hashCode**: Two records are equal if and
  only if all their components are equal. This uses `Objects.equals()`
  for reference types and `==` for primitives.
- **Auto-generated toString()**: Produces a string in the format
  `TypeName[comp1=value1, comp2=value2, ...]`
- **You can override** any auto-generated method (toString, equals,
  hashCode, accessors) — the compiler only generates what you don't
  provide yourself.
*/

// ============================================================
// Section 3: Custom Constructors and Validation
// ============================================================

/*
## Custom Constructors and Validation

- **Compact constructor** is the most common way to add validation
  or normalization to a record. It runs before the implicit field
  assignments:
      record Temperature(double value, String unit) {
          Temperature {   // compact constructor
              unit = unit.trim().toUpperCase();
              if (!Set.of("C", "F", "K").contains(unit))
                  throw new IllegalArgumentException("Unknown unit: " + unit);
          }
      }
- **Custom canonical constructor**: You can also write the full
  constructor with parameters, but then you must assign ALL fields:
      record Temperature(double value, String unit) {
          Temperature(double value, String unit) {
              this.value = value;
              this.unit = unit.trim().toUpperCase();
          }
      }
- **Additional constructors**: You can define overloaded constructors
  but they must delegate to the canonical constructor via `this(...)`:
      Temperature(double value) { this(value, "C"); }
- **Defensive copies**: For mutable components (List, Date, arrays),
  you should make defensive copies in the compact constructor:
      record Tags(String name, List<String> values) {
          Tags { values = List.copyOf(values); }
      }
  This ensures the record remains effectively immutable even if the
  caller modifies the original list after construction.
*/

// ============================================================
// Section 4: Records and Interfaces — Generic Records
// ============================================================

/*
## Records and Interfaces — Generic Records

- Records **can implement interfaces** — this is their primary
  extension mechanism since they cannot extend other classes.
- This enables **polymorphism with data transparency**: you get
  the benefits of an interface contract (Comparable, Serializable,
  custom interfaces) with automatic data-class behavior.
- **Generic records**: Records can have type parameters just like
  classes: `record Pair<A, B>(A first, B second) {}`
    - Generic records are useful for DTOs, value objects, tuples,
      and result wrappers.
- Records **can have**:
    - Static fields and methods
    - Instance methods (beyond accessors)
    - Nested types (classes, interfaces, enums, records)
- Records **cannot have**:
    - Additional instance fields (only component fields)
    - A superclass other than `java.lang.Record`
- **Records as Map keys**: Because records have structural equality
  (equals/hashCode based on all components), they make excellent
  Map keys — two records with the same components always hash to
  the same bucket and compare as equal.
*/

// ============================================================
// Section 5: Records with Pattern Matching
// ============================================================

/*
## Records with Pattern Matching

- **`instanceof` pattern matching** (JEP 394, Java 16):
      if (obj instanceof Point p) { ... p.x() ... }
  Records work naturally with instanceof — no need for casting.
- **Switch pattern matching** (JEP 441, Java 21):
      switch (shape) { case Point p -> ...; case Line l -> ...; }
  The compiler can check exhaustiveness with sealed types.
- **Record deconstruction patterns** (JEP 440, Java 21):
      case Point(var x, var y) -> ...
  Instead of binding the whole record and calling accessors, you
  can deconstruct it directly in the pattern — extracting components
  into variables.
- **Nested record patterns**: Deconstruction can be nested:
      case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...
  This deconstructs the Line into its two Points, and each Point
  into its coordinates — all in a single pattern.
- **Guarded patterns with `when`**:
      case Point(var x, var y) when x > 0 && y > 0 -> "Q1"
  Combines deconstruction with a boolean guard.
- **Records + sealed + pattern matching** = the **ADT trio** in
  modern Java. Sealed interfaces define the sum type, records
  define the product types, and pattern matching provides
  exhaustive, type-safe decomposition.
*/

// ============================================================
// Section 6: Practical Patterns and Limitations
// ============================================================

/*
## Practical Patterns and Limitations

- **When to use records vs classes**:
    - Use records for **data carriers**: DTOs, value objects, tuples,
      API responses, configuration, events, commands.
    - Use classes when you need: mutable state, inheritance, or
      additional instance fields beyond the record components.
- **Limitations of records**:
    - Cannot extend a class (already extend `java.lang.Record`)
    - Cannot have mutable fields (all components are `final`)
    - Cannot declare additional instance fields
    - Are implicitly `final` (cannot be subclassed)
    - Cannot be `abstract`
- **Local records**: Records can be declared inside methods, which
  is particularly useful for intermediate types in stream pipelines:
      record NameAge(String name, int age) {}
      list.stream().map(p -> new NameAge(p.name(), p.age()))...
- **Serialization**: Records use the canonical constructor for
  deserialization (not reflection-based field injection like regular
  classes). This makes record serialization more predictable and
  secure — the same validation that runs during construction also
  runs during deserialization.
- **Builder pattern**: For records with many components, a builder
  provides a more readable construction API with named setters
  and optional defaults.
*/

public class Records {

    // ---- Section 1: Basic records ----

    record Point(double x, double y) {}
    record FullName(String first, String last) {}

    // ---- Section 2: Anatomy ----

    record RGB(int red, int green, int blue) {}

    // ---- Section 3: Constructors and validation ----

    record Temperature(double value, String unit) {
        Temperature {
            unit = unit.trim().toUpperCase();
            if (!Set.of("C", "F", "K").contains(unit)) {
                throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        Temperature(double value) {
            this(value, "C");
        }

        double toCelsius() {
            return switch (unit) {
                case "C" -> value;
                case "F" -> (value - 32) * 5.0 / 9.0;
                case "K" -> value - 273.15;
                default -> throw new IllegalStateException("Unknown unit: " + unit);
            };
        }
    }

    record Tags(String name, List<String> values) {
        Tags {
            values = List.copyOf(values);
        }
    }

    record Range(int from, int to) {
        Range {
            if (from > to) {
                throw new IllegalArgumentException("from (" + from + ") must be <= to (" + to + ")");
            }
        }

        int length() {
            return to - from;
        }

        boolean contains(int value) {
            return value >= from && value <= to;
        }
    }

    // ---- Section 4: Interfaces and generics ----

    interface Printable {
        String prettyPrint();
    }

    record Money(double amount, String currency) implements Printable, Comparable<Money> {
        @Override
        public String prettyPrint() {
            return String.format("%.2f %s", amount, currency);
        }

        @Override
        public int compareTo(Money other) {
            if (!this.currency.equals(other.currency)) {
                throw new IllegalArgumentException("Cannot compare different currencies: " + currency + " vs " + other.currency);
            }
            return Double.compare(this.amount, other.amount);
        }
    }

    record Pair<A, B>(A first, B second) {
        <C> Pair<C, B> mapFirst(Function<A, C> fn) {
            return new Pair<>(fn.apply(first), second);
        }

        <C> Pair<A, C> mapSecond(Function<B, C> fn) {
            return new Pair<>(first, fn.apply(second));
        }
    }

    // ---- Section 5: Pattern matching with records ----

    record Line(Point start, Point end) {}
    record ColoredPoint(Point point, String color) {}

    // ---- Section 6: Builder pattern for records ----

    record PersonRecord(String name, int age, String email, String phone, String city) {
        static class Builder {
            private String name;
            private int age;
            private String email = "";
            private String phone = "";
            private String city = "";

            Builder name(String name) { this.name = name; return this; }
            Builder age(int age) { this.age = age; return this; }
            Builder email(String email) { this.email = email; return this; }
            Builder phone(String phone) { this.phone = phone; return this; }
            Builder city(String city) { this.city = city; return this; }

            PersonRecord build() {
                Objects.requireNonNull(name, "name is required");
                return new PersonRecord(name, age, email, phone, city);
            }
        }
    }

    // ============================================================
    // Section 1: Introduction to Records
    // ============================================================

    static void introductionToRecords() {
        System.out.println("=== Section 1: Introduction to Records ===");

        // Create record instances
        var point = new Point(3.0, 4.0);
        var name = new FullName("John", "Doe");

        // toString() is auto-generated
        System.out.println("point: " + point);
        System.out.println("name:  " + name);

        // Accessor methods are named after components — NOT getX()/getY()
        System.out.println("\n--- Accessor methods (point.x(), not point.getX()) ---");
        System.out.println("point.x() = " + point.x());
        System.out.println("point.y() = " + point.y());
        System.out.println("name.first() = " + name.first());
        System.out.println("name.last() = " + name.last());

        // equals() and hashCode() — structural equality
        System.out.println("\n--- equals() and hashCode() ---");
        var point2 = new Point(3.0, 4.0);
        var point3 = new Point(1.0, 2.0);
        System.out.println("point.equals(point2) [same values]: " + point.equals(point2));
        System.out.println("point.equals(point3) [different values]: " + point.equals(point3));
        System.out.println("point.hashCode() == point2.hashCode(): " + (point.hashCode() == point2.hashCode()));

        // Records extend java.lang.Record
        System.out.println("\n--- Records extend java.lang.Record ---");
        System.out.println("point instanceof Record: " + (point instanceof Record));
        System.out.println("Point superclass: " + Point.class.getSuperclass().getName());
        System.out.println("Point is final: " + Modifier.isFinal(Point.class.getModifiers()));
        System.out.println("Point isRecord(): " + Point.class.isRecord());
    }

    // ============================================================
    // Section 2: Anatomy of a Record
    // ============================================================

    static void anatomyOfARecord() {
        System.out.println("\n=== Section 2: Anatomy of a Record ===");

        // Create RGB instances
        var red = new RGB(255, 0, 0);
        var custom = new RGB(255, 128, 0);

        // toString format: RGB[red=255, green=128, blue=0]
        System.out.println("red:    " + red);
        System.out.println("custom: " + custom);

        // Accessors
        System.out.println("\n--- Accessor methods ---");
        System.out.println("custom.red()   = " + custom.red());
        System.out.println("custom.green() = " + custom.green());
        System.out.println("custom.blue()  = " + custom.blue());

        // equals consistency
        System.out.println("\n--- equals consistency ---");
        var custom2 = new RGB(255, 128, 0);
        var different = new RGB(0, 128, 255);
        System.out.println("custom.equals(custom2) [same components]: " + custom.equals(custom2));
        System.out.println("custom.equals(different) [different components]: " + custom.equals(different));

        // Reflection: getRecordComponents()
        System.out.println("\n--- Reflection: getRecordComponents() ---");
        RecordComponent[] components = RGB.class.getRecordComponents();
        System.out.println("RGB has " + components.length + " components:");
        for (var comp : components) {
            System.out.println("  - " + comp.getName() + " : " + comp.getType().getSimpleName());
        }

        // Access component values via reflection
        System.out.println("\n--- Component values via reflection ---");
        for (var comp : components) {
            try {
                Object value = comp.getAccessor().invoke(custom);
                System.out.println("  " + comp.getName() + " = " + value);
            } catch (Exception e) {
                System.out.println("  Error accessing " + comp.getName() + ": " + e.getMessage());
            }
        }
    }

    // ============================================================
    // Section 3: Custom Constructors and Validation
    // ============================================================

    static void customConstructorsAndValidation() {
        System.out.println("\n=== Section 3: Custom Constructors and Validation ===");

        // Temperature — compact constructor with normalization
        System.out.println("--- Temperature: compact constructor with normalization ---");
        var tempC = new Temperature(100.0, "  c  ");
        System.out.println("new Temperature(100.0, \"  c  \"): " + tempC);
        System.out.println("Unit normalized to: " + tempC.unit());

        var tempF = new Temperature(212.0, "f");
        System.out.println("new Temperature(212.0, \"f\"): " + tempF);

        // Validation — invalid unit
        System.out.println("\n--- Temperature: validation ---");
        try {
            new Temperature(100.0, "X");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid unit rejected: " + e.getMessage());
        }

        // Additional constructor
        System.out.println("\n--- Temperature: additional constructor ---");
        var defaultTemp = new Temperature(37.0);
        System.out.println("new Temperature(37.0): " + defaultTemp + " (defaults to Celsius)");

        // toCelsius() conversion
        System.out.println("\n--- Temperature: toCelsius() conversion ---");
        System.out.printf("100°C → %.2f°C%n", new Temperature(100.0, "C").toCelsius());
        System.out.printf("212°F → %.2f°C%n", new Temperature(212.0, "F").toCelsius());
        System.out.printf("373.15K → %.2f°C%n", new Temperature(373.15, "K").toCelsius());

        // Tags — defensive copy
        System.out.println("\n--- Tags: defensive copy ---");
        var mutableList = new ArrayList<>(List.of("java", "records", "modern"));
        var tags = new Tags("tech", mutableList);
        System.out.println("tags: " + tags);

        mutableList.add("MUTATED");
        System.out.println("After mutating original list:");
        System.out.println("  original list: " + mutableList);
        System.out.println("  tags.values(): " + tags.values() + " (unchanged — defensive copy)");

        // Range — validation + methods
        System.out.println("\n--- Range: validation and methods ---");
        var range = new Range(1, 10);
        System.out.println("range: " + range);
        System.out.println("range.length(): " + range.length());
        System.out.println("range.contains(5): " + range.contains(5));
        System.out.println("range.contains(15): " + range.contains(15));

        try {
            new Range(10, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid range rejected: " + e.getMessage());
        }
    }

    // ============================================================
    // Section 4: Records and Interfaces — Generic Records
    // ============================================================

    static void recordsAndInterfacesGenericRecords() {
        System.out.println("\n=== Section 4: Records and Interfaces — Generic Records ===");

        // Money — implements Printable and Comparable
        System.out.println("--- Money: Printable and Comparable ---");
        var m1 = new Money(29.99, "USD");
        var m2 = new Money(49.99, "USD");
        var m3 = new Money(9.99, "USD");

        System.out.println("m1.prettyPrint(): " + m1.prettyPrint());
        System.out.println("m2.prettyPrint(): " + m2.prettyPrint());

        // Sorting via Comparable
        var prices = new ArrayList<>(List.of(m2, m1, m3));
        Collections.sort(prices);
        System.out.println("\nSorted prices:");
        for (var price : prices) {
            System.out.println("  " + price.prettyPrint());
        }

        // Different currencies — compareTo rejects
        try {
            new Money(10, "USD").compareTo(new Money(10, "EUR"));
        } catch (IllegalArgumentException e) {
            System.out.println("\nCross-currency comparison: " + e.getMessage());
        }

        // Pair — generic record
        System.out.println("\n--- Pair: generic record ---");
        var pair1 = new Pair<>("hello", 42);
        var pair2 = new Pair<>(3.14, List.of("a", "b"));
        System.out.println("pair1: " + pair1);
        System.out.println("pair2: " + pair2);

        // mapFirst / mapSecond
        System.out.println("\n--- Pair: mapFirst / mapSecond ---");
        var mapped1 = pair1.mapFirst(String::toUpperCase);
        System.out.println("pair1.mapFirst(toUpperCase): " + mapped1);

        var mapped2 = pair1.mapSecond(n -> n * 2);
        System.out.println("pair1.mapSecond(n -> n * 2): " + mapped2);

        // Records as Map keys (structural equality)
        System.out.println("\n--- Records as Map keys ---");
        var map = new HashMap<Point, String>();
        map.put(new Point(1.0, 2.0), "A");
        map.put(new Point(3.0, 4.0), "B");

        // Lookup with a different instance but same values
        var lookup = new Point(1.0, 2.0);
        System.out.println("map.get(new Point(1.0, 2.0)): " + map.get(lookup));
        System.out.println("Works because records have structural equality!");
    }

    // ============================================================
    // Section 5: Records with Pattern Matching
    // ============================================================

    static void recordsWithPatternMatching() {
        System.out.println("\n=== Section 5: Records with Pattern Matching ===");

        // instanceof with records
        System.out.println("--- instanceof with records ---");
        Object obj = new Point(3.0, 4.0);
        if (obj instanceof Point p) {
            double distance = Math.sqrt(p.x() * p.x() + p.y() * p.y());
            System.out.println(p + " distance from origin: " + distance);
        }

        // switch with record types
        System.out.println("\n--- switch with record types ---");
        List<Object> items = List.of(
                new Point(1.0, 2.0),
                new FullName("Jane", "Smith"),
                new RGB(0, 255, 0),
                "just a string"
        );

        for (var item : items) {
            String description = switch (item) {
                case Point p -> "Point at (" + p.x() + ", " + p.y() + ")";
                case FullName fn -> "Name: " + fn.first() + " " + fn.last();
                case RGB rgb -> "Color: R=" + rgb.red() + " G=" + rgb.green() + " B=" + rgb.blue();
                default -> "Other: " + item;
            };
            System.out.println("  " + description);
        }

        // Record deconstruction patterns (Java 21+)
        System.out.println("\n--- Record deconstruction patterns ---");
        var points = List.of(
                new Point(1.0, 2.0),
                new Point(-3.0, 4.0),
                new Point(5.0, -1.0),
                new Point(-2.0, -7.0)
        );

        for (var point : points) {
            String quadrant = switch (point) {
                case Point(var x, var y) when x > 0 && y > 0 -> "Q1 (positive, positive)";
                case Point(var x, var y) when x < 0 && y > 0 -> "Q2 (negative, positive)";
                case Point(var x, var y) when x < 0 && y < 0 -> "Q3 (negative, negative)";
                case Point(var x, var y) when x > 0 && y < 0 -> "Q4 (positive, negative)";
                case Point(var x, var y) -> "On axis";
            };
            System.out.println("  " + point + " → " + quadrant);
        }

        // Nested record patterns
        System.out.println("\n--- Nested record patterns ---");
        var line = new Line(new Point(0, 0), new Point(3, 4));
        if (line instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.println("Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
            System.out.println("Length: " + length);
        }

        // Nested deconstruction with ColoredPoint
        System.out.println("\n--- Nested deconstruction with ColoredPoint ---");
        var coloredPoints = List.of(
                new ColoredPoint(new Point(1, 2), "red"),
                new ColoredPoint(new Point(-1, 5), "blue"),
                new ColoredPoint(new Point(3, -2), "green")
        );

        for (var cp : coloredPoints) {
            switch (cp) {
                case ColoredPoint(Point(var x, var y), var color) when x > 0 && y > 0 ->
                        System.out.println("  " + color + " point in Q1 at (" + x + ", " + y + ")");
                case ColoredPoint(Point(var x, var y), var color) ->
                        System.out.println("  " + color + " point at (" + x + ", " + y + ")");
            }
        }

        // Pair deconstruction
        System.out.println("\n--- Pair deconstruction ---");
        var pairs = List.of(
                new Pair<>("Alice", 30),
                new Pair<>("Bob", 17),
                new Pair<>("Charlie", 25)
        );

        for (var pair : pairs) {
            switch (pair) {
                case Pair(var name, Integer age) when age >= 18 ->
                        System.out.println("  " + name + " (age " + age + "): adult");
                case Pair(var name, Integer age) ->
                        System.out.println("  " + name + " (age " + age + "): minor");
            }
        }
    }

    // ============================================================
    // Section 6: Practical Patterns and Limitations
    // ============================================================

    static void practicalPatternsAndLimitations() {
        System.out.println("\n=== Section 6: Practical Patterns and Limitations ===");

        // Builder pattern for PersonRecord
        System.out.println("--- Builder pattern for records ---");
        var person = new PersonRecord.Builder()
                .name("Alice Johnson")
                .age(30)
                .email("alice@example.com")
                .phone("+1-555-0123")
                .city("New York")
                .build();
        System.out.println("Built: " + person);

        // Builder with only required fields
        var minimal = new PersonRecord.Builder()
                .name("Bob")
                .age(25)
                .build();
        System.out.println("Minimal: " + minimal);

        // Local records in stream pipelines
        System.out.println("\n--- Local records in stream pipelines ---");
        record NameScore(String name, int score) {}

        var results = List.of(
                new NameScore("Alice", 92),
                new NameScore("Bob", 85),
                new NameScore("Charlie", 97),
                new NameScore("Diana", 88),
                new NameScore("Eve", 91)
        );

        var topScorers = results.stream()
                .filter(ns -> ns.score() >= 90)
                .sorted(Comparator.comparingInt(NameScore::score).reversed())
                .toList();

        System.out.println("Top scorers (score >= 90):");
        for (var ns : topScorers) {
            System.out.println("  " + ns.name() + ": " + ns.score());
        }

        // Record components via reflection
        System.out.println("\n--- Record components via reflection ---");
        System.out.println("PersonRecord components:");
        for (var comp : PersonRecord.class.getRecordComponents()) {
            System.out.println("  - " + comp.getName() + " : " + comp.getType().getSimpleName());
        }

        // Serialization round-trip
        System.out.println("\n--- Serialization round-trip ---");
        record SerializablePoint(double x, double y) implements Serializable {}

        var original = new SerializablePoint(3.14, 2.71);
        try {
            // Serialize
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            // Deserialize
            var bais = new ByteArrayInputStream(baos.toByteArray());
            var ois = new ObjectInputStream(bais);
            var deserialized = (SerializablePoint) ois.readObject();
            ois.close();

            System.out.println("Original:     " + original);
            System.out.println("Deserialized: " + deserialized);
            System.out.println("Equal: " + original.equals(deserialized));
        } catch (Exception e) {
            System.out.println("Serialization error: " + e.getMessage());
        }

        // Summary of limitations
        System.out.println("\n--- Summary of record limitations ---");
        System.out.println("1. Cannot extend a class (already extend java.lang.Record)");
        System.out.println("2. Cannot have mutable fields (all components are final)");
        System.out.println("3. Cannot declare additional instance fields");
        System.out.println("4. Are implicitly final (cannot be subclassed)");
        System.out.println("5. Cannot be abstract");
        System.out.println("→ Use records for data carriers; use classes for mutable/complex objects");
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        introductionToRecords();
        anatomyOfARecord();
        customConstructorsAndValidation();
        recordsAndInterfacesGenericRecords();
        recordsWithPatternMatching();
        practicalPatternsAndLimitations();
    }
}
