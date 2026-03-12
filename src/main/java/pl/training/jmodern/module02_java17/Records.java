package pl.training.jmodern.module02_java17;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do Record
// ============================================================

/*
## Wprowadzenie do Record

- W tradycyjnej Javie tworzenie prostej klasy przenoszenia danych wymaga
  wielu **szablonowego kodu**: konstruktor, gettery, equals(), hashCode(),
  toString(). Prosta klasa z 3 polami może łatwo mieć 50+ linii.
- **Inne języki rozwiązały to dawno temu**:
    - Kotlin: `data class Point(val x: Double, val y: Double)`
    - Scala: `case class Point(x: Double, y: Double)`
    - C#: `record Point(double X, double Y);`
- **Harmonogram JEP**:
    - JEP 359: Podgląd w Javie 14
    - JEP 384: Drugi podgląd w Javie 15
    - JEP 395: Sfinalizowano w Javie 16 (omawiane z Java 17 LTS)
- **Podstawowa składnia**: `record Point(double x, double y) {}`
    - Lista w nawiasach nazywana jest **nagłówkiem record** lub
      **listą komponentów**. Każdy wpis to **komponent record**.
    - Kompilator automatycznie generuje:
        - Pole `private final` dla każdego komponentu
        - **Konstruktor kanoniczny** przypisujący wszystkie pola
        - **Metody akcesorowe** nazwane od komponentów (`x()`, `y()`)
          — NIE `getX()` / `getY()`
        - `equals()` oparty na wszystkich komponentach
        - `hashCode()` oparty na wszystkich komponentach
        - `toString()` w formacie `Point[x=1.0, y=2.0]`
- Record są **niejawnie `final`** — nie można ich rozszerzać.
- Record **niejawnie rozszerzają `java.lang.Record`** — nie mogą
  rozszerzać żadnej innej klasy (ale mogą implementować interfejsy).
- Record są **transparentnymi nośnikami danych**: ich API jest w pełni
  determinowane przez opis stanu (listę komponentów).
*/

// ============================================================
// Sekcja 2: Anatomia Record
// ============================================================

/*
## Anatomia Record

- **Komponenty record** stają się polami `private final`. Nie można
  dodawać dodatkowych pól instancji do record — jest to wymuszane
  przez kompilator.
- **Konstruktor kanoniczny**: Kompilator generuje konstruktor
  przyjmujący wszystkie komponenty w kolejności:
      new RGB(255, 128, 0)
- **Konstruktor kompaktowy**: Skrócona składnia, w której pomijasz
  listę parametrów i końcowe przypisania:
      record RGB(int red, int green, int blue) {
          RGB { // ← konstruktor kompaktowy (bez nawiasów)
              if (red < 0 || red > 255) throw new IllegalArgumentException();
              // przypisania następują automatycznie na końcu
          }
      }
- **Metody akcesorowe** są nazwane od komponentów:
    - `rgb.red()` — NIE `rgb.getRed()`
    - To podąża za wzorcem ustanowionym przez przemianowanie
      `Map.Entry.getKey()` na `key()` w nowszych API
- **Automatycznie generowane equals/hashCode**: Dwa record są równe
  wtedy i tylko wtedy, gdy wszystkie ich komponenty są równe. Używa
  `Objects.equals()` dla typów referencyjnych i `==` dla prymitywów.
- **Automatycznie generowany toString()**: Produkuje ciąg w formacie
  `NazwaTypu[komp1=wartość1, komp2=wartość2, ...]`
- **Możesz nadpisać** każdą automatycznie generowaną metodę (toString,
  equals, hashCode, akcesory) — kompilator generuje tylko to, czego
  sam nie dostarczysz.
*/

// ============================================================
// Sekcja 3: Niestandardowe konstruktory i walidacja
// ============================================================

