package pl.training.jmodern.module02_java8;

import java.util.*;
import java.util.function.*;

// ============================================================
// Sekcja 1: Wprowadzenie do wyrażeń Lambda
// ============================================================

/*
## Wprowadzenie do wyrażeń Lambda

- **Wyrażenie lambda** to zwięzły sposób reprezentowania anonimowej
  funkcji — bloku kodu, który może być przekazywany jako wartość.
- Przed Java 8 jedynym sposobem przekazywania zachowania było użycie
  **anonimowych klas wewnętrznych**, które były rozwlekłe i pełne szablonowego kodu.
- Lambdy umożliwiają bardziej funkcyjny styl programowania w Javie:
  traktowanie zachowania jako danych, które mogą być przechowywane w zmiennych,
  przekazywane do metod i zwracane z metod.
- Wyrażenie lambda składa się z trzech części:
    - **Lista parametrów**: `(Type param1, Type param2)` lub po prostu `(param1, param2)`
      z inferencją typów.
    - **Token strzałki**: `->` oddziela parametry od ciała.
    - **Ciało**: pojedyncze wyrażenie lub blok instrukcji `{ ... }`.
- Lambdy **nie** istnieją w izolacji — zawsze implementują
  **interfejs funkcyjny** (interfejs z dokładnie jedną metodą abstrakcyjną).
  Kompilator wnioskuje z kontekstu, który interfejs funkcyjny jest celem lambdy.
- Pod spodem lambdy **nie** są klasami anonimowymi.
  JVM wykorzystuje `invokedynamic` (wprowadzony w Java 7) do generowania
  lekkich implementacji w czasie wykonania, unikając narzutu
  tworzenia nowego pliku `.class` dla każdej lambdy.
*/

// ============================================================
// Sekcja 2: Warianty składni Lambda
// ============================================================

/*
## Warianty składni Lambda

- **Pełna składnia**: `(Type param1, Type param2) -> { statements; return value; }`
- **Wnioskowane typy**: `(param1, param2) -> expression`
  Kompilator wnioskuje typy parametrów z docelowego interfejsu funkcyjnego.
- **Pojedynczy parametr, bez nawiasów**: `param -> expression`
  Nawiasy są opcjonalne, gdy jest dokładnie jeden parametr z
  wnioskowanym typem. Jeśli typ jest jawnie zadeklarowany, nawiasy
  są wymagane: `(String s) -> s.length()`.
- **Brak parametrów**: `() -> expression`
- **Ciało wieloliniowe**: `(params) -> { statement1; statement2; return value; }`
  Gdy ciało zawiera wiele instrukcji, wymagane są nawiasy klamrowe,
  a instrukcja `return` jest potrzebna, jeśli lambda musi zwrócić wartość.
- **Ciało z pojedynczym wyrażeniem**: `(params) -> expression`
  Bez nawiasów klamrowych, bez słowa kluczowego `return` — wartość wyrażenia
  jest automatycznie zwracana.
- **`var` w parametrach** (Java 11+): `(var x, var y) -> x + y`
  Umożliwia adnotacje na parametrach lambda: `(@NonNull var x) -> x.length()`.
  Jeśli `var` jest użyte dla jednego parametru, musi być użyte dla wszystkich.
*/

// ============================================================
// Sekcja 3: Interfejsy funkcyjne
// ============================================================

/*
## Interfejsy funkcyjne

- **Interfejs funkcyjny** to interfejs z dokładnie **jedną
  metodą abstrakcyjną** (SAM — Single Abstract Method). Może mieć
  dowolną liczbę metod `default` lub `static`.
- Adnotacja `@FunctionalInterface` jest opcjonalna, ale zalecana.
  Instruuje kompilator, aby zweryfikował, że interfejs ma dokładnie
  jedną metodę abstrakcyjną — błąd kompilacji występuje, jeśli nie.
- Metody odziedziczone z `java.lang.Object` (jak `equals`, `hashCode`,
  `toString`) **nie** liczą się do limitu metod abstrakcyjnych,
  ponieważ każda klasa już dostarcza ich implementacje.
- Lambdy mogą być użyte tylko tam, gdzie oczekiwany jest typ interfejsu
  funkcyjnego. Kompilator dopasowuje sygnaturę lambdy (typy parametrów,
  typ zwracany) do metody abstrakcyjnej interfejsu funkcyjnego.
- Możesz definiować własne interfejsy funkcyjne dla celów
  domenowych, ale Java dostarcza bogaty zestaw wbudowanych interfejsów
  w `java.util.function`.
*/

