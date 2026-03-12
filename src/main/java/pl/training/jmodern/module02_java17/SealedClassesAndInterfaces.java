package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do klas sealed — Dlaczego ograniczać dziedziczenie?
// ============================================================

/*
## Wprowadzenie do klas sealed — Dlaczego ograniczać dziedziczenie?

- W standardowej Javie każda klasa, która nie jest `final`, może być
  rozszerzona przez **kogokolwiek**, gdziekolwiek. To tworzy **otwarte
  hierarchie**, gdzie autor nie ma kontroli nad tym, jakie podtypy istnieją.
- **Problemy z otwartymi hierarchiami**:
    - Nie można pisać wyczerpujących łańcuchów `switch` lub `if-else`,
      ponieważ nieznana podklasa może pojawić się w czasie wykonania.
    - Twórcy bibliotek nie mogą bezpiecznie ewoluować hierarchii klas —
      każda zmiana może złamać nieznane podklasy w kodzie użytkownika.
    - Modelowanie domeny jest nieprecyzyjne: jeśli Shape powinien być
      tylko Circle, Rectangle lub Triangle, nic nie wymusza tej reguły.
- **Obejścia sprzed Javy 17** (wszystkie niezadowalające):
    - `final` — zapobiega WSZELKIEMU rozszerzaniu, zbyt restrykcyjne
    - Konstruktory pakietowo-prywatne — ograniczają rozszerzanie do tego
      samego pakietu, ale pozwalają na każdą klasę w tym pakiecie
    - Komentarze Javadoc jak "nie rozszerzaj" — niewymuszone
    - Enum — ograniczone do singletonów (brak danych per-instancja)
- **Klasy/interfejsy sealed** (JEP 409, Java 17) rozwiązują to:
    - Modyfikator `sealed` + klauzula `permits` deklaruje **dokładnie**
      które klasy/interfejsy mogą rozszerzać lub implementować dany typ.
    - Kompilator wymusza ograniczenie w czasie kompilacji.
    - Dopasowywanie wzorców może polegać na **zamkniętym zbiorze**
      podtypów, aby umożliwić wyczerpujące wyrażenia switch bez `default`.
- **Harmonogram**:
    - JEP 360: Podgląd w Javie 15
    - JEP 397: Drugi podgląd w Javie 16
    - JEP 409: Sfinalizowano w Javie 17
*/

// ============================================================
// Sekcja 2: Składnia i reguły
// ============================================================

/*
## Składnia i reguły

- Klasa lub interfejs sealed używa modyfikatora `sealed` i klauzuli
  `permits` do wylistowania dozwolonych bezpośrednich podtypów:
      sealed interface Shape permits Circle, Rectangle, Triangle
- **Reguła tego samego modułu**: Wszystkie dozwolone podtypy muszą
  być w tym samym module co typ sealed (lub w tym samym pakiecie
  jeśli w nienazwanym module). Jest to wymuszane przez kompilator.
- **Trzy wymagane modyfikatory** — każdy bezpośredni podtyp typu
  sealed musi być zadeklarowany jako jeden z:
    - `final` — nie może być dalej rozszerzany (hierarchia kończy się tutaj)
    - `sealed` — kontynuuje łańcuch sealed z własną klauzulą `permits`
    - `non-sealed` — ponownie otwiera hierarchię, każda klasa może rozszerzać
  Jest to obowiązkowe — kompilator odrzuci podtyp, który nie
  określa jednego z tych trzech modyfikatorów.
- **Kiedy używać każdego**:
    - `final` — najczęstszy; węzły liściowe hierarchii
    - `sealed` — gdy chcesz kolejny poziom kontrolowanych podtypów
    - `non-sealed` — gdy celowo pozwalasz na otwarte rozszerzanie
      w konkretnym punkcie (przydatne dla punktów wtyczek/rozszerzeń)
- **Record** jako dozwolone podtypy są niejawnie `final` (wszystkie
  record są final), więc doskonale działają jako węzły liściowe.
- **Enum** jako dozwolone podtypy są również niejawnie `final`.
- **API refleksji**:
    - `Class.isSealed()` — zwraca true jeśli klasa jest sealed
    - `Class.getPermittedSubclasses()` — zwraca dozwolone
      podtypy jako tablicę `ClassDesc`
*/

