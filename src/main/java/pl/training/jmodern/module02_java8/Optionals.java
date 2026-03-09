package pl.training.jmodern.module02_java8;

import java.util.*;
import java.util.stream.*;

// ============================================================
// Section 1: Introduction to Optional
// ============================================================

/*
## Introduction to Optional

- `Optional<T>` is a container object that may or may not hold a
  non-null value. Introduced in Java 8 to provide a **type-level**
  solution for representing the absence of a value.
- **Why it was introduced**: `NullPointerException` is the most
  common runtime exception in Java. When a method returns `null`,
  the caller has no way of knowing — from the signature alone —
  whether `null` is a valid return. `Optional` makes "absence"
  **explicit** in the type system.
- **`Optional` as return type vs returning `null`**:
    - Returning `null` forces every caller to defensively check
      for `null`, and forgetting the check leads to NPE.
    - Returning `Optional<T>` communicates *intent*: "this method
      may legitimately not produce a result." The compiler and IDE
      can guide the caller to handle both cases.
- **`Optional` is not a general-purpose `Maybe`**: it was designed
  for **method return types**, not for:
    - **Fields** — adds indirection, breaks serialization.
    - **Method parameters** — use overloading or `@Nullable`.
    - **Collections** — use an empty collection instead.
- **Relationship to the null-object pattern**: both approaches
  eliminate explicit null checks, but Optional is a general-purpose
  container while the null-object pattern provides a domain-specific
  "do nothing" implementation of an interface.
*/

// ============================================================
// Section 2: Creating Optionals
// ============================================================

/*
## Creating Optionals

- `Optional.of(value)` — wraps a non-null value. Throws
  `NullPointerException` immediately if `value` is null.
  Use when you are certain the value is not null.
- `Optional.ofNullable(value)` — wraps the value if non-null,
  returns `Optional.empty()` if null. Use when the value may
  legitimately be null (e.g., from a legacy API).
- `Optional.empty()` — returns an empty Optional. Use as a
  return value when there is no result to provide.
- **When to use each factory method**:
    - `of()` → fast-fail if null (programming error to pass null).
    - `ofNullable()` → gracefully handle nullable values.
    - `empty()` → explicit "no result" in conditional returns.
- **Primitive variants**: `OptionalInt`, `OptionalLong`,
  `OptionalDouble` — avoid autoboxing overhead. They mirror
  `Optional<T>` but with primitive-specific methods like
  `getAsInt()`, `orElse(int)`, etc.
*/

// ============================================================
// Section 3: Checking and Extracting Values
// ============================================================

/*
## Checking and Extracting Values

- `isPresent()` — returns `true` if a value is present.
- `isEmpty()` (Java 11) — returns `true` if no value is present;
  the logical negation of `isPresent()`.
- `get()` — returns the value if present, otherwise throws
  `NoSuchElementException`. **Avoid in production code** — it
  defeats the purpose of Optional by introducing a potential
  unchecked exception.
- `orElse(defaultValue)` — returns the value if present, or
  the provided default. The default is **always evaluated**,
  even when the Optional has a value.
- `orElseGet(Supplier)` — returns the value if present, or
  **lazily** computes the default using the Supplier. The
  Supplier is called **only when the Optional is empty**.
- `orElseThrow()` (Java 10) — same as `get()` but with a
  clearer, more intentional name.
- `orElseThrow(Supplier<Exception>)` — throws a custom
  exception if empty. Preferred for domain-specific errors.
- **Important**: `orElse` vs `orElseGet` — `orElse` always
  evaluates its argument (which can be costly), while
  `orElseGet` defers evaluation. Use `orElseGet` when the
  default value is expensive to compute.
*/

// ============================================================
// Section 4: Transforming Optionals (map, flatMap, filter)
// ============================================================