// ============================================================
// Sekcja 4: Wbudowane interfejsy funkcyjne
// ============================================================

/*
## Wbudowane interfejsy funkcyjne (`java.util.function`)

Pakiet `java.util.function` dostarcza 43 interfejsy funkcyjne.
Główne z nich to:

| Interfejs             | Metoda              | Sygnatura          | Przeznaczenie                     |
|-----------------------|---------------------|--------------------|-----------------------------------|
| `Predicate<T>`        | `test(T)`           | `T -> boolean`     | Testowanie warunku                |
| `Function<T,R>`       | `apply(T)`          | `T -> R`           | Transformacja wartości            |
| `Consumer<T>`         | `accept(T)`         | `T -> void`        | Wykonanie efektu ubocznego       |
| `Supplier<T>`         | `get()`             | `() -> T`          | Dostarczenie/wytworzenie wartości |
| `UnaryOperator<T>`    | `apply(T)`          | `T -> T`           | Transformacja, ten sam typ we/wy  |
| `BinaryOperator<T>`   | `apply(T, T)`       | `(T,T) -> T`       | Łączenie dwóch wartości           |
| `BiFunction<T,U,R>`   | `apply(T, U)`       | `(T,U) -> R`       | Transformacja dwóch wartości      |
| `BiPredicate<T,U>`    | `test(T, U)`        | `(T,U) -> boolean` | Testowanie dwóch wartości         |
| `BiConsumer<T,U>`     | `accept(T, U)`      | `(T,U) -> void`    | Efekt uboczny z dwoma wartościami |

### Specjalizacje prymitywne

- Aby uniknąć narzutu autoboxingu, Java dostarcza specjalizowane
  wersje prymitywne dla `int`, `long` i `double`:
  `IntPredicate`, `LongFunction<R>`, `DoubleConsumer`,
  `IntSupplier`, `IntUnaryOperator`, `IntBinaryOperator`,
  `ToIntFunction<T>`, `IntToDoubleFunction`, itp.
- **Zasada ogólna**: podczas pracy z prymitywami w gorących ścieżkach
  (pętle, strumienie), preferuj specjalizacje prymitywne, aby uniknąć
  narzutu boxing/unboxing.
*/

// ============================================================
// Sekcja 5: Referencje do metod
// ============================================================

/*
## Referencje do metod

- **Referencja do metody** to skrót dla lambdy, która po prostu
  wywołuje istniejącą metodę. Używa operatora `::`.
- Cztery rodzaje referencji do metod:

| Rodzaj                 | Składnia                 | Odpowiednik Lambda                         |
|------------------------|--------------------------|--------------------------------------------|
| Metoda statyczna       | `ClassName::staticMethod`| `(args) -> ClassName.staticMethod(args)`   |
| Instancja (powiązana)  | `instance::method`       | `(args) -> instance.method(args)`          |
| Instancja (niepowiązana)| `ClassName::method`     | `(obj, args) -> obj.method(args)`          |
| Konstruktor            | `ClassName::new`         | `(args) -> new ClassName(args)`            |

- Referencje **powiązane** do instancji przechwytują konkretny obiekt:
  `System.out::println` zawsze wypisuje do tego samego `System.out`.
- Referencje **niepowiązane** do instancji przyjmują instancję jako pierwszy
  parametr: `String::toLowerCase` jest odpowiednikiem `(String s) -> s.toLowerCase()`.
- Referencje do konstruktorów działają z interfejsami funkcyjnymi, których
  sygnatura metody abstrakcyjnej pasuje do konstruktora.
- Referencje do metod poprawiają czytelność, gdy ciało lambdy
  jest tylko wywołaniem metody bez dodatkowej logiki.
*/