// ============================================================
// Sekcja 3: Interfejsy sealed
// ============================================================

/*
## Interfejsy sealed

- Interfejsy sealed działają dokładnie jak klasy sealed — możesz
  zadeklarować `sealed interface X permits A, B, C` i tylko A, B, C
  mogą implementować X.
- **Record implementujące interfejsy sealed**: Record są idealnym
  dopasowaniem do interfejsów sealed, ponieważ:
    - Record są niejawnie `final` (spełniając regułę modyfikatora)
    - Record są transparentnymi nośnikami danych — idealne do
      modelowania algebraicznych typów danych (typów sumy)
    - Połączenie interfejsów sealed + record daje **typy sumy**
      (sealed = "jedna z tych opcji") **typów iloczynu**
      (record = krotka nazwanych pól)
- **Enum implementujące interfejsy sealed**: Enum może implementować
  interfejs sealed. Ponieważ enum są niejawnie `final` i mają
  stały zbiór instancji, naturalnie pasują do koncepcji sealed.
- **Algebraiczne typy danych (ADT)**: W programowaniu funkcyjnym
  ADT są fundamentalnym narzędziem modelowania:
    - **Typ sumy** = "A lub B lub C" (interfejs sealed)
    - **Typ iloczynu** = "A i B i C" (record/klasa z polami)
    - Interfejsy sealed + record w Javie dają nam pełne wsparcie ADT.
  Ten wzorzec nazywa się "interfejs sealed + record" lub
  "unie dyskryminowane" w innych językach.
*/

// ============================================================
// Sekcja 4: Klasy sealed i dopasowywanie wzorców
// ============================================================

/*
## Klasy sealed i dopasowywanie wzorców

- Prawdziwa moc typów sealed ujawnia się przy **dopasowywaniu wzorców
  w wyrażeniach switch** (JEP 441, sfinalizowane w Javie 21).
- Ponieważ kompilator zna **kompletny zbiór** dozwolonych
  podtypów, może zweryfikować, że wyrażenie switch jest
  **wyczerpujące** — pokrywające wszystkie możliwe przypadki.
- **Nie potrzeba `default`**: Gdy wszystkie dozwolone podtypy są
  obsłużone, kompilator akceptuje switch bez gałęzi default. To
  jest istotna zaleta, ponieważ:
    - Dodanie nowego podtypu wyzwala błędy kompilacji w każdym
      switch, który go nie obsługuje — nie można zapomnieć o aktualizacji.
    - Z `default` nowe podtypy cicho przechodzą dalej.
- **Wzorce ze strażnikami `when`**: Możesz dodać warunki do
  wzorców: `case Circle c when c.radius() > 100 -> ...`
  To łączy sprawdzanie typu, dekonstrukcję i filtrowanie
  w jednym, czytelnym wyrażeniu.
- **Powiązanie z JEP 441**: Dopasowywanie wzorców dla switch było
  w podglądzie od Javy 17 i sfinalizowane w Javie 21.
  Klasy sealed zostały zaprojektowane z myślą o tej synergii.
*/

// ============================================================
// Sekcja 5: Klasy sealed z Record — Algebraiczne typy danych
// ============================================================

/*
## Klasy sealed z Record — Algebraiczne typy danych

- **Algebraiczne typy danych (ADT)** to połączenie:
    - **Typów sumy** (unie tagowane): "jeden z A, B lub C"
    - **Typów iloczynu** (krotki/record): "A zawiera x, y, z"
- W językach takich jak Haskell, Scala, Kotlin i Rust ADT są
  podstawową cechą:
    - Haskell: `data Expr = Num Double | Add Expr Expr | Mul Expr Expr`
    - Scala 3: `enum Expr { case Num(v: Double); case Add(l: Expr, r: Expr) }`
    - Kotlin: `sealed class Expr { data class Num(val v: Double) : Expr() }`
- Interfejsy sealed + record w Javie osiągają to samo:
    - `sealed interface Expression permits Num, Add, Mul, Neg, Var`
    - Każdy wariant jest record z własnymi polami
- **Drzewa wyrażeń / AST**: Klasyczny przypadek użycia ADT to
  modelowanie wyrażeń arytmetycznych jako struktury drzewiastej.
  Każdy węzeł jest wariantem (Num, Add, Mul, Neg, Var), a rekurencyjna
  ewaluacja lub transformacja odbywa się przez dopasowywanie wzorców
  na interfejsie sealed.
- **Korzyści w porównaniu ze wzorcem visitor**: Tradycyjna Java
  używałaby wzorca Visitor do operacji na hierarchiach typów. Typy
  sealed + dopasowywanie wzorców są bardziej zwięzłe, bardziej
  czytelne i nie wymagają szablonu metod accept/visit.
*/

