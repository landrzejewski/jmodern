package pl.training.jmodern.module02_java25;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;

// ============================================================
// Sekcja 1: Wprowadzenie -- Dlaczego ScopedValue
// ============================================================

/*
## Wprowadzenie -- Dlaczego ScopedValue

- **Problemy ThreadLocal z virtual threads**:
    - Marnowanie pamięci: miliony virtual threads = miliony
      niezależnych kopii ThreadLocal.
    - Mutowalność: każdy kod z referencją może wywołać `set()`,
      co utrudnia rozumowanie, jaka wartość jest aktualna.
    - Brak ograniczonego czasu życia: jeśli zapomnisz `remove()`,
      wartość wycieka przez cały czas życia wątku.
    - Odziedziczone kopie (InheritableThreadLocal) to niezależne
      mutowalne migawki — rodzic i dziecko mogą się cicho rozejść.
- **ScopedValue**: lekka, niemutowalna alternatywa.
    - Wartość jest wiązana na czas trwania lambdy przekazanej do
      `run()` lub `call()`, a potem automatycznie odwiązywana.
    - Gwarancja niemutowalności: raz związana, wartość nie może
      się zmienić w tym zakresie. Nie ma metody `set()`.
    - Automatycznie dziedziczone przez potomne virtual threads
      utworzone przez `StructuredTaskScope.fork()`.
*/

// ============================================================
// Sekcja 2: Tworzenie i wiązanie -- newInstance, where, run, call
// ============================================================

/*
## Tworzenie i wiązanie -- newInstance, where, run, call

- `ScopedValue.newInstance()` tworzy nowe niezwiązane ScopedValue.
  Konwencjonalnie deklarowane jako pola `private static final` —
  podobnie do loggerów.
- `ScopedValue.where(sv, value)` zwraca `Carrier`, który przechowuje
  wiązanie, ale jeszcze go nie aktywuje.
- `Carrier.run(Runnable)` — aktywuje wiązania, uruchamia lambdę,
  a następnie automatycznie odwiązuje po powrocie lambdy.
- `Carrier.call(CallableOp<R, X>)` — jak `run()`, ale zwraca
  wartość. `CallableOp<T, X extends Throwable>` to interfejs
  funkcyjny parametryzujący typ wyjątku (w przeciwieństwie do
  `Callable<T>`, który zawsze rzuca `Exception`).
- `ScopedValue.get()` pobiera związaną wartość z zakresu bieżącego
  wątku. Rzuca `NoSuchElementException` jeśli niezwiązana.
- Najlepsza praktyka: deklaruj jako pola `static final`, wiąż
  w punkcie wejścia (kontroler, handler), odczytuj głęboko w stosie wywołań.
*/

// ============================================================
// Sekcja 3: Ponowne wiązanie i zagnieżdżanie -- Przesłanianie, orElse, orElseThrow
// ============================================================

/*
## Ponowne wiązanie i zagnieżdżanie -- Przesłanianie, orElse, orElseThrow

- **Ponowne wiązanie** w zagnieżdżonym zakresie **przesłania** wiązanie zewnętrzne.
  Gdy wewnętrzny zakres się kończy, wiązanie zewnętrzne jest przywracane.
- Jest to analogiczne do leksykalnego przesłaniania zmiennych w większości
  języków programowania.
- Ponowne wiązanie to **jedyny sposób na "zmianę"** ScopedValue —
  nie ma metody `set()`.
- `orElse(T defaultValue)` — zwraca związaną wartość jeśli jest związana,
  lub wartość domyślną jeśli niezwiązana. Nigdy nie rzuca wyjątku.
- `orElseThrow(Supplier<X>)` — zwraca związaną wartość jeśli jest związana,
  lub rzuca dostarczony wyjątek jeśli niezwiązana. Przydatne do
  wymuszania obowiązkowego kontekstu.
*/

// ============================================================
// Sekcja 4: Wiązanie wielu wartości -- Łączenie Carrier
// ============================================================

/*
## Wiązanie wielu wartości -- Łączenie Carrier

- Łączenie wielu wiązań przez `.where().where().where()`:
    ScopedValue.where(USER, "alice")
               .where(TRACE_ID, "abc-123")
               .where(TENANT, "acme")
               .run(() -> { ... });
- Wszystkie wiązania są ustanawiane atomowo, gdy `run()` lub
  `call()` się rozpoczyna, i usuwane po powrocie lambdy.
- `Carrier.get(ScopedValue<T>)` pozwala sprawdzić wiązanie
  w carrierze przed wykonaniem zakresu.
- Zastępuje to anty-wzorzec "obiektu kontekstu" lub żonglowanie
  wieloma ThreadLocal.
*/

