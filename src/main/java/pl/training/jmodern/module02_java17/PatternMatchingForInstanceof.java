package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ============================================================
// Section 1: The instanceof Problem
// ============================================================

/*
## The instanceof Problem

- In traditional Java, checking the type of an object and then
  using it requires **three separate steps**:
    1. `if (obj instanceof String)` — check the type
    2. `String s = (String) obj;` — cast to the type
    3. Use `s` — finally work with the value
  This is 3 lines for what is conceptually a single operation.
- **The cast is redundant**: If the `instanceof` check passed,
  we already know the type — yet Java forced us to write an
  explicit cast that can never fail. This is pure boilerplate.
- **ClassCastException risk**: If a developer mistakenly casts
  to the wrong type (or casts without checking instanceof first),
  a `ClassCastException` is thrown at runtime. The pattern of
  check-then-cast should be a single atomic operation.
- **JEP 394** (finalized in Java 16):
    - Preview in Java 14 (JEP 305)
    - Second preview in Java 15 (JEP 375)
    - Finalized in Java 16 (JEP 394)
    - Taught here with Java 17 LTS
- **Pattern variable binding**: The new syntax combines the check
  and cast into one expression:
      if (obj instanceof String s) { // use s directly }
  The variable s is called a **pattern variable**. It is
  automatically bound to the cast value and is only in scope
  where the compiler can prove the check succeeded.
- This feature works with **any reference type** -- classes,
  interfaces, abstract classes -- not just records or sealed types.
*/

// ============================================================
// Section 2: Pattern Variables and Scope
// ============================================================

/*
## Pattern Variables and Scope

- Pattern variables use **flow scoping** (also called
  **definite-assignment analysis**), NOT traditional block
  scoping. The variable is in scope wherever the compiler can
  prove the instanceof check succeeded.
- **Scope in if-body**: The pattern variable is in scope inside
  the if-body because the check is guaranteed to have passed:
      if (obj instanceof String s) {
          // s is in scope here
      }
      // s is NOT in scope here
- **Negated condition + early return**: When you negate the
  condition and return early, the variable is in scope AFTER
  the if-block (in the rest of the method):
      if (!(obj instanceof String s)) return;
      // s is in scope here — the check must have passed
  This pattern is common for guard clauses / preconditions.
- **Scope in else with negation**: If the condition is negated
  in a different way (via `else`), the variable is available in
  the else branch only if the logic guarantees it:
      if (obj instanceof String s) {
          // s in scope
      } else {
          // s NOT in scope — obj might not be a String
      }
- **Scope in while loops**: Pattern variables also work in
  while-loop conditions and the loop body.
- **Shadowing**: A pattern variable can shadow a field or local
  variable of the same name, just like other local variables.
*/

// ============================================================
// Section 3: Complex Conditions
// ============================================================

/*
## Complex Conditions

- **Combining with `&&`**: You can combine instanceof patterns
  with additional conditions using `&&` (logical AND):
      if (obj instanceof String s && s.length() > 5)
  This works because `&&` is short-circuit: `s` is only evaluated
  if the instanceof check passed, so the variable is in scope.
- **Cannot use with `||`**: Using `||` with a pattern variable
  is a **compile error**:
      if (obj instanceof String s || s.isEmpty()) // ERROR!
  With `||`, the right side executes when the left is false,
  meaning `s` might not be bound. The compiler rejects this.
- **Multiple pattern variables**: You can chain multiple
  instanceof checks with `&&`:
      if (a instanceof String s && b instanceof Integer i) {
          // both s and i are in scope
      }
- **Guard-like conditions**: Combine instanceof with property
  checks for powerful filtering:
      if (animal instanceof Dog d && d.breed().equals("Labrador"))
  This replaces two nested if-statements with a single line.
- **Nested conditions**: Pattern variables from an outer if are
  in scope in nested if-statements:
      if (obj instanceof List<?> list) {
          if (list.size() > 0 && list.get(0) instanceof String s) {
              // both list and s in scope
          }
      }
*/

// ============================================================
// Section 4: Inheritance Hierarchies
// ============================================================