// ============================================================
// Sekcja 6: Wzorce praktyczne i wytyczne projektowe
// ============================================================

/*
## Wzorce praktyczne i wytyczne projektowe

- **Sealed vs final vs otwarty**:
    - `final` — żadnego rozszerzania (pojedynczy konkretny typ)
    - `sealed` — kontrolowane rozszerzanie (stały zbiór podtypów)
    - otwarty (domyślnie) — każdy może rozszerzać (tradycyjna Java)
  Wybierz sealed gdy potrzebujesz **znanego, skończonego zbioru** wariantów.
- **Sealed vs enum**:
    - Enum — każdy wariant jest **singletonem** (brak danych per-instancja)
    - Sealed — każdy wariant może przenosić **różne dane** (pola)
    - Użyj enum dla prostych flag/kategorii, sealed dla bogatych
      typów domenowych, gdzie każdy wariant ma swój kształt.
- **Wytyczne modelowania domeny**:
    - Modeluj stany jako interfejsy sealed: PaymentState, OrderStatus
    - Modeluj wyniki jako interfejsy sealed: Result<T>, Validation<T>
    - Modeluj polecenia/zdarzenia jako interfejsy sealed w systemach CQRS/ES
    - Każdy wariant jako record = niemutowalny, transparentny, equals/hashCode za darmo
- **Punkty rozszerzeń `non-sealed`**: Używaj non-sealed oszczędnie,
  gdy jedna gałąź hierarchii powinna być otwarta (np. system
  wtyczek, gdzie strony trzecie dostarczają implementacje).
- **Maszyny stanów**: Interfejsy sealed modelują maszyny stanów
  skończonych w naturalny sposób — każdy stan jest wariantem,
  przejścia są metodami przyjmującymi jeden stan i zwracającymi
  inny, a kompilator zapewnia, że wszystkie stany są obsłużone.
*/

public class SealedClassesAndInterfaces {

