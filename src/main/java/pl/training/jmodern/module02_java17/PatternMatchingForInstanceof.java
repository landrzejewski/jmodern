package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Problem z instanceof
// ============================================================

/*
## Problem z instanceof

- W tradycyjnej Javie sprawdzenie typu obiektu, a następnie
  jego użycie wymaga **trzech oddzielnych kroków**:
    1. `if (obj instanceof String)` — sprawdź typ
    2. `String s = (String) obj;` — rzutuj na typ
    3. Użyj `s` — w końcu pracuj z wartością
  To 3 linie na coś, co koncepcyjnie jest pojedynczą operacją.
- **Rzutowanie jest nadmiarowe**: Jeśli sprawdzenie `instanceof`
  przeszło, już znamy typ — a mimo to Java zmuszała nas do
  napisania jawnego rzutowania, które nigdy nie może się nie powieść.
  To czysta szablonowość.
- **Ryzyko ClassCastException**: Jeśli programista omyłkowo rzutuje
  na niewłaściwy typ (lub rzutuje bez wcześniejszego sprawdzenia
  instanceof), w czasie wykonania zostaje rzucony
  `ClassCastException`. Wzorzec sprawdź-potem-rzutuj powinien być
  pojedynczą atomową operacją.
- **JEP 394** (sfinalizowano w Javie 16):
    - Podgląd w Javie 14 (JEP 305)
    - Drugi podgląd w Javie 15 (JEP 375)
    - Sfinalizowano w Javie 16 (JEP 394)
    - Omawiane tutaj z Java 17 LTS
- **Wiązanie zmiennej wzorca**: Nowa składnia łączy sprawdzenie
  i rzutowanie w jedno wyrażenie:
      if (obj instanceof String s) { // użyj s bezpośrednio }
  Zmienna s nazywana jest **zmienną wzorca**. Jest
  automatycznie wiązana z rzutowaną wartością i jest w zasięgu
  tylko tam, gdzie kompilator może udowodnić, że sprawdzenie
  się powiodło.
- Ta funkcja działa z **dowolnym typem referencyjnym** — klasami,
  interfejsami, klasami abstrakcyjnymi — nie tylko record lub sealed typami.
*/

// ============================================================
// Sekcja 2: Zmienne wzorca i zasięg
// ============================================================

/*
## Zmienne wzorca i zasięg

- Zmienne wzorca używają **zasięgu przepływowego** (zwanego też
  **analizą określonego przypisania**), a NIE tradycyjnego zasięgu
  blokowego. Zmienna jest w zasięgu wszędzie, gdzie kompilator
  może udowodnić, że sprawdzenie instanceof się powiodło.
- **Zasięg w ciele if**: Zmienna wzorca jest w zasięgu wewnątrz
  ciała if, ponieważ sprawdzenie gwarantowanie się powiodło:
      if (obj instanceof String s) {
          // s jest w zasięgu tutaj
      }
      // s NIE jest w zasięgu tutaj
- **Zanegowany warunek + wczesny powrót**: Gdy negujesz
  warunek i wracasz wcześniej, zmienna jest w zasięgu PO
  bloku if (w pozostałej części metody):
      if (!(obj instanceof String s)) return;
      // s jest w zasięgu tutaj — sprawdzenie musiało się powieść
  Ten wzorzec jest powszechny dla klauzul strażniczych / warunków wstępnych.
- **Zasięg w else z negacją**: Jeśli warunek jest zanegowany
  w inny sposób (przez `else`), zmienna jest dostępna w
  gałęzi else tylko jeśli logika to gwarantuje:
      if (obj instanceof String s) {
          // s w zasięgu
      } else {
          // s NIE w zasięgu — obj może nie być String
      }
- **Zasięg w pętlach while**: Zmienne wzorca działają również
  w warunkach pętli while i w ciele pętli.
- **Przesłanianie**: Zmienna wzorca może przesłonić pole lub zmienną
  lokalną o tej samej nazwie, tak jak inne zmienne lokalne.
*/

// ============================================================
// Sekcja 3: Złożone warunki
// ============================================================