// ============================================================
// Sekcja 5: Integracja ze structured concurrency
// ============================================================

/*
## Integracja ze structured concurrency

- Scoped values są **automatycznie dziedziczone** przez potomne virtual
  threads tworzone przez `StructuredTaskScope.fork()`.
- Kluczowa synergia: structured concurrency = ograniczone czasy życia wątków,
  ScopedValue = ograniczone czasy życia kontekstu. Razem zapewniają,
  że propagacja kontekstu jest bezpieczna i wolna od wycieków.
- W przeciwieństwie do ThreadLocal (który daje każdemu dziecku niezależną
  mutowalną kopię), ScopedValue daje wszystkim dzieciom **widok
  tylko do odczytu** tego samego niemutowalnego wiązania.
- Typowy wzorzec:
    ScopedValue.where(USER, "alice").run(() -> {
        try (var scope = StructuredTaskScope.open(...)) {
            scope.fork(() -> { ... USER.get() ... }); // dziedziczone
            scope.join();
        }
    });
- Zadanie potomne może **ponownie związać** ScopedValue we własnym
  zagnieżdżonym zakresie bez wpływu na rodzica lub zadania siostrzane.
- Odniesienie: StructuredConcurrency.java Sekcja 5.
*/

public class ScopedValues {

    // ---- Deklaracje ScopedValue na poziomie klasy ----

    static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();
    static final ScopedValue<String> TENANT = ScopedValue.newInstance();

    // ---- Typy wewnętrzne ----

    record RequestContext(String requestId, String userId, String traceId) {}
    record AuditEntry(String action, String user, String threadName) {}
    record ServiceResult(String service, String data, String boundUser) {}

    // ---- Metody pomocnicze ----

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
    // Sekcja 1: Wprowadzenie -- Dlaczego ScopedValue
    // ============================================================

