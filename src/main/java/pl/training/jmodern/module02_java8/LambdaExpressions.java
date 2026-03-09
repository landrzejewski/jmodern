package pl.training.jmodern.module02_java8;

import java.util.*;
import java.util.function.*;

// ============================================================
// Section 1: Introduction to Lambda Expressions
// ============================================================

/*
## Introduction to Lambda Expressions

- A **lambda expression** is a concise way to represent an anonymous
  function — a block of code that can be passed around as a value.
- Before Java 8, the only way to pass behavior was through
  **anonymous inner classes**, which were verbose and boilerplate-heavy.
- Lambdas enable a more functional style of programming in Java:
  treating behavior as data that can be stored in variables, passed
  to methods, and returned from methods.
- A lambda expression has three parts:
    - **Parameter list**: `(Type param1, Type param2)` or just `(param1, param2)`
      with type inference.
    - **Arrow token**: `->` separates parameters from the body.
    - **Body**: a single expression or a block of statements `{ ... }`.
- Lambdas do **not** exist in isolation — they always implement a
  **functional interface** (an interface with exactly one abstract
  method). The compiler infers which functional interface the lambda
  targets from context.
- Under the hood, lambdas are **not** anonymous inner classes.
  The JVM uses `invokedynamic` (introduced in Java 7) to generate
  lightweight implementations at runtime, avoiding the overhead of
  creating a new `.class` file for each lambda.
*/

// ============================================================
// Section 2: Lambda Syntax Variants
// ============================================================

/*
## Lambda Syntax Variants

- **Full syntax**: `(Type param1, Type param2) -> { statements; return value; }`
- **Inferred types**: `(param1, param2) -> expression`
  The compiler infers parameter types from the target functional interface.
- **Single parameter, no parentheses**: `param -> expression`
  Parentheses are optional when there is exactly one parameter with
  an inferred type. If the type is explicitly declared, parentheses
  are required: `(String s) -> s.length()`.
- **No parameters**: `() -> expression`
- **Multi-line body**: `(params) -> { statement1; statement2; return value; }`
  When the body contains multiple statements, curly braces are required,
  and a `return` statement is needed if the lambda must produce a value.
- **Single expression body**: `(params) -> expression`
  No braces, no `return` keyword — the expression's value is
  automatically returned.
- **`var` in parameters** (Java 11+): `(var x, var y) -> x + y`
  Allows annotations on lambda parameters: `(@NonNull var x) -> x.length()`.
  If `var` is used for one parameter, it must be used for all.
*/

// ============================================================
// Section 3: Functional Interfaces
// ============================================================

/*
## Functional Interfaces

- A **functional interface** is an interface with exactly **one
  abstract method** (SAM — Single Abstract Method). It may have
  any number of `default` or `static` methods.
- The `@FunctionalInterface` annotation is optional but recommended.
  It instructs the compiler to verify that the interface has exactly
  one abstract method — a compile-time error occurs if it does not.
- Methods inherited from `java.lang.Object` (like `equals`, `hashCode`,
  `toString`) do **not** count toward the abstract method limit,
  because every class already provides implementations.
- Lambdas can only be used where a functional interface type is
  expected. The compiler matches the lambda's signature (parameter
  types, return type) against the functional interface's abstract method.
- You can define your own functional interfaces for domain-specific
  purposes, but Java provides a rich set of built-in ones in
  `java.util.function`.
*/

// ============================================================
// Section 4: Built-in Functional Interfaces
// ============================================================

/*
## Built-in Functional Interfaces (`java.util.function`)

The `java.util.function` package provides 43 functional interfaces.
The core ones are:

| Interface             | Method              | Signature         | Purpose                       |
|-----------------------|---------------------|--------------------|-------------------------------|
| `Predicate<T>`        | `test(T)`           | `T -> boolean`     | Test a condition              |
| `Function<T,R>`       | `apply(T)`          | `T -> R`           | Transform a value             |
| `Consumer<T>`         | `accept(T)`         | `T -> void`        | Perform a side effect         |
| `Supplier<T>`         | `get()`             | `() -> T`          | Provide/produce a value       |
| `UnaryOperator<T>`    | `apply(T)`          | `T -> T`           | Transform, same type in/out   |
| `BinaryOperator<T>`   | `apply(T, T)`       | `(T,T) -> T`       | Combine two values            |
| `BiFunction<T,U,R>`   | `apply(T, U)`       | `(T,U) -> R`       | Transform two values          |
| `BiPredicate<T,U>`    | `test(T, U)`        | `(T,U) -> boolean` | Test two values               |
| `BiConsumer<T,U>`     | `accept(T, U)`      | `(T,U) -> void`    | Side effect with two values   |

### Primitive Specializations

- To avoid autoboxing overhead, Java provides primitive-specialized
  versions for `int`, `long`, and `double`:
  `IntPredicate`, `LongFunction<R>`, `DoubleConsumer`,
  `IntSupplier`, `IntUnaryOperator`, `IntBinaryOperator`,
  `ToIntFunction<T>`, `IntToDoubleFunction`, etc.
- **Rule of thumb**: when working with primitives in hot paths
  (loops, streams), prefer the primitive specializations to avoid
  boxing/unboxing overhead.
*/