// ============================================================
// Sekcja 6: Przechwytywanie zmiennych i effectively final
// ============================================================

/*
## Przechwytywanie zmiennych i effectively final

- Lambdy mogą **przechwytywać** (zamykać) zmienne z ich
  otaczającego zakresu — co czyni je **domknięciami** (closures).
- Lambdy mogą swobodnie uzyskiwać dostęp do:
    - **Pól instancji** i **pól statycznych** — mogą być
      odczytywane i modyfikowane bez ograniczeń.
    - **Zmiennych lokalnych** — ale tylko jeśli są **effectively final**.
- Zmienna lokalna jest **effectively final**, jeśli nigdy nie jest ponownie
  przypisywana po inicjalizacji. Nie potrzebuje słowa kluczowego `final` —
  kompilator sprawdza zachowanie.
- **Dlaczego to ograniczenie?** Zmienne lokalne żyją na stosie i są
  niszczone, gdy metoda kończy działanie. Lambda może przeżyć metodę
  (np. przechowywana w polu lub przekazana do innego wątku). Java kopiuje
  wartość zmiennej do domknięcia lambdy. Gdyby zmienna mogła się zmienić
  po skopiowaniu, lambda widziałaby przestarzałą wartość —
  prowadząc do mylących błędów. Wymaganie effectively final temu zapobiega.
- Obejście dla mutowalnego stanu: użyj jednoelementowej tablicy,
  `AtomicInteger` lub mutowalnego obiektu kontenera. Są to typy
  referencyjne — referencja jest effectively final, ale zawartość może
  być zmieniana.
*/

// ============================================================
// Sekcja 7: Lambdy z kolekcjami
// ============================================================

/*
## Lambdy z kolekcjami

Java 8 dodała kilka metod domyślnych do interfejsów kolekcji,
które przyjmują interfejsy funkcyjne, umożliwiając bardziej deklaratywny
styl:

- `Iterable.forEach(Consumer)` — iterowanie i wykonanie akcji
  na każdym elemencie.
- `Collection.removeIf(Predicate)` — usunięcie elementów pasujących
  do warunku (zastępuje pętle usuwania oparte na iteratorze).
- `List.replaceAll(UnaryOperator)` — transformacja każdego elementu
  w miejscu.
- `List.sort(Comparator)` — sortowanie z komparatorem opartym na lambdzie
  (zastępuje `Collections.sort()`).
- `Map.forEach(BiConsumer)` — iterowanie po parach klucz-wartość.
- `Map.computeIfAbsent(key, Function)` — leniwe obliczanie wartości,
  jeśli klucz nie jest obecny.
- `Map.replaceAll(BiFunction)` — transformacja wszystkich wartości w mapie.
- `Map.merge(key, value, BiFunction)` — scalanie wartości z
  istniejącym wpisem.

Te metody dobrze współpracują z lambdami i referencjami do metod,
czyniąc manipulację kolekcjami zwięzłą i czytelną.
*/

// ============================================================
// Sekcja 8: Komponowanie lambd
// ============================================================

/*
## Komponowanie lambd

Interfejsy funkcyjne w `java.util.function` dostarczają **metody
domyślne** do komponowania wielu funkcji w potoki:

### Kompozycja funkcji
- `f.andThen(g)` — najpierw zastosuj `f`, potem zastosuj `g` do wyniku.
  Odpowiednik `g(f(x))`.
- `f.compose(g)` — najpierw zastosuj `g`, potem zastosuj `f` do wyniku.
  Odpowiednik `f(g(x))`. Odwrotna kolejność niż `andThen`.

### Kompozycja predykatów
- `p1.and(p2)` — logiczne AND: oba predykaty muszą być prawdziwe.
- `p1.or(p2)` — logiczne OR: co najmniej jeden predykat musi być prawdziwy.
- `p.negate()` — logiczne NOT: odwraca predykat.

### Kompozycja konsumentów
- `c1.andThen(c2)` — wykonaj `c1`, potem wykonaj `c2` na
  tym samym wejściu.

### Kompozycja komparatorów
- `Comparator.comparing(keyExtractor)` — tworzenie komparatora
  z funkcji ekstrakcji klucza.
- `c.thenComparing(keyExtractor)` — sortowanie wtórne, gdy
  porównanie pierwotne jest równe.
- `c.reversed()` — odwrócenie kolejności.

Kompozycja umożliwia budowanie złożonego zachowania z prostych,
wielokrotnego użytku klocków bez pisania klas niestandardowych.
*/

