package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction to Sealed Classes — Why Restrict Inheritance?
// ============================================================

/*
## Introduction to Sealed Classes — Why Restrict Inheritance?

- In standard Java, any class that is not `final` can be extended
  by **anyone**, anywhere. This creates **open hierarchies** where
  the author has no control over which subtypes exist.
- **Problems with open hierarchies**:
    - You cannot write exhaustive `switch` or `if-else` chains
      because an unknown subclass might appear at runtime.
    - Library maintainers cannot evolve a class hierarchy safely —
      any change might break unknown subclasses in user code.
    - Domain modeling is imprecise: if a Shape should only be
      Circle, Rectangle, or Triangle, nothing enforces that rule.
- **Pre-Java 17 workarounds** (all unsatisfying):
    - `final` — prevents ALL extension, too restrictive
    - Package-private constructors — limits extension to the same
      package, but allows any class in that package
    - Javadoc comments like "do not extend" — unenforceable
    - Enums — limited to singletons (no per-instance data)
- **Sealed classes/interfaces** (JEP 409, Java 17) solve this:
    - The `sealed` modifier + `permits` clause declares **exactly**
      which classes/interfaces may extend or implement a type.
    - The compiler enforces the restriction at compile time.
    - Pattern matching can rely on the **closed set** of subtypes
      to enable exhaustive switch expressions without `default`.
- **Timeline**:
    - JEP 360: Preview in Java 15
    - JEP 397: Second preview in Java 16
    - JEP 409: Finalized in Java 17
*/

// ============================================================
// Section 2: Syntax and Rules
// ============================================================

/*
## Syntax and Rules

- A sealed class or interface uses the `sealed` modifier and a
  `permits` clause to list the allowed direct subtypes:
      sealed interface Shape permits Circle, Rectangle, Triangle
- **Same-module rule**: All permitted subtypes must be in the
  same module as the sealed type (or in the same package if in
  the unnamed module). This is enforced by the compiler.
- **Three required modifiers** — every direct subtype of a sealed
  type must be declared as one of:
    - `final` — cannot be extended further (hierarchy ends here)
    - `sealed` — continues the sealed chain with its own `permits`
    - `non-sealed` — reopens the hierarchy, any class can extend
  This is mandatory — the compiler will reject a subtype that
  doesn't specify one of these three modifiers.
- **When to use each**:
    - `final` — most common; leaf nodes of the hierarchy
    - `sealed` — when you want another level of controlled subtypes
    - `non-sealed` — when you intentionally allow open extension
      at a specific point (useful for plugin/extension points)
- **Records** as permitted subtypes are implicitly `final` (all
  records are final), so they work perfectly as leaf nodes.
- **Enums** as permitted subtypes are implicitly `final` as well.
- **Reflection API**:
    - `Class.isSealed()` — returns true if the class is sealed
    - `Class.getPermittedSubclasses()` — returns the permitted
      subtypes as an array of `ClassDesc`
*/

// ============================================================
// Section 3: Sealed Interfaces
// ============================================================

/*
## Sealed Interfaces

- Sealed interfaces work exactly like sealed classes — you can
  declare `sealed interface X permits A, B, C` and only A, B, C
  may implement X.
- **Records implementing sealed interfaces**: Records are an ideal
  match for sealed interfaces because:
    - Records are implicitly `final` (satisfying the modifier rule)
    - Records are transparent data carriers — perfect for modeling
      algebraic data types (sum types)
    - Combining sealed interfaces + records gives you **sum types**
      (sealed = "one of these options") of **product types**
      (record = tuple of named fields)
- **Enums implementing sealed interfaces**: An enum can implement
  a sealed interface. Since enums are implicitly `final` and have
  a fixed set of instances, they naturally fit the sealed concept.
- **Algebraic Data Types (ADTs)**: In functional programming,
  ADTs are a fundamental modeling tool:
    - **Sum type** = "A or B or C" (sealed interface)
    - **Product type** = "A and B and C" (record/class with fields)
    - Java's sealed interfaces + records give us full ADT support.
  This pattern is called "sealed interface + records" or
  "discriminated unions" in other languages.
*/