// ============================================================
// Section 5: Method References
// ============================================================

/*
## Method References

- A **method reference** is a shorthand for a lambda that simply
  calls an existing method. It uses the `::` operator.
- Four kinds of method references:

| Kind                   | Syntax                   | Lambda Equivalent                  |
|------------------------|--------------------------|------------------------------------|
| Static method          | `ClassName::staticMethod`| `(args) -> ClassName.staticMethod(args)` |
| Instance (bound)       | `instance::method`       | `(args) -> instance.method(args)`  |
| Instance (unbound)     | `ClassName::method`      | `(obj, args) -> obj.method(args)`  |
| Constructor            | `ClassName::new`         | `(args) -> new ClassName(args)`    |

- **Bound** instance references capture a specific object:
  `System.out::println` always prints to the same `System.out`.
- **Unbound** instance references take the instance as the first
  parameter: `String::toLowerCase` is equivalent to `(String s) -> s.toLowerCase()`.
- Constructor references work with functional interfaces whose
  abstract method signature matches a constructor.
- Method references improve readability when the lambda body is
  just a method call with no additional logic.
*/

// ============================================================
// Section 6: Variable Capture and Effectively Final
// ============================================================

/*
## Variable Capture and Effectively Final

- Lambdas can **capture** (close over) variables from their
  enclosing scope — this makes them **closures**.
- Lambdas can freely access:
    - **Instance fields** and **static fields** — these can be
      read and modified without restriction.
    - **Local variables** — but only if they are **effectively final**.
- A local variable is **effectively final** if it is never reassigned
  after initialization. It does not need the `final` keyword — the
  compiler checks the behavior.
- **Why the restriction?** Local variables live on the stack and are
  destroyed when the method returns. The lambda may outlive the method
  (e.g., stored in a field or passed to another thread). Java copies
  the variable's value into the lambda's closure. If the variable
  could change after the copy, the lambda would see a stale value —
  leading to confusing bugs. Requiring effectively final prevents this.
- Workaround for mutable state: use a single-element array, an
  `AtomicInteger`, or a mutable container object. These are reference
  types — the reference is effectively final, but the contents can
  be mutated.
*/

// ============================================================
// Section 7: Lambdas with Collections
// ============================================================

/*
## Lambdas with Collections

Java 8 added several default methods to collection interfaces
that accept functional interfaces, enabling a more declarative
style:

- `Iterable.forEach(Consumer)` — iterate and perform an action
  on each element.
- `Collection.removeIf(Predicate)` — remove elements matching
  a condition (replaces iterator-based removal loops).
- `List.replaceAll(UnaryOperator)` — transform each element
  in place.
- `List.sort(Comparator)` — sort using a lambda-based comparator
  (replaces `Collections.sort()`).
- `Map.forEach(BiConsumer)` — iterate over key-value pairs.
- `Map.computeIfAbsent(key, Function)` — lazily compute a value
  if the key is not present.
- `Map.replaceAll(BiFunction)` — transform all values in a map.
- `Map.merge(key, value, BiFunction)` — merge a value with an
  existing entry.

These methods work well with lambdas and method references,
making collection manipulation concise and readable.
*/

// ============================================================
// Section 8: Composing Lambdas
// ============================================================

/*
## Composing Lambdas

Functional interfaces in `java.util.function` provide **default
methods** for composing multiple functions into pipelines:

### Function composition
- `f.andThen(g)` — first apply `f`, then apply `g` to the result.
  Equivalent to `g(f(x))`.
- `f.compose(g)` — first apply `g`, then apply `f` to the result.
  Equivalent to `f(g(x))`. Reverse order of `andThen`.

### Predicate composition
- `p1.and(p2)` — logical AND: both predicates must be true.
- `p1.or(p2)` — logical OR: at least one predicate must be true.
- `p.negate()` — logical NOT: inverts the predicate.

### Consumer composition
- `c1.andThen(c2)` — execute `c1`, then execute `c2` on the
  same input.

### Comparator composition
- `Comparator.comparing(keyExtractor)` — create a comparator
  from a key-extraction function.
- `c.thenComparing(keyExtractor)` — secondary sort when the
  primary comparison is equal.
- `c.reversed()` — reverse the ordering.

Composition enables building complex behavior from simple,
reusable building blocks without writing custom classes.
*/