/*
## Transforming Optionals (`map`, `flatMap`, `filter`)

- `map(Function)` — if a value is present, applies the function
  and wraps the result in a new Optional. If empty, returns empty.
  Signature: `Optional<T>.map(T -> U)` → `Optional<U>`.
- `flatMap(Function)` — like `map`, but the mapping function
  itself returns `Optional<U>`. Avoids `Optional<Optional<T>>`.
  Use when the transformation may also produce an absent result.
- `filter(Predicate)` — if the value is present **and** matches
  the predicate, returns the same Optional; otherwise returns empty.
- **Chaining**: `map`, `filter`, and `flatMap` can be chained to
  build fluent pipelines that replace nested null checks:
  ```
  optional.map(User::getAddress)
          .map(Address::getCity)
          .filter(city -> city.startsWith("W"))
          .orElse("Unknown")
  ```
- **`map` vs `flatMap`** — the same distinction as in the Stream
  API: use `map` when the function returns a plain value, use
  `flatMap` when the function returns an `Optional`.
*/

// ============================================================
// Section 5: Conditional Actions (ifPresent, ifPresentOrElse)
// ============================================================

/*
## Conditional Actions (`ifPresent`, `ifPresentOrElse`)

- `ifPresent(Consumer)` — executes the given action only if a
  value is present. Returns `void`. This is the "do something"
  equivalent of `map` (which is "transform something").
- `ifPresentOrElse(Consumer, Runnable)` (Java 9) — executes the
  Consumer if present, or the Runnable if empty. Handles both
  cases in a single call.
- **Replacing `if (x != null)` patterns**: instead of:
  ```
  if (opt.isPresent()) {
      process(opt.get());
  }
  ```
  use:
  ```
  opt.ifPresent(this::process);
  ```
- **`ifPresent` vs `isPresent` + `get`**: `ifPresent` is safer
  because it eliminates the possibility of calling `get()` on
  an empty Optional. It expresses intent more clearly and is
  more concise.
*/

// ============================================================
// Section 6: Combining Optionals (or, stream)
// ============================================================

/*
## Combining Optionals (`or`, `stream`)

- `or(Supplier<Optional>)` (Java 9) — if a value is present,
  returns `this`; otherwise returns the Optional produced by the
  Supplier. Unlike `orElse`/`orElseGet` which unwrap to the raw
  value, `or()` stays in the Optional world.
- `stream()` (Java 9) — converts the Optional to a zero-or-one
  element Stream. Returns `Stream.of(value)` if present, or
  `Stream.empty()` if not.
- **Using `stream()` with `flatMap` to filter empty Optionals**:
  given a `List<Optional<T>>`, you can extract all present values:
  ```
  optionals.stream()
           .flatMap(Optional::stream)
           .collect(toList())
  ```
- **Fallback chains**: multiple `or()` calls can be chained:
  ```
  findInCache(key)
      .or(() -> findInDatabase(key))
      .or(() -> findInRemoteService(key))
  ```
*/

// ============================================================
// Section 7: Optional with Streams
// ============================================================

/*
## Optional with Streams

- Many Stream terminal operations return Optional:
    - `findFirst()` / `findAny()` — first/any matching element.
    - `min(Comparator)` / `max(Comparator)` — smallest/largest.
    - `reduce(BinaryOperator)` — accumulated result (the
      two-argument `reduce(identity, op)` does **not** return
      Optional because it always has the identity as a fallback).
- `Optional.stream()` (Java 9) bridges Optional back into the
  Stream world, enabling integration in larger pipelines.
- **Pattern: `Stream<Optional<T>>` → `Stream<T>`**:
  ```
  stream.flatMap(Optional::stream)
  ```
  This replaces the pre-Java-9 idiom:
  ```
  stream.filter(Optional::isPresent).map(Optional::get)
  ```
- **Primitive Optional variants**: primitive streams (`IntStream`,
  `LongStream`, `DoubleStream`) return `OptionalInt`,
  `OptionalLong`, `OptionalDouble` from `min()`, `max()`,
  `findFirst()`, `average()`, `reduce()`.
*/

// ============================================================
// Section 8: Common Patterns and Anti-Patterns
// ============================================================