/*
## Inheritance Hierarchies

- When using instanceof with inheritance, **order matters**:
  check the most specific type first, then more general types.
  If you check `Rect` before `Square`, a Square will match
  as a Rect and never reach the Square case.
      if (shape instanceof Square sq)       // check specific first
      else if (shape instanceof Rect r)      // then general
      else if (shape instanceof Circle c)
- **Polymorphism vs pattern matching**: These are complementary
  tools, not competitors:
    - **Polymorphism** (virtual methods): Best when each type
      knows how to perform an operation on itself. Add the method
      to the class hierarchy. Operations on own data.
    - **Pattern matching**: Best for **external operations** that
      combine data from the object with external context, or when
      you cannot modify the class hierarchy.
- **When to use each**:
    - `area()` → polymorphism (each shape computes its own area)
    - `describe(Shape)` → pattern matching (external description
      logic that doesn't belong inside the shape classes)
    - `render(Shape, Canvas)` → pattern matching (involves
      external Canvas that shapes shouldn't depend on)
- **Mixed approach**: Use polymorphism for core behavior and
  pattern matching for utility/display/external operations.
*/

// ============================================================
// Section 5: Practical Patterns
// ============================================================

/*
## Practical Patterns

- **equals() implementation**: The most common real-world use
  of pattern matching for instanceof is in `equals()` methods:
      @Override
      public boolean equals(Object o) {
          return o instanceof Sensor s
              && id == s.id
              && Objects.equals(type, s.type);
      }
  This replaces 5+ lines of boilerplate with a single expression.
- **Stream filtering by type**: Pattern matching combines
  naturally with streams to filter and map by type:
      list.stream()
          .filter(obj -> obj instanceof Dog)
          .map(obj -> (Dog) obj)  // still needed in map
  Or using `Stream.mapMulti` for a cleaner approach.
- **Heterogeneous containers**: Processing a `List<Object>` with
  mixed types becomes readable with pattern matching:
      for (Object item : items) {
          if (item instanceof String s) { ... }
          else if (item instanceof Integer i) { ... }
      }
- **Visitor pattern replacement**: Pattern matching can replace
  the visitor pattern for simple type dispatch — no need for
  accept/visit boilerplate.
- **Null safety**: `null instanceof X` always returns `false`
  for any type X. This means you don't need a separate null
  check before instanceof — it's built in.
*/

// ============================================================
// Section 6: Pattern Matching Evolution
// ============================================================

/*
## Pattern Matching Evolution

- **Java 16**: Pattern matching for instanceof (JEP 394)
    - `if (obj instanceof String s) { ... }`
    - The foundation — eliminates cast-after-check boilerplate
- **Java 17**: Sealed classes (JEP 409) — sets the stage
    - Sealed types enable exhaustive type checking later
    - No direct pattern matching changes, but essential groundwork
- **Java 21**: Pattern matching for switch (JEP 441) +
  Record patterns (JEP 440)
    - `switch (obj) { case String s -> ...; }`
    - `case Point(var x, var y) -> ...` (record deconstruction)
    - `when` guards: `case String s when s.length() > 5 -> ...`
    - Exhaustiveness checking with sealed types
- **Java 22+**: Unnamed patterns `_` (JEP 456)
    - `case Point(var x, _) -> ...` (ignore y component)
    - Useful when you don't need all components
- **Future directions** (in development):
    - Primitive type patterns (`case int i -> ...`)
    - Array patterns
    - More deconstruction patterns beyond records
- The overall trajectory: Java is becoming a language where
  **data-oriented programming** is a first-class paradigm
  alongside object-oriented programming. Pattern matching is
  the key enabler of this shift.
*/

public class PatternMatchingForInstanceof {

    // ---- Section 1: Animal hierarchy ----