// ============================================================
// Sekcja 9: Typowe wzorce i najlepsze praktyki
// ============================================================

/*
## Typowe wzorce i najlepsze praktyki

- **Preferuj referencje do metod** zamiast lambd, gdy ciało lambdy
  to pojedyncze wywołanie metody: `list.forEach(System.out::println)`
  jest czytelniejsze niż `list.forEach(x -> System.out.println(x))`.
- **Utrzymuj lambdy krótkie** — jeśli lambda przekracza 2-3 linie, wyodrębnij
  ją do nazwanej metody i użyj referencji do metody.
- **Używaj wbudowanych interfejsów funkcyjnych** z `java.util.function`
  przed definiowaniem własnych.
- **Unikaj efektów ubocznych w lambdach** używanych ze strumieniami — lambdy
  przekazywane do `map`, `filter`, `reduce` powinny być czystymi funkcjami.
  Efekty uboczne należą do `forEach` lub operacji terminalnych.
- **Obsługa wyjątków**: lambdy rzucające wyjątki kontrolowane
  nie mogą być przypisane do standardowych interfejsów funkcyjnych (które
  nie deklarują wyjątków kontrolowanych). Rozwiązania:
    - Opakuj wywołanie w try-catch wewnątrz lambdy.
    - Utwórz niestandardowy interfejs funkcyjny deklarujący wyjątek.
    - Użyj metody narzędziowej, która opakowuje wyjątki kontrolowane w
      niekontrolowane.
- **Nie nadużywaj lambd** — czasami prosta pętla `for` lub
  nazwana klasa jest bardziej czytelna, szczególnie dla złożonej logiki
  lub gdy lambda musi obsługiwać wiele zagadnień.
- **Inferencja typów** działa najlepiej, gdy typ docelowy jest jasny.
  Jeśli kompilator nie może wywnioskować typów, podaj jawne typy
  parametrów lub przypisz lambdę do typowanej zmiennej.
*/

public class LambdaExpressions {

    // Pomocnicze interfejsy funkcyjne dla samodzielnych przykładów

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
    // Sekcja 1: Wprowadzenie do wyrażeń Lambda
    // ============================================================

