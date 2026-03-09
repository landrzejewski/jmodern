package pl.training.jmodern.module02_java25;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;

// ============================================================
// Section 1: Introduction -- Why ScopedValue
// ============================================================

/*
## Introduction -- Why ScopedValue

- **ThreadLocal problems with virtual threads**:
    - Memory waste: millions of virtual threads = millions of
      independent ThreadLocal copies.
    - Mutable: any code with a reference can call `set()`, making
      it hard to reason about what value is current.
    - No bounded lifetime: if you forget `remove()`, the value
      leaks for the thread's entire lifetime.
    - Inherited copies (InheritableThreadLocal) are independent
      mutable snapshots — parent and child can diverge silently.
- **ScopedValue**: a lightweight, immutable alternative.
    - Value is bound for the duration of a lambda passed to
      `run()` or `call()`, then automatically unbound.
    - Immutability guarantee: once bound, value cannot change
      within that scope. There is no `set()` method.
    - Automatically inherited by child virtual threads forked
      via `StructuredTaskScope.fork()`.
- **JEP timeline**:
    - JEP 429: Incubator in Java 20
    - JEP 446: Preview in Java 21
    - JEP 464: Second preview in Java 22
    - JEP 481: Third preview in Java 23
    - JEP 487: Fourth preview in Java 24
    - Preview in Java 25
- **API shape** (current as of Java 25):
    - `ScopedValue.newInstance()` — factory method
    - `ScopedValue.where(sv, value)` → `Carrier`
    - `Carrier.where(sv, value)` — chaining multiple bindings
    - `Carrier.run(Runnable)` / `Carrier.call(CallableOp)`
    - `ScopedValue.get()` — read the bound value
    - `ScopedValue.isBound()` — check if currently bound
    - `ScopedValue.orElse(T)` — bound value or default
    - `ScopedValue.orElseThrow(Supplier)` — bound value or throw
- **Note**: old `ScopedValue.runWhere()` no longer exists; the
  current API is `ScopedValue.where(...).run(...)`.
*/

// ============================================================
// Section 2: Creating and Binding -- newInstance, where, run, call
// ============================================================

/*
## Creating and Binding -- newInstance, where, run, call

- `ScopedValue.newInstance()` creates a new unbound ScopedValue.
  By convention, declared as `private static final` fields —
  similar to loggers.
- `ScopedValue.where(sv, value)` returns a `Carrier` that holds
  the binding but does not yet activate it.
- `Carrier.run(Runnable)` — activates bindings, runs the lambda,
  then automatically unbinds when the lambda returns.
- `Carrier.call(CallableOp<R, X>)` — like `run()` but returns
  a value. `CallableOp<T, X extends Throwable>` is a functional
  interface that parameterizes the exception type (unlike
  `Callable<T>` which always throws `Exception`).
- `ScopedValue.get()` retrieves the bound value from the current
  thread's scope. Throws `NoSuchElementException` if unbound.
- Best practice: declare as `static final` fields, bind at the
  entry point (controller, handler), read deep in the call stack.
*/

// ============================================================
// Section 3: Rebinding and Nesting -- Shadowing, orElse, orElseThrow
// ============================================================

/*
## Rebinding and Nesting -- Shadowing, orElse, orElseThrow

- **Rebinding** in a nested scope **shadows** the outer binding.
  When the inner scope exits, the outer binding is restored.
- This is analogous to lexical variable shadowing in most
  programming languages.
- Rebinding is the **only way to "change"** a ScopedValue —
  there is no `set()` method.
- `orElse(T defaultValue)` — returns the bound value if bound,
  or the default value if unbound. Never throws.
- `orElseThrow(Supplier<X>)` — returns the bound value if bound,
  or throws the supplied exception if unbound. Useful for
  mandatory context enforcement.
*/

// ============================================================
// Section 4: Multiple Bindings -- Carrier Chaining
// ============================================================