    // ---- Sekcja 1: Hierarchia Shape ----

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double a, double b, double c) implements Shape {}

    // ---- Sekcja 2: Hierarchia Vehicle (trzy typy modyfikatorów) ----

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

    // ---- Sekcja 3: Interfejsy sealed z generykami, record, enum ----

    sealed interface Result<T> permits Success, Failure {}
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {}

    sealed interface Loggable permits LogLevel {}
    enum LogLevel implements Loggable { DEBUG, INFO, WARN, ERROR }

    // ---- Sekcja 5: Drzewo wyrażeń (ADT) ----

    sealed interface Expression permits Num, Add, Mul, Neg, Var {}
    record Num(double value) implements Expression {}
    record Add(Expression left, Expression right) implements Expression {}
    record Mul(Expression left, Expression right) implements Expression {}
    record Neg(Expression expr) implements Expression {}
    record Var(String name) implements Expression {}

    // ---- Sekcja 6: Maszyna stanów płatności + Walidacja ----

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
    // Sekcja 1: Wprowadzenie do klas sealed
    // ============================================================

    static void introductionToSealedClasses() {
        System.out.println("=== Section 1: Introduction to Sealed Classes ===");

        // Tworzenie instancji Shape
        Shape circle = new Circle(5.0);
        Shape rectangle = new Rectangle(4.0, 6.0);
        Shape triangle = new Triangle(3.0, 4.0, 5.0);

        System.out.println("circle:    " + circle);
        System.out.println("rectangle: " + rectangle);
        System.out.println("triangle:  " + triangle);

        // Dopasowywanie wzorców instanceof — dozwolone podtypy są znane
        List<Shape> shapes = List.of(circle, rectangle, triangle);
        for (var shape : shapes) {
            if (shape instanceof Circle c) {
                System.out.println("Circle with radius " + c.radius()
                        + " → area = " + (Math.PI * c.radius() * c.radius()));
            } else if (shape instanceof Rectangle r) {
                System.out.println("Rectangle " + r.width() + "x" + r.height()
                        + " → area = " + (r.width() * r.height()));
            } else if (shape instanceof Triangle t) {
                // Wzór Herona
                double s = (t.a() + t.b() + t.c()) / 2;
                double area = Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
                System.out.println("Triangle (" + t.a() + ", " + t.b() + ", " + t.c()
                        + ") → area = " + area);
            }
            // Nie potrzeba else — kompilator wie, że to wszystkie opcje
        }

        // Interfejs sealed gwarantuje zamknięty zbiór podtypów
        System.out.println("\nShape is sealed: " + Shape.class.isSealed());
        System.out.println("Permitted subtypes of Shape:");
        for (var subclass : Shape.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
    }

    // ============================================================
    // Sekcja 2: Składnia i reguły
    // ============================================================

    static void syntaxAndRules() {
        System.out.println("\n=== Section 2: Syntax and Rules ===");

        // Hierarchia Vehicle demonstruje wszystkie trzy wymagane modyfikatory
        var car = new Car("Toyota", "Camry");
        var heavyTruck = new HeavyTruck();
        var lightTruck = new LightTruck();
        var motorcycle = new Motorcycle();

        System.out.println("car:        " + car);
        System.out.println("heavyTruck: " + heavyTruck);
        System.out.println("lightTruck: " + lightTruck);
        System.out.println("motorcycle: " + motorcycle);

        // Pokazanie trzech typów modyfikatorów
        System.out.println("\n--- Modifier types in the Vehicle hierarchy ---");
        System.out.println("Vehicle is sealed: " + Vehicle.class.isSealed());
        System.out.println("Car is final: " + java.lang.reflect.Modifier.isFinal(Car.class.getModifiers()));
        System.out.println("Truck is sealed: " + Truck.class.isSealed());
        System.out.println("Motorcycle is non-sealed (isSealed=false, isFinal=false): "
                + "isSealed=" + Motorcycle.class.isSealed()
                + ", isFinal=" + java.lang.reflect.Modifier.isFinal(Motorcycle.class.getModifiers()));

        // API refleksji: getPermittedSubclasses()
        System.out.println("\nPermitted subtypes of Vehicle:");
        for (var subclass : Vehicle.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
        System.out.println("Permitted subtypes of Truck:");
        for (var subclass : Truck.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }

        // non-sealed pozwala na dalsze rozszerzanie
        class SportBike extends Motorcycle {
            @Override public String toString() { return "SportBike (extends Motorcycle)"; }
        }
        var sportBike = new SportBike();
        System.out.println("\nnon-sealed allows extension: " + sportBike);
        System.out.println("SportBike is a Vehicle: " + (sportBike instanceof Vehicle));
    }

    // ============================================================
    // Sekcja 3: Interfejsy sealed
    // ============================================================

    static void sealedInterfaces() {
        System.out.println("\n=== Section 3: Sealed Interfaces ===");

        // Result<T> z Success/Failure — klasyczny typ sumy
        Result<String> success = new Success<>("Hello, World!");
        Result<String> failure = new Failure<>("Connection timeout");

        System.out.println("success: " + success);
        System.out.println("failure: " + failure);

        // Symulacja wywołań serwisowych zwracających Result<T>
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

        // Zliczanie sukcesów i niepowodzeń
        long successCount = results.stream().filter(r -> r instanceof Success).count();
        long failureCount = results.stream().filter(r -> r instanceof Failure).count();
        System.out.println("successes: " + successCount + ", failures: " + failureCount);

        // Enum implementujący interfejs sealed
        System.out.println("\n--- Enum implementing sealed interface ---");
        System.out.println("LogLevel values: " + Arrays.toString(LogLevel.values()));
        System.out.println("LogLevel.DEBUG is Loggable: " + (LogLevel.DEBUG instanceof Loggable));
        System.out.println("Loggable is sealed: " + Loggable.class.isSealed());

        // Użycie LogLevel jako Loggable
        Loggable loggable = LogLevel.WARN;
        if (loggable instanceof LogLevel level) {
            System.out.println("Log level: " + level + " (ordinal: " + level.ordinal() + ")");
        }
    }

    // ============================================================
    // Sekcja 4: Klasy sealed i dopasowywanie wzorców
    // ============================================================

    static double area(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> {
                double s = (t.a() + t.b() + t.c()) / 2;
                yield Math.sqrt(s * (s - t.a()) * (s - t.b()) * (s - t.c()));
            }
            // Nie potrzeba default — wszystkie dozwolone podtypy są pokryte!
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

        // Wyczerpujące wyrażenie switch z area()
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

        // formatResult() przez switch
        System.out.println("\n--- formatResult() via switch expression ---");
        List<Result<String>> results = List.of(
                new Success<>("data loaded"),
                new Failure<>("timeout"),
                new Success<>("processed")
        );
        for (var result : results) {
            System.out.println("  " + formatResult(result));
        }

        // Wzorce ze strażnikami `when`
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

        // Przetworzenie listy kształtów — łączne pole
        double totalArea = shapes.stream()
                .mapToDouble(SealedClassesAndInterfaces::area)
                .sum();
        System.out.printf("\nTotal area of all shapes: %.4f%n", totalArea);
    }

    // ============================================================
    // Sekcja 5: Klasy sealed z Record — Algebraiczne typy danych
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

        // Budowanie drzewa wyrażeń: (x + 2) * -(3 + x)
        Expression expr = new Mul(
                new Add(new Var("x"), new Num(2)),
                new Neg(new Add(new Num(3), new Var("x")))
        );

        System.out.println("Expression: " + prettyPrint(expr));

        // Ewaluacja z x = 5
        var env = Map.of("x", 5.0);
        double result = evaluate(expr, env);
        System.out.println("With x=5:   " + prettyPrint(expr) + " = " + result);
        System.out.println("Expected:   (5 + 2) * -(3 + 5) = 7 * -8 = -56.0");

        // Ewaluacja z różnymi wartościami
        System.out.println("\n--- Evaluating with different x values ---");
        for (int x = -3; x <= 3; x++) {
            var e = Map.of("x", (double) x);
            System.out.printf("  x=%2d → %s = %.1f%n", x, prettyPrint(expr), evaluate(expr, e));
        }

        // Więcej wyrażeń
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

        // Pokazanie że sealed + record dają nam prawdziwe ADT
        System.out.println("\n--- Expression hierarchy ---");
        System.out.println("Expression is sealed: " + Expression.class.isSealed());
        System.out.println("Permitted subtypes:");
        for (var subclass : Expression.class.getPermittedSubclasses()) {
            System.out.println("  - " + subclass.getSimpleName());
        }
    }

    // ============================================================
    // Sekcja 6: Wzorce praktyczne i wytyczne projektowe
    // ============================================================

    // Przejścia maszyny stanów płatności
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

    // Narzędzia walidacji
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

        // Maszyna stanów płatności — ścieżka pozytywna
        System.out.println("--- Payment state machine: happy path ---");
        PaymentState state = new Pending("ORD-001", 99.99);
        System.out.println("  " + describePaymentState(state));

        state = authorize((Pending) state, "AUTH-12345");
        System.out.println("  " + describePaymentState(state));

        state = capture((Authorized) state);
        System.out.println("  " + describePaymentState(state));

        // Maszyna stanów płatności — ścieżka odrzucenia
        System.out.println("\n--- Payment state machine: decline path ---");
        PaymentState state2 = new Pending("ORD-002", 5000.00);
        System.out.println("  " + describePaymentState(state2));

        state2 = decline((Pending) state2, "Insufficient funds");
        System.out.println("  " + describePaymentState(state2));

        // Maszyna stanów płatności — ścieżka zwrotu
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

        // Mapowanie walidacji
        System.out.println("\n--- Mapping validations ---");
        var valid = validateEmail("user@example.com");
        var mapped = map(valid, String::toUpperCase);
        System.out.println("  map(valid email, toUpperCase): " + mapped);

        var invalid = validateEmail("bad");
        var mappedInvalid = map(invalid, String::toUpperCase);
        System.out.println("  map(invalid email, toUpperCase): " + mappedInvalid);

        // Łączenie walidacji
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
    // Main — uruchomienie wszystkich sekcji
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