/*
## Common Patterns and Anti-Patterns

- **Anti-pattern**: `if (opt.isPresent()) opt.get()` — use
  `orElse`, `map`, or `ifPresent` instead. Using `get()` after
  `isPresent()` is verbose and error-prone.
- **Anti-pattern**: `Optional` as method parameter — forces
  callers to wrap values unnecessarily. Use method overloading
  or `@Nullable` annotations instead.
- **Anti-pattern**: `Optional` as field type — adds memory
  overhead, breaks serialization frameworks. Use `null` internally
  and expose Optional via a getter if needed:
  ```
  private String email; // nullable
  public Optional<String> getEmail() { return Optional.ofNullable(email); }
  ```
- **Anti-pattern**: `Optional.of(collection)` — an empty collection
  already represents "no elements". Wrapping it adds nothing.
  Prefer returning an empty `List`/`Set`/`Map`.
- **Pattern**: replacing nested null checks with `map`/`flatMap`:
  ```
  // Before: if (user != null && user.getAddress() != null && ...)
  // After:
  Optional.ofNullable(user)
          .flatMap(User::optionalAddress)
          .map(Address::city)
          .orElse("unknown")
  ```
- **Pattern**: Optional in return types for repository/finder
  methods: `Optional<User> findByEmail(String email)`.
- **Pattern**: converting legacy nullable APIs to Optional:
  `Optional.ofNullable(legacyMap.get(key))`.
- **Serialization warning**: `Optional` does **not** implement
  `Serializable`. It should not be used as a field in classes
  that need to be serialized (DTOs, entities, etc.).
*/

public class Optionals {

    // --- Helper types for demonstrations ---

    record Address(String city, String zip) {}

    record User(String name, String email, Address address) {

        Optional<String> optionalEmail() {
            return Optional.ofNullable(email);
        }

        Optional<Address> optionalAddress() {
            return Optional.ofNullable(address);
        }
    }

    // --- Helper repository methods ---

    private static final Map<String, User> USER_DB = Map.of(
            "alice", new User("Alice", "alice@example.com", new Address("Warsaw", "00-001")),
            "bob", new User("Bob", null, new Address("Krakow", "30-001")),
            "charlie", new User("Charlie", "charlie@example.com", null)
    );

    static Optional<User> findUserByName(String name) {
        return Optional.ofNullable(USER_DB.get(name.toLowerCase()));
    }

    static Optional<String> findEmailByName(String name) {
        return findUserByName(name).flatMap(User::optionalEmail);
    }

    // Simulates an expensive default computation
    static String computeExpensiveDefault() {
        System.out.println("  (computing expensive default...)");
        return "default@example.com";
    }

    // ============================================================
    // Section 1: Introduction to Optional
    // ============================================================

    static void introductionToOptional() {
        System.out.println("=== Introduction to Optional ===");

        // The problem: a method returns null — caller has no type-level signal
        Map<String, String> config = Map.of("host", "localhost", "port", "8080");
        String timeout = config.get("timeout"); // returns null — no entry
        // Without Optional, we must remember to check:
        if (timeout != null) {
            System.out.println("timeout: " + timeout);
        } else {
            System.out.println("timeout not configured (null check)");
        }

        // With Optional: intent is explicit in the return type
        Optional<String> maybeTimeout = Optional.ofNullable(config.get("timeout"));
        System.out.println("timeout via Optional: " + maybeTimeout.orElse("30s (default)"));

        // Optional communicates that absence is a valid outcome
        Optional<User> foundUser = findUserByName("alice");
        Optional<User> missingUser = findUserByName("unknown");
        System.out.println("found user: " + foundUser);
        System.out.println("missing user: " + missingUser);

        // Optional vs null-object pattern
        // null-object: a concrete "do nothing" implementation (domain-specific)
        // Optional: a general-purpose container for any type
        System.out.println("Optional.empty() is a general-purpose 'no value': " + Optional.empty());
    }

    // ============================================================
    // Section 2: Creating Optionals
    // ============================================================

    static void creatingOptionals() {
        System.out.println("\n=== Creating Optionals ===");

        // Optional.of — wraps a non-null value
        Optional<String> present = Optional.of("Hello");
        System.out.println("Optional.of(\"Hello\"): " + present);

        // Optional.of(null) throws NullPointerException immediately
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("Optional.of(null): NullPointerException — " + e.getMessage());
        }

        // Optional.ofNullable — safe for potentially null values
        String value = null;
        Optional<String> nullable = Optional.ofNullable(value);
        System.out.println("Optional.ofNullable(null): " + nullable);

