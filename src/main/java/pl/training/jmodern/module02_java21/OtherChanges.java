package pl.training.jmodern.module02_java21;

import java.util.*;

// ============================================================
// Sekcja 1: Dopasowanie wzorców w switch -- Wzorce typów i null
// ============================================================

/*
## Dopasowanie wzorców w switch -- Wzorce typów i null

- JEP 441 sfinalizował dopasowanie wzorców w switch w Java 21,
  po rundach podglądu w Java 17 (JEP 406), 18, 19, 20.
- **Wzorce typów w switch**: Można teraz napisać
  case String s -> ... aby dopasować i powiązać w jednym kroku. Zastępuje
  to rozwlekłe łańcuchy instanceof + rzutowanie jednym wyrażeniem
  switch lub instrukcją.
- **Switch na Object**: Przed Java 21 switch obsługiwał tylko
  kilka typów (int, String, enum). Teraz można wykonać switch
  na dowolnym typie referencyjnym i dopasować za pomocą wzorców typów.
- **case null**: Przed Java 21 przekazanie null do switch
  zawsze rzucało NullPointerException. Teraz można jawnie
  obsłużyć null za pomocą case null -> ..., co zapobiega NPE.
  Można również połączyć null z default: case null, default ->
- **Wyczerpywalność z typami zapieczętowanymi**: Gdy wyrażenie switch
  pokrywa wszystkie dozwolone podtypy zapieczętowanego
  interfejsu, gałąź default nie jest wymagana. Kompilator
  weryfikuje wyczerpywalność w czasie kompilacji.
- **Różnica od podglądów w Java 17**: Składnia jest taka sama,
  ale Java 21 to pierwsza wersja, w której jest to stała,
  nie-podglądowa funkcjonalność. Kod, który jej używa, nie wymaga
  już --enable-preview.
*/

// ============================================================
// Sekcja 2: Dopasowanie wzorców w switch -- Wzorce warunkowane i dominacja
// ============================================================

/*
## Dopasowanie wzorców w switch -- Wzorce warunkowane i dominacja

- **Wzorce warunkowane** używają słowa kluczowego when do dodania
  warunku logicznego do etykiety case:
      case String s when s.length() > 5 -> ...
  Wzorzec pasuje tylko jeśli typ pasuje ORAZ warunek
  ewaluuje się do true. Zastępuje to zagnieżdżone if-else wewnątrz
  ciał case.
- **Reguły dominacji**: Bardziej szczegółowy wzorzec musi pojawić się
  przed bardziej ogólnym. Na przykład case String s musi
  być przed case Object o, a warunkowany case musi być
  przed swoim bezwarunkowym odpowiednikiem. Naruszenie dominacji jest
  błędem kompilacji -- w przeciwieństwie do łańcuchów if-else, gdzie błędy
  kolejności są ciche.
- **Łączenie typu + warunku** umożliwia bogatą logikę rozdzielania,
  która wcześniej była możliwa tylko z długimi łańcuchami if-else lub
  wzorcem wizytatora.
- **Typy zapieczętowane + warunki**: Można łączyć wyczerpywalność
  typów zapieczętowanych z warunkami when, tworząc potężne, a zarazem
  bezpieczne typowo rozdzielanie. Kompilator nadal sprawdza, czy
  wszystkie dozwolone podtypy są pokryte.
*/

// ============================================================
// Sekcja 3: Wzorce rekordów -- Destrukturyzacja
// ============================================================

/*
## Wzorce rekordów -- Destrukturyzacja

- JEP 440 sfinalizował wzorce rekordów w Java 21 (podgląd
  w Java 19 i 20).
- **Składnia**: case Point(var x, var y) -> ... destrukturyzuje
  rekord bezpośrednio w etykiecie case, wiążąc jego składowe
  ze zmiennymi lokalnymi.
- Można użyć jawnych typów (case Point(double x, double y))
  lub var do inferencji typów.
- Wzorce rekordów działają zarówno w switch, jak i instanceof:
      if (obj instanceof Point(var x, var y)) { ... }
- **Dekompozycja strukturalna vs wywołania akcesorów**: Wzorce
  rekordów dekomponują strukturę w jednym kroku, zamiast
  wywoływać p.x() i p.y() osobno. Jest to szczególnie
  potężne przy zagnieżdżeniu (patrz Sekcja 4).
- **Tylko rekordy**: Wzorce rekordów działają tylko z typami
  rekordów. Zwykłe klasy nie mogą być dekonstruowane w ten sposób
  (chyba że przyszłe JEP-y dodadzą wzorce dekonstrukcji dla klas).
- **Różnica od zapowiedzi w module Java 17**: Records.java
  Sekcja 5 krótko pokazała tę ideę. Tutaj zagłębiamy się w
  wiele składowych, kombinacje z warunkami when i
  praktyczne przykłady klasyfikacji.
*/