/*
## Multiple Bindings -- Carrier Chaining

- Chain multiple bindings with `.where().where().where()`:
    ScopedValue.where(USER, "alice")
               .where(TRACE_ID, "abc-123")
               .where(TENANT, "acme")
               .run(() -> { ... });
- All bindings are established atomically when `run()` or
  `call()` begins, and torn down when the lambda returns.
- `Carrier.get(ScopedValue<T>)` lets you inspect a binding
  in the carrier before executing the scope.
- This replaces the "context object" anti-pattern or juggling
  multiple ThreadLocals.
*/

// ============================================================
// Section 5: Integration with Structured Concurrency
// ============================================================

/*
## Integration with Structured Concurrency

- ScopedValues are **automatically inherited** by child virtual
  threads created via `StructuredTaskScope.fork()`.
- Key synergy: structured concurrency = bounded thread lifetimes,
  ScopedValue = bounded context lifetimes. Together they ensure
  context propagation is safe and leak-free.
- Unlike ThreadLocal (which gives each child an independent
  mutable copy), ScopedValue gives all children a **read-only
  view** of the same immutable binding.
- Typical pattern:
    ScopedValue.where(USER, "alice").run(() -> {
        try (var scope = StructuredTaskScope.open(...)) {
            scope.fork(() -> { ... USER.get() ... }); // inherited
            scope.join();
        }
    });
- A child task can **rebind** a ScopedValue in its own nested
  scope without affecting the parent or sibling tasks.
- Cross-reference: StructuredConcurrency.java Section 5.
*/

public class ScopedValues {

    // ---- Class-level ScopedValue declarations ----

    static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();
    static final ScopedValue<String> TENANT = ScopedValue.newInstance();

    // ---- Inner types ----

    record RequestContext(String requestId, String userId, String traceId) {}
    record AuditEntry(String action, String user, String threadName) {}
    record ServiceResult(String service, String data, String boundUser) {}

    // ---- Helper methods ----

    static void handleLayer(String layerName) {
        System.out.println("    [" + layerName + "] CURRENT_USER = " + CURRENT_USER.get());
    }

    static String auditAction(String action) {
        return "AUDIT: " + action + " by " + CURRENT_USER.get() + " on " + Thread.currentThread().getName();
    }

    static String resolveUser() {
        if (CURRENT_USER.isBound()) {
            return CURRENT_USER.get();
        }
        return "anonymous";
    }

    // ============================================================
    // Section 1: Introduction -- Why ScopedValue
    // ============================================================

