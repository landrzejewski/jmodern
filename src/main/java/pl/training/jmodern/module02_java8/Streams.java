package pl.training.jmodern.module02_java8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction to Streams
// ============================================================

/*
## Introduction to Streams

- A **Stream** is a sequence of elements that supports declarative,
  functional-style operations for processing data. Introduced in
  Java 8, streams fundamentally change how we work with collections.
- **Streams vs Collections**:
    - Collections are **in-memory data structures** — they store
      elements. Streams do **not store** elements; they carry values
      from a source through a pipeline of operations.
    - Collections are **eagerly populated**. Streams are **lazy** —
      intermediate operations are not executed until a terminal
      operation is invoked.
    - Collections can be iterated multiple times. Streams are
      **single-use** — once consumed by a terminal operation,
      the stream is exhausted and cannot be reused.
- **Stream pipeline** consists of three parts:
    1. **Source** — where data comes from (collection, array, generator, I/O).
    2. **Intermediate operations** — transform the stream into another
       stream (`filter`, `map`, `sorted`, etc.). They are **lazy** and
       return a new stream.
    3. **Terminal operation** — produces a result or side effect
       (`collect`, `forEach`, `reduce`, etc.). This triggers the
       actual processing of the pipeline.
- **Internal vs external iteration**:
    - External iteration: the programmer controls the loop
      (`for`, `while`, `Iterator`).
    - Internal iteration: the stream library handles iteration;
      the programmer declares *what* to do, not *how* to iterate.
      This allows the library to optimize (parallelism, short-circuiting,
      loop fusion).
*/

// ============================================================
// Section 2: Creating Streams
// ============================================================

/*
## Creating Streams

There are many ways to obtain a stream:

- **From a Collection**: `collection.stream()` — the most common way.
- **From values**: `Stream.of("a", "b", "c")` — creates a stream
  from explicit values.
- **From an array**: `Arrays.stream(array)` — streams an array or
  a slice: `Arrays.stream(array, startInclusive, endExclusive)`.
- **Empty stream**: `Stream.empty()` — useful as a default or in
  conditional logic.
- **Generated (infinite)**: `Stream.generate(supplier)` — creates
  an infinite stream by repeatedly calling a `Supplier`. Must be
  limited with `limit()`.
- **Iterated (infinite)**: `Stream.iterate(seed, unaryOperator)` —
  creates an infinite stream: `seed, f(seed), f(f(seed)), ...`.
  Java 9 added `Stream.iterate(seed, predicate, operator)` with
  a built-in termination condition (similar to a for-loop).
- **Primitive ranges**: `IntStream.range(0, 10)` (exclusive end),
  `IntStream.rangeClosed(1, 10)` (inclusive end).
- **From a String**: `"hello".chars()` returns an `IntStream` of
  character values.
- **From nullable** (Java 9): `Stream.ofNullable(value)` — returns
  a single-element stream or an empty stream if the value is null.
- **From files**: `Files.lines(path)` — lazily reads lines from
  a file as a `Stream<String>`. Should be used with try-with-resources
  as the stream holds an open file handle.
*/

// ============================================================
// Section 3: Intermediate Operations
// ============================================================

/*
## Intermediate Operations

Intermediate operations are **lazy** — they do not process elements
until a terminal operation is invoked. They return a new stream
and can be chained.

- `filter(Predicate)` — keeps only elements matching the predicate.
- `map(Function)` — transforms each element to another value.
- `flatMap(Function)` — transforms each element into a stream and
  flattens all resulting streams into one.
- `distinct()` — removes duplicates (uses `equals()`).
- `sorted()` — sorts in natural order; `sorted(Comparator)` for
  custom ordering.
- `peek(Consumer)` — performs an action on each element without
  consuming the stream. Useful for debugging.
- `limit(n)` — truncates the stream to at most `n` elements.
  A **short-circuiting** operation.
- `skip(n)` — discards the first `n` elements.
- `mapToInt(ToIntFunction)` / `mapToLong` / `mapToDouble` —
  converts to a primitive stream to avoid boxing.
- `takeWhile(Predicate)` (Java 9) — takes elements while the
  predicate is true, then stops.
- `dropWhile(Predicate)` (Java 9) — drops elements while the
  predicate is true, then passes the rest.

### Laziness demonstration
No intermediate operation executes until a terminal operation
is called. This also enables **loop fusion** — the JVM can merge
multiple operations into a single pass over the data.
*/

// ============================================================
// Section 4: Terminal Operations
// ============================================================