// ============================================================
// Sekcja 4: Wzorce rekordów -- Zagnieżdżone i złożone wzorce
// ============================================================

/*
## Wzorce rekordów -- Zagnieżdżone i złożone wzorce

- Wzorce rekordów mogą być **zagnieżdżone**: jeśli składowa rekordu
  jest sama w sobie rekordem, można dekonstruować oba poziomy naraz:
      case ColoredPoint(Point(var x, var y), var color) -> ...
  Eliminuje to zmienne pośrednie i wyraża intencję
  bardziej bezpośrednio.
- **Rekurencyjne dopasowanie wzorców**: Hierarchie zapieczętowanych
  interfejsów (jak drzewa wyrażeń) są naturalnym zastosowaniem.
  Można ewaluować drzewo wyrażeń jednym switch używając zagnieżdżonych
  wzorców rekordów -- bez potrzeby wzorca wizytatora.
- **Łączenie zagnieżdżonych wzorców z warunkami when** pozwala
  zwięźle wyrażać złożone warunki, np. dopasowanie pary,
  w której pierwszy element spełnia pewien warunek.
- **Praktyczne przetwarzanie ADT (algebraiczny typ danych)**: Zapieczętowane
  interfejsy + rekordy + dopasowanie wzorców razem dają Javie
  formę algebraicznych typów danych podobną do case classes w Scali
  lub enumów z match w Rust.
*/

// ============================================================
// Sekcja 5: Kolekcje sekwencyjne -- SequencedCollection i SequencedSet
// ============================================================

/*
## Kolekcje sekwencyjne -- SequencedCollection i SequencedSet

- JEP 431 (Java 21) wprowadził trzy nowe interfejsy:
  SequencedCollection, SequencedSet i SequencedMap.
- **Problem**: Przed Java 21 nie było jednolitego API do
  dostępu do pierwszego i ostatniego elementu uporządkowanych kolekcji.
  Każda kolekcja miała swój sposób:
    - List: get(0) / get(size()-1)
    - Deque: getFirst() / getLast()
    - SortedSet: first() / last()
    - LinkedHashSet: iterator().next() / brak łatwego dostępu do ostatniego
  Ta niespójność utrudniała generyczne programowanie z uporządkowanymi
  kolekcjami.
- **SequencedCollection rozszerza Collection** i dodaje:
    - addFirst(E) / addLast(E)
    - getFirst() / getLast()
    - removeFirst() / removeLast()
    - reversed() -- zwraca widok w odwróconej kolejności
- **Retrofitowane** do istniejących klas: ArrayList, LinkedList,
  ArrayDeque, LinkedHashSet, TreeSet i inne. Klasy te
  teraz implementują SequencedCollection (lub SequencedSet).
- **reversed() zwraca widok**, nie kopię. Modyfikacje
  przez odwrócony widok są odzwierciedlane w oryginalnej
  kolekcji i odwrotnie.
- **SequencedSet rozszerza SequencedCollection i Set** -- nie
  dodaje nowych metod, ale zawęża reversed() do zwracania
  SequencedSet.
*/

// ============================================================
// Sekcja 6: Kolekcje sekwencyjne -- SequencedMap
// ============================================================

/*
## Kolekcje sekwencyjne -- SequencedMap

- **SequencedMap rozszerza Map** i dodaje operacje uwzględniające
  kolejność napotkania:
    - firstEntry() / lastEntry() -- zwracają Map.Entry lub null
    - putFirst(K, V) / putLast(K, V) -- wstawiają lub przesuwają wpis
      na pierwszą/ostatnią pozycję
    - pollFirstEntry() / pollLastEntry() -- usuwają i zwracają
    - sequencedKeySet() -- zwraca SequencedSet kluczy
    - sequencedValues() -- zwraca SequencedCollection wartości
    - sequencedEntrySet() -- zwraca SequencedSet wpisów
    - reversed() -- zwraca odwrócony widok SequencedMap
- **Retrofitowane** do LinkedHashMap, TreeMap i
  ConcurrentSkipListMap.
- **LinkedHashMap** ma teraz przewidywalny dostęp do pierwszego/ostatniego elementu.
  Przed Java 21 uzyskanie pierwszego lub ostatniego wpisu z
  LinkedHashMap wymagało iteracji (brak bezpośredniego API).
- **putFirst / putLast** mogą zmieniać kolejność istniejących wpisów w
  LinkedHashMap. Jeśli klucz już istnieje, putFirst przesuwa
  go na pierwszą pozycję (i aktualizuje wartość).
- **reversed()** na SequencedMap zwraca widok, w którym
  iteracja, firstEntry/lastEntry i operacje Stream
  odzwierciedlają odwróconą kolejność.
*/