    static void introductionWhyScopedValue() {
        System.out.println("=== Section 1: Introduction -- Why ScopedValue ===");

        // ---- Demo 1: ThreadLocal vs ScopedValue ----
        System.out.println("\n--- Demo 1: ThreadLocal vs ScopedValue ---");

        // Podejście ThreadLocal: ręczne set/get/remove
        var threadLocal = new ThreadLocal<String>();
        threadLocal.set("alice");
        System.out.println("  ThreadLocal.get(): " + threadLocal.get());
        threadLocal.remove(); // Trzeba pamiętać o wywołaniu remove()!
        System.out.println("  ThreadLocal after remove(): " + threadLocal.get());
        System.out.println("  Problem: forgetting remove() causes leaks");

        // Podejście ScopedValue: automatyczne odwiązanie po wyjściu z run()
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  ScopedValue.get() inside run(): " + CURRENT_USER.get());
        });
        System.out.println("  ScopedValue after run(): isBound=" + CURRENT_USER.isBound());
        System.out.println("  Benefit: automatic cleanup, no possible leak");

        // ---- Demo 2: Niemutowalność ----
        System.out.println("\n--- Demo 2: Immutability ---");
        ScopedValue.where(CURRENT_USER, "bob").run(() -> {
            System.out.println("  CURRENT_USER = " + CURRENT_USER.get());
            System.out.println("  There is no set() method on ScopedValue");
            System.out.println("  The value is fixed for the entire scope");
            // CURRENT_USER.set("charlie"); // Nie kompiluje się — brak takiej metody
        });

        // ---- Demo 3: Dostęp do niezwiązanej wartości ----
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
    // Sekcja 2: Tworzenie i wiązanie -- newInstance, where, run, call
    // ============================================================

    static void creatingAndBindingScopedValues() throws Exception {
        System.out.println("\n=== Section 2: Creating and Binding -- newInstance, where, run, call ===");

        // ---- Demo 1: Podstawowe wiązanie z run() ----
        System.out.println("\n--- Demo 1: Basic binding with run() ---");
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Entry point: CURRENT_USER = " + CURRENT_USER.get());
            handleLayer("controller");
            handleLayer("service");
            handleLayer("repository");
            System.out.println("  Value propagated through entire call stack without parameter passing");
        });

        // ---- Demo 2: Wiązanie z call() ----
        System.out.println("\n--- Demo 2: Binding with call() ---");
        String result = ScopedValue.where(CURRENT_USER, "bob").call(() -> {
            var audit = auditAction("CREATE_ORDER");
            System.out.println("  " + audit);
            return "Order created for " + CURRENT_USER.get();
        });
        System.out.println("  call() returned: " + result);

        // call() z obsługą wyjątków
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

        // ---- Demo 3: Ponowne użycie Carrier ----
        System.out.println("\n--- Demo 3: Carrier reuse ---");
        var carrier = ScopedValue.where(CURRENT_USER, "eve");
        carrier.run(() -> System.out.println("  First run: " + CURRENT_USER.get()));
        carrier.run(() -> System.out.println("  Second run: " + CURRENT_USER.get()));
    }

    // ============================================================
    // Sekcja 3: Ponowne wiązanie i zagnieżdżanie -- Przesłanianie, orElse, orElseThrow
    // ============================================================

    static void rebindingAndNesting() {
        System.out.println("\n=== Section 3: Rebinding and Nesting -- Shadowing, orElse, orElseThrow ===");

        // ---- Demo 1: Zagnieżdżone ponowne wiązanie ----
        System.out.println("\n--- Demo 1: Nested rebinding (shadowing) ---");
        ScopedValue.where(CURRENT_USER, "outer-user").run(() -> {
            System.out.println("  Outer scope: " + CURRENT_USER.get());

            ScopedValue.where(CURRENT_USER, "inner-user").run(() -> {
                System.out.println("  Inner scope: " + CURRENT_USER.get());
            });

            System.out.println("  Outer scope restored: " + CURRENT_USER.get());
        });

        // ---- Demo 2: Wiele poziomów zagnieżdżania ----
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
        System.out.println("\n--- Demo 3: orElse() -- bezpieczne wartości domyślne ---");
        System.out.println("  Outside scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Inside scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));
        });
        System.out.println("  After scope: CURRENT_USER.orElse(\"guest\") = " + CURRENT_USER.orElse("guest"));

        // ---- Demo 4: orElseThrow() ----
        System.out.println("\n--- Demo 4: orElseThrow() -- wymuszanie obowiązkowego kontekstu ---");
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
    // Sekcja 4: Wiązanie wielu wartości
    // ============================================================

    static void multipleBindingsCarrierChaining() throws Exception {
        System.out.println("\n=== Section 4: Multiple Bindings -- Carrier Chaining ===");

        // ---- Demo 1: Łączenie ----
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
        System.out.println("\n--- Demo 2: Carrier.get() -- inspekcja przed wykonaniem ---");
        var carrier = ScopedValue.where(CURRENT_USER, "bob")
                .where(TRACE_ID, "trace-xyz-789")
                .where(TENANT, "globex");

        System.out.println("  Carrier.get(CURRENT_USER) = " + carrier.get(CURRENT_USER));
        System.out.println("  Carrier.get(TRACE_ID)     = " + carrier.get(TRACE_ID));
        System.out.println("  Carrier.get(TENANT)       = " + carrier.get(TENANT));
        System.out.println("  Inspected bindings before calling run()");
        carrier.run(() -> System.out.println("  Inside run(): CURRENT_USER = " + CURRENT_USER.get()));
    }

    // ============================================================
    // Sekcja 5: Integracja ze structured concurrency
    // ============================================================

    static void integrationWithStructuredConcurrency() throws Exception {
        System.out.println("\n=== Section 5: Integration with Structured Concurrency ===");

        System.out.println("\n--- Demo 1: Request tracing with ScopedValues ---");
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

        System.out.println("\n--- Demo 2: Rebinding in child task ---");
        ScopedValue.where(CURRENT_USER, "admin").run(() -> {
            try (var scope = StructuredTaskScope.open(Joiner.<String>allSuccessfulOrThrow())) {
                // To zadanie ponownie wiąże USER do "service-account" we własnym zakresie
                scope.fork(() -> {
                    return ScopedValue.where(CURRENT_USER, "service-account").call(() -> {
                        Thread.sleep(50);
                        return "Elevated task: user=" + CURRENT_USER.get();
                    });
                });

                // Te zadania nadal widzą oryginalne wiązanie "admin"
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
    // Main -- uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) throws Exception {
        introductionWhyScopedValue();
        creatingAndBindingScopedValues();
        rebindingAndNesting();
        multipleBindingsCarrierChaining();
        integrationWithStructuredConcurrency();
    }
}