/*
## Niestandardowe konstruktory i walidacja

- **Konstruktor kompaktowy** jest najczęstszym sposobem dodawania
  walidacji lub normalizacji do record. Wykonuje się przed
  niejawnymi przypisaniami pól:
      record Temperature(double value, String unit) {
          Temperature {   // konstruktor kompaktowy
              unit = unit.trim().toUpperCase();
              if (!Set.of("C", "F", "K").contains(unit))
                  throw new IllegalArgumentException("Unknown unit: " + unit);
          }
      }
- **Niestandardowy konstruktor kanoniczny**: Możesz również napisać
  pełny konstruktor z parametrami, ale wtedy musisz przypisać
  WSZYSTKIE pola:
      record Temperature(double value, String unit) {
          Temperature(double value, String unit) {
              this.value = value;
              this.unit = unit.trim().toUpperCase();
          }
      }
- **Dodatkowe konstruktory**: Możesz definiować przeciążone
  konstruktory, ale muszą delegować do kanonicznego przez `this(...)`:
      Temperature(double value) { this(value, "C"); }
- **Kopie obronne**: Dla mutowalnych komponentów (List, Date, tablice)
  powinieneś tworzyć kopie obronne w konstruktorze kompaktowym:
      record Tags(String name, List<String> values) {
          Tags { values = List.copyOf(values); }
      }
  To zapewnia, że record pozostaje efektywnie niemutowalny nawet
  jeśli wywołujący zmodyfikuje oryginalną listę po konstrukcji.
*/

// ============================================================
// Sekcja 4: Record i interfejsy — Generyczne Record
// ============================================================

/*
## Record i interfejsy — Generyczne Record

- Record **mogą implementować interfejsy** — to jest ich główny
  mechanizm rozszerzania, ponieważ nie mogą rozszerzać innych klas.
- Umożliwia to **polimorfizm z transparentnością danych**: uzyskujesz
  korzyści kontraktu interfejsu (Comparable, Serializable,
  niestandardowe interfejsy) z automatycznym zachowaniem klasy danych.
- **Generyczne record**: Record mogą mieć parametry typów tak jak
  klasy: `record Pair<A, B>(A first, B second) {}`
    - Generyczne record są przydatne dla DTO, obiektów wartości,
      krotek i wrapperów wyników.
- Record **mogą mieć**:
    - Statyczne pola i metody
    - Metody instancji (poza akcesorami)
    - Zagnieżdżone typy (klasy, interfejsy, enum, record)
- Record **nie mogą mieć**:
    - Dodatkowych pól instancji (tylko pola komponentów)
    - Nadklasy innej niż `java.lang.Record`
- **Record jako klucze Map**: Ponieważ record mają równość strukturalną
  (equals/hashCode oparty na wszystkich komponentach), doskonale
  nadają się jako klucze Map — dwa record z tymi samymi komponentami
  zawsze trafią do tego samego kubełka i porównają się jako równe.
*/

// ============================================================
// Sekcja 5: Record z dopasowywaniem wzorców
// ============================================================

/*
## Record z dopasowywaniem wzorców

- **Dopasowywanie wzorców `instanceof`** (JEP 394, Java 16):
      if (obj instanceof Point p) { ... p.x() ... }
  Record działają naturalnie z instanceof — nie ma potrzeby rzutowania.
- **Dopasowywanie wzorców w switch** (JEP 441, Java 21):
      switch (shape) { case Point p -> ...; case Line l -> ...; }
  Kompilator może sprawdzić wyczerpywalność z typami sealed.
- **Wzorce dekonstrukcji record** (JEP 440, Java 21):
      case Point(var x, var y) -> ...
  Zamiast wiązać cały record i wywoływać akcesory, możesz
  zdekonstruować go bezpośrednio we wzorcu — wyodrębniając
  komponenty do zmiennych.
- **Zagnieżdżone wzorce record**: Dekonstrukcja może być zagnieżdżona:
      case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...
  To dekonstruuje Line na dwa Point, a każdy Point
  na jego współrzędne — wszystko w jednym wzorcu.
- **Wzorce ze strażnikami `when`**:
      case Point(var x, var y) when x > 0 && y > 0 -> "Q1"
  Łączy dekonstrukcję z logicznym strażnikiem.
- **Record + sealed + dopasowywanie wzorców** = **trójca ADT** w
  nowoczesnej Javie. Interfejsy sealed definiują typ sumy, record
  definiują typy iloczynu, a dopasowywanie wzorców zapewnia
  wyczerpującą, bezpieczną typowo dekompozycję.
*/

