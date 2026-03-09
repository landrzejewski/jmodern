package pl.training.jmodern.module02_java21;

import java.util.*;

// ============================================================
// Section 1: Pattern Matching for switch -- Type Patterns and Null
// ============================================================

/*
## Pattern Matching for switch -- Type Patterns and Null

- JEP 441 finalized pattern matching for switch in Java 21,
  after preview rounds in Java 17 (JEP 406), 18, 19, 20.
- **Type patterns in switch**: You can now write
  case String s -> ... to match and bind in one step. This
  replaces verbose instanceof + cast chains with a single
  switch expression or statement.
- **Switching on Object**: Prior to Java 21, switch only
  supported a few types (int, String, enum). Now you can
  switch on any reference type and match with type patterns.
- **case null**: Before Java 21, passing null to a switch
  always threw NullPointerException. Now you can explicitly
  handle null with case null -> ... which prevents the NPE.
  You can also combine null with default: case null, default ->
- **Exhaustiveness with sealed types**: When the switch
  expression covers all permitted subtypes of a sealed
  interface, no default branch is required. The compiler
  verifies exhaustiveness at compile time.
- **Difference from Java 17 previews**: The syntax is the
  same, but Java 21 is the first version where this is a
  permanent, non-preview feature. Code using it no longer
  needs --enable-preview.
*/

// ============================================================
// Section 2: Pattern Matching for switch -- Guarded Patterns and Dominance
// ============================================================

/*
## Pattern Matching for switch -- Guarded Patterns and Dominance

- **Guarded patterns** use the when keyword to add a boolean
  condition to a case label:
      case String s when s.length() > 5 -> ...
  The pattern matches only if the type matches AND the guard
  evaluates to true. This replaces nested if-else inside case
  bodies.
- **Dominance rules**: A more specific pattern must appear
  before a more general one. For example, case String s must
  come before case Object o, and a guarded case must come
  before its unguarded counterpart. Violating dominance is a
  compile-time error -- unlike if-else chains where ordering
  bugs are silent.
- **Combining type + guard** enables rich dispatch logic that
  was previously only possible with long if-else chains or
  the visitor pattern.
- **Sealed types + guards**: You can combine sealed-type
  exhaustiveness with when guards to create powerful yet
  type-safe dispatch. The compiler still checks that all
  permitted subtypes are covered.
*/

// ============================================================
// Section 3: Record Patterns -- Deconstruction
// ============================================================

/*
## Record Patterns -- Deconstruction

- JEP 440 finalized record patterns in Java 21 (previewed
  in Java 19 and 20).
- **Syntax**: case Point(var x, var y) -> ... destructures
  a record directly in the case label, binding its components
  to local variables.
- You can use explicit types (case Point(double x, double y))
  or var for type inference.
- Record patterns work in both switch and instanceof:
      if (obj instanceof Point(var x, var y)) { ... }
- **Structural decomposition vs accessor calls**: Record
  patterns decompose the structure in one step, rather than
  calling p.x() and p.y() separately. This is especially
  powerful when nested (see Section 4).
- **Records only**: Record patterns only work with record
  types. Regular classes cannot be deconstructed this way
  (unless future JEPs add deconstruction patterns for classes).
- **Difference from the Java 17 module teaser**: Records.java
  Section 5 briefly showed the idea. Here we go deeper with
  multiple components, combinations with when guards, and
  practical classification examples.
*/

// ============================================================
// Section 4: Record Patterns -- Nested and Complex Patterns
// ============================================================

/*
## Record Patterns -- Nested and Complex Patterns

- Record patterns can be **nested**: if a record's component
  is itself a record, you can deconstruct both levels at once:
      case ColoredPoint(Point(var x, var y), var color) -> ...
  This eliminates intermediate variables and expresses intent
  more directly.
- **Recursive pattern matching**: Sealed interface hierarchies
  (like expression trees) are a natural fit. You can evaluate
  an expression tree with a single switch using nested record
  patterns -- no visitor pattern needed.
- **Combining nested patterns with when guards** lets you
  express complex conditions concisely, e.g., matching a pair
  where the first element satisfies some condition.
- **Practical ADT (Algebraic Data Type) processing**: Sealed
  interfaces + records + pattern matching together give Java
  a form of algebraic data types similar to Scala case classes
  or Rust enums with match.
*/