/*
## Terminal Operations

Terminal operations trigger the processing of the stream pipeline
and produce a result or side effect. After a terminal operation,
the stream is consumed and cannot be reused.

- `forEach(Consumer)` — performs an action on each element.
  Does **not** guarantee order in parallel streams.
- `forEachOrdered(Consumer)` — like `forEach`, but preserves
  encounter order even in parallel streams.
- `collect(Collector)` — the most versatile terminal operation;
  accumulates elements into a mutable container (List, Set, Map,
  String, etc.) using a `Collector`.
- `toList()` (Java 16) — shorthand for collecting into an
  unmodifiable `List`. Equivalent to `collect(Collectors.toUnmodifiableList())`.
- `reduce(identity, accumulator)` — combines all elements into
  a single value using an associative accumulation function.
- `count()` — returns the number of elements.
- `min(Comparator)` / `max(Comparator)` — returns the minimum
  or maximum element wrapped in an `Optional`.
- `findFirst()` — returns the first element as an `Optional`.
  Useful with `filter` for "find first matching" patterns.
- `findAny()` — returns any element (non-deterministic in parallel).
- `anyMatch(Predicate)` — returns `true` if any element matches.
  **Short-circuiting**.
- `allMatch(Predicate)` — returns `true` if all elements match.
- `noneMatch(Predicate)` — returns `true` if no elements match.
- `toArray()` — collects elements into an array.
  `toArray(String[]::new)` produces a typed array.
*/

// ============================================================
// Section 5: Collectors
// ============================================================

/*
## Collectors

The `Collectors` utility class provides a rich set of predefined
collectors for use with `stream.collect()`:

- `toList()` — collects into an `ArrayList`.
- `toSet()` — collects into a `HashSet`.
- `toMap(keyMapper, valueMapper)` — collects into a `HashMap`.
  A merge function can handle duplicate keys:
  `toMap(k, v, (v1, v2) -> v1)`.
- `toUnmodifiableList()` / `toUnmodifiableSet()` / `toUnmodifiableMap()`
  (Java 10) — immutable variants.
- `joining()` / `joining(delimiter)` / `joining(delimiter, prefix, suffix)`
  — concatenates `CharSequence` elements into a single `String`.
- `groupingBy(classifier)` — groups elements into a `Map<K, List<V>>`.
  Supports a downstream collector:
  `groupingBy(classifier, downstream)`.
- `partitioningBy(predicate)` — special case of grouping with
  a boolean key: `Map<Boolean, List<V>>`.
- `counting()` — counts elements (used as a downstream collector).
- `summarizingInt(mapper)` — produces `IntSummaryStatistics`
  (count, sum, min, max, average).
- `mapping(mapper, downstream)` — applies a mapping function
  before collecting with the downstream collector.
- `reducing(identity, accumulator)` — performs a reduction as
  a collector (useful as a downstream collector).
- `collectingAndThen(downstream, finisher)` — applies a final
  transformation after collecting: e.g., wrapping a list in
  `Collections.unmodifiableList()`.
*/

// ============================================================
// Section 6: Reduction Operations
// ============================================================

/*
## Reduction Operations

A **reduction** combines all elements of a stream into a single
result by repeatedly applying a combining function.

- `reduce(BinaryOperator)` — returns `Optional<T>`. No identity
  value; the result is empty if the stream is empty.
- `reduce(identity, BinaryOperator)` — returns `T`. The identity
  value is returned for empty streams and serves as the initial
  accumulator value. The identity must be a true identity for the
  operator: `op(identity, x) == x`.
- `reduce(identity, BiFunction accumulator, BinaryOperator combiner)`
  — the three-argument form used when the result type differs from
  the element type, or for parallel streams. The combiner merges
  partial results from parallel execution.

### `reduce` vs `collect`
- `reduce` produces an **immutable** result — each step creates
  a new value. Best for operations like sum, product, min, max.
- `collect` uses a **mutable accumulator** (like a `List` or
  `StringBuilder`). More efficient for building collections because
  it avoids creating intermediate objects.
*/

// ============================================================
// Section 7: Primitive Streams
// ============================================================

/*
## Primitive Streams

Java provides three primitive stream specializations to avoid
the cost of boxing/unboxing:

- `IntStream` — for `int` values.
- `LongStream` — for `long` values.
- `DoubleStream` — for `double` values.

### Additional methods not on `Stream<T>`
- `sum()` — returns the sum of all elements.
- `average()` — returns `OptionalDouble` with the average.
- `summaryStatistics()` — returns count, sum, min, max, average
  in a single pass (`IntSummaryStatistics`, etc.).
- `range(start, end)` / `rangeClosed(start, end)` — generate
  sequential int/long values.

### Conversion
- `mapToInt(ToIntFunction)` — converts `Stream<T>` to `IntStream`.
- `boxed()` — converts `IntStream` back to `Stream<Integer>`.
- `mapToObj(IntFunction)` — maps each int to an object.
- `asLongStream()` / `asDoubleStream()` — widening conversions.
*/

// ============================================================
// Section 8: FlatMap and Stream Flattening
// ============================================================