// ============================================================
// Section 9: Common Patterns and Best Practices
// ============================================================

/*
## Common Patterns and Best Practices

- **Prefer method references** over lambdas when the lambda body
  is a single method call: `list.forEach(System.out::println)`
  is clearer than `list.forEach(x -> System.out.println(x))`.
- **Keep lambdas short** — if a lambda exceeds 2-3 lines, extract
  it into a named method and use a method reference.
- **Use built-in functional interfaces** from `java.util.function`
  before defining custom ones.
- **Avoid side effects in lambdas** used with streams — lambdas
  passed to `map`, `filter`, `reduce` should be pure functions.
  Side effects belong in `forEach` or terminal operations.
- **Exception handling**: lambdas that throw checked exceptions
  cannot be assigned to standard functional interfaces (which
  do not declare checked exceptions). Solutions:
    - Wrap the call in a try-catch inside the lambda.
    - Create a custom functional interface that declares the exception.
    - Use a utility method that wraps checked exceptions into
      unchecked ones.
- **Don't overuse lambdas** — sometimes a simple `for` loop or
  a named class is more readable, especially for complex logic
  or when the lambda needs to handle multiple concerns.
- **Type inference** works best when the target type is clear.
  If the compiler cannot infer types, provide explicit parameter
  types or assign the lambda to a typed variable.
*/

public class LambdaExpressions {

    // Helper functional interfaces for self-contained examples

    @FunctionalInterface
    interface Converter<F, T> {
        T convert(F from);
    }

    @FunctionalInterface
    interface Validator<T> {
        boolean validate(T value);
    }

    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    // ============================================================
    // Section 1: Introduction to Lambda Expressions
    // ============================================================

    static void introductionToLambdas() {
        System.out.println("=== Introduction to Lambda Expressions ===");

        // Before Java 8: anonymous inner class to define behavior
        Comparator<String> byLengthOldStyle = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        };

        // Java 8+: lambda expression — same behavior, far less boilerplate
        Comparator<String> byLengthLambda = (s1, s2) -> Integer.compare(s1.length(), s2.length());

        List<String> words = new ArrayList<>(Arrays.asList("banana", "apple", "fig", "cherry"));

        words.sort(byLengthOldStyle);
        System.out.println("sorted by length (anonymous class): " + words);

        words.sort(byLengthLambda);
        System.out.println("sorted by length (lambda): " + words);