// ============================================================
// Sekcja 7: Nienazwane zmienne i wzorce (JEP 456, Java 22)
// ============================================================

/*
## Nienazwane zmienne i wzorce

- JEP 456 sfinalizował nienazwane zmienne i wzorce w Java 22
  (podgląd w Java 21 przez JEP 443).
- **Problem**: Przed Java 22 nieużywane zmienne wymagały fikcyjnych
  nazw jak _unused, ignored lub tmp. Zaciemniało to
  intencję programisty i wywoływało ostrzeżenia IDE.
- **Podkreślnik `_`** jest teraz zarezerwowanym słowem kluczowym, które sygnalizuje
  "ta wartość jest celowo nieużywana." Był legalnym identyfikatorem
  w Java 8, przestarzałym w Java 9 i w pełni zarezerwowanym w Java 22.
- **7 obsługiwanych kontekstów**:
  1. Deklaracje zmiennych lokalnych: var _ = someMethod();
  2. Rozszerzone pętle for: for (var _ : collection)
  3. Try-with-resources: try (var _ = acquireResource())
  4. Bloki catch: catch (SomeException _)
  5. Parametry lambda: (_, value) -> process(value)
  6. Zmienne wzorcowe: case Integer _ -> "int"
  7. Składowe wzorców rekordów: case Point(var x, _) -> x
- **Wiele `_` może współistnieć** w tym samym zakresie, w przeciwieństwie do
  nazwanych zmiennych. Jest to szczególnie przydatne w zagnieżdżonych pętlach
  lub wzorcach, gdzie kilka wartości jest nieużywanych.
- **Naturalnie łączy się ze wzorcami rekordów** z Sekcji 3-4:
  przy destrukturyzacji rekordów można ignorować składowe, których
  nie potrzebujesz, bez wymyślania jednorazowych nazw.
*/

public class OtherChanges {