// ============================================================
// Section 5: Sequenced Collections -- SequencedCollection and SequencedSet
// ============================================================

/*
## Sequenced Collections -- SequencedCollection and SequencedSet

- JEP 431 (Java 21) introduced three new interfaces:
  SequencedCollection, SequencedSet, and SequencedMap.
- **The problem**: Before Java 21, there was no uniform API to
  access the first and last elements of ordered collections.
  Each collection had its own way:
    - List: get(0) / get(size()-1)
    - Deque: getFirst() / getLast()
    - SortedSet: first() / last()
    - LinkedHashSet: iterator().next() / no easy last access
  This inconsistency made generic programming with ordered
  collections unnecessarily difficult.
- **SequencedCollection extends Collection** and adds:
    - addFirst(E) / addLast(E)
    - getFirst() / getLast()
    - removeFirst() / removeLast()
    - reversed() -- returns a reversed-order view
- **Retrofitted** to existing classes: ArrayList, LinkedList,
  ArrayDeque, LinkedHashSet, TreeSet, and more. These classes
  now implement SequencedCollection (or SequencedSet).
- **reversed() returns a view**, not a copy. Modifications
  through the reversed view are reflected in the original
  collection and vice versa.
- **SequencedSet extends SequencedCollection and Set** -- it
  adds no new methods but narrows reversed() to return a
  SequencedSet.
*/

// ============================================================
// Section 6: Sequenced Collections -- SequencedMap
// ============================================================

/*
## Sequenced Collections -- SequencedMap

- **SequencedMap extends Map** and adds encounter-order-aware
  operations:
    - firstEntry() / lastEntry() -- return Map.Entry or null
    - putFirst(K, V) / putLast(K, V) -- insert or move entry
      to first/last position
    - pollFirstEntry() / pollLastEntry() -- remove and return
    - sequencedKeySet() -- returns a SequencedSet of keys
    - sequencedValues() -- returns a SequencedCollection of values
    - sequencedEntrySet() -- returns a SequencedSet of entries
    - reversed() -- returns a reversed SequencedMap view
- **Retrofitted** to LinkedHashMap, TreeMap, and
  ConcurrentSkipListMap.
- **LinkedHashMap** now has predictable first/last access.
  Before Java 21, getting the first or last entry of a
  LinkedHashMap required iterating (no direct API).
- **putFirst / putLast** can reorder existing entries in a
  LinkedHashMap. If the key already exists, putFirst moves
  it to the first position (and updates the value).
- **reversed()** on a SequencedMap returns a view where
  iteration, firstEntry/lastEntry, and stream operations
  all reflect the reversed order.
*/

// ============================================================
// Section 7: Unnamed Variables and Patterns (JEP 456, Java 22)
// ============================================================

/*
## Unnamed Variables and Patterns

- JEP 456 finalized unnamed variables and patterns in Java 22
  (previewed in Java 21 via JEP 443).
- **The problem**: Before Java 22, unused variables needed dummy
  names like _unused, ignored, or tmp. This obscured the
  developer's intent and triggered IDE warnings.
- **The underscore `_`** is now a reserved keyword that signals
  "this value is intentionally unused." It was a legal identifier
  in Java 8, deprecated in Java 9, and fully reserved in Java 22.
- **7 supported contexts**:
  1. Local variable declarations: var _ = someMethod();
  2. Enhanced for loops: for (var _ : collection)
  3. Try-with-resources: try (var _ = acquireResource())
  4. Catch blocks: catch (SomeException _)
  5. Lambda parameters: (_, value) -> process(value)
  6. Pattern variables: case Integer _ -> "int"
  7. Record pattern components: case Point(var x, _) -> x
- **Multiple `_` can coexist** in the same scope, unlike named
  variables. This is especially useful in nested loops or
  patterns where several values are unused.
- **Pairs naturally with record patterns** from Sections 3-4:
  when deconstructing records, you can ignore components you
  don't need without inventing throwaway names.
*/

public class OtherChanges {