/*
## FlatMap and Stream Flattening

`flatMap` is used when each element maps to **multiple** values
(i.e., a stream). It flattens the resulting streams into one.

- **Pattern**: `stream.flatMap(element -> element.getSubElements().stream())`
- Common use cases:
    - Flattening a list of lists: `listOfLists.stream().flatMap(Collection::stream)`
    - Splitting strings: `words.stream().flatMap(w -> Arrays.stream(w.split("")))`
    - Combining with Optional: `Optional.stream()` (Java 9) converts
      an `Optional<T>` to a `Stream<T>` (0 or 1 element), enabling
      `flatMap(opt -> opt.stream())` to filter out empty optionals.
- `flatMapToInt`, `flatMapToLong`, `flatMapToDouble` — primitive
  variants for producing primitive streams.
*/

// ============================================================
// Section 9: Parallel Streams
// ============================================================

/*
## Parallel Streams

Parallel streams split the data into multiple chunks and process
them concurrently using the **ForkJoinPool**.

- **Creating**: `collection.parallelStream()` or
  `stream.parallel()`.
- **When to use**:
    - Large data sets (thousands+ elements).
    - CPU-intensive per-element operations.
    - Operations that are easily parallelizable (stateless,
      no shared mutable state, associative reduction).
- **When to avoid**:
    - Small data sets — overhead of parallelization exceeds benefit.
    - I/O-bound operations — threads block, wasting pool resources.
    - Operations with side effects or shared mutable state.
    - When encounter order matters and cannot use `forEachOrdered`.
    - `LinkedList` or other sources with poor splittability.
- **Thread safety**: operations must be stateless and
  thread-safe. The accumulator and combiner in `reduce`/`collect`
  must be associative.
- **Ordering**: `forEach` does not preserve order in parallel;
  use `forEachOrdered` if order matters (at a performance cost).
- **Custom ForkJoinPool**: by default, parallel streams use the
  common `ForkJoinPool`. To control parallelism, submit the
  stream operation to a custom pool:
  `new ForkJoinPool(n).submit(() -> stream.parallel()....).join()`.
- **Performance tip**: measure before parallelizing. Use
  benchmarks (JMH) to verify that parallel is actually faster.
*/

// ============================================================
// Section 10: Common Patterns and Best Practices
// ============================================================

/*
## Common Patterns and Best Practices

- **Streams vs loops**: streams are more readable for data
  transformation pipelines. Loops can be clearer for simple
  iterations, mutations, or when you need `break`/`continue`.
- **Avoid side effects** in intermediate operations. `map`,
  `filter`, `flatMap` should be **pure functions**. Side effects
  belong in `forEach` or should be encapsulated in a `Collector`.
- **Don't reuse streams**: a stream can only be consumed once.
  If you need to process the same data twice, create a new stream
  from the source.
- **Prefer method references** when the lambda is a simple
  delegation: `map(String::toUpperCase)` over `map(s -> s.toUpperCase())`.
- **Infinite streams**: always pair `Stream.generate()` or
  `Stream.iterate()` with `limit()` to prevent infinite loops.
- **Prefer `collect` over `reduce`** for mutable accumulation
  (building lists, strings, maps). `reduce` with mutable objects
  is incorrect and can produce wrong results in parallel streams.
- **Use primitive streams** (`IntStream`, `LongStream`, `DoubleStream`)
  when working with numeric data to avoid autoboxing overhead.
- **`Optional` in streams**: use `findFirst()`, `findAny()`, `min()`,
  `max()` which return `Optional`. Chain with `orElse()`,
  `orElseThrow()`, `ifPresent()` for clean handling.
- **Debugging**: use `peek()` to inspect elements at any point
  in the pipeline without affecting the result.
*/

public class Streams {

    // ---- Helper classes for self-contained examples ----

    record Person(String name, int age, String city) {}

    record Order(String customer, List<String> items, double total) {}

    // ============================================================
    // Section 1: Introduction to Streams
    // ============================================================

    static void introductionToStreams() {
        System.out.println("=== Introduction to Streams ===");

        List<String> names = Arrays.asList("Charlie", "Alice", "Bob", "Dave", "Eve");

        // External iteration — the programmer controls the loop
        System.out.print("external iteration: ");
        for (String name : names) {
            if (name.length() <= 4) {
                System.out.print(name.toUpperCase() + " ");
            }
        }
        System.out.println();

        // Internal iteration — the stream library handles the loop
        System.out.print("internal iteration (stream): ");
        names.stream()
                .filter(name -> name.length() <= 4)
                .map(String::toUpperCase)
                .forEach(name -> System.out.print(name + " "));
        System.out.println();

        // Streams are lazy — intermediate operations don't execute until a terminal operation
        System.out.println("--- laziness demonstration ---");
        List<String> result = names.stream()
                .filter(name -> {
                    System.out.println("  filtering: " + name);
                    return name.length() > 3;
                })
                .map(name -> {
                    System.out.println("  mapping: " + name);
                    return name.toUpperCase();
                })
                .toList(); // terminal operation triggers processing
        System.out.println("result: " + result);

        // Streams are single-use — consuming a stream twice throws IllegalStateException
        var stream = names.stream();
        stream.forEach(n -> {}); // first consumption
        try {
            stream.forEach(n -> {}); // second consumption — throws!
        } catch (IllegalStateException e) {
            System.out.println("stream reuse error: " + e.getMessage());
        }
    }