// ============================================================
// Sekcja 6: Wzorce praktyczne i ograniczenia
// ============================================================

/*
## Wzorce praktyczne i ograniczenia

- **Kiedy używać record vs klas**:
    - Użyj record dla **nośników danych**: DTO, obiekty wartości,
      krotki, odpowiedzi API, konfiguracja, zdarzenia, polecenia.
    - Użyj klas gdy potrzebujesz: mutowalnego stanu, dziedziczenia
      lub dodatkowych pól instancji poza komponentami record.
- **Ograniczenia record**:
    - Nie mogą rozszerzać klasy (już rozszerzają `java.lang.Record`)
    - Nie mogą mieć mutowalnych pól (wszystkie komponenty są `final`)
    - Nie mogą deklarować dodatkowych pól instancji
    - Są niejawnie `final` (nie można tworzyć podklas)
    - Nie mogą być `abstract`
- **Lokalne record**: Record mogą być deklarowane wewnątrz metod, co
  jest szczególnie przydatne dla typów pośrednich w potokach Stream:
      record NameAge(String name, int age) {}
      list.stream().map(p -> new NameAge(p.name(), p.age()))...
- **Serializacja**: Record używają konstruktora kanonicznego do
  deserializacji (nie wstrzykiwania pól opartego na refleksji jak
  zwykłe klasy). To czyni serializację record bardziej przewidywalną
  i bezpieczną — ta sama walidacja, która działa podczas konstrukcji,
  działa również podczas deserializacji.
- **Wzorzec Builder**: Dla record z wieloma komponentami builder
  zapewnia bardziej czytelne API konstrukcji z nazwanymi setterami
  i opcjonalnymi wartościami domyślnymi.
*/

public class Records {

    // ---- Sekcja 1: Podstawowe record ----

    record Point(double x, double y) {}
    record FullName(String first, String last) {}

    // ---- Sekcja 2: Anatomia ----

    record RGB(int red, int green, int blue) {}

    // ---- Sekcja 3: Konstruktory i walidacja ----