// ============================================================
// Section 4: Sealed Classes and Pattern Matching
// ============================================================

/*
## Sealed Classes and Pattern Matching

- The real power of sealed types emerges with **pattern matching
  in switch expressions** (JEP 441, finalized Java 21).
- Because the compiler knows the **complete set** of permitted
  subtypes, it can verify that a switch expression is
  **exhaustive** — covering all possible cases.
- **No `default` needed**: When all permitted subtypes are handled,
  the compiler accepts the switch without a default branch. This
  is a significant advantage because:
    - Adding a new subtype triggers compile errors in every switch
      that doesn't handle it — you can't forget to update them.
    - With `default`, new subtypes silently fall through.
- **Guarded patterns with `when`**: You can add conditions to
  patterns: `case Circle c when c.radius() > 100 -> ...`
  This combines type checking, deconstruction, and filtering
  in a single, readable expression.
- **Connection to JEP 441**: Pattern matching for switch was
  previewed starting in Java 17 and finalized in Java 21.
  Sealed classes were designed with this synergy in mind.
*/

// ============================================================
// Section 5: Sealed Classes with Records — Algebraic Data Types
// ============================================================

/*
## Sealed Classes with Records — Algebraic Data Types

- **Algebraic Data Types (ADTs)** are the combination of:
    - **Sum types** (tagged unions): "one of A, B, or C"
    - **Product types** (tuples/records): "A contains x, y, z"
- In languages like Haskell, Scala, Kotlin, and Rust, ADTs are
  a core feature:
    - Haskell: `data Expr = Num Double | Add Expr Expr | Mul Expr Expr`
    - Scala 3: `enum Expr { case Num(v: Double); case Add(l: Expr, r: Expr) }`
    - Kotlin: `sealed class Expr { data class Num(val v: Double) : Expr() }`
- Java's sealed interfaces + records achieve the same thing:
    - `sealed interface Expression permits Num, Add, Mul, Neg, Var`
    - Each variant is a record with its own fields
- **Expression trees / ASTs**: A classic ADT use case is modeling
  arithmetic expressions as a tree structure. Each node is a
  variant (Num, Add, Mul, Neg, Var), and recursive evaluation
  or transformation is done via pattern matching on the sealed
  interface.
- **Benefits over visitor pattern**: Traditional Java would use
  the Visitor pattern for operations on type hierarchies. Sealed
  types + pattern matching are more concise, more readable, and
  don't require the boilerplate of accept/visit methods.
*/

// ============================================================
// Section 6: Practical Patterns and Design Guidelines
// ============================================================

/*
## Practical Patterns and Design Guidelines

- **Sealed vs final vs open**:
    - `final` — no extension at all (single concrete type)
    - `sealed` — controlled extension (fixed set of subtypes)
    - open (default) — anyone can extend (traditional Java)
  Choose sealed when you need a **known, finite set** of variants.
- **Sealed vs enums**:
    - Enums — each variant is a **singleton** (no per-instance data)
    - Sealed — each variant can carry **different data** (fields)
    - Use enums for simple flags/categories, sealed for rich domain
      types where each variant has its own shape.
- **Domain modeling guidelines**:
    - Model states as sealed interfaces: PaymentState, OrderStatus
    - Model results as sealed interfaces: Result<T>, Validation<T>
    - Model commands/events as sealed interfaces in CQRS/ES systems
    - Each variant as a record = immutable, transparent, equals/hashCode for free
- **`non-sealed` extension points**: Use non-sealed sparingly,
  when one branch of the hierarchy should be open (e.g., a
  plugin system where third parties provide implementations).
- **State machines**: Sealed interfaces model finite state machines
  naturally — each state is a variant, transitions are methods
  that take one state and return another, and the compiler
  ensures all states are handled.
*/

