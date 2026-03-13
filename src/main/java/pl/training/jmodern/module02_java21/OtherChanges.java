package pl.training.jmodern.module02_java21;

import java.util.*;

// ============================================================
// Sekcja 1: Kolekcje sekwencyjne -- SequencedCollection i SequencedSet
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
// Sekcja 2: Kolekcje sekwencyjne -- SequencedMap
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
// Sekcja 3: Nienazwane zmienne i wzorce (JEP 456, Java 22)
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
- **Naturalnie łączy się ze wzorcami rekordów**: przy destrukturyzacji
  rekordów można ignorować składowe, których nie potrzebujesz,
  bez wymyślania jednorazowych nazw.
*/

public class OtherChanges {

    // ---- Typy wewnętrzne dla demo nienazwanych zmiennych ----

    record Point(double x, double y) {}
    record ColoredPoint(Point point, String color) {}
    record Pair<A, B>(A first, B second) {}

    // ============================================================
    // Sekcja 1: Kolekcje sekwencyjne -- SequencedCollection i SequencedSet
    // ============================================================

    static void sequencedCollections() {
        System.out.println("=== Sekcja 1: Kolekcje sekwencyjne -- SequencedCollection i SequencedSet ===");

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
    // Sekcja 2: Kolekcje sekwencyjne -- SequencedMap
    // ============================================================

    static void sequencedMaps() {
        System.out.println("\n=== Sekcja 2: Kolekcje sekwencyjne -- SequencedMap ===");

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
    // Sekcja 3: Nienazwane zmienne i wzorce (JEP 456, Java 22)
    // ============================================================

    static void unnamedVariables() {
        System.out.println("\n=== Sekcja 3: Nienazwane zmienne i wzorce (JEP 456, Java 22) ===");

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
    // Main
    // ============================================================

    public static void main(String[] args) {
        sequencedCollections();
        sequencedMaps();
        unnamedVariables();
    }
}