    static abstract class Animal {
        abstract String name();
        abstract String sound();
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + name() + "]";
        }
    }

    static class Dog extends Animal {
        private final String name;
        private final String breed;
        Dog(String name, String breed) { this.name = name; this.breed = breed; }
        @Override String name() { return name; }
        @Override String sound() { return "Woof!"; }
        String breed() { return breed; }
        String fetch() { return name + " fetches the ball!"; }
    }

    static class Cat extends Animal {
        private final String name;
        private final boolean indoor;
        Cat(String name, boolean indoor) { this.name = name; this.indoor = indoor; }
        @Override String name() { return name; }
        @Override String sound() { return "Meow!"; }
        boolean isIndoor() { return indoor; }
        String purr() { return name + " purrs contentedly."; }
    }

    static class Bird extends Animal {
        private final String name;
        private final boolean canFly;
        Bird(String name, boolean canFly) { this.name = name; this.canFly = canFly; }
        @Override String name() { return name; }
        @Override String sound() { return canFly ? "Tweet!" : "Squawk!"; }
        boolean canFly() { return canFly; }
    }

    // ---- Section 4: Shape hierarchy ----

    static abstract class Shape {
        abstract double area();
    }

    static class Circle extends Shape {
        private final double radius;
        Circle(double radius) { this.radius = radius; }
        double radius() { return radius; }
        @Override double area() { return Math.PI * radius * radius; }
        @Override public String toString() { return "Circle[radius=" + radius + "]"; }
    }

    static class Rect extends Shape {
        private final double width;
        private final double height;
        Rect(double width, double height) { this.width = width; this.height = height; }
        double width() { return width; }
        double height() { return height; }
        @Override double area() { return width * height; }
        @Override public String toString() { return "Rect[" + width + "x" + height + "]"; }
    }

    static class Square extends Rect {
        Square(double side) { super(side, side); }
        double side() { return width(); }
        @Override public String toString() { return "Square[side=" + side() + "]"; }
    }

    // ---- Section 5: Sensor with equals() ----

    static class Sensor {
        private final int id;
        private final String type;
        Sensor(int id, String type) { this.id = id; this.type = type; }
        int id() { return id; }
        String type() { return type; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Sensor s
                    && id == s.id
                    && Objects.equals(type, s.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type);
        }

        @Override
        public String toString() {
            return "Sensor[id=" + id + ", type=" + type + "]";
        }
    }

    // ============================================================
    // Section 1: The instanceof Problem
    // ============================================================

    static void theInstanceofProblem() {
        System.out.println("=== Section 1: The instanceof Problem ===");

        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Bird("Polly", true),
                new Dog("Buddy", "Labrador"),
                new Cat("Shadow", false),
                new Bird("Kiwi", false)
        );

        // OLD WAY: instanceof + explicit cast
        System.out.println("--- Old way: instanceof + explicit cast ---");
        for (Animal animal : animals) {
            if (animal instanceof Dog) {
                Dog d = (Dog) animal;  // redundant cast
                System.out.println("  Dog: " + d.name() + " (" + d.breed() + ") - " + d.fetch());
            } else if (animal instanceof Cat) {
                Cat c = (Cat) animal;  // redundant cast
                System.out.println("  Cat: " + c.name() + " (indoor: " + c.isIndoor() + ") - " + c.purr());
            } else if (animal instanceof Bird) {
                Bird b = (Bird) animal;  // redundant cast
                System.out.println("  Bird: " + b.name() + " (can fly: " + b.canFly() + ")");
            }
        }

        // NEW WAY: pattern matching for instanceof
        System.out.println("\n--- New way: pattern matching ---");
        for (Animal animal : animals) {
            if (animal instanceof Dog d) {
                System.out.println("  Dog: " + d.name() + " (" + d.breed() + ") - " + d.fetch());
            } else if (animal instanceof Cat c) {
                System.out.println("  Cat: " + c.name() + " (indoor: " + c.isIndoor() + ") - " + c.purr());
            } else if (animal instanceof Bird b) {
                System.out.println("  Bird: " + b.name() + " (can fly: " + b.canFly() + ")");
            }
        }

        // Side-by-side comparison — lines of code
        System.out.println("\n--- Side-by-side: old vs new ---");
        Animal animal = new Dog("Max", "Poodle");

        // Old: 3 lines
        System.out.println("  Old (3 lines):");
        if (animal instanceof Dog) {
            Dog d = (Dog) animal;
            System.out.println("    " + d.name() + " is a " + d.breed());
        }

        // New: 1 line (conceptually)
        System.out.println("  New (1 line check+bind):");
        if (animal instanceof Dog d) {
            System.out.println("    " + d.name() + " is a " + d.breed());
        }
    }

    // ============================================================
    // Section 2: Pattern Variables and Scope
    // ============================================================

    static String describeWithEarlyReturn(Object obj) {
        if (!(obj instanceof String s)) {
            return "Not a string";
        }
        // s is in scope here because the instanceof must have succeeded
        // (if it failed, we would have returned above)
        return "String of length " + s.length() + ": \"" + s + "\"";
    }

    static void patternVariablesAndScope() {
        System.out.println("\n=== Section 2: Pattern Variables and Scope ===");

        // Basic scope in if-body
        System.out.println("--- Basic scope in if-body ---");
        Object obj = "Hello, Pattern Matching!";
        if (obj instanceof String s) {
            System.out.println("  s is in scope: \"" + s + "\" (length: " + s.length() + ")");
        }
        // s is NOT in scope here — cannot use it outside the if-body

        // Early return pattern with negation
        System.out.println("\n--- Early return pattern with negation ---");
        System.out.println("  " + describeWithEarlyReturn("Hello World"));
        System.out.println("  " + describeWithEarlyReturn(42));
        System.out.println("  " + describeWithEarlyReturn(null));

        // Scope in else with negated condition
        System.out.println("\n--- Scope in if vs else ---");
        Object value = 42;
        if (value instanceof String s) {
            System.out.println("  It's a string: " + s);
        } else {
            // s is NOT in scope here — value is not a String
            System.out.println("  Not a string, it's a: " + value.getClass().getSimpleName());
        }

        // Compiler error example (commented out)
        // Object x = 42;
        // if (x instanceof String s) {
        //     System.out.println(s);
        // }
        // System.out.println(s); // ERROR: s is not in scope here

        // Pattern variable in while loop
        System.out.println("\n--- Pattern variable in loop ---");
        List<Object> items = List.of("first", 2, "third", 4, "fifth");
        System.out.println("  Strings found in list:");
        for (Object item : items) {
            if (item instanceof String s) {
                System.out.println("    → \"" + s + "\"");
            }
        }

        // Negation scope in a processing loop
        System.out.println("\n--- Negation scope: skip non-strings ---");
        for (Object item : items) {
            if (!(item instanceof String s)) {
                continue;  // skip non-strings
            }
            // s is in scope here due to flow scoping
            System.out.println("    Processing string: \"" + s.toUpperCase() + "\"");
        }
    }

    // ============================================================
    // Section 3: Complex Conditions
    // ============================================================

    static void complexConditions() {
        System.out.println("\n=== Section 3: Complex Conditions ===");

        // Combining instanceof with && (short-circuit)
        System.out.println("--- instanceof with && (short-circuit) ---");
        List<Object> values = List.of("Hello World", "Hi", "", 42, "Pattern Matching is great", 3.14);
        for (Object val : values) {
            if (val instanceof String s && s.length() > 5) {
                System.out.println("  Long string: \"" + s + "\" (length: " + s.length() + ")");
            }
        }

        // Multiple instanceof checks with &&
        System.out.println("\n--- Multiple instanceof checks with && ---");
        Object a = "Hello";
        Object b = 42;
        if (a instanceof String s && b instanceof Integer i) {
            System.out.println("  s = \"" + s + "\", i = " + i);
            System.out.println("  Combined: \"" + s + "\" repeated " + i + " times would be " + (s.length() * i) + " chars");
        }

        // Cannot use || with pattern variables (compile error)
        System.out.println("\n--- Why || doesn't work with pattern variables ---");
        System.out.println("  // if (obj instanceof String s || s.isEmpty()) → COMPILE ERROR");
        System.out.println("  // With ||, the right side runs when left is false,");
        System.out.println("  // so 's' might not be bound → compiler rejects it");

        // Guard-like conditions with Animal hierarchy
        System.out.println("\n--- Guard-like conditions ---");
        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Dog("Buddy", "Labrador"),
                new Dog("Max", "Poodle"),
                new Cat("Whiskers", true),
                new Cat("Shadow", false)
        );

        System.out.println("  Labradors only:");
        for (Animal animal : animals) {
            if (animal instanceof Dog d && d.breed().equals("Labrador")) {
                System.out.println("    Found Labrador: " + d.name());
            }
        }

        System.out.println("  Indoor cats only:");
        for (Animal animal : animals) {
            if (animal instanceof Cat c && c.isIndoor()) {
                System.out.println("    Indoor cat: " + c.name());
            }
        }

        // Nested instanceof checks
        System.out.println("\n--- Nested instanceof checks ---");
        List<Object> containers = List.of(
                List.of("hello", "world"),
                List.of(1, 2, 3),
                "just a string",
                List.of()
        );
        for (Object container : containers) {
            if (container instanceof List<?> list && !list.isEmpty()) {
                if (list.getFirst() instanceof String s) {
                    System.out.println("  List starting with string: \"" + s + "\" (list size: " + list.size() + ")");
                } else if (list.getFirst() instanceof Integer i) {
                    System.out.println("  List starting with integer: " + i + " (list size: " + list.size() + ")");
                }
            }
        }
    }

    // ============================================================
    // Section 4: Inheritance Hierarchies
    // ============================================================

    static String describeShape(Shape shape) {
        // Check most specific type first (Square before Rect)
        if (shape instanceof Square sq) {
            return "Square with side " + sq.side() + " (area: " + sq.area() + ")";
        } else if (shape instanceof Rect r) {
            return "Rectangle " + r.width() + "x" + r.height() + " (area: " + r.area() + ")";
        } else if (shape instanceof Circle c) {
            return "Circle with radius " + c.radius() + " (area: " + String.format("%.2f", c.area()) + ")";
        }
        return "Unknown shape";
    }

    static double perimeter(Shape shape) {
        if (shape instanceof Square sq) {
            return 4 * sq.side();
        } else if (shape instanceof Rect r) {
            return 2 * (r.width() + r.height());
        } else if (shape instanceof Circle c) {
            return 2 * Math.PI * c.radius();
        }
        return 0;
    }

    static void inheritanceHierarchies() {
        System.out.println("\n=== Section 4: Inheritance Hierarchies ===");

        List<Shape> shapes = List.of(
                new Circle(5.0),
                new Rect(4.0, 6.0),
                new Square(3.0),
                new Circle(1.5),
                new Square(10.0),
                new Rect(7.0, 2.0)
        );

        // describeShape — checking most specific first
        System.out.println("--- describeShape: most specific type first ---");
        for (Shape shape : shapes) {
            System.out.println("  " + describeShape(shape));
        }

        // Perimeter via pattern matching (external operation)
        System.out.println("\n--- Perimeter via instanceof (external operation) ---");
        for (Shape shape : shapes) {
            System.out.printf("  %-25s → perimeter: %.2f%n", shape, perimeter(shape));
        }

        // Why order matters — demonstrating Square is a Rect
        System.out.println("\n--- Why order matters: Square is a Rect ---");
        Shape square = new Square(5.0);
        System.out.println("  square instanceof Square: " + (square instanceof Square));
        System.out.println("  square instanceof Rect:   " + (square instanceof Rect));
        System.out.println("  square instanceof Shape:  " + (square instanceof Shape));
        System.out.println("  → Must check Square before Rect to get the specific match");

        // Polymorphism for area vs pattern matching for description
        System.out.println("\n--- Polymorphism (area) vs pattern matching (describe) ---");
        for (Shape shape : shapes) {
            // area() uses polymorphism — each shape computes its own area
            double area = shape.area();
            // describeShape uses pattern matching — external operation
            String desc = describeShape(shape);
            System.out.printf("  area()=%-10.2f  describe()=%s%n", area, desc);
        }

        // Mixed approach: polymorphism + pattern matching
        System.out.println("\n--- Mixed approach ---");
        for (Shape shape : shapes) {
            String extra = "";
            if (shape instanceof Circle c) {
                extra = " (diameter: " + (c.radius() * 2) + ")";
            } else if (shape instanceof Square sq) {
                extra = " (diagonal: " + String.format("%.2f", sq.side() * Math.sqrt(2)) + ")";
            } else if (shape instanceof Rect r) {
                extra = " (diagonal: " + String.format("%.2f", Math.sqrt(r.width() * r.width() + r.height() * r.height())) + ")";
            }
            System.out.println("  " + shape + " → area=" + String.format("%.2f", shape.area()) + extra);
        }
    }

    // ============================================================
    // Section 5: Practical Patterns
    // ============================================================

    static String formatObject(Object obj) {
        if (obj instanceof String s) {
            return "String(\"" + s + "\")";
        } else if (obj instanceof Integer i) {
            return "Int(" + i + ")";
        } else if (obj instanceof Double d) {
            return "Double(" + d + ")";
        } else if (obj instanceof Boolean b) {
            return "Bool(" + b + ")";
        } else if (obj instanceof List<?> list) {
            return "List(size=" + list.size() + ")";
        } else if (obj == null) {
            return "Null";
        }
        return "Unknown(" + obj.getClass().getSimpleName() + ")";
    }

    static void practicalPatterns() {
        System.out.println("\n=== Section 5: Practical Patterns ===");

        // Sensor.equals() demo — most common use case
        System.out.println("--- Sensor.equals() with pattern matching ---");
        var s1 = new Sensor(1, "temperature");
        var s2 = new Sensor(1, "temperature");
        var s3 = new Sensor(2, "humidity");
        var s4 = new Sensor(1, "pressure");

        System.out.println("  s1 = " + s1);
        System.out.println("  s2 = " + s2);
        System.out.println("  s3 = " + s3);
        System.out.println("  s1.equals(s2) [same id+type]: " + s1.equals(s2));
        System.out.println("  s1.equals(s3) [different id]:  " + s1.equals(s3));
        System.out.println("  s1.equals(s4) [different type]: " + s1.equals(s4));
        System.out.println("  s1.equals(null):               " + s1.equals(null));
        System.out.println("  s1.equals(\"string\"):           " + s1.equals("string"));

        // Sensors as map keys (equals + hashCode)
        var sensorMap = new HashMap<Sensor, String>();
        sensorMap.put(s1, "Living Room");
        sensorMap.put(s3, "Bathroom");
        System.out.println("  sensorMap.get(new Sensor(1, \"temperature\")): "
                + sensorMap.get(new Sensor(1, "temperature")));

        // Stream filtering by type — extracting all Dogs
        System.out.println("\n--- Stream filtering by type ---");
        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Dog("Buddy", "Labrador"),
                new Bird("Polly", true),
                new Cat("Shadow", false),
                new Dog("Max", "Poodle")
        );

        List<Dog> dogs = animals.stream()
                .filter(a -> a instanceof Dog)
                .map(a -> (Dog) a)
                .toList();
        System.out.println("  All dogs: " + dogs.stream().map(d -> d.name() + " (" + d.breed() + ")").toList());

        // Alternative: using mapMulti for type-safe filtering
        List<String> dogNames = animals.stream()
                .<String>mapMulti((animal, consumer) -> {
                    if (animal instanceof Dog d) {
                        consumer.accept(d.name() + " the " + d.breed());
                    }
                })
                .toList();
        System.out.println("  Dogs (mapMulti): " + dogNames);

        // Heterogeneous container: List<Object>
        System.out.println("\n--- Processing heterogeneous List<Object> ---");
        List<Object> mixed = List.of("hello", 42, 3.14, true, List.of(1, 2), "world", 100);
        int stringCount = 0;
        int numberCount = 0;
        for (Object item : mixed) {
            if (item instanceof String s) {
                System.out.println("  String: \"" + s + "\"");
                stringCount++;
            } else if (item instanceof Integer i) {
                System.out.println("  Integer: " + i);
                numberCount++;
            } else if (item instanceof Double d) {
                System.out.println("  Double: " + d);
                numberCount++;
            } else if (item instanceof Boolean b) {
                System.out.println("  Boolean: " + b);
            } else if (item instanceof List<?> list) {
                System.out.println("  List: " + list);
            }
        }
        System.out.println("  → Strings: " + stringCount + ", Numbers: " + numberCount);

        // Formatting animals without visitor pattern
        System.out.println("\n--- Formatting animals (no visitor needed) ---");
        for (Animal animal : animals) {
            String formatted;
            if (animal instanceof Dog d) {
                formatted = "🐕 " + d.name() + " (" + d.breed() + ") says " + d.sound();
            } else if (animal instanceof Cat c) {
                formatted = "🐈 " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ") says " + c.sound();
            } else if (animal instanceof Bird b) {
                formatted = "🐦 " + b.name() + " (" + (b.canFly() ? "can fly" : "flightless") + ") says " + b.sound();
            } else {
                formatted = "Unknown animal: " + animal.name();
            }
            System.out.println("  " + formatted);
        }

        // Null safety: null instanceof X is always false
        System.out.println("\n--- Null safety ---");
        Animal nullAnimal = null;
        System.out.println("  null instanceof Dog:    " + (nullAnimal instanceof Dog));
        System.out.println("  null instanceof Cat:    " + (nullAnimal instanceof Cat));
        System.out.println("  null instanceof Animal: " + (nullAnimal instanceof Animal));
        System.out.println("  null instanceof Object: " + (nullAnimal instanceof Object));
        System.out.println("  → null instanceof <anything> is always false — no NPE risk");

        // formatObject helper
        System.out.println("\n--- formatObject helper ---");
        List<Object> objects = List.of("test", 42, 3.14, true, List.of("a", "b"));
        for (Object obj : objects) {
            System.out.println("  " + formatObject(obj));
        }
        System.out.println("  " + formatObject(null));
    }

    // ============================================================
    // Section 6: Pattern Matching Evolution
    // ============================================================

    static String describeAnimal(Animal animal) {
        if (animal instanceof Dog d) {
            return "Dog " + d.name() + " (" + d.breed() + ")";
        } else if (animal instanceof Cat c) {
            return "Cat " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ")";
        } else if (animal instanceof Bird b) {
            return "Bird " + b.name() + " (" + (b.canFly() ? "flying" : "flightless") + ")";
        }
        return "Unknown: " + animal.name();
    }

    static void patternMatchingEvolution() {
        System.out.println("\n=== Section 6: Pattern Matching Evolution ===");

        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Bird("Polly", true),
                new Dog("Buddy", "Labrador"),
                new Cat("Shadow", false),
                new Bird("Kiwi", false)
        );

        // Java 16 style: instanceof if/else chain
        System.out.println("--- Java 16: instanceof if/else chain ---");
        for (Animal animal : animals) {
            System.out.println("  " + describeAnimal(animal));
        }

        // Java 21 style: rewrite as switch expression
        System.out.println("\n--- Java 21: switch expression rewrite ---");
        for (Animal animal : animals) {
            var description = switch (animal) {
                case Dog d -> "Dog " + d.name() + " (" + d.breed() + ")";
                case Cat c -> "Cat " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ")";
                case Bird b -> "Bird " + b.name() + " (" + (b.canFly() ? "flying" : "flightless") + ")";
                default -> "Unknown: " + animal.name();
            };
            System.out.println("  " + description);
        }

        // Java 21: switch with `when` guards
        System.out.println("\n--- Java 21: switch with 'when' guards ---");
        for (Animal animal : animals) {
            var label = switch (animal) {
                case Dog d when d.breed().equals("Labrador") -> "Friendly Labrador: " + d.name();
                case Dog d when d.breed().equals("German Shepherd") -> "Guard dog: " + d.name();
                case Dog d -> "Dog: " + d.name() + " (" + d.breed() + ")";
                case Cat c when c.isIndoor() -> "Indoor cat: " + c.name();
                case Cat c -> "Outdoor cat: " + c.name();
                case Bird b when b.canFly() -> "Flying bird: " + b.name();
                case Bird b -> "Flightless bird: " + b.name();
                default -> "Animal: " + animal.name();
            };
            System.out.println("  " + label);
        }

        // Java 22+: unnamed patterns _ (conceptual)
        System.out.println("\n--- Java 22+: unnamed patterns (conceptual) ---");
        System.out.println("  // In Java 22+, you can use _ to ignore components:");
        System.out.println("  // case Dog _ -> \"It's a dog\";  // don't need the binding");
        System.out.println("  // case Point(var x, _) -> \"x=\" + x;  // ignore y");
        System.out.println("  // Useful when you only care about the type, not the data");

        // Demonstrating unnamed variable with switch
        for (Animal animal : animals) {
            var kind = switch (animal) {
                case Dog _ -> "Dog";
                case Cat _ -> "Cat";
                case Bird _ -> "Bird";
                default -> "Other";
            };
            System.out.println("  " + animal.name() + " is a " + kind);
        }

        // Exhaustiveness with sealed types (conceptual)
        System.out.println("\n--- Exhaustiveness note ---");
        System.out.println("  If Animal were a sealed class permitting only Dog, Cat, Bird:");
        System.out.println("  sealed abstract class Animal permits Dog, Cat, Bird {}");
        System.out.println("  Then switch would be exhaustive — no 'default' needed.");
        System.out.println("  The compiler would enforce that all subtypes are handled.");
        System.out.println("  Adding a new subtype (e.g., Fish) would trigger compile errors");
        System.out.println("  in every switch that doesn't handle it.");

        // Evolution timeline summary
        System.out.println("\n--- Pattern matching evolution timeline ---");
        System.out.println("  Java 14-15: instanceof patterns (preview)");
        System.out.println("  Java 16:    instanceof patterns (finalized, JEP 394)");
        System.out.println("  Java 17:    sealed classes (JEP 409) — groundwork");
        System.out.println("  Java 21:    switch patterns + record patterns (JEP 441, 440)");
        System.out.println("  Java 22:    unnamed patterns _ (JEP 456)");
        System.out.println("  Future:     primitive patterns, array patterns, more deconstruction");
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        theInstanceofProblem();
        patternVariablesAndScope();
        complexConditions();
        inheritanceHierarchies();
        practicalPatterns();
        patternMatchingEvolution();
    }
}