public class SealedClassesAndInterfaces {

    // ---- Section 1: Shape hierarchy ----

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double a, double b, double c) implements Shape {}

    // ---- Section 2: Vehicle hierarchy (three modifier types) ----

    static sealed abstract class Vehicle permits Car, Truck, Motorcycle {}
    static final class Car extends Vehicle {
        private final String make;
        private final String model;
        Car(String make, String model) { this.make = make; this.model = model; }
        String make() { return make; }
        String model() { return model; }
        @Override public String toString() { return "Car[make=" + make + ", model=" + model + "]"; }
    }
    sealed static class Truck extends Vehicle permits HeavyTruck, LightTruck {}
    static final class HeavyTruck extends Truck {
        @Override public String toString() { return "HeavyTruck"; }
    }
    static final class LightTruck extends Truck {
        @Override public String toString() { return "LightTruck"; }
    }
    static non-sealed class Motorcycle extends Vehicle {
        @Override public String toString() { return "Motorcycle"; }
    }

    // ---- Section 3: Sealed interfaces with generics, records, enums ----

    sealed interface Result<T> permits Success, Failure {}
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {}

    sealed interface Loggable permits LogLevel {}
    enum LogLevel implements Loggable { DEBUG, INFO, WARN, ERROR }

    // ---- Section 5: Expression tree (ADTs) ----

    sealed interface Expression permits Num, Add, Mul, Neg, Var {}
    record Num(double value) implements Expression {}
    record Add(Expression left, Expression right) implements Expression {}
    record Mul(Expression left, Expression right) implements Expression {}
    record Neg(Expression expr) implements Expression {}
    record Var(String name) implements Expression {}

    // ---- Section 6: Payment state machine + Validation ----

    sealed interface PaymentState permits Pending, Authorized, Captured, Declined, Refunded {}
    record Pending(String orderId, double amount) implements PaymentState {}
    record Authorized(String orderId, double amount, String authCode) implements PaymentState {}
    record Captured(String orderId, double amount, String transactionId) implements PaymentState {}
    record Declined(String orderId, String reason) implements PaymentState {}
    record Refunded(String orderId, double amount, String refundId) implements PaymentState {}

    sealed interface Validation<T> permits Valid, Invalid {}
    record Valid<T>(T value) implements Validation<T> {}
    record Invalid<T>(List<String> errors) implements Validation<T> {}

    // ============================================================
    // Section 1: Introduction to Sealed Classes
    // ============================================================

    static void introductionToSealedClasses() {
        System.out.println("=== Section 1: Introduction to Sealed Classes ===");

        // Create Shape instances
        Shape circle = new Circle(5.0);
        Shape rectangle = new Rectangle(4.0, 6.0);
        Shape triangle = new Triangle(3.0, 4.0, 5.0);

        System.out.println("circle:    " + circle);
        System.out.println("rectangle: " + rectangle);
        System.out.println("triangle:  " + triangle);

        // instanceof pattern matching — the permitted subtypes are known
        List<Shape> shapes = List.of(circle, rectangle, triangle);
        for (var shape : shapes) {
            if (shape instanceof Circle c) {
                System.out.println("Circle with radius " + c.radius()
                        + " → area = " + (Math.PI * c.radius() * c.radius()));
            } else if (shape instanceof Rectangle r) {
                System.out.println("Rectangle " + r.width() + "x" + r.height()
                        + " → area = " + (r.width() * r.height()));
            } else if (shape instanceof Triangle t) {
                // Heron's formula
                double s = (t.a() + t.b() + t.c()) / 2;
                double area = Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
                System.out.println("Triangle (" + t.a() + ", " + t.b() + ", " + t.c()
                        + ") → area = " + area);
            }
            // No else needed — compiler knows these are all the options
        }

        // The sealed interface guarantees a closed set of subtypes
        System.out.println("\nShape is sealed: " + Shape.class.isSealed());
        System.out.println("Permitted subtypes of Shape:");
        for (var subclass : Shape.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
    }

    // ============================================================
    // Section 2: Syntax and Rules
    // ============================================================

    static void syntaxAndRules() {
        System.out.println("\n=== Section 2: Syntax and Rules ===");

        // Vehicle hierarchy demonstrates all three required modifiers
        var car = new Car("Toyota", "Camry");
        var heavyTruck = new HeavyTruck();
        var lightTruck = new LightTruck();
        var motorcycle = new Motorcycle();

        System.out.println("car:        " + car);
        System.out.println("heavyTruck: " + heavyTruck);
        System.out.println("lightTruck: " + lightTruck);
        System.out.println("motorcycle: " + motorcycle);

        // Show the three modifier types
        System.out.println("\n--- Modifier types in the Vehicle hierarchy ---");
        System.out.println("Vehicle is sealed: " + Vehicle.class.isSealed());
        System.out.println("Car is final: " + java.lang.reflect.Modifier.isFinal(Car.class.getModifiers()));
        System.out.println("Truck is sealed: " + Truck.class.isSealed());
        System.out.println("Motorcycle is non-sealed (isSealed=false, isFinal=false): "
                + "isSealed=" + Motorcycle.class.isSealed()
                + ", isFinal=" + java.lang.reflect.Modifier.isFinal(Motorcycle.class.getModifiers()));

        // Reflection API: getPermittedSubclasses()
        System.out.println("\nPermitted subtypes of Vehicle:");
        for (var subclass : Vehicle.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
        System.out.println("Permitted subtypes of Truck:");
        for (var subclass : Truck.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }

        // non-sealed allows further extension
        class SportBike extends Motorcycle {
            @Override public String toString() { return "SportBike (extends Motorcycle)"; }
        }
        var sportBike = new SportBike();
        System.out.println("\nnon-sealed allows extension: " + sportBike);
        System.out.println("SportBike is a Vehicle: " + (sportBike instanceof Vehicle));
    }

    // ============================================================
    // Section 3: Sealed Interfaces
    // ============================================================

    static void sealedInterfaces() {
        System.out.println("\n=== Section 3: Sealed Interfaces ===");

        // Result<T> with Success/Failure — a classic sum type
        Result<String> success = new Success<>("Hello, World!");
        Result<String> failure = new Failure<>("Connection timeout");

        System.out.println("success: " + success);
        System.out.println("failure: " + failure);

        // Simulate service calls returning Result<T>
        System.out.println("\n--- Simulating service calls ---");
        List<Result<Integer>> results = List.of(
                new Success<>(42),
                new Failure<>("Division by zero"),
                new Success<>(100),
                new Failure<>("Overflow"),
                new Success<>(7)
        );

        for (var result : results) {
            if (result instanceof Success<Integer> s) {
                System.out.println("  SUCCESS: value = " + s.value());
            } else if (result instanceof Failure<Integer> f) {
                System.out.println("  FAILURE: " + f.error());
            }
        }

        // Count successes and failures
        long successCount = results.stream().filter(r -> r instanceof Success).count();
        long failureCount = results.stream().filter(r -> r instanceof Failure).count();
        System.out.println("successes: " + successCount + ", failures: " + failureCount);

        // Enum implementing sealed interface
        System.out.println("\n--- Enum implementing sealed interface ---");
        System.out.println("LogLevel values: " + Arrays.toString(LogLevel.values()));
        System.out.println("LogLevel.DEBUG is Loggable: " + (LogLevel.DEBUG instanceof Loggable));
        System.out.println("Loggable is sealed: " + Loggable.class.isSealed());

        // Using LogLevel as Loggable
        Loggable loggable = LogLevel.WARN;
        if (loggable instanceof LogLevel level) {
            System.out.println("Log level: " + level + " (ordinal: " + level.ordinal() + ")");
        }
    }

    // ============================================================
    // Section 4: Sealed Classes and Pattern Matching
    // ============================================================

    static double area(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> {
                double s = (t.a() + t.b() + t.c()) / 2;
                yield Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
            }
            // No default needed — all permitted subtypes are covered!
        };
    }

    static <T> String formatResult(Result<T> result) {
        return switch (result) {
            case Success<T> s -> "OK: " + s.value();
            case Failure<T> f -> "ERROR: " + f.error();
        };
    }

    static void sealedClassesAndPatternMatching() {
        System.out.println("\n=== Section 4: Sealed Classes and Pattern Matching ===");

        // Exhaustive switch expression with area()
        List<Shape> shapes = List.of(
                new Circle(5.0),
                new Rectangle(4.0, 6.0),
                new Triangle(3.0, 4.0, 5.0),
                new Circle(1.0),
                new Rectangle(10.0, 2.5)
        );

        System.out.println("--- area() via switch expression (no default needed) ---");
        for (var shape : shapes) {
            System.out.printf("  %-30s → area = %.4f%n", shape, area(shape));
        }

        // formatResult() via switch
        System.out.println("\n--- formatResult() via switch expression ---");
        List<Result<String>> results = List.of(
                new Success<>("data loaded"),
                new Failure<>("timeout"),
                new Success<>("processed")
        );
        for (var result : results) {
            System.out.println("  " + formatResult(result));
        }

        // Guarded patterns with `when`
        System.out.println("\n--- Guarded patterns with `when` ---");
        List<Shape> moreShapes = List.of(
                new Circle(150.0),
                new Circle(3.0),
                new Rectangle(200.0, 300.0),
                new Rectangle(2.0, 3.0),
                new Triangle(3.0, 4.0, 5.0)
        );

        for (var shape : moreShapes) {
            String description = switch (shape) {
                case Circle c when c.radius() > 100 -> "Large circle (r=" + c.radius() + ")";
                case Circle c -> "Small circle (r=" + c.radius() + ")";
                case Rectangle r when r.width() * r.height() > 10000 -> "Huge rectangle (" + r.width() + "x" + r.height() + ")";
                case Rectangle r -> "Normal rectangle (" + r.width() + "x" + r.height() + ")";
                case Triangle t -> "Triangle (" + t.a() + ", " + t.b() + ", " + t.c() + ")";
            };
            System.out.println("  " + description);
        }

        // Process a list of shapes — total area
        double totalArea = shapes.stream()
                .mapToDouble(SealedClassesAndInterfaces::area)
                .sum();
        System.out.printf("\nTotal area of all shapes: %.4f%n", totalArea);
    }

    // ============================================================
    // Section 5: Sealed Classes with Records — Algebraic Data Types
    // ============================================================

    static double evaluate(Expression expr, Map<String, Double> env) {
        return switch (expr) {
            case Num n -> n.value();
            case Add a -> evaluate(a.left(), env) + evaluate(a.right(), env);
            case Mul m -> evaluate(m.left(), env) * evaluate(m.right(), env);
            case Neg n -> -evaluate(n.expr(), env);
            case Var v -> {
                Double value = env.get(v.name());
                if (value == null) throw new IllegalArgumentException("Unknown variable: " + v.name());
                yield value;
            }
        };
    }

    static String prettyPrint(Expression expr) {
        return switch (expr) {
            case Num n -> String.valueOf(n.value());
            case Add a -> "(" + prettyPrint(a.left()) + " + " + prettyPrint(a.right()) + ")";
            case Mul m -> "(" + prettyPrint(m.left()) + " * " + prettyPrint(m.right()) + ")";
            case Neg n -> "-(" + prettyPrint(n.expr()) + ")";
            case Var v -> v.name();
        };
    }

    static void sealedClassesWithRecordsADTs() {
        System.out.println("\n=== Section 5: Sealed Classes with Records — Algebraic Data Types ===");

        // Build expression tree: (x + 2) * -(3 + x)
        Expression expr = new Mul(
                new Add(new Var("x"), new Num(2)),
                new Neg(new Add(new Num(3), new Var("x")))
        );

        System.out.println("Expression: " + prettyPrint(expr));

        // Evaluate with x = 5
        var env = Map.of("x", 5.0);
        double result = evaluate(expr, env);
        System.out.println("With x=5:   " + prettyPrint(expr) + " = " + result);
        System.out.println("Expected:   (5 + 2) * -(3 + 5) = 7 * -8 = -56.0");

        // Evaluate with different values
        System.out.println("\n--- Evaluating with different x values ---");
        for (int x = -3; x <= 3; x++) {
            var e = Map.of("x", (double) x);
            System.out.printf("  x=%2d → %s = %.1f%n", x, prettyPrint(expr), evaluate(expr, e));
        }

        // More expressions
        System.out.println("\n--- More expression examples ---");

        Expression simple = new Add(new Num(1), new Num(2));
        System.out.println(prettyPrint(simple) + " = " + evaluate(simple, Map.of()));

        Expression nested = new Neg(new Mul(new Num(3), new Add(new Num(4), new Num(5))));
        System.out.println(prettyPrint(nested) + " = " + evaluate(nested, Map.of()));

        Expression withVars = new Add(
                new Mul(new Var("a"), new Var("b")),
                new Neg(new Var("c"))
        );
        var multiEnv = Map.of("a", 2.0, "b", 3.0, "c", 1.0);
        System.out.println(prettyPrint(withVars) + " with a=2, b=3, c=1 = " + evaluate(withVars, multiEnv));

        // Show that sealed + records give us true ADTs
        System.out.println("\n--- Expression hierarchy ---");
        System.out.println("Expression is sealed: " + Expression.class.isSealed());
        System.out.println("Permitted subtypes:");
        for (var subclass : Expression.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
    }

    // ============================================================
    // Section 6: Practical Patterns and Design Guidelines
    // ============================================================

    // Payment state machine transitions
    static PaymentState authorize(Pending pending, String authCode) {
        System.out.println("  Authorizing order " + pending.orderId() + "...");
        return new Authorized(pending.orderId(), pending.amount(), authCode);
    }

    static PaymentState capture(Authorized authorized) {
        System.out.println("  Capturing order " + authorized.orderId() + "...");
        return new Captured(authorized.orderId(), authorized.amount(), "TXN-" + System.nanoTime());
    }

    static PaymentState decline(Pending pending, String reason) {
        System.out.println("  Declining order " + pending.orderId() + "...");
        return new Declined(pending.orderId(), reason);
    }

    static PaymentState refund(Captured captured) {
        System.out.println("  Refunding order " + captured.orderId() + "...");
        return new Refunded(captured.orderId(), captured.amount(), "REF-" + System.nanoTime());
    }

    static String describePaymentState(PaymentState state) {
        return switch (state) {
            case Pending p -> "PENDING: order " + p.orderId() + ", amount $" + p.amount();
            case Authorized a -> "AUTHORIZED: order " + a.orderId() + ", auth code " + a.authCode();
            case Captured c -> "CAPTURED: order " + c.orderId() + ", txn " + c.transactionId();
            case Declined d -> "DECLINED: order " + d.orderId() + ", reason: " + d.reason();
            case Refunded r -> "REFUNDED: order " + r.orderId() + ", refund " + r.refundId();
        };
    }

    // Validation utilities
    static Validation<String> validateEmail(String email) {
        var errors = new ArrayList<String>();
        if (email == null || email.isBlank()) {
            errors.add("Email must not be blank");
        } else {
            if (!email.contains("@")) errors.add("Email must contain @");
            if (!email.contains(".")) errors.add("Email must contain a dot");
            if (email.length() < 5) errors.add("Email must be at least 5 characters");
        }
        return errors.isEmpty() ? new Valid<>(email) : new Invalid<>(errors);
    }

    static <T, U> Validation<U> map(Validation<T> validation, java.util.function.Function<T, U> fn) {
        return switch (validation) {
            case Valid<T> v -> new Valid<>(fn.apply(v.value()));
            case Invalid<T> i -> new Invalid<>(i.errors());
        };
    }

    static <T> Validation<T> combine(Validation<T> first, Validation<T> second) {
        return switch (first) {
            case Valid<T> v -> second;
            case Invalid<T> i1 -> switch (second) {
                case Valid<T> v -> first;
                case Invalid<T> i2 -> {
                    var allErrors = new ArrayList<>(i1.errors());
                    allErrors.addAll(i2.errors());
                    yield new Invalid<>(allErrors);
                }
            };
        };
    }

    static void practicalPatternsAndDesignGuidelines() {
        System.out.println("\n=== Section 6: Practical Patterns and Design Guidelines ===");

        // Payment state machine — happy path
        System.out.println("--- Payment state machine: happy path ---");
        PaymentState state = new Pending("ORD-001", 99.99);
        System.out.println("  " + describePaymentState(state));

        state = authorize((Pending) state, "AUTH-12345");
        System.out.println("  " + describePaymentState(state));

        state = capture((Authorized) state);
        System.out.println("  " + describePaymentState(state));

        // Payment state machine — decline path
        System.out.println("\n--- Payment state machine: decline path ---");
        PaymentState state2 = new Pending("ORD-002", 5000.00);
        System.out.println("  " + describePaymentState(state2));

        state2 = decline((Pending) state2, "Insufficient funds");
        System.out.println("  " + describePaymentState(state2));

        // Payment state machine — refund path
        System.out.println("\n--- Payment state machine: refund path ---");
        PaymentState state3 = new Pending("ORD-003", 249.50);
        state3 = authorize((Pending) state3, "AUTH-67890");
        state3 = capture((Authorized) state3);
        System.out.println("  " + describePaymentState(state3));

        state3 = refund((Captured) state3);
        System.out.println("  " + describePaymentState(state3));

        // Validation<T>
        System.out.println("\n--- Validation<T> ---");
        List<String> testEmails = List.of(
                "user@example.com",
                "invalid",
                "",
                "a@b",
                "hello@world.org"
        );

        for (var email : testEmails) {
            var validation = validateEmail(email);
            String desc = switch (validation) {
                case Valid<String> v -> "VALID: " + v.value();
                case Invalid<String> i -> "INVALID: " + i.errors();
            };
            System.out.println("  \"" + email + "\" → " + desc);
        }

        // Mapping validations
        System.out.println("\n--- Mapping validations ---");
        var valid = validateEmail("user@example.com");
        var mapped = map(valid, String::toUpperCase);
        System.out.println("  map(valid email, toUpperCase): " + mapped);

        var invalid = validateEmail("bad");
        var mappedInvalid = map(invalid, String::toUpperCase);
        System.out.println("  map(invalid email, toUpperCase): " + mappedInvalid);

        // Combining validations
        System.out.println("\n--- Combining validations ---");
        Validation<String> v1 = new Invalid<>(List.of("too short"));
        Validation<String> v2 = new Invalid<>(List.of("missing @", "missing dot"));
        var combined = combine(v1, v2);
        System.out.println("  combine(invalid, invalid): " + combined);

        Validation<String> v3 = new Valid<>("ok@test.com");
        var combined2 = combine(v3, v2);
        System.out.println("  combine(valid, invalid): " + combined2);
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        introductionToSealedClasses();
        syntaxAndRules();
        sealedInterfaces();
        sealedClassesAndPatternMatching();
        sealedClassesWithRecordsADTs();
        practicalPatternsAndDesignGuidelines();
    }
}