/*
## Złożone warunki

- **Łączenie z `&&`**: Możesz łączyć wzorce instanceof
  z dodatkowymi warunkami używając `&&` (logiczne I):
      if (obj instanceof String s && s.length() > 5)
  To działa, ponieważ `&&` jest skrócone: `s` jest ewaluowane
  tylko jeśli sprawdzenie instanceof się powiodło, więc zmienna
  jest w zasięgu.
- **Nie można użyć z `||`**: Użycie `||` ze zmienną wzorca
  jest **błędem kompilacji**:
      if (obj instanceof String s || s.isEmpty()) // BŁĄD!
  Przy `||` prawa strona wykonuje się gdy lewa jest fałszywa,
  co oznacza, że `s` może nie być związane. Kompilator to odrzuca.
- **Wiele zmiennych wzorca**: Możesz łączyć wiele
  sprawdzeń instanceof z `&&`:
      if (a instanceof String s && b instanceof Integer i) {
          // zarówno s jak i i są w zasięgu
      }
- **Warunki strażnicze**: Połącz instanceof ze sprawdzaniem
  właściwości dla potężnego filtrowania:
      if (animal instanceof Dog d && d.breed().equals("Labrador"))
  To zastępuje dwie zagnieżdżone instrukcje if jedną linią.
- **Zagnieżdżone warunki**: Zmienne wzorca z zewnętrznego if
  są w zasięgu w zagnieżdżonych instrukcjach if:
      if (obj instanceof List<?> list) {
          if (list.size() > 0 && list.get(0) instanceof String s) {
              // zarówno list jak i s w zasięgu
          }
      }
*/

// ============================================================
// Sekcja 4: Hierarchie dziedziczenia
// ============================================================

/*
## Hierarchie dziedziczenia

- Przy używaniu instanceof z dziedziczeniem **kolejność ma znaczenie**:
  sprawdzaj najpierw najbardziej konkretny typ, potem bardziej ogólne.
  Jeśli sprawdzisz `Rect` przed `Square`, Square dopasuje się
  jako Rect i nigdy nie dotrze do przypadku Square.
      if (shape instanceof Square sq)       // sprawdź konkretny najpierw
      else if (shape instanceof Rect r)      // potem ogólny
      else if (shape instanceof Circle c)
- **Polimorfizm vs dopasowywanie wzorców**: To są narzędzia
  komplementarne, nie konkurencyjne:
    - **Polimorfizm** (metody wirtualne): Najlepszy gdy każdy typ
      wie jak wykonać operację na sobie. Dodaj metodę
      do hierarchii klas. Operacje na własnych danych.
    - **Dopasowywanie wzorców**: Najlepsze dla **operacji zewnętrznych**
      łączących dane z obiektu z zewnętrznym kontekstem, lub gdy
      nie można modyfikować hierarchii klas.
- **Kiedy używać każdego**:
    - `area()` → polimorfizm (każdy kształt oblicza swoje własne pole)
    - `describe(Shape)` → dopasowywanie wzorców (zewnętrzna logika
      opisu, która nie należy do klas kształtów)
    - `render(Shape, Canvas)` → dopasowywanie wzorców (wymaga
      zewnętrznego Canvas, od którego kształty nie powinny zależeć)
- **Podejście mieszane**: Użyj polimorfizmu dla podstawowego zachowania
  i dopasowywania wzorców dla operacji narzędziowych/wyświetlania/zewnętrznych.
*/

// ============================================================
// Sekcja 5: Wzorce praktyczne
// ============================================================

/*
## Wzorce praktyczne

- **Implementacja equals()**: Najpowszechniejsze rzeczywiste
  zastosowanie dopasowywania wzorców dla instanceof to metody `equals()`:
      @Override
      public boolean equals(Object o) {
          return o instanceof Sensor s
              && id == s.id
              && Objects.equals(type, s.type);
      }
  To zastępuje 5+ linii szablonowego kodu jednym wyrażeniem.
- **Filtrowanie Stream po typie**: Dopasowywanie wzorców łączy się
  naturalnie ze Stream do filtrowania i mapowania po typie:
      list.stream()
          .filter(obj -> obj instanceof Dog)
          .map(obj -> (Dog) obj)  // wciąż potrzebne w map
  Lub używając `Stream.mapMulti` dla czystszego podejścia.
- **Kontenery heterogeniczne**: Przetwarzanie `List<Object>` z
  mieszanymi typami staje się czytelne z dopasowywaniem wzorców:
      for (Object item : items) {
          if (item instanceof String s) { ... }
          else if (item instanceof Integer i) { ... }
      }
- **Zamiennik wzorca Visitor**: Dopasowywanie wzorców może zastąpić
  wzorzec visitor dla prostego dispatchu typów — nie ma potrzeby
  stosowania szablonu accept/visit.
- **Bezpieczeństwo null**: `null instanceof X` zawsze zwraca `false`
  dla dowolnego typu X. To oznacza, że nie potrzebujesz osobnego
  sprawdzenia null przed instanceof — jest wbudowane.
*/