    static void introductionWhyScopedValue() {
        System.out.println("=== Section 1: Introduction -- Why ScopedValue ===");

        // ---- Demo 1: ThreadLocal vs ScopedValue ----
        System.out.println("\n--- Demo 1: ThreadLocal vs ScopedValue ---");

        // ThreadLocal approach: manual set/get/remove
        var threadLocal = new ThreadLocal<String>();
        threadLocal.set("alice");
        System.out.println("  ThreadLocal.get(): " + threadLocal.get());
        threadLocal.remove(); // Must remember to call remove()!
        System.out.println("  ThreadLocal after remove(): " + threadLocal.get());
        System.out.println("  Problem: forgetting remove() causes leaks");

        // ScopedValue approach: auto-unbinds after run() exits
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  ScopedValue.get() inside run(): " + CURRENT_USER.get());
        });
        System.out.println("  ScopedValue after run(): isBound=" + CURRENT_USER.isBound());
        System.out.println("  Benefit: automatic cleanup, no possible leak");

        // ---- Demo 2: Immutability ----
        System.out.println("\n--- Demo 2: Immutability ---");
        ScopedValue.where(CURRENT_USER, "bob").run(() -> {
            System.out.println("  CURRENT_USER = " + CURRENT_USER.get());
            System.out.println("  There is no set() method on ScopedValue");
            System.out.println("  The value is fixed for the entire scope");
            // CURRENT_USER.set("charlie"); // Does not compile — no such method
        });

        // ---- Demo 3: Unbound access ----
        System.out.println("\n--- Demo 3: Unbound access ---");
        System.out.println("  Outside any scope: isBound=" + CURRENT_USER.isBound());
        try {
            CURRENT_USER.get();
            System.out.println("  Should not reach here");
        } catch (NoSuchElementException e) {
            System.out.println("  get() outside scope throws NoSuchElementException: " + e.getMessage());
        }

        ScopedValue.where(CURRENT_USER, "charlie").run(() -> {
            System.out.println("  Inside scope: isBound=" + CURRENT_USER.isBound() + ", get()=" + CURRENT_USER.get());
        });
    }

    // ============================================================
    // Section 2: Creating and Binding -- newInstance, where, run, call
    // ============================================================

    static void creatingAndBindingScopedValues() throws Exception {
        System.out.println("\n=== Section 2: Creating and Binding -- newInstance, where, run, call ===");

        // ---- Demo 1: Basic binding with run() ----
        System.out.println("\n--- Demo 1: Basic binding with run() ---");
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Entry point: CURRENT_USER = " + CURRENT_USER.get());
            handleLayer("controller");
            handleLayer("service");
            handleLayer("repository");
            System.out.println("  Value propagated through entire call stack without parameter passing");
        });

        // ---- Demo 2: Binding with call() ----
        System.out.println("\n--- Demo 2: Binding with call() ---");
        String result = ScopedValue.where(CURRENT_USER, "bob").call(() -> {
            var audit = auditAction("CREATE_ORDER");
            System.out.println("  " + audit);
            return "Order created for " + CURRENT_USER.get();
        });
        System.out.println("  call() returned: " + result);

        // call() with checked exception support
        System.out.println("  call() supports checked exceptions via CallableOp<R, X>:");
        try {
            ScopedValue.where(CURRENT_USER, "charlie").call(() -> {
                if (CURRENT_USER.get().equals("charlie")) {
                    throw new Exception("Access denied for " + CURRENT_USER.get());
                }
                return "OK";
            });
        } catch (Exception e) {
            System.out.println("  Caught checked exception: " + e.getMessage());
        }

        // ---- Demo 3: isBound() for conditional logic ----
        System.out.println("\n--- Demo 3: isBound() for conditional logic ---");
        System.out.println("  resolveUser() outside scope: " + resolveUser());
        ScopedValue.where(CURRENT_USER, "diana").run(() -> {
            System.out.println("  resolveUser() inside scope: " + resolveUser());
        });

        // ---- Demo 4: Carrier reuse ----
        System.out.println("\n--- Demo 4: Carrier reuse ---");
        var carrier = ScopedValue.where(CURRENT_USER, "eve");
        carrier.run(() -> System.out.println("  First run: " + CURRENT_USER.get()));
        carrier.run(() -> System.out.println("  Second run: " + CURRENT_USER.get()));
        carrier.run(() -> System.out.println("  Third run (same carrier): " + CURRENT_USER.get()));
    }

    // ============================================================
    // Section 3: Rebinding and Nesting -- Shadowing, orElse, orElseThrow
    // ============================================================

    static void rebindingAndNesting() {
        System.out.println("\n=== Section 3: Rebinding and Nesting -- Shadowing, orElse, orElseThrow ===");

        // ---- Demo 1: Nested rebinding ----
        System.out.println("\n--- Demo 1: Nested rebinding (shadowing) ---");
        ScopedValue.where(CURRENT_USER, "outer-user").run(() -> {
            System.out.println("  Outer scope: " + CURRENT_USER.get());

            ScopedValue.where(CURRENT_USER, "inner-user").run(() -> {
                System.out.println("  Inner scope: " + CURRENT_USER.get());
            });

            System.out.println("  Outer scope restored: " + CURRENT_USER.get());
        });

        // ---- Demo 2: Multiple nesting levels ----
        System.out.println("\n--- Demo 2: Multiple nesting levels ---");
        ScopedValue.where(CURRENT_USER, "level-1").run(() -> {
            System.out.println("  Enter level 1: " + CURRENT_USER.get());

            ScopedValue.where(CURRENT_USER, "level-2").run(() -> {
                System.out.println("  Enter level 2: " + CURRENT_USER.get());

                ScopedValue.where(CURRENT_USER, "level-3").run(() -> {
                    System.out.println("  Enter level 3: " + CURRENT_USER.get());
                });

                System.out.println("  Exit level 3, back to level 2: " + CURRENT_USER.get());
            });

            System.out.println("  Exit level 2, back to level 1: " + CURRENT_USER.get());
        });

        // ---- Demo 3: orElse() ----
        System.out.println("\n--- Demo 3: orElse() -- safe defaults ---");
        System.out.println("  Outside scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Inside scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));
        });
        System.out.println("  After scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));

        // ---- Demo 4: orElseThrow() ----
        System.out.println("\n--- Demo 4: orElseThrow() -- mandatory context enforcement ---");
        try {
            String user = CURRENT_USER.orElseThrow(() -> new IllegalStateException("No user in context!"));
            System.out.println("  Should not reach here: " + user);
        } catch (IllegalStateException e) {
            System.out.println("  Outside scope: " + e.getMessage());
        }

        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            String user = CURRENT_USER.orElseThrow(() -> new IllegalStateException("No user in context!"));
            System.out.println("  Inside scope: orElseThrow() returned \"" + user + "\"");
        });
    }

    // ============================================================
    // Section 4: Multiple Bindings -- Carrier Chaining
    // ============================================================

    static void multipleBindingsCarrierChaining() throws Exception {
        System.out.println("\n=== Section 4: Multiple Bindings -- Carrier Chaining ===");

        // ---- Demo 1: Chaining ----
        System.out.println("\n--- Demo 1: Chaining multiple ScopedValues ---");
        ScopedValue.where(CURRENT_USER, "alice")
                .where(TRACE_ID, "trace-abc-123")
                .where(TENANT, "acme-corp")
                .run(() -> {
                    System.out.println("  CURRENT_USER = " + CURRENT_USER.get());
                    System.out.println("  TRACE_ID     = " + TRACE_ID.get());
                    System.out.println("  TENANT       = " + TENANT.get());
                    System.out.println("  All three bound atomically with the scope");
                });

        // ---- Demo 2: Carrier.get() ----
        System.out.println("\n--- Demo 2: Carrier.get() -- inspect before execution ---");
        var carrier = ScopedValue.where(CURRENT_USER, "bob")
                .where(TRACE_ID, "trace-xyz-789")
                .where(TENANT, "globex");

        System.out.println("  Carrier.get(CURRENT_USER) = " + carrier.get(CURRENT_USER));
        System.out.println("  Carrier.get(TRACE_ID)     = " + carrier.get(TRACE_ID));
        System.out.println("  Carrier.get(TENANT)       = " + carrier.get(TENANT));
        System.out.println("  Inspected bindings before calling run()");
        carrier.run(() -> System.out.println("  Inside run(): CURRENT_USER = " + CURRENT_USER.get()));

        // ---- Demo 3: Request context pattern ----
        System.out.println("\n--- Demo 3: Request context pattern (controller -> service -> repository) ---");
        ScopedValue.where(CURRENT_USER, "charlie")
                .where(TRACE_ID, "trace-req-001")
                .where(TENANT, "initech")
                .run(() -> {
                    // Controller layer
                    System.out.println("  [Controller] Handling request for user=" + CURRENT_USER.get()
                            + ", trace=" + TRACE_ID.get());

                    // Service layer — no parameters passed, reads ScopedValues directly
                    System.out.println("  [Service] Processing order for tenant=" + TENANT.get()
                            + ", user=" + CURRENT_USER.get());

                    // Repository layer
                    System.out.println("  [Repository] Querying DB for tenant=" + TENANT.get()
                            + ", trace=" + TRACE_ID.get());

                    System.out.println("  No parameter passing needed — all layers read ScopedValues directly");
                });

        // ---- Demo 4: Partial rebinding ----
        System.out.println("\n--- Demo 4: Partial rebinding ---");
        ScopedValue.where(CURRENT_USER, "alice")
                .where(TENANT, "acme")
                .run(() -> {
                    System.out.println("  Outer: USER=" + CURRENT_USER.get() + ", TENANT=" + TENANT.get());

                    // Rebind only USER, TENANT stays from outer scope
                    ScopedValue.where(CURRENT_USER, "bob").run(() -> {
                        System.out.println("  Inner: USER=" + CURRENT_USER.get() + ", TENANT=" + TENANT.get());
                        System.out.println("  Only USER was rebound; TENANT inherited from outer scope");
                    });

                    System.out.println("  Outer restored: USER=" + CURRENT_USER.get() + ", TENANT=" + TENANT.get());
                });
    }

    // ============================================================
    // Section 5: Integration with Structured Concurrency
    // ============================================================

    static void integrationWithStructuredConcurrency() throws Exception {
        System.out.println("\n=== Section 5: Integration with Structured Concurrency ===");

        // ---- Demo 1: Inherited by forked tasks ----
        System.out.println("\n--- Demo 1: ScopedValue inherited by forked tasks ---");
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            try (var scope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                scope.fork(() -> {
                    Thread.sleep(50);
                    return "Task-1: user=" + CURRENT_USER.get() + " on " + Thread.currentThread().getName();
                });
                scope.fork(() -> {
                    Thread.sleep(80);
                    return "Task-2: user=" + CURRENT_USER.get() + " on " + Thread.currentThread().getName();
                });
                scope.fork(() -> {
                    Thread.sleep(60);
                    return "Task-3: user=" + CURRENT_USER.get() + " on " + Thread.currentThread().getName();
                });

                var results = scope.join().map(Subtask::get).toList();
                for (var r : results) {
                    System.out.println("  " + r);
                }
                System.out.println("  All forked tasks inherited CURRENT_USER=\"alice\"");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // ---- Demo 2: Request tracing ----
        System.out.println("\n--- Demo 2: Request tracing with ScopedValues ---");
        ScopedValue.where(CURRENT_USER, "bob")
                .where(TRACE_ID, "trace-req-42")
                .run(() -> {
                    try (var scope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                        scope.fork(() -> {
                            Thread.sleep(100);
                            return "[Profile] user=" + CURRENT_USER.get() + ", trace=" + TRACE_ID.get();
                        });
                        scope.fork(() -> {
                            Thread.sleep(120);
                            return "[Orders] user=" + CURRENT_USER.get() + ", trace=" + TRACE_ID.get();
                        });
                        scope.fork(() -> {
                            Thread.sleep(80);
                            return "[Recommendations] user=" + CURRENT_USER.get() + ", trace=" + TRACE_ID.get();
                        });

                        var results = scope.join().map(Subtask::get).toList();
                        System.out.println("  Request trace=" + TRACE_ID.get() + " results:");
                        for (var r : results) {
                            System.out.println("    " + r);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // ---- Demo 3: Rebinding in child task ----
        System.out.println("\n--- Demo 3: Rebinding in child task ---");
        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            try (var scope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                // This task rebinds USER to "service-account" in its own scope
                scope.fork(() -> {
                    return ScopedValue.where(CURRENT_USER, "service-account").call(() -> {
                        Thread.sleep(50);
                        return "Elevated task: user=" + CURRENT_USER.get();
                    });
                });

                // These tasks still see the original "admin" binding
                scope.fork(() -> {
                    Thread.sleep(60);
                    return "Sibling task 1: user=" + CURRENT_USER.get();
                });
                scope.fork(() -> {
                    Thread.sleep(70);
                    return "Sibling task 2: user=" + CURRENT_USER.get();
                });

                var results = scope.join().map(Subtask::get).toList();
                for (var r : results) {
                    System.out.println("  " + r);
                }
                System.out.println("  Rebinding in child didn't affect parent or siblings");
                System.out.println("  Parent still sees: user=" + CURRENT_USER.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ============================================================
    // Main -- run all sections
    // ============================================================

    public static void main(String[] args) throws Exception {
        introductionWhyScopedValue();
        creatingAndBindingScopedValues();
        rebindingAndNesting();
        multipleBindingsCarrierChaining();
        integrationWithStructuredConcurrency();
    }
}