    // ============================================================
    // Section 2: Creating Streams
    // ============================================================

    static void creatingStreams() {
        System.out.println("\n=== Creating Streams ===");

        // From a Collection
        List<String> fruits = List.of("apple", "banana", "cherry");
        long count = fruits.stream().count();
        System.out.println("from collection: " + count + " elements");

        // Stream.of — from explicit values
        Stream<String> streamOf = Stream.of("one", "two", "three");
        System.out.println("Stream.of: " + streamOf.toList());

        // Arrays.stream — from an array
        int[] numbers = {10, 20, 30, 40, 50};
        int sum = Arrays.stream(numbers).sum();
        System.out.println("Arrays.stream sum: " + sum);

        // Arrays.stream with range (slice of an array)
        int sliceSum = Arrays.stream(numbers, 1, 4).sum(); // indices 1,2,3
        System.out.println("array slice [1,4) sum: " + sliceSum);

        // Stream.empty
        Stream<String> empty = Stream.empty();
        System.out.println("empty stream count: " + empty.count());

        // Stream.generate — infinite stream from a Supplier
        List<Double> randoms = Stream.generate(Math::random)
                .limit(5)
                .toList();
        System.out.println("generated randoms: " + randoms.stream()
                .map(d -> String.format("%.2f", d))
                .collect(Collectors.joining(", ")));

        // Stream.iterate — infinite stream with seed and unary operator
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2)
                .limit(10)
                .toList();
        System.out.println("powers of 2: " + powersOfTwo);

        // Stream.iterate with predicate (Java 9) — built-in termination
        List<Integer> countdown = Stream.iterate(10, n -> n > 0, n -> n - 1)
                .toList();
        System.out.println("countdown (iterate with predicate): " + countdown);

        // IntStream.range / rangeClosed
        int rangeSum = IntStream.range(1, 6).sum(); // 1+2+3+4+5
        int rangeClosedSum = IntStream.rangeClosed(1, 5).sum(); // same
        System.out.println("range(1,6) sum: " + rangeSum + ", rangeClosed(1,5) sum: " + rangeClosedSum);

        // Stream from a String — chars() returns IntStream
        String text = "Hello";
        System.out.print("chars of \"" + text + "\": ");
        text.chars()
                .mapToObj(c -> (char) c + " ")
                .forEach(System.out::print);
        System.out.println();

        // Stream.ofNullable (Java 9) — 0 or 1 element
        Stream<String> nonNull = Stream.ofNullable("value");
        Stream<String> fromNull = Stream.ofNullable(null);
        System.out.println("ofNullable(\"value\"): " + nonNull.count()
                + ", ofNullable(null): " + fromNull.count());