// ============================================================
// Sekcja 6: Ewolucja dopasowywania wzorców
// ============================================================

/*
## Ewolucja dopasowywania wzorców

- **Java 16**: Dopasowywanie wzorców dla instanceof (JEP 394)
    - `if (obj instanceof String s) { ... }`
    - Fundament — eliminuje szablonowość rzutuj-po-sprawdzeniu
- **Java 17**: Klasy sealed (JEP 409) — przygotowanie gruntu
    - Typy sealed umożliwiają wyczerpujące sprawdzanie typów później
    - Brak bezpośrednich zmian w dopasowywaniu wzorców, ale
      niezbędne przygotowanie
- **Java 21**: Dopasowywanie wzorców dla switch (JEP 441) +
  Wzorce record (JEP 440)
    - `switch (obj) { case String s -> ...; }`
    - `case Point(var x, var y) -> ...` (dekonstrukcja record)
    - Strażniki `when`: `case String s when s.length() > 5 -> ...`
    - Sprawdzanie wyczerpywalności z typami sealed
- **Java 22+**: Nienazwane wzorce `_` (JEP 456)
    - `case Point(var x, _) -> ...` (ignoruj komponent y)
    - Przydatne gdy nie potrzebujesz wszystkich komponentów
- **Przyszłe kierunki** (w trakcie rozwoju):
    - Wzorce typów prymitywnych (`case int i -> ...`)
    - Wzorce tablic
    - Więcej wzorców dekonstrukcji poza record
- Ogólny kierunek: Java staje się językiem, w którym
  **programowanie zorientowane na dane** jest paradygmatem
  pierwszej klasy obok programowania obiektowego. Dopasowywanie
  wzorców jest kluczowym czynnikiem tej zmiany.
*/

public class PatternMatchingForInstanceof {

    // ---- Sekcja 1: Hierarchia Animal ----