    record Temperature(double value, String unit) {
        Temperature {
            unit = unit.trim().toUpperCase();
            if (!Set.of("C", "F", "K").contains(unit)) {
                throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        Temperature(double value) {
            this(value, "C");
        }

        double toCelsius() {
            return switch (unit) {
                case "C" -> value;
                case "F" -> (value - 32) * 5.0 / 9.0;
                case "K" -> value - 273.15;
                default -> throw new IllegalStateException("Unknown unit: " + unit);
            };
        }
    }

    record Tags(String name, List<String> values) {
        Tags {
            values = List.copyOf(values);
        }
    }

    record Range(int from, int to) {
        Range {
            if (from > to) {
                throw new IllegalArgumentException("from (" + from + ") must be <= to (" + to + ")");
            }
        }

        int length() {
            return to - from;
        }

        boolean contains(int value) {
            return value >= from && value <= to;
        }
    }

    // ---- Sekcja 4: Interfejsy i generyki ----

    interface Printable {
        String prettyPrint();
    }

    record Money(double amount, String currency) implements Printable, Comparable<Money> {
        @Override
        public String prettyPrint() {
            return String.format("%.2f %s", amount, currency);
        }

        @Override
        public int compareTo(Money other) {
            if (!this.currency.equals(other.currency)) {
                throw new IllegalArgumentException("Cannot compare different currencies: " + currency + " vs " + other.currency);
            }
            return Double.compare(this.amount, other.amount);
        }
    }

    record Pair<A, B>(A first, B second) {
        <C> Pair<C, B> mapFirst(Function<A, C> fn) {
            return new Pair<>(fn.apply(first), second);
        }

        <C> Pair<A, C> mapSecond(Function<B, C> fn) {
            return new Pair<>(first, fn.apply(second));
        }
    }

    // ---- Sekcja 5: Dopasowywanie wzorców z record ----

    record Line(Point start, Point end) {}
    record ColoredPoint(Point point, String color) {}

    // ---- Sekcja 6: Wzorzec Builder dla record ----

    record PersonRecord(String name, int age, String email, String phone, String city) {
        static class Builder {
            private String name;
            private int age;
            private String email = "";
            private String phone = "";
            private String city = "";

            Builder name(String name) { this.name = name; return this; }
            Builder age(int age) { this.age = age; return this; }
            Builder email(String email) { this.email = email; return this; }
            Builder phone(String phone) { this.phone = phone; return this; }
            Builder city(String city) { this.city = city; return this; }

            PersonRecord build() {
                Objects.requireNonNull(name, "name is required");
                return new PersonRecord(name, age, email, phone, city);
            }
        }
    }

    // ============================================================
    // Sekcja 1: Wprowadzenie do Record
    // ============================================================

    static void introductionToRecords() {
        System.out.println("=== Section 1: Introduction to Records ===");

        // Tworzenie instancji record
        var point = new Point(3.0, 4.0);
        var name = new FullName("John", "Doe");

        // toString() jest automatycznie generowany
        System.out.println("point: " + point);
        System.out.println("name:  " + name);

        // Metody akcesorowe nazwane od komponentów — NIE getX()/getY()
        System.out.println("\n--- Accessor methods (point.x(), not point.getX()) ---");
        System.out.println("point.x() = " + point.x());
        System.out.println("point.y() = " + point.y());
        System.out.println("name.first() = " + name.first());
        System.out.println("name.last() = " + name.last());

        // equals() i hashCode() — równość strukturalna
        System.out.println("\n--- equals() and hashCode() ---");
        var point2 = new Point(3.0, 4.0);
        var point3 = new Point(1.0, 2.0);
        System.out.println("point.equals(point2) [same values]: " + point.equals(point2));
        System.out.println("point.equals(point3) [different values]: " + point.equals(point3));
        System.out.println("point.hashCode() == point2.hashCode(): " + (point.hashCode() == point2.hashCode()));

        // Record rozszerzają java.lang.Record
        System.out.println("\n--- Records extend java.lang.Record ---");
        System.out.println("point instanceof Record: " + (point instanceof Record));
        System.out.println("Point superclass: " + Point.class.getSuperclass().getName());
        System.out.println("Point is final: " + Modifier.isFinal(Point.class.getModifiers()));
        System.out.println("Point isRecord(): " + Point.class.isRecord());
    }

    // ============================================================
    // Sekcja 2: Anatomia Record
    // ============================================================

    static void anatomyOfARecord() {
        System.out.println("\n=== Section 2: Anatomy of a Record ===");

        // Tworzenie instancji RGB
        var red = new RGB(255, 0, 0);
        var custom = new RGB(255, 128, 0);

        // Format toString: RGB[red=255, green=128, blue=0]
        System.out.println("red:    " + red);
        System.out.println("custom: " + custom);

        // Akcesory
        System.out.println("\n--- Accessor methods ---");
        System.out.println("custom.red()   = " + custom.red());
        System.out.println("custom.green() = " + custom.green());
        System.out.println("custom.blue()  = " + custom.blue());

        // Spójność equals
        System.out.println("\n--- equals consistency ---");
        var custom2 = new RGB(255, 128, 0);
        var different = new RGB(0, 128, 255);
        System.out.println("custom.equals(custom2) [same components]: " + custom.equals(custom2));
        System.out.println("custom.equals(different) [different components]: " + custom.equals(different));

        // Refleksja: getRecordComponents()
        System.out.println("\n--- Reflection: getRecordComponents() ---");
        RecordComponent[] components = RGB.class.getRecordComponents();
        System.out.println("RGB has " + components.length + " components:");
        for (var comp : components) {
            System.out.println("  - " + comp.getName() + " : " + comp.getType().getSimpleName());
        }

        // Dostęp do wartości komponentów przez refleksję
        System.out.println("\n--- Component values via reflection ---");
        for (var comp : components) {
            try {
                Object value = comp.getAccessor().invoke(custom);
                System.out.println("  " + comp.getName() + " = " + value);
            } catch (Exception e) {
                System.out.println("  Error accessing " + comp.getName() + ": " + e.getMessage());
            }
        }
    }

    // ============================================================
    // Sekcja 3: Niestandardowe konstruktory i walidacja
    // ============================================================

    static void customConstructorsAndValidation() {
        System.out.println("\n=== Section 3: Custom Constructors and Validation ===");

        // Temperature — konstruktor kompaktowy z normalizacją
        System.out.println("--- Temperature: compact constructor with normalization ---");
        var tempC = new Temperature(100.0, "  c  ");
        System.out.println("new Temperature(100.0, \"  c  \"): " + tempC);
        System.out.println("Unit normalized to: " + tempC.unit());

        var tempF = new Temperature(212.0, "f");
        System.out.println("new Temperature(212.0, \"f\"): " + tempF);

        // Walidacja — nieprawidłowa jednostka
        System.out.println("\n--- Temperature: validation ---");
        try {
            new Temperature(100.0, "X");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid unit rejected: " + e.getMessage());
        }

        // Dodatkowy konstruktor
        System.out.println("\n--- Temperature: additional constructor ---");
        var defaultTemp = new Temperature(37.0);
        System.out.println("new Temperature(37.0): " + defaultTemp + " (defaults to Celsius)");

        // Konwersja toCelsius()
        System.out.println("\n--- Temperature: toCelsius() conversion ---");
        System.out.printf("100°C → %.2f°C%n", new Temperature(100.0, "C").toCelsius());
        System.out.printf("212°F → %.2f°C%n", new Temperature(212.0, "F").toCelsius());
        System.out.printf("373.15K → %.2f°C%n", new Temperature(373.15, "K").toCelsius());

        // Tags — kopia obronna
        System.out.println("\n--- Tags: defensive copy ---");
        var mutableList = new ArrayList<>(List.of("java", "records", "modern"));
        var tags = new Tags("tech", mutableList);
        System.out.println("tags: " + tags);

        mutableList.add("MUTATED");
        System.out.println("After mutating original list:");
        System.out.println("  original list: " + mutableList);
        System.out.println("  tags.values(): " + tags.values() + " (unchanged — defensive copy)");

        // Range — walidacja + metody
        System.out.println("\n--- Range: validation and methods ---");
        var range = new Range(1, 10);
        System.out.println("range: " + range);
        System.out.println("range.length(): " + range.length());
        System.out.println("range.contains(5): " + range.contains(5));
        System.out.println("range.contains(15): " + range.contains(15));

        try {
            new Range(10, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid range rejected: " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 4: Record i interfejsy — Generyczne Record
    // ============================================================

    static void recordsAndInterfacesGenericRecords() {
        System.out.println("\n=== Section 4: Records and Interfaces — Generic Records ===");

        // Money — implementuje Printable i Comparable
        System.out.println("--- Money: Printable and Comparable ---");
        var m1 = new Money(29.99, "USD");
        var m2 = new Money(49.99, "USD");
        var m3 = new Money(9.99, "USD");

        System.out.println("m1.prettyPrint(): " + m1.prettyPrint());
        System.out.println("m2.prettyPrint(): " + m2.prettyPrint());

        // Sortowanie przez Comparable
        var prices = new ArrayList<>(List.of(m2, m1, m3));
        Collections.sort(prices);
        System.out.println("\nSorted prices:");
        for (var price : prices) {
            System.out.println("  " + price.prettyPrint());
        }

        // Różne waluty — compareTo odrzuca
        try {
            new Money(10, "USD").compareTo(new Money(10, "EUR"));
        } catch (IllegalArgumentException e) {
            System.out.println("\nCross-currency comparison: " + e.getMessage());
        }

        // Pair — generyczny record
        System.out.println("\n--- Pair: generic record ---");
        var pair1 = new Pair<>("hello", 42);
        var pair2 = new Pair<>(3.14, List.of("a", "b"));
        System.out.println("pair1: " + pair1);
        System.out.println("pair2: " + pair2);

        // mapFirst / mapSecond
        System.out.println("\n--- Pair: mapFirst / mapSecond ---");
        var mapped1 = pair1.mapFirst(String::toUpperCase);
        System.out.println("pair1.mapFirst(toUpperCase): " + mapped1);

        var mapped2 = pair1.mapSecond(n -> n * 2);
        System.out.println("pair1.mapSecond(n -> n * 2): " + mapped2);

        // Record jako klucze Map (równość strukturalna)
        System.out.println("\n--- Records as Map keys ---");
        var map = new HashMap<Point, String>();
        map.put(new Point(1.0, 2.0), "A");
        map.put(new Point(3.0, 4.0), "B");

        // Wyszukiwanie z inną instancją, ale tymi samymi wartościami
        var lookup = new Point(1.0, 2.0);
        System.out.println("map.get(new Point(1.0, 2.0)): " + map.get(lookup));
        System.out.println("Works because records have structural equality!");
    }

    // ============================================================
    // Sekcja 5: Record z dopasowywaniem wzorców
    // ============================================================

    static void recordsWithPatternMatching() {
        System.out.println("\n=== Section 5: Records with Pattern Matching ===");

        // instanceof z record
        System.out.println("--- instanceof with records ---");
        Object obj = new Point(3.0, 4.0);
        if (obj instanceof Point p) {
            double distance = Math.sqrt(p.x() * p.x() + p.y() * p.y());
            System.out.println(p + " distance from origin: " + distance);
        }

        // switch z typami record
        System.out.println("\n--- switch with record types ---");
        List<Object> items = List.of(
                new Point(1.0, 2.0),
                new FullName("Jane", "Smith"),
                new RGB(0, 255, 0),
                "just a string"
        );

        for (var item : items) {
            String description = switch (item) {
                case Point p -> "Point at (" + p.x() + ", " + p.y() + ")";
                case FullName fn -> "Name: " + fn.first() + " " + fn.last();
                case RGB rgb -> "Color: R=" + rgb.red() + " G=" + rgb.green() + " B=" + rgb.blue();
                default -> "Other: " + item;
            };
            System.out.println("  " + description);
        }

        // Wzorce dekonstrukcji record (Java 21+)
        System.out.println("\n--- Record deconstruction patterns ---");
        var points = List.of(
                new Point(1.0, 2.0),
                new Point(-3.0, 4.0),
                new Point(5.0, -1.0),
                new Point(-2.0, -7.0)
        );

        for (var point : points) {
            String quadrant = switch (point) {
                case Point(var x, var y) when x > 0 && y > 0 -> "Q1 (positive, positive)";
                case Point(var x, var y) when x < 0 && y > 0 -> "Q2 (negative, positive)";
                case Point(var x, var y) when x < 0 && y < 0 -> "Q3 (negative, negative)";
                case Point(var x, var y) when x > 0 && y < 0 -> "Q4 (positive, negative)";
                case Point(var x, var y) -> "On axis";
            };
            System.out.println("  " + point + " → " + quadrant);
        }

        // Zagnieżdżone wzorce record
        System.out.println("\n--- Nested record patterns ---");
        var line = new Line(new Point(0, 0), new Point(3, 4));
        if (line instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.println("Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
            System.out.println("Length: " + length);
        }

        // Zagnieżdżona dekonstrukcja z ColoredPoint
        System.out.println("\n--- Nested deconstruction with ColoredPoint ---");
        var coloredPoints = List.of(
                new ColoredPoint(new Point(1, 2), "red"),
                new ColoredPoint(new Point(-1, 5), "blue"),
                new ColoredPoint(new Point(3, -2), "green")
        );

        for (var cp : coloredPoints) {
            switch (cp) {
                case ColoredPoint(Point(var x, var y), var color) when x > 0 && y > 0 ->
                        System.out.println("  " + color + " point in Q1 at (" + x + ", " + y + ")");
                case ColoredPoint(Point(var x, var y), var color) ->
                        System.out.println("  " + color + " point at (" + x + ", " + y + ")");
            }
        }

        // Dekonstrukcja Pair
        System.out.println("\n--- Pair deconstruction ---");
        var pairs = List.of(
                new Pair<>("Alice", 30),
                new Pair<>("Bob", 17),
                new Pair<>("Charlie", 25)
        );

        for (var pair : pairs) {
            switch (pair) {
                case Pair(var name, Integer age) when age >= 18 ->
                        System.out.println("  " + name + " (age " + age + "): adult");
                case Pair(var name, Integer age) ->
                        System.out.println("  " + name + " (age " + age + "): minor");
            }
        }
    }

    // ============================================================
    // Sekcja 6: Wzorce praktyczne i ograniczenia
    // ============================================================

    static void practicalPatternsAndLimitations() {
        System.out.println("\n=== Section 6: Practical Patterns and Limitations ===");

        // Wzorzec Builder dla PersonRecord
        System.out.println("--- Builder pattern for records ---");
        var person = new PersonRecord.Builder()
                .name("Alice Johnson")
                .age(30)
                .email("alice@example.com")
                .phone("+1-555-0123")
                .city("New York")
                .build();
        System.out.println("Built: " + person);

        // Builder z tylko wymaganymi polami
        var minimal = new PersonRecord.Builder()
                .name("Bob")
                .age(25)
                .build();
        System.out.println("Minimal: " + minimal);

        // Lokalne record w potokach Stream
        System.out.println("\n--- Local records in stream pipelines ---");
        record NameScore(String name, int score) {}

        var results = List.of(
                new NameScore("Alice", 92),
                new NameScore("Bob", 85),
                new NameScore("Charlie", 97),
                new NameScore("Diana", 88),
                new NameScore("Eve", 91)
        );

        var topScorers = results.stream()
                .filter(ns -> ns.score() >= 90)
                .sorted(Comparator.comparingInt(NameScore::score).reversed())
                .toList();

        System.out.println("Top scorers (score >= 90):");
        for (var ns : topScorers) {
            System.out.println("  " + ns.name() + ": " + ns.score());
        }

        // Komponenty record przez refleksję
        System.out.println("\n--- Record components via reflection ---");
        System.out.println("PersonRecord components:");
        for (var comp : PersonRecord.class.getRecordComponents()) {
            System.out.println("  - " + comp.getName() + " : " + comp.getType().getSimpleName());
        }

        // Serializacja — podróż w obie strony
        System.out.println("\n--- Serialization round-trip ---");
        record SerializablePoint(double x, double y) implements Serializable {}

        var original = new SerializablePoint(3.14, 2.71);
        try {
            // Serializacja
            var baos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            // Deserializacja
            var bais = new ByteArrayInputStream(baos.toByteArray());
            var ois = new ObjectInputStream(bais);
            var deserialized = (SerializablePoint) ois.readObject();
            ois.close();

            System.out.println("Original:     " + original);
            System.out.println("Deserialized: " + deserialized);
            System.out.println("Equal: " + original.equals(deserialized));
        } catch (Exception e) {
            System.out.println("Serialization error: " + e.getMessage());
        }

        // Podsumowanie ograniczeń
        System.out.println("\n--- Summary of record limitations ---");
        System.out.println("1. Cannot extend a class (already extend java.lang.Record)");
        System.out.println("2. Cannot have mutable fields (all components are final)");
        System.out.println("3. Cannot declare additional instance fields");
        System.out.println("4. Are implicitly final (cannot be subclassed)");
        System.out.println("5. Cannot be abstract");
        System.out.println("→ Use records for data carriers; use classes for mutable/complex objects");
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        introductionToRecords();
        anatomyOfARecord();
        customConstructorsAndValidation();
        recordsAndInterfacesGenericRecords();
        recordsWithPatternMatching();
        practicalPatternsAndLimitations();
    }
}