        // Files.lines — reading lines from a file (demonstrated with a temp file)
        try {
            Path tempFile = Files.createTempFile("stream-demo", ".txt");
            Files.writeString(tempFile, "line one\nline two\nline three");
            try (var lines = Files.lines(tempFile)) {
                List<String> fileLines = lines.toList();
                System.out.println("Files.lines: " + fileLines);
            }
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            System.out.println("file stream error: " + e.getMessage());
        }
    }

    // ============================================================
    // Section 3: Intermediate Operations
    // ============================================================

    static void intermediateOperations() {
        System.out.println("\n=== Intermediate Operations ===");

        List<String> words = List.of("banana", "apple", "cherry", "avocado", "banana", "date", "apple");

        // filter — keep elements matching a predicate
        List<String> startsWithA = words.stream()
                .filter(w -> w.startsWith("a"))
                .toList();
        System.out.println("filter (starts with 'a'): " + startsWithA);

        // map — transform each element
        List<Integer> lengths = words.stream()
                .map(String::length)
                .toList();
        System.out.println("map (lengths): " + lengths);

        // distinct — remove duplicates
        List<String> unique = words.stream()
                .distinct()
                .toList();
        System.out.println("distinct: " + unique);

        // sorted — natural order
        List<String> sorted = words.stream()
                .sorted()
                .toList();
        System.out.println("sorted: " + sorted);

        // sorted with Comparator
        List<String> sortedByLength = words.stream()
                .sorted(Comparator.comparingInt(String::length))
                .toList();
        System.out.println("sorted by length: " + sortedByLength);

        // peek — inspect elements without consuming (useful for debugging)
        System.out.print("peek demo: ");
        long longWordCount = words.stream()
                .filter(w -> w.length() > 4)
                .peek(w -> System.out.print("[" + w + "] "))
                .distinct()
                .count();
        System.out.println("-> " + longWordCount + " distinct long words");

        // limit and skip — truncate / skip elements
        List<String> limited = words.stream().limit(3).toList();
        List<String> skipped = words.stream().skip(4).toList();
        System.out.println("limit(3): " + limited);
        System.out.println("skip(4): " + skipped);

        // skip + limit for pagination
        List<String> page = words.stream().skip(2).limit(3).toList();
        System.out.println("skip(2).limit(3) (page): " + page);

        // mapToInt — convert to IntStream for numeric operations
        int totalLength = words.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("mapToInt total length: " + totalLength);

        // Laziness: intermediate operations are not executed without a terminal operation
        System.out.println("--- laziness: no output without terminal op ---");
        words.stream()
                .filter(w -> {
                    System.out.println("  THIS SHOULD NOT PRINT");
                    return true;
                });
        // ^^^ no terminal operation — filter lambda is NEVER called
        System.out.println("  (nothing printed — stream was never consumed)");

        // takeWhile (Java 9) — take elements while predicate is true
        List<Integer> nums = List.of(2, 4, 6, 7, 8, 10);
        List<Integer> takeWhileEven = nums.stream()
                .takeWhile(n -> n % 2 == 0)
                .toList();
        System.out.println("takeWhile(even): " + takeWhileEven);

        // dropWhile (Java 9) — drop elements while predicate is true
        List<Integer> dropWhileEven = nums.stream()
                .dropWhile(n -> n % 2 == 0)
                .toList();
        System.out.println("dropWhile(even): " + dropWhileEven);
    }

    // ============================================================
    // Section 4: Terminal Operations
    // ============================================================

    static void terminalOperations() {
        System.out.println("\n=== Terminal Operations ===");

        List<String> names = List.of("Alice", "Bob", "Charlie", "Dave", "Eve");

        // forEach — perform an action on each element
        System.out.print("forEach: ");
        names.stream().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // collect — accumulate into a collection
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("collect toList: " + upperNames);

        // toList() (Java 16) — shorthand for unmodifiable list
        List<String> immutableList = names.stream()
                .filter(n -> n.length() > 3)
                .toList();
        System.out.println("toList() (Java 16): " + immutableList);

        // reduce — combine elements into a single value
        Optional<String> concatenated = names.stream()
                .reduce((a, b) -> a + ", " + b);
        System.out.println("reduce (concatenate): " + concatenated.orElse(""));

        // reduce with identity
        int totalLength = names.stream()
                .reduce(0, (acc, name) -> acc + name.length(), Integer::sum);
        System.out.println("reduce (total length): " + totalLength);

        // count
        long count = names.stream().filter(n -> n.length() == 3).count();
        System.out.println("count (3-letter names): " + count);

        // min / max
        Optional<String> shortest = names.stream()
                .min(Comparator.comparingInt(String::length));
        Optional<String> longest = names.stream()
                .max(Comparator.comparingInt(String::length));
        System.out.println("min (shortest): " + shortest.orElse("none"));
        System.out.println("max (longest): " + longest.orElse("none"));

        // findFirst / findAny
        Optional<String> firstLong = names.stream()
                .filter(n -> n.length() > 3)
                .findFirst();
        System.out.println("findFirst (length > 3): " + firstLong.orElse("none"));

        // anyMatch / allMatch / noneMatch
        boolean anyShort = names.stream().anyMatch(n -> n.length() <= 3);
        boolean allNonEmpty = names.stream().allMatch(n -> !n.isEmpty());
        boolean noneStartsWithZ = names.stream().noneMatch(n -> n.startsWith("Z"));
        System.out.println("anyMatch (<=3 chars): " + anyShort);
        System.out.println("allMatch (non-empty): " + allNonEmpty);
        System.out.println("noneMatch (starts with Z): " + noneStartsWithZ);

        // toArray
        String[] nameArray = names.stream()
                .filter(n -> n.length() > 3)
                .toArray(String[]::new);
        System.out.println("toArray: " + Arrays.toString(nameArray));
    }

    // ============================================================
    // Section 5: Collectors
    // ============================================================

    static void collectors() {
        System.out.println("\n=== Collectors ===");

        List<Person> people = List.of(
                new Person("Alice", 30, "Warsaw"),
                new Person("Bob", 25, "Krakow"),
                new Person("Charlie", 35, "Warsaw"),
                new Person("Dave", 28, "Gdansk"),
                new Person("Eve", 30, "Krakow"),
                new Person("Frank", 22, "Warsaw")
        );

        // toList / toSet
        List<String> nameList = people.stream()
                .map(Person::name)
                .collect(Collectors.toList());
        Set<String> citySet = people.stream()
                .map(Person::city)
                .collect(Collectors.toSet());
        System.out.println("toList (names): " + nameList);
        System.out.println("toSet (cities): " + citySet);

        // toUnmodifiableList
        List<String> immutableNames = people.stream()
                .map(Person::name)
                .collect(Collectors.toUnmodifiableList());
        System.out.println("toUnmodifiableList: " + immutableNames);

        // toMap — name -> age
        Map<String, Integer> nameToAge = people.stream()
                .collect(Collectors.toMap(Person::name, Person::age));
        System.out.println("toMap (name -> age): " + nameToAge);

        // toMap with merge function (handling duplicate keys)
        Map<String, Long> cityCount = people.stream()
                .collect(Collectors.toMap(Person::city, p -> 1L, Long::sum));
        System.out.println("toMap with merge (city counts): " + cityCount);

        // joining
        String joined = people.stream()
                .map(Person::name)
                .collect(Collectors.joining(", "));
        System.out.println("joining: " + joined);

        String joinedWithBrackets = people.stream()
                .map(Person::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining with prefix/suffix: " + joinedWithBrackets);

        // groupingBy — group by city
        Map<String, List<Person>> byCity = people.stream()
                .collect(Collectors.groupingBy(Person::city));
        System.out.println("groupingBy city:");
        byCity.forEach((city, persons) -> System.out.println("  " + city + ": "
                + persons.stream().map(Person::name).collect(Collectors.joining(", "))));

        // groupingBy with downstream collector — count per city
        Map<String, Long> countByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
        System.out.println("groupingBy + counting: " + countByCity);

        // groupingBy with downstream — names per city
        Map<String, List<String>> namesByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.mapping(Person::name, Collectors.toList())));
        System.out.println("groupingBy + mapping: " + namesByCity);

        // partitioningBy — split into two groups by a predicate
        Map<Boolean, List<Person>> partitioned = people.stream()
                .collect(Collectors.partitioningBy(p -> p.age() >= 30));
        System.out.println("partitioningBy (age >= 30): "
                + partitioned.get(true).stream().map(Person::name).collect(Collectors.joining(", "))
                + " | under 30: "
                + partitioned.get(false).stream().map(Person::name).collect(Collectors.joining(", ")));

        // summarizingInt
        IntSummaryStatistics ageStats = people.stream()
                .collect(Collectors.summarizingInt(Person::age));
        System.out.println("summarizingInt (age): " + ageStats);

        // reducing as a downstream collector
        Map<String, Optional<Person>> oldestByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.reducing((p1, p2) -> p1.age() >= p2.age() ? p1 : p2)));
        System.out.println("groupingBy + reducing (oldest per city):");
        oldestByCity.forEach((city, person) ->
                System.out.println("  " + city + ": " + person.map(Person::name).orElse("none")));

        // collectingAndThen — apply a finishing transformation
        List<String> unmodifiableNames = people.stream()
                .map(Person::name)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        Collections::unmodifiableList));
        System.out.println("collectingAndThen (unmodifiable): " + unmodifiableNames);
    }

    // ============================================================
    // Section 6: Reduction Operations
    // ============================================================

    static void reductionOperations() {
        System.out.println("\n=== Reduction Operations ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // reduce with no identity — returns Optional
        Optional<Integer> sum = numbers.stream()
                .reduce(Integer::sum);
        System.out.println("reduce (sum, no identity): " + sum.orElse(0));

        // reduce with identity — returns T directly
        int sumWithIdentity = numbers.stream()
                .reduce(0, Integer::sum);
        System.out.println("reduce (sum, identity=0): " + sumWithIdentity);

        // reduce — product
        int product = numbers.stream()
                .reduce(1, (a, b) -> a * b);
        System.out.println("reduce (product): " + product);

        // reduce — find max manually
        Optional<Integer> max = numbers.stream()
                .reduce(Integer::max);
        System.out.println("reduce (max): " + max.orElse(0));

        // reduce — string concatenation
        List<String> words = List.of("Stream", "API", "is", "powerful");
        String sentence = words.stream()
                .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
        System.out.println("reduce (sentence): " + sentence);

        // Three-argument reduce — when result type differs from element type
        // identity, accumulator (U, T) -> U, combiner (U, U) -> U
        int totalLength = words.stream()
                .reduce(0, (len, word) -> len + word.length(), Integer::sum);
        System.out.println("3-arg reduce (total length): " + totalLength);

        // Demonstrating reduce vs collect for building a list
        // WRONG way — reduce with mutable accumulator (broken in parallel!)
        // CORRECT way — use collect
        List<String> collected = words.stream()
                .filter(w -> w.length() > 2)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        System.out.println("collect (3-arg, manual): " + collected);
    }

    // ============================================================
    // Section 7: Primitive Streams
    // ============================================================

    static void primitiveStreams() {
        System.out.println("\n=== Primitive Streams ===");

        // IntStream.range and rangeClosed
        System.out.print("IntStream.range(1, 6): ");
        IntStream.range(1, 6).forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.print("IntStream.rangeClosed(1, 5): ");
        IntStream.rangeClosed(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // sum, average, min, max
        int sum = IntStream.rangeClosed(1, 100).sum();
        OptionalDouble avg = IntStream.rangeClosed(1, 100).average();
        System.out.println("sum(1..100): " + sum);
        System.out.println("average(1..100): " + avg.orElse(0));

        // summaryStatistics — count, sum, min, max, average in one pass
        IntSummaryStatistics stats = IntStream.of(5, 10, 15, 20, 25)
                .summaryStatistics();
        System.out.println("summaryStatistics: " + stats);

        // Converting Stream<T> to IntStream via mapToInt
        List<String> words = List.of("hello", "world", "java", "streams");
        int totalLength = words.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("mapToInt (total length): " + totalLength);

        // boxed() — convert IntStream to Stream<Integer>
        List<Integer> boxedList = IntStream.rangeClosed(1, 5)
                .boxed()
                .toList();
        System.out.println("boxed(): " + boxedList);

        // mapToObj — convert each int to an object
        List<String> hexValues = IntStream.of(10, 15, 255, 128)
                .mapToObj(n -> "0x" + Integer.toHexString(n).toUpperCase())
                .toList();
        System.out.println("mapToObj (hex): " + hexValues);

        // DoubleStream
        double doubleSum = DoubleStream.of(1.5, 2.5, 3.5, 4.5)
                .sum();
        System.out.println("DoubleStream sum: " + doubleSum);

        // LongStream
        long longSum = LongStream.rangeClosed(1, 1_000_000)
                .sum();
        System.out.println("LongStream sum(1..1_000_000): " + longSum);
    }

    // ============================================================
    // Section 8: FlatMap and Stream Flattening
    // ============================================================

    static void flatMapAndFlattening() {
        System.out.println("\n=== FlatMap and Stream Flattening ===");

        // Flattening a list of lists
        List<List<String>> nested = List.of(
                List.of("a", "b", "c"),
                List.of("d", "e"),
                List.of("f", "g", "h", "i")
        );
        List<String> flat = nested.stream()
                .flatMap(Collection::stream)
                .toList();
        System.out.println("flatMap (nested lists): " + flat);

        // FlatMap with orders — get all items across all orders
        List<Order> orders = List.of(
                new Order("Alice", List.of("laptop", "mouse"), 1200.0),
                new Order("Bob", List.of("keyboard", "monitor", "webcam"), 800.0),
                new Order("Charlie", List.of("headphones"), 150.0)
        );

        List<String> allItems = orders.stream()
                .flatMap(order -> order.items().stream())
                .toList();
        System.out.println("flatMap (all items): " + allItems);

        // FlatMap — split words into characters
        List<String> words = List.of("hello", "world");
        List<String> chars = words.stream()
                .flatMap(word -> word.chars().mapToObj(c -> String.valueOf((char) c)))
                .toList();
        System.out.println("flatMap (chars): " + chars);

        // FlatMap with Optional.stream() (Java 9) — filter out empty optionals
        List<Optional<String>> optionals = List.of(
                Optional.of("present"),
                Optional.empty(),
                Optional.of("also present"),
                Optional.empty()
        );
        List<String> presentValues = optionals.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("flatMap Optional.stream(): " + presentValues);

        // Practical pattern: map that may produce null, combined with ofNullable + flatMap
        Map<String, String> config = Map.of("host", "localhost", "port", "8080");
        List<String> keys = List.of("host", "port", "timeout");
        List<String> values = keys.stream()
                .flatMap(key -> Stream.ofNullable(config.get(key)))
                .toList();
        System.out.println("ofNullable + flatMap (config lookup): " + values);

        // flatMapToInt — primitive variant
        int totalItems = orders.stream()
                .flatMapToInt(order -> IntStream.of(order.items().size()))
                .sum();
        System.out.println("flatMapToInt (total item count): " + totalItems);
    }

    // ============================================================
    // Section 9: Parallel Streams
    // ============================================================

    static void parallelStreams() {
        System.out.println("\n=== Parallel Streams ===");

        List<Integer> numbers = IntStream.rangeClosed(1, 100).boxed().toList();

        // Creating a parallel stream from a collection
        long parallelSum = numbers.parallelStream()
                .mapToLong(Integer::longValue)
                .sum();
        System.out.println("parallelStream sum(1..100): " + parallelSum);

        // Converting sequential to parallel
        long sequentialSum = numbers.stream()
                .parallel()
                .mapToLong(Integer::longValue)
                .sum();
        System.out.println("stream().parallel() sum: " + sequentialSum);

        // forEach vs forEachOrdered in parallel
        System.out.print("parallel forEach (unordered): ");
        List.of(1, 2, 3, 4, 5).parallelStream()
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.print("parallel forEachOrdered: ");
        List.of(1, 2, 3, 4, 5).parallelStream()
                .forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // Parallel reduce — accumulator and combiner must be associative
        int parallelProduct = IntStream.rangeClosed(1, 10)
                .parallel()
                .reduce(1, (a, b) -> a * b);
        System.out.println("parallel reduce (product 1..10): " + parallelProduct);

        // Thread safety concern — DON'T do this (side effects with shared mutable state)
        // Correct approach: use collect or reduce
        List<Integer> safeResult = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList()); // thread-safe — Collectors handles synchronization
        System.out.println("parallel collect (even numbers count): " + safeResult.size());

        // Custom ForkJoinPool — control parallelism level
        try {
            ForkJoinPool customPool = new ForkJoinPool(2);
            long customResult = customPool.submit(() ->
                    numbers.parallelStream()
                            .filter(n -> n % 3 == 0)
                            .mapToLong(Integer::longValue)
                            .sum()
            ).join();
            System.out.println("custom ForkJoinPool(2) sum of multiples of 3: " + customResult);
            customPool.shutdown();
        } catch (Exception e) {
            System.out.println("custom pool error: " + e.getMessage());
        }

        // Checking if a stream is parallel
        var seqStream = numbers.stream();
        var parStream = numbers.parallelStream();
        System.out.println("sequential isParallel: " + seqStream.isParallel());
        System.out.println("parallel isParallel: " + parStream.isParallel());
        // Clean up unused streams
        seqStream.close();
        parStream.close();
    }

    // ============================================================
    // Section 10: Common Patterns and Best Practices
    // ============================================================

    static void commonPatternsAndBestPractices() {
        System.out.println("\n=== Common Patterns and Best Practices ===");

        // Pattern: find first matching element
        List<Person> people = List.of(
                new Person("Alice", 30, "Warsaw"),
                new Person("Bob", 25, "Krakow"),
                new Person("Charlie", 35, "Warsaw")
        );

        String firstFromWarsaw = people.stream()
                .filter(p -> p.city().equals("Warsaw"))
                .map(Person::name)
                .findFirst()
                .orElse("nobody");
        System.out.println("first from Warsaw: " + firstFromWarsaw);

        // Pattern: check existence
        boolean hasYoung = people.stream()
                .anyMatch(p -> p.age() < 30);
        System.out.println("has someone under 30: " + hasYoung);

        // Pattern: transform and collect with complex logic
        Map<String, List<String>> citiesWithPeople = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.mapping(Person::name, Collectors.toList())));
        System.out.println("cities with people: " + citiesWithPeople);

        // Pattern: chaining Optional with stream results
        String result = people.stream()
                .filter(p -> p.city().equals("Gdansk"))
                .map(Person::name)
                .findFirst()
                .map(name -> "Found: " + name)
                .orElse("No one from Gdansk");
        System.out.println("optional chaining: " + result);

        // Pattern: infinite stream with limit
        List<Integer> fibonacci = Stream.iterate(
                        new int[]{0, 1},
                        pair -> new int[]{pair[1], pair[0] + pair[1]}
                )
                .limit(10)
                .map(pair -> pair[0])
                .toList();
        System.out.println("fibonacci (first 10): " + fibonacci);

        // Pattern: stream as an index-based loop replacement
        List<String> items = List.of("alpha", "beta", "gamma", "delta");
        String indexed = IntStream.range(0, items.size())
                .mapToObj(i -> i + ": " + items.get(i))
                .collect(Collectors.joining(", "));
        System.out.println("indexed iteration: " + indexed);

        // Pattern: frequency map
        String sentence = "the quick brown fox jumps over the lazy dog the fox";
        Map<String, Long> wordFrequency = Arrays.stream(sentence.split(" "))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        System.out.println("word frequency: " + wordFrequency);

        // Don't reuse streams — create a new one each time
        // WRONG: var s = list.stream(); s.count(); s.forEach(...) // throws!
        // RIGHT: call list.stream() again for each operation
        List<String> names = List.of("Alice", "Bob", "Charlie");
        long nameCount = names.stream().count();
        String first = names.stream().findFirst().orElse("none");
        System.out.println("separate streams: count=" + nameCount + ", first=" + first);

        // Prefer method references for clarity
        List<String> upper = names.stream()
                .map(String::toUpperCase) // cleaner than s -> s.toUpperCase()
                .toList();
        System.out.println("method reference (toUpperCase): " + upper);
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        introductionToStreams();
        creatingStreams();
        intermediateOperations();
        terminalOperations();
        collectors();
        reductionOperations();
        primitiveStreams();
        flatMapAndFlattening();
        parallelStreams();
        commonPatternsAndBestPractices();
    }
}