    // ---- Inner types for pattern matching demos ----

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double a, double b, double c) implements Shape {}

    // Expression tree for nested record patterns
    sealed interface Expression permits Num, Add, Mul, Neg {}
    record Num(double value) implements Expression {}
    record Add(Expression left, Expression right) implements Expression {}
    record Mul(Expression left, Expression right) implements Expression {}
    record Neg(Expression expr) implements Expression {}

    // For guarded patterns demo
    record Person(String name, int age) {}

    // For nested record pattern demos
    record Point(double x, double y) {}
    record ColoredPoint(Point point, String color) {}
    record Pair<A, B>(A first, B second) {}

    // ============================================================
    // Section 1: Pattern Matching for switch -- Type Patterns and Null
    // ============================================================

    static void patternMatchingTypePatterns() {
        System.out.println("=== Section 1: Pattern Matching for switch -- Type Patterns and Null ===");

        // ---- Switch on Object with type patterns ----
        System.out.println("--- Switch on Object with type patterns ---");
        Object[] values = {"Hello", 42, 3.14, List.of(1, 2, 3), null, new int[]{10, 20}};

        for (Object obj : values) {
            String description = switch (obj) {
                case null -> "null value (no NPE!)";
                case String s -> "String of length " + s.length() + ": \"" + s + "\"";
                case Integer i -> "Integer: " + i + " (even: " + (i % 2 == 0) + ")";
                case Double d -> "Double: " + String.format("%.2f", d);
                case List<?> list -> "List with " + list.size() + " elements: " + list;
                default -> "Other type: " + obj.getClass().getSimpleName();
            };
            System.out.println("  " + obj + " -> " + description);
        }

        // ---- case null prevents NPE ----
        System.out.println("\n--- case null prevents NPE ---");
        String input = null;
        String result = switch (input) {
            case null -> "handled null safely";
            case String s when s.isBlank() -> "blank string";
            case String s -> "non-blank: " + s;
        };
        System.out.println("  null input -> " + result);

        // ---- Exhaustive switch on sealed type (no default needed) ----
        System.out.println("\n--- Exhaustive switch on sealed Shape (no default) ---");
        Shape[] shapes = {new Circle(5), new Rectangle(3, 4), new Triangle(3, 4, 5)};

        for (Shape shape : shapes) {
            String info = switch (shape) {
                case Circle c -> "Circle with radius " + c.radius();
                case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
                case Triangle t -> "Triangle with sides " + t.a() + ", " + t.b() + ", " + t.c();
                // No default needed -- compiler knows all Shape subtypes
            };
            System.out.println("  " + info);
        }
    }

    // ============================================================
    // Section 2: Pattern Matching for switch -- Guarded Patterns and Dominance
    // ============================================================

    static void guardedPatternsAndDominance() {
        System.out.println("\n=== Section 2: Pattern Matching for switch -- Guarded Patterns and Dominance ===");

        // ---- Person age classification with when guards ----
        System.out.println("--- Person age classification with when guards ---");
        var people = List.of(
                new Person("Alice", 5),
                new Person("Bob", 15),
                new Person("Charlie", 30),
                new Person("Diana", 70)
        );

        for (Person p : people) {
            String category = switch (p) {
                case Person(var name, var age) when age < 13 -> name + " is a child";
                case Person(var name, var age) when age < 18 -> name + " is a teenager";
                case Person(var name, var age) when age < 65 -> name + " is an adult";
                case Person(var name, var age) -> name + " is a senior";
            };
            System.out.println("  " + category);
        }

        // ---- Shape area filtering with when guards ----
        System.out.println("\n--- Shape area filtering with when guards ---");
        Shape[] shapes = {
                new Circle(1), new Circle(10),
                new Rectangle(2, 3), new Rectangle(20, 30),
                new Triangle(3, 4, 5)
        };

        for (Shape shape : shapes) {
            String result = switch (shape) {
                case Circle c when c.radius() > 5 -> "Large circle (r=" + c.radius() + ")";
                case Circle c -> "Small circle (r=" + c.radius() + ")";
                case Rectangle r when r.width() * r.height() > 100 -> "Large rectangle (" + r.width() + "x" + r.height() + ")";
                case Rectangle r -> "Small rectangle (" + r.width() + "x" + r.height() + ")";
                case Triangle t -> "Triangle (" + t.a() + ", " + t.b() + ", " + t.c() + ")";
            };
            System.out.println("  " + result);
        }

        // ---- Dominance rules (compile-time enforcement) ----
        System.out.println("\n--- Dominance rules ---");
        System.out.println("  Dominance = specific patterns must come before general ones.");
        System.out.println("  Example: case String s MUST come before case Object o");
        System.out.println("  Example: case Circle c when c.radius() > 5 MUST come before case Circle c");
        System.out.println("  Violating dominance is a COMPILE ERROR (not a silent bug).");

        // Uncommenting the following would cause a compile error:
        // String test = switch ((Object) "hello") {
        //     case Object o -> "object";       // ERROR: dominates the String case below
        //     case String s -> "string";        // unreachable
        // };

        // ---- Sealed type dispatch with guards ----
        System.out.println("\n--- Sealed type + guards for area calculation ---");
        Shape[] moreShapes = {new Circle(5), new Rectangle(3, 4), new Triangle(3, 4, 5)};

        for (Shape shape : moreShapes) {
            double area = switch (shape) {
                case Circle c -> Math.PI * c.radius() * c.radius();
                case Rectangle r -> r.width() * r.height();
                case Triangle t -> {
                    double s = (t.a() + t.b() + t.c()) / 2;
                    yield Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
                }
            };
            String label = switch (shape) {
                case Circle c -> "Circle(r=" + c.radius() + ")";
                case Rectangle r -> "Rect(" + r.width() + "x" + r.height() + ")";
                case Triangle t -> "Tri(" + t.a() + "," + t.b() + "," + t.c() + ")";
            };
            System.out.printf("  %-20s area = %.2f%n", label, area);
        }
    }

    // ============================================================
    // Section 3: Record Patterns -- Deconstruction
    // ============================================================

    static void recordPatternDeconstruction() {
        System.out.println("\n=== Section 3: Record Patterns -- Deconstruction ===");

        // ---- Point quadrant classification via deconstruction ----
        System.out.println("--- Point quadrant classification ---");
        var points = List.of(
                new Point(3, 4), new Point(-2, 5),
                new Point(-1, -3), new Point(7, -2),
                new Point(0, 0)
        );

        for (Point p : points) {
            String quadrant = switch (p) {
                case Point(var x, var y) when x > 0 && y > 0 -> "Quadrant I";
                case Point(var x, var y) when x < 0 && y > 0 -> "Quadrant II";
                case Point(var x, var y) when x < 0 && y < 0 -> "Quadrant III";
                case Point(var x, var y) when x > 0 && y < 0 -> "Quadrant IV";
                case Point(var x, var y) -> "On axis/origin";
            };
            System.out.printf("  (%.0f, %.0f) -> %s%n", p.x(), p.y(), quadrant);
        }

        // ---- Person age check via deconstruction ----
        System.out.println("\n--- Person deconstruction in switch ---");
        var person = new Person("Alice", 30);
        String info = switch (person) {
            case Person(var name, var age) when age >= 18 -> name + " is an adult (age " + age + ")";
            case Person(var name, var age) -> name + " is a minor (age " + age + ")";
        };
        System.out.println("  " + info);

        // ---- instanceof with record pattern ----
        System.out.println("\n--- instanceof with record pattern ---");
        Object obj = new Point(10, 20);
        if (obj instanceof Point(var x, var y)) {
            System.out.println("  Deconstructed via instanceof: x=" + x + ", y=" + y);
            System.out.println("  Distance from origin: " + Math.sqrt(x * x + y * y));
        }

        // ---- Comparison: deconstruction vs accessor style ----
        System.out.println("\n--- Deconstruction vs accessor style ---");
        var rect = new Rectangle(5, 10);

        // Accessor style (traditional)
        double area1 = rect.width() * rect.height();

        // Deconstruction style (Java 21)
        double area2 = switch (rect) {
            case Rectangle(var w, var h) -> w * h;
        };

        System.out.println("  Accessor style area: " + area1);
        System.out.println("  Deconstruction style area: " + area2);
        System.out.println("  Both equivalent, but deconstruction shines with nested patterns");
    }

    // ============================================================
    // Section 4: Record Patterns -- Nested and Complex Patterns
    // ============================================================

    static void nestedRecordPatterns() {
        System.out.println("\n=== Section 4: Record Patterns -- Nested and Complex Patterns ===");

        // ---- ColoredPoint nested deconstruction ----
        System.out.println("--- ColoredPoint nested deconstruction ---");
        var coloredPoints = List.of(
                new ColoredPoint(new Point(1, 2), "red"),
                new ColoredPoint(new Point(-3, 4), "blue"),
                new ColoredPoint(new Point(0, 0), "green")
        );

        for (var cp : coloredPoints) {
            String desc = switch (cp) {
                case ColoredPoint(Point(var x, var y), var color) when x == 0 && y == 0 ->
                        color + " point at origin";
                case ColoredPoint(Point(var x, var y), var color) ->
                        color + " point at (" + x + ", " + y + ")";
            };
            System.out.println("  " + desc);
        }

        // ---- Expression tree evaluation via switch with record patterns ----
        System.out.println("\n--- Expression tree evaluation ---");

        // Build: (2 + 3) * 4
        Expression expr1 = new Mul(new Add(new Num(2), new Num(3)), new Num(4));
        System.out.println("  (2 + 3) * 4 = " + evaluate(expr1));

        // Build: -(5 + 3)
        Expression expr2 = new Neg(new Add(new Num(5), new Num(3)));
        System.out.println("  -(5 + 3) = " + evaluate(expr2));

        // Build: (10 * 2) + (-(3))
        Expression expr3 = new Add(new Mul(new Num(10), new Num(2)), new Neg(new Num(3)));
        System.out.println("  (10 * 2) + (-(3)) = " + evaluate(expr3));

        // ---- Pretty-print expressions ----
        System.out.println("\n--- Pretty-print expressions ---");
        System.out.println("  " + prettyPrint(expr1));
        System.out.println("  " + prettyPrint(expr2));
        System.out.println("  " + prettyPrint(expr3));

        // ---- Pair deconstruction ----
        System.out.println("\n--- Pair deconstruction ---");
        var pairs = List.of(
                new Pair<>(new Point(1, 2), new Point(3, 4)),
                new Pair<>(new Point(0, 0), new Point(5, 5)),
                new Pair<>(new Point(-1, -1), new Point(1, 1))
        );

        for (var pair : pairs) {
            if (pair instanceof Pair(Point(var x1, var y1), Point(var x2, var y2))) {
                double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                System.out.printf("  Distance from (%.0f,%.0f) to (%.0f,%.0f) = %.2f%n",
                        x1, y1, x2, y2, distance);
            }
        }
    }

    static double evaluate(Expression expr) {
        return switch (expr) {
            case Num(var value) -> value;
            case Add(var left, var right) -> evaluate(left) + evaluate(right);
            case Mul(var left, var right) -> evaluate(left) * evaluate(right);
            case Neg(var inner) -> -evaluate(inner);
        };
    }

    static String prettyPrint(Expression expr) {
        return switch (expr) {
            case Num(var value) -> String.valueOf(value);
            case Add(var left, var right) -> "(" + prettyPrint(left) + " + " + prettyPrint(right) + ")";
            case Mul(var left, var right) -> "(" + prettyPrint(left) + " * " + prettyPrint(right) + ")";
            case Neg(var inner) -> "(-" + prettyPrint(inner) + ")";
        };
    }

    // ============================================================
    // Section 5: Sequenced Collections -- SequencedCollection and SequencedSet
    // ============================================================

    static void sequencedCollections() {
        System.out.println("\n=== Section 5: Sequenced Collections -- SequencedCollection and SequencedSet ===");

        // ---- ArrayList: addFirst/getLast/reversed ----
        System.out.println("--- ArrayList: addFirst / getLast / reversed ---");
        var list = new ArrayList<>(List.of("B", "C", "D"));
        System.out.println("  Original:    " + list);

        list.addFirst("A");
        list.addLast("E");
        System.out.println("  After addFirst(A), addLast(E): " + list);
        System.out.println("  getFirst(): " + list.getFirst());
        System.out.println("  getLast():  " + list.getLast());

        var reversed = list.reversed();
        System.out.println("  reversed():  " + reversed);
        System.out.println("  reversed() type: " + reversed.getClass().getSimpleName());

        // ---- LinkedHashSet: insertion-order sequenced access ----
        System.out.println("\n--- LinkedHashSet: sequenced access ---");
        var linkedSet = new LinkedHashSet<>(List.of("apple", "banana", "cherry", "date"));
        System.out.println("  Original:   " + linkedSet);
        System.out.println("  getFirst(): " + linkedSet.getFirst());
        System.out.println("  getLast():  " + linkedSet.getLast());

        var reversedSet = linkedSet.reversed();
        System.out.println("  reversed(): " + reversedSet);

        // ---- TreeSet: sorted + sequenced ----
        System.out.println("\n--- TreeSet: sorted + sequenced ---");
        var treeSet = new TreeSet<>(List.of(50, 10, 30, 20, 40));
        System.out.println("  TreeSet:    " + treeSet);
        System.out.println("  getFirst(): " + treeSet.getFirst());
        System.out.println("  getLast():  " + treeSet.getLast());
        System.out.println("  reversed(): " + treeSet.reversed());

        // ---- reversed() is a view, not a copy ----
        System.out.println("\n--- reversed() is a view (mutations reflect) ---");
        var original = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        var view = original.reversed();
        System.out.println("  Original: " + original);
        System.out.println("  View:     " + view);

        original.addLast(6);
        System.out.println("  After original.addLast(6):");
        System.out.println("    Original: " + original);
        System.out.println("    View:     " + view);

        view.addFirst(7);  // addFirst on reversed = addLast on original
        System.out.println("  After view.addFirst(7):");
        System.out.println("    Original: " + original);
        System.out.println("    View:     " + view);

        // ---- removeFirst / removeLast ----
        System.out.println("\n--- removeFirst / removeLast ---");
        var deque = new ArrayDeque<>(List.of("X", "Y", "Z"));
        System.out.println("  Before: " + deque);
        System.out.println("  removeFirst(): " + deque.removeFirst());
        System.out.println("  removeLast():  " + deque.removeLast());
        System.out.println("  After:  " + deque);
    }

    // ============================================================
    // Section 6: Sequenced Collections -- SequencedMap
    // ============================================================

    static void sequencedMaps() {
        System.out.println("\n=== Section 6: Sequenced Collections -- SequencedMap ===");

        // ---- LinkedHashMap: firstEntry / lastEntry ----
        System.out.println("--- LinkedHashMap: firstEntry / lastEntry ---");
        var map = new LinkedHashMap<String, Integer>();
        map.put("alpha", 1);
        map.put("beta", 2);
        map.put("gamma", 3);
        map.put("delta", 4);

        System.out.println("  Map:         " + map);
        System.out.println("  firstEntry(): " + map.firstEntry());
        System.out.println("  lastEntry():  " + map.lastEntry());

        // ---- putFirst reorders existing entries ----
        System.out.println("\n--- putFirst reorders entries ---");
        System.out.println("  Before putFirst(\"gamma\", 30): " + map);
        map.putFirst("gamma", 30);
        System.out.println("  After putFirst(\"gamma\", 30):  " + map);
        System.out.println("  gamma moved to first position and value updated");

        map.putLast("alpha", 100);
        System.out.println("  After putLast(\"alpha\", 100):  " + map);

        // ---- TreeMap: sorted + sequenced access ----
        System.out.println("\n--- TreeMap: sorted sequenced access ---");
        var treeMap = new TreeMap<String, Integer>();
        treeMap.put("cherry", 3);
        treeMap.put("apple", 1);
        treeMap.put("banana", 2);
        treeMap.put("date", 4);

        System.out.println("  TreeMap:      " + treeMap);
        System.out.println("  firstEntry(): " + treeMap.firstEntry());
        System.out.println("  lastEntry():  " + treeMap.lastEntry());

        // ---- sequencedKeySet / sequencedValues ----
        System.out.println("\n--- sequencedKeySet / sequencedValues ---");
        System.out.println("  sequencedKeySet(): " + treeMap.sequencedKeySet());
        System.out.println("  sequencedValues(): " + treeMap.sequencedValues());

        SequencedSet<String> reversedKeys = treeMap.sequencedKeySet().reversed();
        System.out.println("  reversed keys:     " + reversedKeys);

        // ---- Reversed map iteration ----
        System.out.println("\n--- Reversed map iteration ---");
        var reversedMap = treeMap.reversed();
        System.out.println("  Reversed TreeMap:");
        reversedMap.forEach((k, v) -> System.out.println("    " + k + " = " + v));

        System.out.println("  reversed().firstEntry(): " + reversedMap.firstEntry());
        System.out.println("  reversed().lastEntry():  " + reversedMap.lastEntry());

        // ---- pollFirstEntry / pollLastEntry ----
        System.out.println("\n--- pollFirstEntry / pollLastEntry ---");
        var pollMap = new LinkedHashMap<String, Integer>();
        pollMap.put("one", 1);
        pollMap.put("two", 2);
        pollMap.put("three", 3);
        pollMap.put("four", 4);

        System.out.println("  Before:           " + pollMap);
        System.out.println("  pollFirstEntry(): " + pollMap.pollFirstEntry());
        System.out.println("  pollLastEntry():  " + pollMap.pollLastEntry());
        System.out.println("  After:            " + pollMap);
    }

    // ============================================================
    // Section 7: Unnamed Variables and Patterns (JEP 456, Java 22)
    // ============================================================

    static void unnamedVariables() {
        System.out.println("\n=== Section 7: Unnamed Variables and Patterns (JEP 456, Java 22) ===");

        // ---- Enhanced for loop: count without using loop variable ----
        System.out.println("--- Enhanced for: count without using loop variable ---");
        var items = List.of("apple", "banana", "cherry", "date", "elderberry");
        int count = 0;
        for (var _ : items) {
            count++;
        }
        System.out.println("  Counted " + count + " items (loop variable unnamed with _)");

        // ---- Catch block with unnamed exception ----
        System.out.println("\n--- Catch block: unnamed exception variable ---");
        String[] inputs = {"42", "not_a_number", "100", "oops"};
        for (String input : inputs) {
            try {
                int value = Integer.parseInt(input);
                System.out.println("  Parsed \"" + input + "\" -> " + value);
            } catch (NumberFormatException _) {
                System.out.println("  Skipped \"" + input + "\" (not a number)");
            }
        }

        // ---- Lambda parameters with _ ----
        System.out.println("\n--- Lambda: unnamed parameters ---");
        var prices = new LinkedHashMap<String, Double>();
        prices.put("Coffee", 4.50);
        prices.put("Tea", 3.00);
        prices.put("Juice", 5.25);
        System.out.println("  Values only (key ignored via _):");
        prices.forEach((_, value) -> System.out.println("    $" + value));

        // ---- Pattern matching in switch with unnamed pattern variables ----
        System.out.println("\n--- Switch: unnamed pattern variables ---");
        Object[] values = {42, "hello", 3.14, List.of(1, 2), true};
        for (Object obj : values) {
            String type = switch (obj) {
                case Integer _ -> "integer";
                case String _ -> "string";
                case Double _ -> "double";
                case List<?> _ -> "list";
                default -> "other";
            };
            System.out.println("  " + obj + " -> " + type);
        }

        // ---- Record patterns with unnamed components ----
        System.out.println("\n--- Record patterns: unnamed components ---");
        var coloredPoints = List.of(
                new ColoredPoint(new Point(1, 2), "red"),
                new ColoredPoint(new Point(-3, 4), "blue"),
                new ColoredPoint(new Point(5, -1), "green")
        );
        System.out.println("  Colors only (point ignored):");
        for (var cp : coloredPoints) {
            if (cp instanceof ColoredPoint(_, var color)) {
                System.out.println("    " + color);
            }
        }
        System.out.println("  X coordinates only (y and color ignored):");
        for (var cp : coloredPoints) {
            if (cp instanceof ColoredPoint(Point(var x, _), _)) {
                System.out.println("    x = " + x);
            }
        }

        // ---- Multiple _ in same scope ----
        System.out.println("\n--- Multiple _ in same scope ---");
        var pairs = List.of(
                new Pair<>("Alice", 30),
                new Pair<>("Bob", 25),
                new Pair<>("Charlie", 35)
        );
        int pairCount = 0;
        for (var _ : pairs) {
            for (var _ : items) {  // two _ loop variables in nested scopes
                pairCount++;
            }
        }
        System.out.println("  Nested loops with two unnamed variables: " + pairCount + " iterations");
    }

    // ============================================================
    // Main -- run all sections
    // ============================================================

    public static void main(String[] args) {
        patternMatchingTypePatterns();
        guardedPatternsAndDominance();
        recordPatternDeconstruction();
        nestedRecordPatterns();
        sequencedCollections();
        sequencedMaps();
        unnamedVariables();
    }
}