        // Lambdas can be stored in variables, passed as arguments, and returned from methods
        Runnable greeting = () -> System.out.println("Hello from a lambda!");
        greeting.run();
    }

    // ============================================================
    // Section 2: Lambda Syntax Variants
    // ============================================================

    static void lambdaSyntaxVariants() {
        System.out.println("\n=== Lambda Syntax Variants ===");

        // Full syntax with explicit types and block body
        BinaryOperator<Integer> addFull = (Integer a, Integer b) -> {
            int sum = a + b;
            return sum;
        };
        System.out.println("full syntax: 3 + 4 = " + addFull.apply(3, 4));

        // Inferred parameter types — compiler knows from BinaryOperator<Integer>
        BinaryOperator<Integer> addInferred = (a, b) -> a + b;
        System.out.println("inferred types: 3 + 4 = " + addInferred.apply(3, 4));

        // Single parameter — parentheses are optional
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        System.out.println("single param: " + shout.apply("hello"));

        // No parameters
        Supplier<String> timestamp = () -> "Current time: " + System.currentTimeMillis();
        System.out.println("no params: " + timestamp.get());

        // Multi-line body with explicit return
        Function<String, Integer> wordCount = text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            return text.trim().split("\\s+").length;
        };
        System.out.println("word count of 'hello world': " + wordCount.apply("hello world"));
        System.out.println("word count of '': " + wordCount.apply(""));

        // Single expression body — no braces, no return keyword
        Function<Double, Double> circleArea = r -> Math.PI * r * r;
        System.out.println("area of circle (r=5): " + String.format("%.2f", circleArea.apply(5.0)));
    }

    // ============================================================
    // Section 3: Functional Interfaces
    // ============================================================

    static void functionalInterfaces() {
        System.out.println("\n=== Functional Interfaces ===");

        // Using a custom @FunctionalInterface defined above
        Converter<String, Integer> stringToInt = Integer::valueOf;
        System.out.println("converter: \"123\" -> " + stringToInt.convert("123"));

        Validator<String> notEmpty = s -> s != null && !s.isEmpty();
        System.out.println("validate \"hello\": " + notEmpty.validate("hello"));
        System.out.println("validate \"\": " + notEmpty.validate(""));

        // A functional interface can have default methods
        // Comparator<T> has one abstract method (compare) but many default methods
        Comparator<String> caseInsensitive = String::compareToIgnoreCase;
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "alice", "Bob"));
        names.sort(caseInsensitive);
        System.out.println("case-insensitive sort: " + names);

        // Using Runnable (void -> void) and Callable-like patterns
        Runnable task = () -> System.out.println("task executed");
        task.run();
    }

    // ============================================================
    // Section 4: Built-in Functional Interfaces
    // ============================================================

    static void builtInFunctionalInterfaces() {
        System.out.println("\n=== Built-in Functional Interfaces ===");

        // Predicate<T> — T -> boolean
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("is 4 even? " + isEven.test(4));
        System.out.println("is 7 even? " + isEven.test(7));

        // Function<T, R> — T -> R
        Function<String, Integer> strlen = String::length;
        System.out.println("length of \"lambda\": " + strlen.apply("lambda"));

        // Consumer<T> — T -> void
        Consumer<String> printer = s -> System.out.println("consumed: " + s);
        printer.accept("a value");

        // Supplier<T> — () -> T
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> freshList = listFactory.get();
        freshList.add("created by supplier");
        System.out.println("supplier-created list: " + freshList);

        // UnaryOperator<T> — T -> T (specialization of Function<T, T>)
        UnaryOperator<String> trim = String::trim;
        System.out.println("trimmed: \"" + trim.apply("  spaces  ") + "\"");

        // BinaryOperator<T> — (T, T) -> T (specialization of BiFunction<T, T, T>)
        BinaryOperator<Integer> max = Integer::max;
        System.out.println("max(3, 7): " + max.apply(3, 7));

        // BiFunction<T, U, R> — (T, U) -> R
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("repeat(\"ab\", 3): " + repeat.apply("ab", 3));

        // Primitive specializations — avoid autoboxing
        IntPredicate isPositive = n -> n > 0;
        System.out.println("is 5 positive (IntPredicate)? " + isPositive.test(5));

        IntUnaryOperator doubleIt = n -> n * 2;
        System.out.println("double 21 (IntUnaryOperator): " + doubleIt.applyAsInt(21));

        ToIntFunction<String> toLength = String::length;
        System.out.println("toLength(\"test\"): " + toLength.applyAsInt("test"));

        IntSupplier randomInt = () -> (int) (Math.random() * 100);
        System.out.println("random int: " + randomInt.getAsInt());
    }

    // ============================================================
    // Section 5: Method References
    // ============================================================

    static void methodReferences() {
        System.out.println("\n=== Method References ===");

        List<String> words = Arrays.asList("hello", "world", "java", "lambda");

        // Static method reference: ClassName::staticMethod
        // Integer.parseInt is a static method: String -> int
        Function<String, Integer> parser = Integer::parseInt;
        System.out.println("parsed \"42\": " + parser.apply("42"));

        // Bound instance method reference: instance::method
        // System.out is a specific instance; println is called on that instance
        System.out.println("--- forEach with bound method reference ---");
        words.forEach(System.out::println);

        // Unbound instance method reference: ClassName::method
        // The first parameter becomes the receiver: (String s) -> s.toUpperCase()
        Function<String, String> upper = String::toUpperCase;
        System.out.println("unbound ref: " + upper.apply("lambda"));

        // Unbound with Comparator — String::compareToIgnoreCase becomes
        // (s1, s2) -> s1.compareToIgnoreCase(s2)
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "alice", "Bob"));
        names.sort(String::compareToIgnoreCase);
        System.out.println("sorted (unbound method ref): " + names);

        // Constructor reference: ClassName::new
        // ArrayList::new matches Supplier<ArrayList<String>>
        Supplier<ArrayList<String>> listMaker = ArrayList::new;
        ArrayList<String> newList = listMaker.get();
        newList.add("constructed via ::new");
        System.out.println("constructor ref: " + newList);

        // Constructor reference with parameter: String[]::new matches IntFunction<String[]>
        IntFunction<String[]> arrayMaker = String[]::new;
        String[] arr = arrayMaker.apply(5);
        System.out.println("array constructor ref, length: " + arr.length);
    }

    // ============================================================
    // Section 6: Variable Capture and Effectively Final
    // ============================================================

    static void variableCaptureAndEffectivelyFinal() {
        System.out.println("\n=== Variable Capture and Effectively Final ===");

        // Capturing a local variable — it must be effectively final
        String greeting = "Hello";
        // greeting is never reassigned, so it is effectively final
        Consumer<String> greeter = name -> System.out.println(greeting + ", " + name + "!");
        greeter.accept("World");

        // This would cause a compile-time error:
        // greeting = "Hi"; // ERROR: local variable used in lambda must be effectively final

        // Capturing instance/static fields — no restriction on mutability
        // (demonstrated with a mutable array as a workaround for local vars)
        int[] counter = {0}; // single-element array — reference is effectively final
        Runnable incrementer = () -> counter[0]++;
        incrementer.run();
        incrementer.run();
        incrementer.run();
        System.out.println("counter via array workaround: " + counter[0]);

        // Effectively final — the keyword 'final' is optional
        final String explicit = "explicitly final";
        String implicit = "effectively final"; // never reassigned — same behavior
        Consumer<Void> demo = v -> {
            System.out.println(explicit);
            System.out.println(implicit);
        };
        demo.accept(null);

        // Common workaround: using a mutable container
        List<String> captured = new ArrayList<>();
        Runnable collector = () -> captured.add("item " + captured.size());
        collector.run();
        collector.run();
        System.out.println("mutable container: " + captured);
    }

    // ============================================================
    // Section 7: Lambdas with Collections
    // ============================================================

    static void lambdasWithCollections() {
        System.out.println("\n=== Lambdas with Collections ===");

        // forEach — iterate with a Consumer
        List<String> fruits = new ArrayList<>(Arrays.asList("apple", "banana", "cherry", "date", "elderberry"));
        System.out.print("forEach: ");
        fruits.forEach(f -> System.out.print(f + " "));
        System.out.println();

        // removeIf — remove elements matching a Predicate
        fruits.removeIf(f -> f.length() > 5);
        System.out.println("after removeIf (length > 5): " + fruits);

        // replaceAll — transform each element with a UnaryOperator
        fruits.replaceAll(String::toUpperCase);
        System.out.println("after replaceAll (toUpperCase): " + fruits);

        // sort — sort with a Comparator lambda
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob", "Dave"));
        names.sort((a, b) -> Integer.compare(a.length(), b.length()));
        System.out.println("sorted by length: " + names);

        // Comparator.comparing — cleaner way to create comparators
        names.sort(Comparator.comparing(String::length));
        System.out.println("sorted by length (Comparator.comparing): " + names);

        // Map.forEach — iterate over key-value pairs with BiConsumer
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 95);
        scores.put("Bob", 87);
        scores.put("Charlie", 92);
        System.out.print("map forEach: ");
        scores.forEach((name, score) -> System.out.print(name + "=" + score + " "));
        System.out.println();

        // Map.computeIfAbsent — lazily compute a value
        Map<String, List<String>> groups = new HashMap<>();
        groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
        groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("banana");
        groups.computeIfAbsent("vegs", k -> new ArrayList<>()).add("carrot");
        System.out.println("computeIfAbsent groups: " + groups);

        // Map.replaceAll — transform all values with BiFunction
        Map<String, Integer> prices = new HashMap<>();
        prices.put("coffee", 3);
        prices.put("tea", 2);
        prices.replaceAll((item, price) -> price * 2);
        System.out.println("prices after replaceAll (*2): " + prices);

        // Map.merge — merge with existing entry
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String word : "the cat sat on the mat".split(" ")) {
            wordCounts.merge(word, 1, Integer::sum);
        }
        System.out.println("word counts via merge: " + wordCounts);
    }

    // ============================================================
    // Section 8: Composing Lambdas
    // ============================================================

    static void composingLambdas() {
        System.out.println("\n=== Composing Lambdas ===");

        // Function.andThen — f.andThen(g) = g(f(x))
        Function<String, String> trim = String::trim;
        Function<String, String> upper = String::toUpperCase;
        Function<String, String> trimThenUpper = trim.andThen(upper);
        System.out.println("andThen: \"" + trimThenUpper.apply("  hello  ") + "\"");

        // Function.compose — f.compose(g) = f(g(x))
        Function<Integer, Integer> doubleIt = n -> n * 2;
        Function<Integer, Integer> addThree = n -> n + 3;
        // compose: first addThree, then doubleIt => (5 + 3) * 2 = 16
        System.out.println("compose (double after addThree): " + doubleIt.compose(addThree).apply(5));
        // andThen: first doubleIt, then addThree => (5 * 2) + 3 = 13
        System.out.println("andThen (addThree after double): " + doubleIt.andThen(addThree).apply(5));

        // Predicate.and, or, negate
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> isEven = n -> n % 2 == 0;

        Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
        System.out.println("4 is positive and even? " + isPositiveAndEven.test(4));
        System.out.println("-2 is positive and even? " + isPositiveAndEven.test(-2));

        Predicate<Integer> isPositiveOrEven = isPositive.or(isEven);
        System.out.println("-2 is positive or even? " + isPositiveOrEven.test(-2));

        Predicate<Integer> isNotPositive = isPositive.negate();
        System.out.println("-5 is not positive? " + isNotPositive.test(-5));

        // Consumer.andThen — chain side effects
        Consumer<String> print = System.out::println;
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        Consumer<String> printBoth = print.andThen(printUpper);
        System.out.print("consumer andThen: ");
        printBoth.accept("hello");

        // Comparator.comparing + thenComparing + reversed
        List<String> words = new ArrayList<>(Arrays.asList("banana", "fig", "apple", "cherry", "date", "fig"));
        words.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("sorted by length, then alphabetically: " + words);

        words.sort(Comparator.comparing(String::length).reversed());
        System.out.println("sorted by length descending: " + words);
    }

    // ============================================================
    // Section 9: Common Patterns and Best Practices
    // ============================================================

    // Utility method: wraps a throwing function into a regular Function
    static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static void commonPatternsAndBestPractices() {
        System.out.println("\n=== Common Patterns and Best Practices ===");

        // Prefer method references when the lambda is a simple delegation
        List<String> items = Arrays.asList("one", "two", "three");
        // Less readable:
        items.forEach(item -> System.out.println(item));
        // More readable:
        items.forEach(System.out::println);

        // Strategy pattern — pass different behaviors as lambdas
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("evens: " + filter(numbers, n -> n % 2 == 0));
        System.out.println("odds: " + filter(numbers, n -> n % 2 != 0));
        System.out.println("> 5: " + filter(numbers, n -> n > 5));

        // Exception handling in lambdas — checked exceptions need wrapping
        // Standard Function<T, R> does not allow checked exceptions
        List<String> numberStrings = Arrays.asList("1", "2", "three", "4");

        // Using the unchecked() utility to handle exceptions
        System.out.print("parsing with exception handling: ");
        for (String s : numberStrings) {
            try {
                int value = unchecked((String str) -> {
                    int n = Integer.parseInt(str);
                    if (n < 0) throw new Exception("negative");
                    return n;
                }).apply(s);
                System.out.print(value + " ");
            } catch (RuntimeException e) {
                System.out.print("[error: " + s + "] ");
            }
        }
        System.out.println();

        // Lambda as factory — Supplier for deferred/lazy creation
        Map<String, Supplier<List<String>>> factories = new HashMap<>();
        factories.put("array", ArrayList::new);
        factories.put("linked", LinkedList::new);
        List<String> list = factories.get("linked").get();
        list.add("created lazily");
        System.out.println("factory pattern: " + list + " (" + list.getClass().getSimpleName() + ")");

        // Execute-around pattern — encapsulate setup/teardown logic
        String result = withTiming("slow operation", () -> {
            // Simulate work
            return "computed result";
        });
        System.out.println("execute-around result: " + result);
    }

    // Helper: filter a list using a Predicate (strategy pattern)
    static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // Helper: execute-around pattern — wraps a Supplier with timing
    static <T> T withTiming(String label, Supplier<T> action) {
        long start = System.nanoTime();
        T result = action.get();
        long elapsed = System.nanoTime() - start;
        System.out.printf("  [%s took %.3f ms]%n", label, elapsed / 1_000_000.0);
        return result;
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        introductionToLambdas();
        lambdaSyntaxVariants();
        functionalInterfaces();
        builtInFunctionalInterfaces();
        methodReferences();
        variableCaptureAndEffectivelyFinal();
        lambdasWithCollections();
        composingLambdas();
        commonPatternsAndBestPractices();
    }
}