    static void introductionToLambdas() {
        System.out.println("=== Introduction to Lambda Expressions ===");

        // Przed Java 8: anonimowa klasa wewnętrzna do definiowania zachowania
        Comparator<String> byLengthOldStyle = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.compare(s1.length(), s2.length());
            }
        };

        // Java 8+: wyrażenie lambda — to samo zachowanie, znacznie mniej szablonowego kodu
        Comparator<String> byLengthLambda = (s1, s2) -> Integer.compare(s1.length(), s2.length());

        List<String> words = new ArrayList<>(Arrays.asList("banana", "apple", "fig", "cherry"));

        words.sort(byLengthOldStyle);
        System.out.println("sorted by length (anonymous class): " + words);

        words.sort(byLengthLambda);
        System.out.println("sorted by length (lambda): " + words);

        // Lambdy mogą być przechowywane w zmiennych, przekazywane jako argumenty i zwracane z metod
        Runnable greeting = () -> System.out.println("Hello from a lambda!");
        greeting.run();
    }

    // ============================================================
    // Sekcja 2: Warianty składni Lambda
    // ============================================================

    static void lambdaSyntaxVariants() {
        System.out.println("\n=== Lambda Syntax Variants ===");

        // Pełna składnia z jawnymi typami i ciałem blokowym
        BinaryOperator<Integer> addFull = (Integer a, Integer b) -> {
            int sum = a + b;
            return sum;
        };
        System.out.println("full syntax: 3 + 4 = " + addFull.apply(3, 4));

        // Wnioskowane typy parametrów — kompilator wie z BinaryOperator<Integer>
        BinaryOperator<Integer> addInferred = (a, b) -> a + b;
        System.out.println("inferred types: 3 + 4 = " + addInferred.apply(3, 4));

        // Pojedynczy parametr — nawiasy są opcjonalne
        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        System.out.println("single param: " + shout.apply("hello"));

        // Brak parametrów
        Supplier<String> timestamp = () -> "Current time: " + System.currentTimeMillis();
        System.out.println("no params: " + timestamp.get());

        // Ciało wieloliniowe z jawnym return
        Function<String, Integer> wordCount = text -> {
            if (text == null || text.isBlank()) {
                return 0;
            }
            return text.trim().split("\\s+").length;
        };
        System.out.println("word count of 'hello world': " + wordCount.apply("hello world"));
        System.out.println("word count of '': " + wordCount.apply(""));

        // Ciało z pojedynczym wyrażeniem — bez nawiasów klamrowych, bez słowa kluczowego return
        Function<Double, Double> circleArea = r -> Math.PI * r * r;
        System.out.println("area of circle (r=5): " + String.format("%.2f", circleArea.apply(5.0)));
    }

    // ============================================================
    // Sekcja 3: Interfejsy funkcyjne
    // ============================================================

    static void functionalInterfaces() {
        System.out.println("\n=== Functional Interfaces ===");

        // Użycie niestandardowego @FunctionalInterface zdefiniowanego powyżej
        Converter<String, Integer> stringToInt = Integer::valueOf;
        System.out.println("converter: \"123\" -> " + stringToInt.convert("123"));

        Validator<String> notEmpty = s -> s != null && !s.isEmpty();
        System.out.println("validate \"hello\": " + notEmpty.validate("hello"));
        System.out.println("validate \"\": " + notEmpty.validate(""));

        // Interfejs funkcyjny może mieć metody domyślne
        // Comparator<T> ma jedną metodę abstrakcyjną (compare), ale wiele metod domyślnych
        Comparator<String> caseInsensitive = String::compareToIgnoreCase;
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "alice", "Bob"));
        names.sort(caseInsensitive);
        System.out.println("case-insensitive sort: " + names);

        // Użycie Runnable
        Runnable task = () -> System.out.println("task executed");
        task.run();
    }

    // ============================================================
    // Sekcja 4: Wbudowane interfejsy funkcyjne
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

        // UnaryOperator<T> — T -> T (specjalizacja Function<T, T>)
        UnaryOperator<String> trim = String::trim;
        System.out.println("trimmed: \"" + trim.apply("  spaces  ") + "\"");

        // BinaryOperator<T> — (T, T) -> T (specjalizacja BiFunction<T, T, T>)
        BinaryOperator<Integer> max = Integer::max;
        System.out.println("max(3, 7): " + max.apply(3, 7));

        // BiFunction<T, U, R> — (T, U) -> R
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("repeat(\"ab\", 3): " + repeat.apply("ab", 3));

        // Specjalizacje prymitywne — unikanie autoboxingu
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
    // Sekcja 5: Referencje do metod
    // ============================================================

    static void methodReferences() {
        System.out.println("\n=== Method References ===");

        List<String> words = Arrays.asList("hello", "world", "java", "lambda");

        // Referencja do metody statycznej: ClassName::staticMethod
        // Integer.parseInt jest metodą statyczną: String -> int
        Function<String, Integer> parser = Integer::parseInt;
        System.out.println("parsed \"42\": " + parser.apply("42"));

        // Referencja do metody instancji powiązanej: instance::method
        // System.out jest konkretną instancją; println jest wywoływane na tej instancji
        System.out.println("--- forEach with bound method reference ---");
        words.forEach(System.out::println);

        // Referencja do metody instancji niepowiązanej: ClassName::method
        // Pierwszy parametr staje się odbiorcą: (String s) -> s.toUpperCase()
        Function<String, String> upper = String::toUpperCase;
        System.out.println("unbound ref: " + upper.apply("lambda"));

        // Niepowiązana z Comparator — String::compareToIgnoreCase staje się
        // (s1, s2) -> s1.compareToIgnoreCase(s2)
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "alice", "Bob"));
        names.sort(String::compareToIgnoreCase);
        System.out.println("sorted (unbound method ref): " + names);

        // Referencja do konstruktora: ClassName::new
        // ArrayList::new pasuje do Supplier<ArrayList<String>>
        Supplier<ArrayList<String>> listMaker = ArrayList::new;
        ArrayList<String> newList = listMaker.get();
        newList.add("constructed via ::new");
        System.out.println("constructor ref: " + newList);

        // Referencja do konstruktora z parametrem: String[]::new pasuje do IntFunction<String[]>
        IntFunction<String[]> arrayMaker = String[]::new;
        String[] arr = arrayMaker.apply(5);
        System.out.println("array constructor ref, length: " + arr.length);
    }

    // ============================================================
    // Sekcja 6: Przechwytywanie zmiennych i effectively final
    // ============================================================

    static void variableCaptureAndEffectivelyFinal() {
        System.out.println("\n=== Variable Capture and Effectively Final ===");

        // Przechwytywanie zmiennej lokalnej — musi być effectively final
        String greeting = "Hello";
        // greeting nigdy nie jest ponownie przypisywane, więc jest effectively final
        Consumer<String> greeter = name -> System.out.println(greeting + ", " + name + "!");
        greeter.accept("World");

        // To spowodowałoby błąd kompilacji:
        // greeting = "Hi"; // BŁĄD: zmienna lokalna używana w lambdzie musi być effectively final

        // Przechwytywanie pól instancji/statycznych — brak ograniczeń mutowalności
        // (demonstracja z mutowalną tablicą jako obejście dla zmiennych lokalnych)
        int[] counter = {0}; // jednoelementowa tablica — referencja jest effectively final
        Runnable incrementer = () -> counter[0]++;
        incrementer.run();
        incrementer.run();
        incrementer.run();
        System.out.println("counter via array workaround: " + counter[0]);

        // Effectively final — słowo kluczowe 'final' jest opcjonalne
        final String explicit = "explicitly final";
        String implicit = "effectively final"; // nigdy nie jest ponownie przypisywane — to samo zachowanie
        Consumer<Void> demo = v -> {
            System.out.println(explicit);
            System.out.println(implicit);
        };
        demo.accept(null);

        // Typowe obejście: użycie mutowalnego kontenera
        List<String> captured = new ArrayList<>();
        Runnable collector = () -> captured.add("item " + captured.size());
        collector.run();
        collector.run();
        System.out.println("mutable container: " + captured);
    }

    // ============================================================
    // Sekcja 7: Lambdy z kolekcjami
    // ============================================================

    static void lambdasWithCollections() {
        System.out.println("\n=== Lambdas with Collections ===");

        // forEach — iterowanie z Consumer
        List<String> fruits = new ArrayList<>(Arrays.asList("apple", "banana", "cherry", "date", "elderberry"));
        System.out.print("forEach: ");
        fruits.forEach(f -> System.out.print(f + " "));
        System.out.println();

        // removeIf — usuwanie elementów pasujących do Predicate
        fruits.removeIf(f -> f.length() > 5);
        System.out.println("after removeIf (length > 5): " + fruits);

        // replaceAll — transformacja każdego elementu z UnaryOperator
        fruits.replaceAll(String::toUpperCase);
        System.out.println("after replaceAll (toUpperCase): " + fruits);

        // sort — sortowanie z lambdą Comparator
        List<String> names = new ArrayList<>(Arrays.asList("Charlie", "Alice", "Bob", "Dave"));
        names.sort((a, b) -> Integer.compare(a.length(), b.length()));
        System.out.println("sorted by length: " + names);

        // Comparator.comparing — czystszy sposób tworzenia komparatorów
        names.sort(Comparator.comparing(String::length));
        System.out.println("sorted by length (Comparator.comparing): " + names);

        // Map.forEach — iterowanie po parach klucz-wartość z BiConsumer
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Alice", 95);
        scores.put("Bob", 87);
        scores.put("Charlie", 92);
        System.out.print("map forEach: ");
        scores.forEach((name, score) -> System.out.print(name + "=" + score + " "));
        System.out.println();

        // Map.computeIfAbsent — leniwe obliczanie wartości
        Map<String, List<String>> groups = new HashMap<>();
        groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
        groups.computeIfAbsent("fruits", k -> new ArrayList<>()).add("banana");
        groups.computeIfAbsent("vegs", k -> new ArrayList<>()).add("carrot");
        System.out.println("computeIfAbsent groups: " + groups);

        // Map.replaceAll — transformacja wszystkich wartości z BiFunction
        Map<String, Integer> prices = new HashMap<>();
        prices.put("coffee", 3);
        prices.put("tea", 2);
        prices.replaceAll((item, price) -> price * 2);
        System.out.println("prices after replaceAll (*2): " + prices);

        // Map.merge — scalanie z istniejącym wpisem
        Map<String, Integer> wordCounts = new HashMap<>();
        for (String word : "the cat sat on the mat".split(" ")) {
            wordCounts.merge(word, 1, Integer::sum);
        }
        System.out.println("word counts via merge: " + wordCounts);
    }

    // ============================================================
    // Sekcja 8: Komponowanie lambd
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
        // compose: najpierw addThree, potem doubleIt => (5 + 3) * 2 = 16
        System.out.println("compose (double after addThree): " + doubleIt.compose(addThree).apply(5));
        // andThen: najpierw doubleIt, potem addThree => (5 * 2) + 3 = 13
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

        // Consumer.andThen — łączenie efektów ubocznych
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
    // Sekcja 9: Typowe wzorce i najlepsze praktyki
    // ============================================================

    // Metoda narzędziowa: opakowuje rzucającą funkcję w zwykłą Function
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

        // Preferuj referencje do metod, gdy lambda jest prostą delegacją
        List<String> items = Arrays.asList("one", "two", "three");
        // Mniej czytelne:
        items.forEach(item -> System.out.println(item));
        // Bardziej czytelne:
        items.forEach(System.out::println);

        // Wzorzec strategii — przekazywanie różnych zachowań jako lambd
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("evens: " + filter(numbers, n -> n % 2 == 0));
        System.out.println("odds: " + filter(numbers, n -> n % 2 != 0));
        System.out.println("> 5: " + filter(numbers, n -> n > 5));

        // Obsługa wyjątków w lambdach — wyjątki kontrolowane wymagają opakowywania
        // Standardowy Function<T, R> nie zezwala na wyjątki kontrolowane
        List<String> numberStrings = Arrays.asList("1", "2", "three", "4");

        // Użycie narzędzia unchecked() do obsługi wyjątków
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

        // Lambda jako fabryka — Supplier do odroczonego/leniwego tworzenia
        Map<String, Supplier<List<String>>> factories = new HashMap<>();
        factories.put("array", ArrayList::new);
        factories.put("linked", LinkedList::new);
        List<String> list = factories.get("linked").get();
        list.add("created lazily");
        System.out.println("factory pattern: " + list + " (" + list.getClass().getSimpleName() + ")");

        // Wzorzec execute-around — enkapsulacja logiki konfiguracji/czyszczenia
        String result = withTiming("slow operation", () -> {
            // Symulacja pracy
            return "computed result";
        });
        System.out.println("execute-around result: " + result);
    }

    // Pomocnik: filtrowanie listy za pomocą Predicate (wzorzec strategii)
    static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }
        return result;
    }

    // Pomocnik: wzorzec execute-around — opakowuje Supplier pomiarem czasu
    static <T> T withTiming(String label, Supplier<T> action) {
        long start = System.nanoTime();
        T result = action.get();
        long elapsed = System.nanoTime() - start;
        System.out.printf("  [%s took %.3f ms]%n", label, elapsed / 1_000_000.0);
        return result;
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
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