    static abstract class Animal {
        abstract String name();
        abstract String sound();
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + name() + "]";
        }
    }

    static class Dog extends Animal {
        private final String name;
        private final String breed;
        Dog(String name, String breed) { this.name = name; this.breed = breed; }
        @Override String name() { return name; }
        @Override String sound() { return "Woof!"; }
        String breed() { return breed; }
        String fetch() { return name + " fetches the ball!"; }
    }

    static class Cat extends Animal {
        private final String name;
        private final boolean indoor;
        Cat(String name, boolean indoor) { this.name = name; this.indoor = indoor; }
        @Override String name() { return name; }
        @Override String sound() { return "Meow!"; }
        boolean isIndoor() { return indoor; }
        String purr() { return name + " purrs contentedly."; }
    }

    static class Bird extends Animal {
        private final String name;
        private final boolean canFly;
        Bird(String name, boolean canFly) { this.name = name; this.canFly = canFly; }
        @Override String name() { return name; }
        @Override String sound() { return canFly ? "Tweet!" : "Squawk!"; }
        boolean canFly() { return canFly; }
    }

    // ---- Sekcja 4: Hierarchia Shape ----

    static abstract class Shape {
        abstract double area();
    }

    static class Circle extends Shape {
        private final double radius;
        Circle(double radius) { this.radius = radius; }
        double radius() { return radius; }
        @Override double area() { return Math.PI * radius * radius; }
        @Override public String toString() { return "Circle[radius=" + radius + "]"; }
    }

    static class Rect extends Shape {
        private final double width;
        private final double height;
        Rect(double width, double height) { this.width = width; this.height = height; }
        double width() { return width; }
        double height() { return height; }
        @Override double area() { return width * height; }
        @Override public String toString() { return "Rect[" + width + "x" + height + "]"; }
    }

    static class Square extends Rect {
        Square(double side) { super(side, side); }
        double side() { return width(); }
        @Override public String toString() { return "Square[side=" + side() + "]"; }
    }

    // ---- Sekcja 5: Sensor z equals() ----

    static class Sensor {
        private final int id;
        private final String type;
        Sensor(int id, String type) { this.id = id; this.type = type; }
        int id() { return id; }
        String type() { return type; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Sensor s
                    && id == s.id
                    && Objects.equals(type, s.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, type);
        }

        @Override
        public String toString() {
            return "Sensor[id=" + id + ", type=" + type + "]";
        }
    }

    // ============================================================
    // Sekcja 1: Problem z instanceof
    // ============================================================

    static void theInstanceofProblem() {
        System.out.println("=== Section 1: The instanceof Problem ===");

        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Bird("Polly", true),
                new Dog("Buddy", "Labrador"),
                new Cat("Shadow", false),
                new Bird("Kiwi", false)
        );

        // STARY SPOSÓB: instanceof + jawne rzutowanie
        System.out.println("--- Old way: instanceof + explicit cast ---");
        for (Animal animal : animals) {
            if (animal instanceof Dog) {
                Dog d = (Dog) animal;  // nadmiarowe rzutowanie
                System.out.println("  Dog: " + d.name() + " (" + d.breed() + ") - " + d.fetch());
            } else if (animal instanceof Cat) {
                Cat c = (Cat) animal;  // nadmiarowe rzutowanie
                System.out.println("  Cat: " + c.name() + " (indoor: " + c.isIndoor() + ") - " + c.purr());
            } else if (animal instanceof Bird) {
                Bird b = (Bird) animal;  // nadmiarowe rzutowanie
                System.out.println("  Bird: " + b.name() + " (can fly: " + b.canFly() + ")");
            }
        }

        // NOWY SPOSÓB: dopasowywanie wzorców dla instanceof
        System.out.println("\n--- New way: pattern matching ---");
        for (Animal animal : animals) {
            if (animal instanceof Dog d) {
                System.out.println("  Dog: " + d.name() + " (" + d.breed() + ") - " + d.fetch());
            } else if (animal instanceof Cat c) {
                System.out.println("  Cat: " + c.name() + " (indoor: " + c.isIndoor() + ") - " + c.purr());
            } else if (animal instanceof Bird b) {
                System.out.println("  Bird: " + b.name() + " (can fly: " + b.canFly() + ")");
            }
        }

        // Porównanie obok siebie — liczba linii kodu
        System.out.println("\n--- Side-by-side: old vs new ---");
        Animal animal = new Dog("Max", "Poodle");

        // Stary: 3 linie
        System.out.println("  Old (3 lines):");
        if (animal instanceof Dog) {
            Dog d = (Dog) animal;
            System.out.println("    " + d.name() + " is a " + d.breed());
        }

        // Nowy: 1 linia (koncepcyjnie)
        System.out.println("  New (1 line check+bind):");
        if (animal instanceof Dog d) {
            System.out.println("    " + d.name() + " is a " + d.breed());
        }
    }

    // ============================================================
    // Sekcja 2: Zmienne wzorca i zasięg
    // ============================================================

    static String describeWithEarlyReturn(Object obj) {
        if (!(obj instanceof String s)) {
            return "Not a string";
        }
        // s jest w zasięgu tutaj, ponieważ instanceof musiało się powieść
        // (jeśli się nie powiodło, wrócilibyśmy powyżej)
        return "String of length " + s.length() + ": \"" + s + "\"";
    }

    static void patternVariablesAndScope() {
        System.out.println("\n=== Section 2: Pattern Variables and Scope ===");

        // Podstawowy zasięg w ciele if
        System.out.println("--- Basic scope in if-body ---");
        Object obj = "Hello, Pattern Matching!";
        if (obj instanceof String s) {
            System.out.println("  s is in scope: \"" + s + "\" (length: " + s.length() + ")");
        }
        // s NIE jest w zasięgu tutaj — nie można go użyć poza ciałem if

        // Wzorzec wczesnego powrotu z negacją
        System.out.println("\n--- Early return pattern with negation ---");
        System.out.println("  " + describeWithEarlyReturn("Hello World"));
        System.out.println("  " + describeWithEarlyReturn(42));
        System.out.println("  " + describeWithEarlyReturn(null));

        // Zasięg w else ze zanegowanym warunkiem
        System.out.println("\n--- Scope in if vs else ---");
        Object value = 42;
        if (value instanceof String s) {
            System.out.println("  It's a string: " + s);
        } else {
            // s NIE jest w zasięgu tutaj — value nie jest String
            System.out.println("  Not a string, it's a: " + value.getClass().getSimpleName());
        }

        // Przykład błędu kompilacji (zakomentowany)
        // Object x = 42;
        // if (x instanceof String s) {
        //     System.out.println(s);
        // }
        // System.out.println(s); // BŁĄD: s nie jest w zasięgu tutaj

        // Zmienna wzorca w pętli
        System.out.println("\n--- Pattern variable in loop ---");
        List<Object> items = List.of("first", 2, "third", 4, "fifth");
        System.out.println("  Strings found in list:");
        for (Object item : items) {
            if (item instanceof String s) {
                System.out.println("    → \"" + s + "\"");
            }
        }

        // Zasięg z negacją: pominięcie nie-ciągów
        System.out.println("\n--- Negation scope: skip non-strings ---");
        for (Object item : items) {
            if (!(item instanceof String s)) {
                continue;  // pomiń nie-ciągi
            }
            // s jest w zasięgu tutaj dzięki zasięgowi przepływowemu
            System.out.println("    Processing string: \"" + s.toUpperCase() + "\"");
        }
    }

    // ============================================================
    // Sekcja 3: Złożone warunki
    // ============================================================

    static void complexConditions() {
        System.out.println("\n=== Section 3: Complex Conditions ===");

        // Łączenie instanceof z && (skrócone wartościowanie)
        System.out.println("--- instanceof with && (short-circuit) ---");
        List<Object> values = List.of("Hello World", "Hi", "", 42, "Pattern Matching is great", 3.14);
        for (Object val : values) {
            if (val instanceof String s && s.length() > 5) {
                System.out.println("  Long string: \"" + s + "\" (length: " + s.length() + ")");
            }
        }

        // Wiele sprawdzeń instanceof z &&
        System.out.println("\n--- Multiple instanceof checks with && ---");
        Object a = "Hello";
        Object b = 42;
        if (a instanceof String s && b instanceof Integer i) {
            System.out.println("  s = \"" + s + "\", i = " + i);
            System.out.println("  Combined: \"" + s + "\" repeated " + i + " times would be " + (s.length() * i) + " chars");
        }

        // Nie można użyć || ze zmiennymi wzorca (błąd kompilacji)
        System.out.println("\n--- Why || doesn't work with pattern variables ---");
        System.out.println("  // if (obj instanceof String s || s.isEmpty()) → COMPILE ERROR");
        System.out.println("  // With ||, the right side runs when left is false,");
        System.out.println("  // so 's' might not be bound → compiler rejects it");

        // Warunki strażnicze z hierarchią Animal
        System.out.println("\n--- Guard-like conditions ---");
        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Dog("Buddy", "Labrador"),
                new Dog("Max", "Poodle"),
                new Cat("Whiskers", true),
                new Cat("Shadow", false)
        );

        System.out.println("  Labradors only:");
        for (Animal animal : animals) {
            if (animal instanceof Dog d && d.breed().equals("Labrador")) {
                System.out.println("    Found Labrador: " + d.name());
            }
        }

        System.out.println("  Indoor cats only:");
        for (Animal animal : animals) {
            if (animal instanceof Cat c && c.isIndoor()) {
                System.out.println("    Indoor cat: " + c.name());
            }
        }

        // Zagnieżdżone sprawdzenia instanceof
        System.out.println("\n--- Nested instanceof checks ---");
        List<Object> containers = List.of(
                List.of("hello", "world"),
                List.of(1, 2, 3),
                "just a string",
                List.of()
        );
        for (Object container : containers) {
            if (container instanceof List<?> list && !list.isEmpty()) {
                if (list.getFirst() instanceof String s) {
                    System.out.println("  List starting with string: \"" + s + "\" (list size: " + list.size() + ")");
                } else if (list.getFirst() instanceof Integer i) {
                    System.out.println("  List starting with integer: " + i + " (list size: " + list.size() + ")");
                }
            }
        }
    }

    // ============================================================
    // Sekcja 4: Hierarchie dziedziczenia
    // ============================================================

    static String describeShape(Shape shape) {
        // Sprawdź najpierw najbardziej konkretny typ (Square przed Rect)
        if (shape instanceof Square sq) {
            return "Square with side " + sq.side() + " (area: " + sq.area() + ")";
        } else if (shape instanceof Rect r) {
            return "Rectangle " + r.width() + "x" + r.height() + " (area: " + r.area() + ")";
        } else if (shape instanceof Circle c) {
            return "Circle with radius " + c.radius() + " (area: " + String.format("%.2f", c.area()) + ")";
        }
        return "Unknown shape";
    }

    static double perimeter(Shape shape) {
        if (shape instanceof Square sq) {
            return 4 * sq.side();
        } else if (shape instanceof Rect r) {
            return 2 * (r.width() + r.height());
        } else if (shape instanceof Circle c) {
            return 2 * Math.PI * c.radius();
        }
        return 0;
    }

    static void inheritanceHierarchies() {
        System.out.println("\n=== Section 4: Inheritance Hierarchies ===");

        List<Shape> shapes = List.of(
                new Circle(5.0),
                new Rect(4.0, 6.0),
                new Square(3.0),
                new Circle(1.5),
                new Square(10.0),
                new Rect(7.0, 2.0)
        );

        // describeShape — sprawdzanie najbardziej konkretnego typu najpierw
        System.out.println("--- describeShape: most specific type first ---");
        for (Shape shape : shapes) {
            System.out.println("  " + describeShape(shape));
        }

        // Obwód przez dopasowywanie wzorców (operacja zewnętrzna)
        System.out.println("\n--- Perimeter via instanceof (external operation) ---");
        for (Shape shape : shapes) {
            System.out.printf("  %-25s → perimeter: %.2f%n", shape, perimeter(shape));
        }

        // Dlaczego kolejność ma znaczenie — demonstracja że Square jest Rect
        System.out.println("\n--- Why order matters: Square is a Rect ---");
        Shape square = new Square(5.0);
        System.out.println("  square instanceof Square: " + (square instanceof Square));
        System.out.println("  square instanceof Rect:   " + (square instanceof Rect));
        System.out.println("  square instanceof Shape:  " + (square instanceof Shape));
        System.out.println("  → Must check Square before Rect to get the specific match");

        // Polimorfizm dla area vs dopasowywanie wzorców dla opisu
        System.out.println("\n--- Polymorphism (area) vs pattern matching (describe) ---");
        for (Shape shape : shapes) {
            // area() używa polimorfizmu — każdy kształt oblicza swoje własne pole
            double area = shape.area();
            // describeShape używa dopasowywania wzorców — operacja zewnętrzna
            String desc = describeShape(shape);
            System.out.printf("  area()=%-10.2f  describe()=%s%n", area, desc);
        }

        // Podejście mieszane: polimorfizm + dopasowywanie wzorców
        System.out.println("\n--- Mixed approach ---");
        for (Shape shape : shapes) {
            String extra = "";
            if (shape instanceof Circle c) {
                extra = " (diameter: " + (c.radius() * 2) + ")";
            } else if (shape instanceof Square sq) {
                extra = " (diagonal: " + String.format("%.2f", sq.side() * Math.sqrt(2)) + ")";
            } else if (shape instanceof Rect r) {
                extra = " (diagonal: " + String.format("%.2f", Math.sqrt(r.width() * r.width() + r.height() * r.height())) + ")";
            }
            System.out.println("  " + shape + " → area=" + String.format("%.2f", shape.area()) + extra);
        }
    }

    // ============================================================
    // Sekcja 5: Wzorce praktyczne
    // ============================================================

    static String formatObject(Object obj) {
        if (obj instanceof String s) {
            return "String(\"" + s + "\")";
        } else if (obj instanceof Integer i) {
            return "Int(" + i + ")";
        } else if (obj instanceof Double d) {
            return "Double(" + d + ")";
        } else if (obj instanceof Boolean b) {
            return "Bool(" + b + ")";
        } else if (obj instanceof List<?> list) {
            return "List(size=" + list.size() + ")";
        } else if (obj == null) {
            return "Null";
        }
        return "Unknown(" + obj.getClass().getSimpleName() + ")";
    }

    static void practicalPatterns() {
        System.out.println("\n=== Section 5: Practical Patterns ===");

        // Demonstracja Sensor.equals() — najczęstszy przypadek użycia
        System.out.println("--- Sensor.equals() with pattern matching ---");
        var s1 = new Sensor(1, "temperature");
        var s2 = new Sensor(1, "temperature");
        var s3 = new Sensor(2, "humidity");
        var s4 = new Sensor(1, "pressure");

        System.out.println("  s1 = " + s1);
        System.out.println("  s2 = " + s2);
        System.out.println("  s3 = " + s3);
        System.out.println("  s1.equals(s2) [same id+type]: " + s1.equals(s2));
        System.out.println("  s1.equals(s3) [different id]:  " + s1.equals(s3));
        System.out.println("  s1.equals(s4) [different type]: " + s1.equals(s4));
        System.out.println("  s1.equals(null):               " + s1.equals(null));
        System.out.println("  s1.equals(\"string\"):           " + s1.equals("string"));

        // Sensory jako klucze mapy (equals + hashCode)
        var sensorMap = new HashMap<Sensor, String>();
        sensorMap.put(s1, "Living Room");
        sensorMap.put(s3, "Bathroom");
        System.out.println("  sensorMap.get(new Sensor(1, \"temperature\")): "
                + sensorMap.get(new Sensor(1, "temperature")));

        // Filtrowanie Stream po typie — wyodrębnianie wszystkich Dog
        System.out.println("\n--- Stream filtering by type ---");
        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Dog("Buddy", "Labrador"),
                new Bird("Polly", true),
                new Cat("Shadow", false),
                new Dog("Max", "Poodle")
        );

        List<Dog> dogs = animals.stream()
                .filter(a -> a instanceof Dog)
                .map(a -> (Dog) a)
                .toList();
        System.out.println("  All dogs: " + dogs.stream().map(d -> d.name() + " (" + d.breed() + ")").toList());

        // Alternatywa: użycie mapMulti do bezpiecznego typowo filtrowania
        List<String> dogNames = animals.stream()
                .<String>mapMulti((animal, consumer) -> {
                    if (animal instanceof Dog d) {
                        consumer.accept(d.name() + " the " + d.breed());
                    }
                })
                .toList();
        System.out.println("  Dogs (mapMulti): " + dogNames);

        // Kontener heterogeniczny: List<Object>
        System.out.println("\n--- Processing heterogeneous List<Object> ---");
        List<Object> mixed = List.of("hello", 42, 3.14, true, List.of(1, 2), "world", 100);
        int stringCount = 0;
        int numberCount = 0;
        for (Object item : mixed) {
            if (item instanceof String s) {
                System.out.println("  String: \"" + s + "\"");
                stringCount++;
            } else if (item instanceof Integer i) {
                System.out.println("  Integer: " + i);
                numberCount++;
            } else if (item instanceof Double d) {
                System.out.println("  Double: " + d);
                numberCount++;
            } else if (item instanceof Boolean b) {
                System.out.println("  Boolean: " + b);
            } else if (item instanceof List<?> list) {
                System.out.println("  List: " + list);
            }
        }
        System.out.println("  → Strings: " + stringCount + ", Numbers: " + numberCount);

        // Formatowanie zwierząt bez wzorca visitor
        System.out.println("\n--- Formatting animals (no visitor needed) ---");
        for (Animal animal : animals) {
            String formatted;
            if (animal instanceof Dog d) {
                formatted = "🐕 " + d.name() + " (" + d.breed() + ") says " + d.sound();
            } else if (animal instanceof Cat c) {
                formatted = "🐈 " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ") says " + c.sound();
            } else if (animal instanceof Bird b) {
                formatted = "🐦 " + b.name() + " (" + (b.canFly() ? "can fly" : "flightless") + ") says " + b.sound();
            } else {
                formatted = "Unknown animal: " + animal.name();
            }
            System.out.println("  " + formatted);
        }

        // Bezpieczeństwo null: null instanceof X jest zawsze false
        System.out.println("\n--- Null safety ---");
        Animal nullAnimal = null;
        System.out.println("  null instanceof Dog:    " + (nullAnimal instanceof Dog));
        System.out.println("  null instanceof Cat:    " + (nullAnimal instanceof Cat));
        System.out.println("  null instanceof Animal: " + (nullAnimal instanceof Animal));
        System.out.println("  null instanceof Object: " + (nullAnimal instanceof Object));
        System.out.println("  → null instanceof <anything> is always false — no NPE risk");

        // Metoda pomocnicza formatObject
        System.out.println("\n--- formatObject helper ---");
        List<Object> objects = List.of("test", 42, 3.14, true, List.of("a", "b"));
        for (Object obj : objects) {
            System.out.println("  " + formatObject(obj));
        }
        System.out.println("  " + formatObject(null));
    }

    // ============================================================
    // Sekcja 6: Ewolucja dopasowywania wzorców
    // ============================================================

    static String describeAnimal(Animal animal) {
        if (animal instanceof Dog d) {
            return "Dog " + d.name() + " (" + d.breed() + ")";
        } else if (animal instanceof Cat c) {
            return "Cat " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ")";
        } else if (animal instanceof Bird b) {
            return "Bird " + b.name() + " (" + (b.canFly() ? "flying" : "flightless") + ")";
        }
        return "Unknown: " + animal.name();
    }

    static void patternMatchingEvolution() {
        System.out.println("\n=== Section 6: Pattern Matching Evolution ===");

        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Bird("Polly", true),
                new Dog("Buddy", "Labrador"),
                new Cat("Shadow", false),
                new Bird("Kiwi", false)
        );

        // Styl Java 16: łańcuch if/else z instanceof
        System.out.println("--- Java 16: instanceof if/else chain ---");
        for (Animal animal : animals) {
            System.out.println("  " + describeAnimal(animal));
        }

        // Styl Java 21: przepisanie jako wyrażenie switch
        System.out.println("\n--- Java 21: switch expression rewrite ---");
        for (Animal animal : animals) {
            var description = switch (animal) {
                case Dog d -> "Dog " + d.name() + " (" + d.breed() + ")";
                case Cat c -> "Cat " + c.name() + " (" + (c.isIndoor() ? "indoor" : "outdoor") + ")";
                case Bird b -> "Bird " + b.name() + " (" + (b.canFly() ? "flying" : "flightless") + ")";
                default -> "Unknown: " + animal.name();
            };
            System.out.println("  " + description);
        }

        // Java 21: switch ze strażnikami `when`
        System.out.println("\n--- Java 21: switch with 'when' guards ---");
        for (Animal animal : animals) {
            var label = switch (animal) {
                case Dog d when d.breed().equals("Labrador") -> "Friendly Labrador: " + d.name();
                case Dog d when d.breed().equals("German Shepherd") -> "Guard dog: " + d.name();
                case Dog d -> "Dog: " + d.name() + " (" + d.breed() + ")";
                case Cat c when c.isIndoor() -> "Indoor cat: " + c.name();
                case Cat c -> "Outdoor cat: " + c.name();
                case Bird b when b.canFly() -> "Flying bird: " + b.name();
                case Bird b -> "Flightless bird: " + b.name();
                default -> "Animal: " + animal.name();
            };
            System.out.println("  " + label);
        }

        // Java 22+: nienazwane wzorce _ (koncepcyjnie)
        System.out.println("\n--- Java 22+: unnamed patterns (conceptual) ---");
        System.out.println("  // In Java 22+, you can use _ to ignore components:");
        System.out.println("  // case Dog _ -> \"It's a dog\";  // don't need the binding");
        System.out.println("  // case Point(var x, _) -> \"x=\" + x;  // ignore y");
        System.out.println("  // Useful when you only care about the type, not the data");

        // Demonstracja nienazwanej zmiennej z switch
        for (Animal animal : animals) {
            var kind = switch (animal) {
                case Dog _ -> "Dog";
                case Cat _ -> "Cat";
                case Bird _ -> "Bird";
                default -> "Other";
            };
            System.out.println("  " + animal.name() + " is a " + kind);
        }

        // Wyczerpywalność z typami sealed (koncepcyjnie)
        System.out.println("\n--- Exhaustiveness note ---");
        System.out.println("  If Animal were a sealed class permitting only Dog, Cat, Bird:");
        System.out.println("  sealed abstract class Animal permits Dog, Cat, Bird {}");
        System.out.println("  Then switch would be exhaustive — no 'default' needed.");
        System.out.println("  The compiler would enforce that all subtypes are handled.");
        System.out.println("  Adding a new subtype (e.g., Fish) would trigger compile errors");
        System.out.println("  in every switch that doesn't handle it.");

        // Podsumowanie harmonogramu ewolucji
        System.out.println("\n--- Pattern matching evolution timeline ---");
        System.out.println("  Java 14-15: instanceof patterns (preview)");
        System.out.println("  Java 16:    instanceof patterns (finalized, JEP 394)");
        System.out.println("  Java 17:    sealed classes (JEP 409) — groundwork");
        System.out.println("  Java 21:    switch patterns + record patterns (JEP 441, 440)");
        System.out.println("  Java 22:    unnamed patterns _ (JEP 456)");
        System.out.println("  Future:     primitive patterns, array patterns, more deconstruction");
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        theInstanceofProblem();
        patternVariablesAndScope();
        complexConditions();
        inheritanceHierarchies();
        practicalPatterns();
        patternMatchingEvolution();
    }
}