        Optional<String> nonNull = Optional.ofNullable("World");
        System.out.println("Optional.ofNullable(\"World\"): " + nonNull);

        // Optional.empty — the empty Optional
        Optional<String> empty = Optional.empty();
        System.out.println("Optional.empty(): " + empty);

        // When to use which:
        // of()         → you know the value is non-null (fail-fast if wrong)
        // ofNullable() → the value may be null (from legacy API, Map.get, etc.)
        // empty()      → explicitly returning "no result"

        // Primitive variants — avoid autoboxing
        OptionalInt optInt = OptionalInt.of(42);
        OptionalLong optLong = OptionalLong.of(100_000_000L);
        OptionalDouble optDouble = OptionalDouble.of(3.14);
        System.out.println("OptionalInt: " + optInt);
        System.out.println("OptionalLong: " + optLong);
        System.out.println("OptionalDouble: " + optDouble);

        OptionalInt emptyInt = OptionalInt.empty();
        System.out.println("OptionalInt.empty(): " + emptyInt);
    }

    // ============================================================
    // Section 3: Checking and Extracting Values
    // ============================================================

    static void checkingAndExtractingValues() {
        System.out.println("\n=== Checking and Extracting Values ===");

        Optional<String> present = Optional.of("Java");
        Optional<String> empty = Optional.empty();

        // isPresent / isEmpty
        System.out.println("present.isPresent(): " + present.isPresent());
        System.out.println("empty.isPresent(): " + empty.isPresent());
        System.out.println("empty.isEmpty(): " + empty.isEmpty()); // Java 11

        // get() — throws NoSuchElementException if empty (avoid in production)
        System.out.println("present.get(): " + present.get());
        try {
            empty.get();
        } catch (NoSuchElementException e) {
            System.out.println("empty.get(): NoSuchElementException — " + e.getMessage());
        }

        // orElse — return default value (always evaluated)
        System.out.println("present.orElse(\"default\"): " + present.orElse("default"));
        System.out.println("empty.orElse(\"default\"): " + empty.orElse("default"));

        // orElseGet — lazily compute default (Supplier called only when empty)
        System.out.println("present.orElseGet(() -> ...): " + present.orElseGet(() -> "computed"));
        System.out.println("empty.orElseGet(() -> ...): " + empty.orElseGet(() -> "computed"));

        // orElse vs orElseGet — important difference:
        // orElse ALWAYS evaluates its argument, even when value is present
        System.out.println("--- orElse vs orElseGet side-effect demo ---");
        System.out.println("present.orElse(expensive): " + present.orElse(computeExpensiveDefault()));
        System.out.println("present.orElseGet(expensive): " + present.orElseGet(Optionals::computeExpensiveDefault));
        // Notice: orElse printed "(computing expensive default...)" even though value was present
        // orElseGet did NOT compute the default because the value was present

        // orElseThrow() — Java 10 — same as get() but with a clearer name
        System.out.println("present.orElseThrow(): " + present.orElseThrow());

        // orElseThrow(Supplier) — throw a custom exception
        try {
            empty.orElseThrow(() -> new IllegalStateException("value is required"));
        } catch (IllegalStateException e) {
            System.out.println("empty.orElseThrow(custom): " + e.getMessage());
        }
    }

    // ============================================================
    // Section 4: Transforming Optionals (map, flatMap, filter)
    // ============================================================

    static void transformingOptionals() {
        System.out.println("\n=== Transforming Optionals (map, flatMap, filter) ===");

        Optional<String> name = Optional.of("Alice");
        Optional<String> empty = Optional.empty();

        // map — transform the value if present
        Optional<Integer> nameLength = name.map(String::length);
        Optional<Integer> emptyLength = empty.map(String::length);
        System.out.println("name.map(length): " + nameLength);
        System.out.println("empty.map(length): " + emptyLength);

        Optional<String> upperName = name.map(String::toUpperCase);
        System.out.println("name.map(toUpperCase): " + upperName);

        // filter — keep value only if it matches the predicate
        Optional<String> startsWithA = name.filter(n -> n.startsWith("A"));
        Optional<String> startsWithB = name.filter(n -> n.startsWith("B"));
        System.out.println("name.filter(startsWith A): " + startsWithA);
        System.out.println("name.filter(startsWith B): " + startsWithB);

        // flatMap — when the mapping function itself returns Optional
        // Avoids Optional<Optional<T>>
        Optional<String> aliceEmail = findUserByName("Alice").flatMap(User::optionalEmail);
        Optional<String> bobEmail = findUserByName("Bob").flatMap(User::optionalEmail);
        Optional<String> unknownEmail = findUserByName("unknown").flatMap(User::optionalEmail);
        System.out.println("Alice's email (flatMap): " + aliceEmail);
        System.out.println("Bob's email (flatMap): " + bobEmail);     // empty — Bob has no email
        System.out.println("Unknown's email (flatMap): " + unknownEmail); // empty — user not found

        // Chaining map/flatMap/filter for fluent pipelines
        // "Find Alice's city, but only if it starts with 'W'"
        String city = findUserByName("Alice")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .filter(c -> c.startsWith("W"))
                .orElse("unknown");
        System.out.println("Alice's city (starts with W): " + city);

        // Same chain for Charlie — who has no address
        String charlieCity = findUserByName("Charlie")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("Charlie's city: " + charlieCity); // unknown — no address

        // map vs flatMap comparison
        // map: function returns a plain value → wrapped in Optional automatically
        // flatMap: function returns Optional<U> → no double wrapping
        Optional<Optional<String>> doubleWrapped = findUserByName("Alice").map(User::optionalEmail);
        Optional<String> singleWrapped = findUserByName("Alice").flatMap(User::optionalEmail);
        System.out.println("map (double wrapped): " + doubleWrapped);
        System.out.println("flatMap (single wrapped): " + singleWrapped);
    }

    // ============================================================
    // Section 5: Conditional Actions (ifPresent, ifPresentOrElse)
    // ============================================================

    static void conditionalActions() {
        System.out.println("\n=== Conditional Actions (ifPresent, ifPresentOrElse) ===");

        Optional<String> present = Optional.of("Java");
        Optional<String> empty = Optional.empty();

        // ifPresent — execute action only if value is present
        System.out.print("present.ifPresent: ");
        present.ifPresent(v -> System.out.println("value is " + v));

        System.out.print("empty.ifPresent: ");
        empty.ifPresent(v -> System.out.println("value is " + v));
        System.out.println("(nothing printed — empty)");

        // ifPresentOrElse — Java 9 — handle both cases
        present.ifPresentOrElse(
                v -> System.out.println("ifPresentOrElse (present): " + v),
                () -> System.out.println("ifPresentOrElse (present): no value")
        );
        empty.ifPresentOrElse(
                v -> System.out.println("ifPresentOrElse (empty): " + v),
                () -> System.out.println("ifPresentOrElse (empty): no value")
        );

        // Replacing if (x != null) patterns
        // Before:
        Optional<User> user = findUserByName("alice");
        if (user.isPresent()) {
            System.out.println("isPresent+get: " + user.get().name());
        }
        // After (preferred):
        findUserByName("alice").ifPresent(u ->
                System.out.println("ifPresent: " + u.name())
        );

        // Practical example: send email only if address is present
        findUserByName("alice").flatMap(User::optionalEmail).ifPresentOrElse(
                email -> System.out.println("sending email to: " + email),
                () -> System.out.println("no email address — skipping notification")
        );

        findUserByName("bob").flatMap(User::optionalEmail).ifPresentOrElse(
                email -> System.out.println("sending email to: " + email),
                () -> System.out.println("no email address for Bob — skipping notification")
        );
    }

    // ============================================================
    // Section 6: Combining Optionals (or, stream)
    // ============================================================

    // Simulated fallback sources
    static Optional<String> findInCache(String key) {
        System.out.println("  looking in cache...");
        return Optional.empty(); // simulate cache miss
    }

    static Optional<String> findInDatabase(String key) {
        System.out.println("  looking in database...");
        if ("config.timeout".equals(key)) {
            return Optional.of("30s");
        }
        return Optional.empty();
    }

    static Optional<String> findInDefaults(String key) {
        System.out.println("  looking in defaults...");
        return Optional.of("60s");
    }

    static void combiningOptionals() {
        System.out.println("\n=== Combining Optionals (or, stream) ===");

        Optional<String> present = Optional.of("primary");
        Optional<String> empty = Optional.empty();

        // or() — Java 9 — fallback to another Optional (stays in Optional world)
        Optional<String> result1 = present.or(() -> Optional.of("fallback"));
        Optional<String> result2 = empty.or(() -> Optional.of("fallback"));
        System.out.println("present.or(fallback): " + result1);
        System.out.println("empty.or(fallback): " + result2);

        // Chaining multiple or() calls for fallback chains
        System.out.println("--- fallback chain for 'config.timeout' ---");
        String timeout = findInCache("config.timeout")
                .or(() -> findInDatabase("config.timeout"))
                .or(() -> findInDefaults("config.timeout"))
                .orElse("unknown");
        System.out.println("resolved timeout: " + timeout);

        System.out.println("--- fallback chain for 'config.missing' ---");
        String missing = findInCache("config.missing")
                .or(() -> findInDatabase("config.missing"))
                .or(() -> findInDefaults("config.missing"))
                .orElse("unknown");
        System.out.println("resolved missing: " + missing);

        // stream() — Java 9 — convert Optional to a zero-or-one element Stream
        Stream<String> presentStream = present.stream();
        Stream<String> emptyStream = empty.stream();
        System.out.println("present.stream().toList(): " + presentStream.toList());
        System.out.println("empty.stream().toList(): " + emptyStream.toList());

        // Using stream() with flatMap to filter empty Optionals from a collection
        List<Optional<String>> optionals = List.of(
                Optional.of("alpha"),
                Optional.empty(),
                Optional.of("beta"),
                Optional.empty(),
                Optional.of("gamma")
        );

        List<String> presentValues = optionals.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("filter empty Optionals: " + presentValues);
    }

    // ============================================================
    // Section 7: Optional with Streams
    // ============================================================

    static void optionalWithStreams() {
        System.out.println("\n=== Optional with Streams ===");

        List<Integer> numbers = List.of(5, 3, 8, 1, 9, 2, 7);

        // Terminal operations returning Optional
        Optional<Integer> first = numbers.stream().filter(n -> n > 6).findFirst();
        Optional<Integer> any = numbers.stream().filter(n -> n > 6).findAny();
        Optional<Integer> min = numbers.stream().min(Comparator.naturalOrder());
        Optional<Integer> max = numbers.stream().max(Comparator.naturalOrder());
        Optional<Integer> sum = numbers.stream().reduce(Integer::sum);

        System.out.println("findFirst (> 6): " + first);
        System.out.println("findAny (> 6): " + any);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("reduce (sum): " + sum);

        // Empty stream — terminal operations return empty Optional
        Optional<Integer> emptyFirst = Stream.<Integer>empty().findFirst();
        Optional<Integer> emptyMin = Stream.<Integer>empty().min(Comparator.naturalOrder());
        System.out.println("findFirst on empty stream: " + emptyFirst);
        System.out.println("min on empty stream: " + emptyMin);

        // reduce with identity does NOT return Optional (always has a result)
        int sumWithIdentity = numbers.stream().reduce(0, Integer::sum);
        System.out.println("reduce with identity: " + sumWithIdentity);

        // Optional.stream() for integrating into stream pipelines
        List<String> userNames = List.of("alice", "bob", "unknown", "charlie", "nobody");
        List<String> emails = userNames.stream()
                .map(Optionals::findEmailByName) // Stream<Optional<String>>
                .flatMap(Optional::stream)          // Stream<String> — empties removed
                .toList();
        System.out.println("emails found: " + emails);

        // Primitive Optional variants from primitive streams
        OptionalInt maxInt = IntStream.of(10, 20, 30).max();
        OptionalDouble average = IntStream.rangeClosed(1, 10).average();
        OptionalLong minLong = LongStream.of(100L, 200L, 50L).min();
        System.out.println("IntStream.max(): " + maxInt);
        System.out.println("IntStream.average(): " + average);
        System.out.println("LongStream.min(): " + minLong);

        // Extracting primitive optional values
        int maxValue = maxInt.orElse(-1);
        double avgValue = average.orElse(0.0);
        System.out.println("maxInt.orElse(-1): " + maxValue);
        System.out.println("average.orElse(0.0): " + avgValue);
    }

    // ============================================================
    // Section 8: Common Patterns and Anti-Patterns
    // ============================================================

    static void commonPatternsAndAntiPatterns() {
        System.out.println("\n=== Common Patterns and Anti-Patterns ===");

        // ANTI-PATTERN 1: isPresent() + get() — verbose and error-prone
        Optional<User> user = findUserByName("alice");
        // Bad:
        if (user.isPresent()) {
            System.out.println("[anti-pattern] isPresent+get: " + user.get().name());
        }
        // Good:
        user.ifPresent(u -> System.out.println("[pattern] ifPresent: " + u.name()));
        String name = user.map(User::name).orElse("unknown");
        System.out.println("[pattern] map+orElse: " + name);

        // ANTI-PATTERN 2: Optional as method parameter
        // Bad: void sendEmail(Optional<String> address) { ... }
        // Good: overload methods or use @Nullable
        // (not demonstrated in code as it's a design guideline)
        System.out.println("[anti-pattern] Optional as parameter — use overloading instead");

        // ANTI-PATTERN 3: Optional as field type
        // Bad: private Optional<String> email;
        // Good: private String email; // nullable
        //       public Optional<String> getEmail() { return Optional.ofNullable(email); }
        // Our User record demonstrates the correct pattern:
        User bob = new User("Bob", null, null);
        System.out.println("[pattern] nullable field + Optional getter: " + bob.optionalEmail());

        // ANTI-PATTERN 4: Optional.of(emptyCollection)
        // Bad:
        Optional<List<String>> wrappedList = Optional.of(List.of());
        System.out.println("[anti-pattern] Optional<List>: " + wrappedList);
        // Good: just return the empty collection
        List<String> emptyList = List.of();
        System.out.println("[pattern] empty collection: " + emptyList);

        // PATTERN: replacing nested null checks with map/flatMap chains
        // Before (imperative):
        //   if (user != null) {
        //     Address addr = user.getAddress();
        //     if (addr != null) {
        //       String city = addr.city();
        //       if (city != null) { ... }
        //     }
        //   }
        // After (functional):
        String aliceCity = findUserByName("alice")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("[pattern] nested null check replacement: " + aliceCity);

        String charlieCity = findUserByName("charlie")
                .flatMap(User::optionalAddress)
                .map(Address::city)
                .orElse("unknown");
        System.out.println("[pattern] charlie's city (no address): " + charlieCity);

        // PATTERN: Optional in return types for repository/finder methods
        // Our findUserByName demonstrates this — returns Optional<User>
        findUserByName("alice").ifPresentOrElse(
                u -> System.out.println("[pattern] repository find: " + u.name()),
                () -> System.out.println("[pattern] repository find: not found")
        );
        findUserByName("unknown").ifPresentOrElse(
                u -> System.out.println("[pattern] repository find: " + u.name()),
                () -> System.out.println("[pattern] repository find: not found")
        );

        // PATTERN: converting legacy nullable APIs to Optional
        Map<String, String> legacyConfig = new HashMap<>();
        legacyConfig.put("host", "localhost");
        // Map.get returns null if key not found — wrap with ofNullable
        Optional<String> host = Optional.ofNullable(legacyConfig.get("host"));
        Optional<String> port = Optional.ofNullable(legacyConfig.get("port"));
        System.out.println("[pattern] legacy API wrapping — host: " + host);
        System.out.println("[pattern] legacy API wrapping — port: " + port);

        // Serialization warning
        System.out.println("[warning] Optional does NOT implement Serializable");
        System.out.println("  → do not use Optional as field type in DTOs or entities");
        System.out.println("  → use it only as method return type");
    }

    // ============================================================
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) {
        introductionToOptional();
        creatingOptionals();
        checkingAndExtractingValues();
        transformingOptionals();
        conditionalActions();
        combiningOptionals();
        optionalWithStreams();
        commonPatternsAndAntiPatterns();
    }
}