    // ---- Typy wewnętrzne dla demo dopasowania wzorców ----

    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double a, double b, double c) implements Shape {}

    // Drzewo wyrażeń dla zagnieżdżonych wzorców rekordów
    sealed interface Expression permits Num, Add, Mul, Neg {}
    record Num(double value) implements Expression {}
    record Add(Expression left, Expression right) implements Expression {}
    record Mul(Expression left, Expression right) implements Expression {}
    record Neg(Expression expr) implements Expression {}

    // Dla demo wzorców warunkowanych
    record Person(String name, int age) {}

    // Dla demo zagnieżdżonych wzorców rekordów
    record Point(double x, double y) {}
    record ColoredPoint(Point point, String color) {}
    record Pair<A, B>(A first, B second) {}

    // ============================================================
    // Sekcja 1: Dopasowanie wzorców w switch -- Wzorce typów i null
    // ============================================================

    static void patternMatchingTypePatterns() {
        System.out.println("=== Section 1: Pattern Matching for switch -- Type Patterns and Null ===");

        // ---- Switch na Object ze wzorcami typów ----
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

        // ---- case null zapobiega NPE ----
        System.out.println("\n--- case null prevents NPE ---");
        String input = null;
        String result = switch (input) {
            case null -> "handled null safely";
            case String s when s.isBlank() -> "blank string";
            case String s -> "non-blank: " + s;
        };
        System.out.println("  null input -> " + result);

        // ---- Wyczerpujący switch na typie zapieczętowanym (brak potrzeby default) ----
        System.out.println("\n--- Exhaustive switch on sealed Shape (no default) ---");
        Shape[] shapes = {new Circle(5), new Rectangle(3, 4), new Triangle(3, 4, 5)};

        for (Shape shape : shapes) {
            String info = switch (shape) {
                case Circle c -> "Circle with radius " + c.radius();
                case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
                case Triangle t -> "Triangle with sides " + t.a() + ", " + t.b() + ", " + t.c();
                // Brak potrzeby default -- kompilator zna wszystkie podtypy Shape
            };
            System.out.println("  " + info);
        }
    }

    // ============================================================
    // Sekcja 2: Dopasowanie wzorców w switch -- Wzorce warunkowane i dominacja
    // ============================================================

    static void guardedPatternsAndDominance() {
        System.out.println("\n=== Section 2: Pattern Matching for switch -- Guarded Patterns and Dominance ===");

        // ---- Klasyfikacja wieku osoby z warunkami when ----
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

        // ---- Filtrowanie powierzchni kształtów z warunkami when ----
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

        // ---- Reguły dominacji (wymuszane w czasie kompilacji) ----
        System.out.println("\n--- Dominance rules ---");
        System.out.println("  Dominance = specific patterns must come before general ones.");
        System.out.println("  Example: case String s MUST come before case Object o");
        System.out.println("  Example: case Circle c when c.radius() > 5 MUST come before case Circle c");
        System.out.println("  Violating dominance is a COMPILE ERROR (not a silent bug).");

        // Odkomentowanie poniższego spowodowałoby błąd kompilacji:
        // String test = switch ((Object) "hello") {
        //     case Object o -> "object";       // BŁĄD: dominuje nad poniższym case String
        //     case String s -> "string";        // nieosiągalny
        // };

        // ---- Rozdzielanie typów zapieczętowanych z warunkami ----
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
    // Sekcja 3: Wzorce rekordów -- Destrukturyzacja
    // ============================================================

    static void recordPatternDeconstruction() {
        System.out.println("\n=== Section 3: Record Patterns -- Deconstruction ===");

        // ---- Klasyfikacja ćwiartki punktu przez destrukturyzację ----
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

        // ---- Sprawdzenie wieku osoby przez destrukturyzację ----
        System.out.println("\n--- Person deconstruction in switch ---");
        var person = new Person("Alice", 30);
        String info = switch (person) {
            case Person(var name, var age) when age >= 18 -> name + " is an adult (age " + age + ")";
            case Person(var name, var age) -> name + " is a minor (age " + age + ")";
        };
        System.out.println("  " + info);

        // ---- instanceof ze wzorcem rekordu ----
        System.out.println("\n--- instanceof with record pattern ---");
        Object obj = new Point(10, 20);
        if (obj instanceof Point(var x, var y)) {
            System.out.println("  Deconstructed via instanceof: x=" + x + ", y=" + y);
            System.out.println("  Distance from origin: " + Math.sqrt(x * x + y * y));
        }

        // ---- Porównanie: destrukturyzacja vs styl akcesorów ----
        System.out.println("\n--- Deconstruction vs accessor style ---");
        var rect = new Rectangle(5, 10);

        // Styl akcesorów (tradycyjny)
        double area1 = rect.width() * rect.height();

        // Styl destrukturyzacji (Java 21)
        double area2 = switch (rect) {
            case Rectangle(var w, var h) -> w * h;
        };

        System.out.println("  Accessor style area: " + area1);
        System.out.println("  Deconstruction style area: " + area2);
        System.out.println("  Both equivalent, but deconstruction shines with nested patterns");
    }

    // ============================================================
    // Sekcja 4: Wzorce rekordów -- Zagnieżdżone i złożone wzorce
    // ============================================================

    static void nestedRecordPatterns() {
        System.out.println("\n=== Section 4: Record Patterns -- Nested and Complex Patterns ===");

        // ---- Zagnieżdżona destrukturyzacja ColoredPoint ----
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

        // ---- Ewaluacja drzewa wyrażeń za pomocą switch ze wzorcami rekordów ----
        System.out.println("\n--- Expression tree evaluation ---");

        // Buduj: (2 + 3) * 4
        Expression expr1 = new Mul(new Add(new Num(2), new Num(3)), new Num(4));
        System.out.println("  (2 + 3) * 4 = " + evaluate(expr1));

        // Buduj: -(5 + 3)
        Expression expr2 = new Neg(new Add(new Num(5), new Num(3)));
        System.out.println("  -(5 + 3) = " + evaluate(expr2));

        // Buduj: (10 * 2) + (-(3))
        Expression expr3 = new Add(new Mul(new Num(10), new Num(2)), new Neg(new Num(3)));
        System.out.println("  (10 * 2) + (-(3)) = " + evaluate(expr3));

        // ---- Ładne formatowanie wyrażeń ----
        System.out.println("\n--- Pretty-print expressions ---");
        System.out.println("  " + prettyPrint(expr1));
        System.out.println("  " + prettyPrint(expr2));
        System.out.println("  " + prettyPrint(expr3));

        // ---- Destrukturyzacja Pair ----
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
    // Sekcja 5: Kolekcje sekwencyjne -- SequencedCollection i SequencedSet
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

        // ---- LinkedHashSet: dostęp sekwencyjny z zachowaniem kolejności wstawiania ----
        System.out.println("\n--- LinkedHashSet: sequenced access ---");
        var linkedSet = new LinkedHashSet<>(List.of("apple", "banana", "cherry", "date"));
        System.out.println("  Original:   " + linkedSet);
        System.out.println("  getFirst(): " + linkedSet.getFirst());
        System.out.println("  getLast():  " + linkedSet.getLast());

        var reversedSet = linkedSet.reversed();
        System.out.println("  reversed(): " + reversedSet);

        // ---- TreeSet: posortowany + sekwencyjny ----
        System.out.println("\n--- TreeSet: sorted + sequenced ---");
        var treeSet = new TreeSet<>(List.of(50, 10, 30, 20, 40));
        System.out.println("  TreeSet:    " + treeSet);
        System.out.println("  getFirst(): " + treeSet.getFirst());
        System.out.println("  getLast():  " + treeSet.getLast());
        System.out.println("  reversed(): " + treeSet.reversed());

        // ---- reversed() to widok, nie kopia ----
        System.out.println("\n--- reversed() is a view (mutations reflect) ---");
        var original = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        var view = original.reversed();
        System.out.println("  Original: " + original);
        System.out.println("  View:     " + view);

        original.addLast(6);
        System.out.println("  After original.addLast(6):");
        System.out.println("    Original: " + original);
        System.out.println("    View:     " + view);

        view.addFirst(7);  // addFirst na odwróconym = addLast na oryginale
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
    // Sekcja 6: Kolekcje sekwencyjne -- SequencedMap
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

        // ---- putFirst zmienia kolejność istniejących wpisów ----
        System.out.println("\n--- putFirst reorders entries ---");
        System.out.println("  Before putFirst(\"gamma\", 30): " + map);
        map.putFirst("gamma", 30);
        System.out.println("  After putFirst(\"gamma\", 30):  " + map);
        System.out.println("  gamma moved to first position and value updated");

        map.putLast("alpha", 100);
        System.out.println("  After putLast(\"alpha\", 100):  " + map);

        // ---- TreeMap: posortowany + dostęp sekwencyjny ----
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

        // ---- Iteracja po odwróconej mapie ----
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
    // Sekcja 7: Nienazwane zmienne i wzorce (JEP 456, Java 22)
    // ============================================================

    static void unnamedVariables() {
        System.out.println("\n=== Section 7: Unnamed Variables and Patterns (JEP 456, Java 22) ===");

        // ---- Rozszerzona pętla for: zliczanie bez użycia zmiennej pętli ----
        System.out.println("--- Enhanced for: count without using loop variable ---");
        var items = List.of("apple", "banana", "cherry", "date", "elderberry");
        int count = 0;
        for (var _ : items) {
            count++;
        }
        System.out.println("  Counted " + count + " items (loop variable unnamed with _)");

        // ---- Blok catch z nienazwaną zmienną wyjątku ----
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

        // ---- Parametry lambda z _ ----
        System.out.println("\n--- Lambda: unnamed parameters ---");
        var prices = new LinkedHashMap<String, Double>();
        prices.put("Coffee", 4.50);
        prices.put("Tea", 3.00);
        prices.put("Juice", 5.25);
        System.out.println("  Values only (key ignored via _):");
        prices.forEach((_, value) -> System.out.println("    $" + value));

        // ---- Dopasowanie wzorców w switch z nienazwanymi zmiennymi wzorcowymi ----
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

        // ---- Wzorce rekordów z nienazwanymi składowymi ----
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

        // ---- Wiele _ w tym samym zakresie ----
        System.out.println("\n--- Multiple _ in same scope ---");
        var pairs = List.of(
                new Pair<>("Alice", 30),
                new Pair<>("Bob", 25),
                new Pair<>("Charlie", 35)
        );
        int pairCount = 0;
        for (var _ : pairs) {
            for (var _ : items) {  // dwie nienazwane zmienne pętli w zagnieżdżonych zakresach
                pairCount++;
            }
        }
        System.out.println("  Nested loops with two unnamed variables: " + pairCount + " iterations");
    }

    // ============================================================
    // Main -- uruchomienie wszystkich sekcji
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
