package pl.training.jmodern.module02_java8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do strumieni
// ============================================================

/*
## Wprowadzenie do strumieni

- **Stream** to sekwencja elementów wspierająca deklaratywne,
  funkcyjne operacje do przetwarzania danych. Wprowadzony w
  Java 8, strumienie fundamentalnie zmieniają sposób pracy z kolekcjami.
- **Strumienie vs kolekcje**:
    - Kolekcje to **struktury danych w pamięci** — przechowują
      elementy. Strumienie **nie przechowują** elementów; przenoszą wartości
      ze źródła przez potok operacji.
    - Kolekcje są **zachłannie wypełniane**. Strumienie są **leniwe** —
      operacje pośrednie nie są wykonywane, dopóki nie zostanie
      wywołana operacja terminalna.
    - Kolekcje mogą być iterowane wielokrotnie. Strumienie są
      **jednorazowe** — po konsumpcji przez operację terminalną
      strumień jest wyczerpany i nie może być ponownie użyty.
- **Potok strumieniowy** składa się z trzech części:
    1. **Źródło** — skąd pochodzą dane (kolekcja, tablica, generator, I/O).
    2. **Operacje pośrednie** — przekształcają strumień w inny
       strumień (`filter`, `map`, `sorted`, itp.). Są **leniwe** i
       zwracają nowy strumień.
    3. **Operacja terminalna** — produkuje wynik lub efekt uboczny
       (`collect`, `forEach`, `reduce`, itp.). To wyzwala
       faktyczne przetwarzanie potoku.
- **Iteracja wewnętrzna vs zewnętrzna**:
    - Iteracja zewnętrzna: programista kontroluje pętlę
      (`for`, `while`, `Iterator`).
    - Iteracja wewnętrzna: biblioteka strumieni obsługuje iterację;
      programista deklaruje *co* zrobić, nie *jak* iterować.
      To pozwala bibliotece optymalizować (równoległość, zwarcie,
      fuzja pętli).
*/

// ============================================================
// Sekcja 2: Tworzenie strumieni
// ============================================================

/*
## Tworzenie strumieni

Istnieje wiele sposobów uzyskania strumienia:

- **Z kolekcji**: `collection.stream()` — najczęstszy sposób.
- **Z wartości**: `Stream.of("a", "b", "c")` — tworzy strumień
  z jawnych wartości.
- **Z tablicy**: `Arrays.stream(array)` — strumieniuje tablicę lub
  wycinek: `Arrays.stream(array, startInclusive, endExclusive)`.
- **Pusty strumień**: `Stream.empty()` — przydatny jako domyślny lub w
  logice warunkowej.
- **Generowany (nieskończony)**: `Stream.generate(supplier)` — tworzy
  nieskończony strumień przez wielokrotne wywoływanie `Supplier`. Musi być
  ograniczony za pomocą `limit()`.
- **Iterowany (nieskończony)**: `Stream.iterate(seed, unaryOperator)` —
  tworzy nieskończony strumień: `seed, f(seed), f(f(seed)), ...`.
  Java 9 dodała `Stream.iterate(seed, predicate, operator)` z
  wbudowanym warunkiem zakończenia (podobnym do pętli for).
- **Zakresy prymitywne**: `IntStream.range(0, 10)` (koniec wyłączny),
  `IntStream.rangeClosed(1, 10)` (koniec włączny).
- **Z łańcucha znaków**: `"hello".chars()` zwraca `IntStream` z
  wartościami znaków.
- **Z nullable** (Java 9): `Stream.ofNullable(value)` — zwraca
  jednoelementowy strumień lub pusty strumień, jeśli wartość jest null.
- **Z plików**: `Files.lines(path)` — leniwie odczytuje linie z
  pliku jako `Stream<String>`. Powinien być używany z try-with-resources,
  ponieważ strumień trzyma otwarty uchwyt pliku.
*/

// ============================================================
// Sekcja 3: Operacje pośrednie
// ============================================================

/*
## Operacje pośrednie

Operacje pośrednie są **leniwe** — nie przetwarzają elementów,
dopóki nie zostanie wywołana operacja terminalna. Zwracają nowy strumień
i mogą być łączone w łańcuchy.

- `filter(Predicate)` — zachowuje tylko elementy pasujące do predykatu.
- `map(Function)` — przekształca każdy element w inną wartość.
- `flatMap(Function)` — przekształca każdy element w strumień i
  spłaszcza wszystkie wynikowe strumienie w jeden.
- `distinct()` — usuwa duplikaty (używa `equals()`).
- `sorted()` — sortuje w porządku naturalnym; `sorted(Comparator)` dla
  niestandardowej kolejności.
- `peek(Consumer)` — wykonuje akcję na każdym elemencie bez
  konsumowania strumienia. Przydatne do debugowania.
- `limit(n)` — skraca strumień do co najwyżej `n` elementów.
  Operacja **zwierająca**.
- `skip(n)` — pomija pierwszych `n` elementów.
- `mapToInt(ToIntFunction)` / `mapToLong` / `mapToDouble` —
  konwertuje na strumień prymitywny, aby uniknąć boxingu.
- `takeWhile(Predicate)` (Java 9) — pobiera elementy dopóki
  predykat jest prawdziwy, potem zatrzymuje się.
- `dropWhile(Predicate)` (Java 9) — pomija elementy dopóki
  predykat jest prawdziwy, potem przepuszcza resztę.

### Demonstracja lenistwa
Żadna operacja pośrednia nie wykonuje się, dopóki nie zostanie wywołana
operacja terminalna. To umożliwia również **fuzję pętli** — JVM może scalić
wiele operacji w jedno przejście po danych.
*/

// ============================================================
// Sekcja 4: Operacje terminalne
// ============================================================

/*
## Operacje terminalne

Operacje terminalne wyzwalają przetwarzanie potoku strumieniowego
i produkują wynik lub efekt uboczny. Po operacji terminalnej
strumień jest skonsumowany i nie może być ponownie użyty.

- `forEach(Consumer)` — wykonuje akcję na każdym elemencie.
  **Nie** gwarantuje kolejności w strumieniach równoległych.
- `forEachOrdered(Consumer)` — jak `forEach`, ale zachowuje
  kolejność napotkania nawet w strumieniach równoległych.
- `collect(Collector)` — najbardziej wszechstronna operacja terminalna;
  akumuluje elementy do mutowalnego kontenera (List, Set, Map,
  String, itp.) za pomocą `Collector`.
- `toList()` (Java 16) — skrót do zbierania w niemodyfikowalną
  `List`. Odpowiednik `collect(Collectors.toUnmodifiableList())`.
- `reduce(identity, accumulator)` — łączy wszystkie elementy w
  pojedynczą wartość za pomocą asocjacyjnej funkcji akumulującej.
- `count()` — zwraca liczbę elementów.
- `min(Comparator)` / `max(Comparator)` — zwraca minimum
  lub maksimum opakowane w `Optional`.
- `findFirst()` — zwraca pierwszy element jako `Optional`.
  Przydatne z `filter` dla wzorców "znajdź pierwszy pasujący".
- `findAny()` — zwraca dowolny element (niedeterministyczne w równoległym).
- `anyMatch(Predicate)` — zwraca `true`, jeśli jakikolwiek element pasuje.
  **Zwierające**.
- `allMatch(Predicate)` — zwraca `true`, jeśli wszystkie elementy pasują.
- `noneMatch(Predicate)` — zwraca `true`, jeśli żaden element nie pasuje.
- `toArray()` — zbiera elementy do tablicy.
  `toArray(String[]::new)` produkuje typowaną tablicę.
*/

// ============================================================
// Sekcja 5: Kolektory
// ============================================================

/*
## Kolektory

Klasa narzędziowa `Collectors` dostarcza bogaty zestaw predefiniowanych
kolektorów do użycia z `stream.collect()`:

- `toList()` — zbiera do `ArrayList`.
- `toSet()` — zbiera do `HashSet`.
- `toMap(keyMapper, valueMapper)` — zbiera do `HashMap`.
  Funkcja scalania może obsługiwać zduplikowane klucze:
  `toMap(k, v, (v1, v2) -> v1)`.
- `toUnmodifiableList()` / `toUnmodifiableSet()` / `toUnmodifiableMap()`
  (Java 10) — niemodyfikowalne warianty.
- `joining()` / `joining(delimiter)` / `joining(delimiter, prefix, suffix)`
  — łączy elementy `CharSequence` w pojedynczy `String`.
- `groupingBy(classifier)` — grupuje elementy w `Map<K, List<V>>`.
  Wspiera kolektor podrzędny:
  `groupingBy(classifier, downstream)`.
- `partitioningBy(predicate)` — szczególny przypadek grupowania z
  kluczem boolowskim: `Map<Boolean, List<V>>`.
- `counting()` — zlicza elementy (używany jako kolektor podrzędny).
- `summarizingInt(mapper)` — produkuje `IntSummaryStatistics`
  (liczba, suma, min, max, średnia).
- `mapping(mapper, downstream)` — stosuje funkcję mapującą
  przed zbieraniem kolektorem podrzędnym.
- `reducing(identity, accumulator)` — wykonuje redukcję jako
  kolektor (przydatne jako kolektor podrzędny).
- `collectingAndThen(downstream, finisher)` — stosuje końcową
  transformację po zebraniu: np. opakowywanie listy w
  `Collections.unmodifiableList()`.
*/

// ============================================================
// Sekcja 6: Operacje redukcji
// ============================================================

/*
## Operacje redukcji

**Redukcja** łączy wszystkie elementy strumienia w pojedynczy
wynik przez wielokrotne stosowanie funkcji łączącej.

- `reduce(BinaryOperator)` — zwraca `Optional<T>`. Brak wartości
  tożsamościowej; wynik jest pusty, jeśli strumień jest pusty.
- `reduce(identity, BinaryOperator)` — zwraca `T`. Wartość
  tożsamościowa jest zwracana dla pustych strumieni i służy jako początkowa
  wartość akumulatora. Tożsamość musi być prawdziwą tożsamością dla
  operatora: `op(identity, x) == x`.
- `reduce(identity, BiFunction accumulator, BinaryOperator combiner)`
  — trzecia forma używana, gdy typ wyniku różni się od
  typu elementu, lub dla strumieni równoległych. Combiner scala
  częściowe wyniki z równoległego wykonania.

### `reduce` vs `collect`
- `reduce` produkuje **niezmienny** wynik — każdy krok tworzy
  nową wartość. Najlepsze dla operacji takich jak suma, iloczyn, min, max.
- `collect` używa **mutowalnego akumulatora** (jak `List` lub
  `StringBuilder`). Bardziej wydajne do budowania kolekcji, ponieważ
  unika tworzenia obiektów pośrednich.
*/

// ============================================================
// Sekcja 7: Strumienie prymitywne
// ============================================================

/*
## Strumienie prymitywne

Java dostarcza trzy specjalizacje strumieni prymitywnych, aby uniknąć
kosztu boxing/unboxing:

- `IntStream` — dla wartości `int`.
- `LongStream` — dla wartości `long`.
- `DoubleStream` — dla wartości `double`.

### Dodatkowe metody niedostępne w `Stream<T>`
- `sum()` — zwraca sumę wszystkich elementów.
- `average()` — zwraca `OptionalDouble` ze średnią.
- `summaryStatistics()` — zwraca liczbę, sumę, min, max, średnią
  w jednym przejściu (`IntSummaryStatistics`, itp.).
- `range(start, end)` / `rangeClosed(start, end)` — generuje
  sekwencyjne wartości int/long.

### Konwersja
- `mapToInt(ToIntFunction)` — konwertuje `Stream<T>` na `IntStream`.
- `boxed()` — konwertuje `IntStream` z powrotem na `Stream<Integer>`.
- `mapToObj(IntFunction)` — mapuje każdy int na obiekt.
- `asLongStream()` / `asDoubleStream()` — konwersje rozszerzające.
*/

// ============================================================
// Sekcja 8: FlatMap i spłaszczanie strumieni
// ============================================================

/*
## FlatMap i spłaszczanie strumieni

`flatMap` jest używany, gdy każdy element mapuje się na **wiele** wartości
(tj. strumień). Spłaszcza wynikowe strumienie w jeden.

- **Wzorzec**: `stream.flatMap(element -> element.getSubElements().stream())`
- Typowe przypadki użycia:
    - Spłaszczanie listy list: `listOfLists.stream().flatMap(Collection::stream)`
    - Dzielenie łańcuchów: `words.stream().flatMap(w -> Arrays.stream(w.split("")))`
    - Łączenie z Optional: `Optional.stream()` (Java 9) konwertuje
      `Optional<T>` na `Stream<T>` (0 lub 1 element), umożliwiając
      `flatMap(opt -> opt.stream())` do odfiltrowywania pustych Optional.
- `flatMapToInt`, `flatMapToLong`, `flatMapToDouble` — warianty
  prymitywne do produkcji strumieni prymitywnych.
*/

// ============================================================
// Sekcja 9: Strumienie równoległe
// ============================================================

/*
## Strumienie równoległe

Strumienie równoległe dzielą dane na wiele fragmentów i przetwarzają
je współbieżnie za pomocą **ForkJoinPool**.

- **Tworzenie**: `collection.parallelStream()` lub
  `stream.parallel()`.
- **Kiedy używać**:
    - Duże zbiory danych (tysiące+ elementów).
    - Operacje intensywne obliczeniowo na element.
    - Operacje łatwe do zrównoleglenia (bezstanowe,
      bez współdzielonego mutowalnego stanu, asocjacyjna redukcja).
- **Kiedy unikać**:
    - Małe zbiory danych — narzut zrównoleglenia przewyższa korzyść.
    - Operacje ograniczone I/O — wątki blokują, marnując zasoby puli.
    - Operacje z efektami ubocznymi lub współdzielonym mutowalnym stanem.
    - Gdy kolejność napotkania ma znaczenie i nie można użyć `forEachOrdered`.
    - `LinkedList` lub inne źródła ze słabą podzielalnością.
- **Bezpieczeństwo wątkowe**: operacje muszą być bezstanowe i
  bezpieczne wątkowo. Akumulator i combiner w `reduce`/`collect`
  muszą być asocjacyjne.
- **Kolejność**: `forEach` nie zachowuje kolejności w równoległym;
  użyj `forEachOrdered`, jeśli kolejność ma znaczenie (kosztem wydajności).
- **Niestandardowy ForkJoinPool**: domyślnie strumienie równoległe używają
  wspólnego `ForkJoinPool`. Aby kontrolować równoległość, prześlij
  operację strumieniową do niestandardowej puli:
  `new ForkJoinPool(n).submit(() -> stream.parallel()....).join()`.
- **Wskazówka wydajnościowa**: mierz przed zrównolegleniem. Używaj
  benchmarków (JMH), aby zweryfikować, że równoległe jest rzeczywiście szybsze.
*/

// ============================================================
// Sekcja 10: Typowe wzorce i najlepsze praktyki
// ============================================================

/*
## Typowe wzorce i najlepsze praktyki

- **Strumienie vs pętle**: strumienie są bardziej czytelne dla potoków
  transformacji danych. Pętle mogą być jaśniejsze dla prostych
  iteracji, mutacji lub gdy potrzebujesz `break`/`continue`.
- **Unikaj efektów ubocznych** w operacjach pośrednich. `map`,
  `filter`, `flatMap` powinny być **czystymi funkcjami**. Efekty uboczne
  należą do `forEach` lub powinny być enkapsulowane w `Collector`.
- **Nie używaj ponownie strumieni**: strumień może być skonsumowany tylko raz.
  Jeśli musisz przetworzyć te same dane dwukrotnie, utwórz nowy strumień
  ze źródła.
- **Preferuj referencje do metod**, gdy lambda jest prostą
  delegacją: `map(String::toUpperCase)` zamiast `map(s -> s.toUpperCase())`.
- **Nieskończone strumienie**: zawsze paruj `Stream.generate()` lub
  `Stream.iterate()` z `limit()`, aby zapobiec nieskończonym pętlom.
- **Preferuj `collect` zamiast `reduce`** dla mutowalnej akumulacji
  (budowanie list, łańcuchów, map). `reduce` z mutowalnymi obiektami
  jest niepoprawne i może dać błędne wyniki w strumieniach równoległych.
- **Używaj strumieni prymitywnych** (`IntStream`, `LongStream`, `DoubleStream`)
  podczas pracy z danymi numerycznymi, aby uniknąć narzutu autoboxingu.
- **`Optional` w strumieniach**: używaj `findFirst()`, `findAny()`, `min()`,
  `max()`, które zwracają `Optional`. Łącz z `orElse()`,
  `orElseThrow()`, `ifPresent()` dla czystej obsługi.
- **Debugowanie**: używaj `peek()` do inspekcji elementów w dowolnym punkcie
  potoku bez wpływu na wynik.
*/

public class Streams {

    // ---- Klasy pomocnicze dla samodzielnych przykładów ----

    record Person(String name, int age, String city) {}

    record Order(String customer, List<String> items, double total) {}

    // ============================================================
    // Sekcja 1: Wprowadzenie do strumieni
    // ============================================================

    static void introductionToStreams() {
        System.out.println("=== Introduction to Streams ===");

        List<String> names = Arrays.asList("Charlie", "Alice", "Bob", "Dave", "Eve");

        // Iteracja zewnętrzna — programista kontroluje pętlę
        System.out.print("external iteration: ");
        for (String name : names) {
            if (name.length() <= 4) {
                System.out.print(name.toUpperCase() + " ");
            }
        }
        System.out.println();

        // Iteracja wewnętrzna — biblioteka strumieni obsługuje pętlę
        System.out.print("internal iteration (stream): ");
        names.stream()
                .filter(name -> name.length() <= 4)
                .map(String::toUpperCase)
                .forEach(name -> System.out.print(name + " "));
        System.out.println();

        // Strumienie są leniwe — operacje pośrednie nie wykonują się, dopóki nie ma operacji terminalnej
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
                .toList(); // operacja terminalna wyzwala przetwarzanie
        System.out.println("result: " + result);

        // Strumienie są jednorazowe — konsumpcja strumienia dwukrotnie rzuca IllegalStateException
        var stream = names.stream();
        stream.forEach(n -> {}); // pierwsza konsumpcja
        try {
            stream.forEach(n -> {}); // druga konsumpcja — rzuca!
        } catch (IllegalStateException e) {
            System.out.println("stream reuse error: " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 2: Tworzenie strumieni
    // ============================================================

    static void creatingStreams() {
        System.out.println("\n=== Creating Streams ===");

        // Z kolekcji
        List<String> fruits = List.of("apple", "banana", "cherry");
        long count = fruits.stream().count();
        System.out.println("from collection: " + count + " elements");

        // Stream.of — z jawnych wartości
        Stream<String> streamOf = Stream.of("one", "two", "three");
        System.out.println("Stream.of: " + streamOf.toList());

        // Arrays.stream — z tablicy
        int[] numbers = {10, 20, 30, 40, 50};
        int sum = Arrays.stream(numbers).sum();
        System.out.println("Arrays.stream sum: " + sum);

        // Arrays.stream z zakresem (wycinek tablicy)
        int sliceSum = Arrays.stream(numbers, 1, 4).sum(); // indeksy 1,2,3
        System.out.println("array slice [1,4) sum: " + sliceSum);

        // Stream.empty
        Stream<String> empty = Stream.empty();
        System.out.println("empty stream count: " + empty.count());

        // Stream.generate — nieskończony strumień z Supplier
        List<Double> randoms = Stream.generate(Math::random)
                .limit(5)
                .toList();
        System.out.println("generated randoms: " + randoms.stream()
                .map(d -> String.format("%.2f", d))
                .collect(Collectors.joining(", ")));

        // Stream.iterate — nieskończony strumień z ziarnem i operatorem unarnym
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2)
                .limit(10)
                .toList();
        System.out.println("powers of 2: " + powersOfTwo);

        // Stream.iterate z predykatem (Java 9) — wbudowane zakończenie
        List<Integer> countdown = Stream.iterate(10, n -> n > 0, n -> n - 1)
                .toList();
        System.out.println("countdown (iterate with predicate): " + countdown);

        // IntStream.range / rangeClosed
        int rangeSum = IntStream.range(1, 6).sum(); // 1+2+3+4+5
        int rangeClosedSum = IntStream.rangeClosed(1, 5).sum(); // to samo
        System.out.println("range(1,6) sum: " + rangeSum + ", rangeClosed(1,5) sum: " + rangeClosedSum);

        // Strumień z łańcucha znaków — chars() zwraca IntStream
        String text = "Hello";
        System.out.print("chars of \"" + text + "\": ");
        text.chars()
                .mapToObj(c -> (char) c + " ")
                .forEach(System.out::print);
        System.out.println();

        // Stream.ofNullable (Java 9) — 0 lub 1 element
        Stream<String> nonNull = Stream.ofNullable("value");
        Stream<String> fromNull = Stream.ofNullable(null);
        System.out.println("ofNullable(\"value\"): " + nonNull.count()
                + ", ofNullable(null): " + fromNull.count());

        // Files.lines — odczytywanie linii z pliku (demonstracja z plikiem tymczasowym)
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
    // Sekcja 3: Operacje pośrednie
    // ============================================================

    static void intermediateOperations() {
        System.out.println("\n=== Intermediate Operations ===");

        List<String> words = List.of("banana", "apple", "cherry", "avocado", "banana", "date", "apple");

        // filter — zachowaj elementy pasujące do predykatu
        List<String> startsWithA = words.stream()
                .filter(w -> w.startsWith("a"))
                .toList();
        System.out.println("filter (starts with 'a'): " + startsWithA);

        // map — przekształć każdy element
        List<Integer> lengths = words.stream()
                .map(String::length)
                .toList();
        System.out.println("map (lengths): " + lengths);

        // distinct — usuń duplikaty
        List<String> unique = words.stream()
                .distinct()
                .toList();
        System.out.println("distinct: " + unique);

        // sorted — porządek naturalny
        List<String> sorted = words.stream()
                .sorted()
                .toList();
        System.out.println("sorted: " + sorted);

        // sorted z Comparator
        List<String> sortedByLength = words.stream()
                .sorted(Comparator.comparingInt(String::length))
                .toList();
        System.out.println("sorted by length: " + sortedByLength);

        // peek — inspekcja elementów bez konsumowania (przydatne do debugowania)
        System.out.print("peek demo: ");
        long longWordCount = words.stream()
                .filter(w -> w.length() > 4)
                .peek(w -> System.out.print("[" + w + "] "))
                .distinct()
                .count();
        System.out.println("-> " + longWordCount + " distinct long words");

        // limit i skip — skrócenie / pominięcie elementów
        List<String> limited = words.stream().limit(3).toList();
        List<String> skipped = words.stream().skip(4).toList();
        System.out.println("limit(3): " + limited);
        System.out.println("skip(4): " + skipped);

        // skip + limit do paginacji
        List<String> page = words.stream().skip(2).limit(3).toList();
        System.out.println("skip(2).limit(3) (page): " + page);

        // mapToInt — konwersja na IntStream dla operacji numerycznych
        int totalLength = words.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("mapToInt total length: " + totalLength);

        // Lenistwo: operacje pośrednie nie są wykonywane bez operacji terminalnej
        System.out.println("--- laziness: no output without terminal op ---");
        words.stream()
                .filter(w -> {
                    System.out.println("  THIS SHOULD NOT PRINT");
                    return true;
                });
        // ^^^ brak operacji terminalnej — lambda filter NIGDY nie jest wywoływana
        System.out.println("  (nothing printed — stream was never consumed)");

        // takeWhile (Java 9) — pobieraj elementy dopóki predykat jest prawdziwy
        List<Integer> nums = List.of(2, 4, 6, 7, 8, 10);
        List<Integer> takeWhileEven = nums.stream()
                .takeWhile(n -> n % 2 == 0)
                .toList();
        System.out.println("takeWhile(even): " + takeWhileEven);

        // dropWhile (Java 9) — pomijaj elementy dopóki predykat jest prawdziwy
        List<Integer> dropWhileEven = nums.stream()
                .dropWhile(n -> n % 2 == 0)
                .toList();
        System.out.println("dropWhile(even): " + dropWhileEven);
    }

    // ============================================================
    // Sekcja 4: Operacje terminalne
    // ============================================================

    static void terminalOperations() {
        System.out.println("\n=== Terminal Operations ===");

        List<String> names = List.of("Alice", "Bob", "Charlie", "Dave", "Eve");

        // forEach — wykonaj akcję na każdym elemencie
        System.out.print("forEach: ");
        names.stream().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // collect — akumuluj do kolekcji
        List<String> upperNames = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("collect toList: " + upperNames);

        // toList() (Java 16) — skrót dla niemodyfikowalnej listy
        List<String> immutableList = names.stream()
                .filter(n -> n.length() > 3)
                .toList();
        System.out.println("toList() (Java 16): " + immutableList);

        // reduce — łączenie elementów w pojedynczą wartość
        Optional<String> concatenated = names.stream()
                .reduce((a, b) -> a + ", " + b);
        System.out.println("reduce (concatenate): " + concatenated.orElse(""));

        // reduce z tożsamością
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
    // Sekcja 5: Kolektory
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

        // toMap z funkcją scalania (obsługa zduplikowanych kluczy)
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

        // groupingBy — grupowanie po mieście
        Map<String, List<Person>> byCity = people.stream()
                .collect(Collectors.groupingBy(Person::city));
        System.out.println("groupingBy city:");
        byCity.forEach((city, persons) -> System.out.println("  " + city + ": "
                + persons.stream().map(Person::name).collect(Collectors.joining(", "))));

        // groupingBy z kolektorem podrzędnym — zliczanie na miasto
        Map<String, Long> countByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
        System.out.println("groupingBy + counting: " + countByCity);

        // groupingBy z kolektorem podrzędnym — nazwy na miasto
        Map<String, List<String>> namesByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.mapping(Person::name, Collectors.toList())));
        System.out.println("groupingBy + mapping: " + namesByCity);

        // partitioningBy — podział na dwie grupy według predykatu
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

        // reducing jako kolektor podrzędny
        Map<String, Optional<Person>> oldestByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.reducing((p1, p2) -> p1.age() >= p2.age() ? p1 : p2)));
        System.out.println("groupingBy + reducing (oldest per city):");
        oldestByCity.forEach((city, person) ->
                System.out.println("  " + city + ": " + person.map(Person::name).orElse("none")));

        // collectingAndThen — zastosowanie końcowej transformacji
        List<String> unmodifiableNames = people.stream()
                .map(Person::name)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        Collections::unmodifiableList));
        System.out.println("collectingAndThen (unmodifiable): " + unmodifiableNames);
    }

    // ============================================================
    // Sekcja 6: Operacje redukcji
    // ============================================================

    static void reductionOperations() {
        System.out.println("\n=== Reduction Operations ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // reduce bez tożsamości — zwraca Optional
        Optional<Integer> sum = numbers.stream()
                .reduce(Integer::sum);
        System.out.println("reduce (sum, no identity): " + sum.orElse(0));

        // reduce z tożsamością — zwraca T bezpośrednio
        int sumWithIdentity = numbers.stream()
                .reduce(0, Integer::sum);
        System.out.println("reduce (sum, identity=0): " + sumWithIdentity);

        // reduce — iloczyn
        int product = numbers.stream()
                .reduce(1, (a, b) -> a * b);
        System.out.println("reduce (product): " + product);

        // reduce — ręczne znajdowanie max
        Optional<Integer> max = numbers.stream()
                .reduce(Integer::max);
        System.out.println("reduce (max): " + max.orElse(0));

        // reduce — konkatenacja łańcuchów
        List<String> words = List.of("Stream", "API", "is", "powerful");
        String sentence = words.stream()
                .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b);
        System.out.println("reduce (sentence): " + sentence);

        // Trójargumentowy reduce — gdy typ wyniku różni się od typu elementu
        // tożsamość, akumulator (U, T) -> U, combiner (U, U) -> U
        int totalLength = words.stream()
                .reduce(0, (len, word) -> len + word.length(), Integer::sum);
        System.out.println("3-arg reduce (total length): " + totalLength);

        // Demonstracja reduce vs collect do budowania listy
        // ŹLE — reduce z mutowalnym akumulatorem (uszkodzone w równoległym!)
        // POPRAWNIE — użyj collect
        List<String> collected = words.stream()
                .filter(w -> w.length() > 2)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        System.out.println("collect (3-arg, manual): " + collected);
    }

    // ============================================================
    // Sekcja 7: Strumienie prymitywne
    // ============================================================

    static void primitiveStreams() {
        System.out.println("\n=== Primitive Streams ===");

        // IntStream.range i rangeClosed
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

        // summaryStatistics — liczba, suma, min, max, średnia w jednym przejściu
        IntSummaryStatistics stats = IntStream.of(5, 10, 15, 20, 25)
                .summaryStatistics();
        System.out.println("summaryStatistics: " + stats);

        // Konwersja Stream<T> na IntStream przez mapToInt
        List<String> words = List.of("hello", "world", "java", "streams");
        int totalLength = words.stream()
                .mapToInt(String::length)
                .sum();
        System.out.println("mapToInt (total length): " + totalLength);

        // boxed() — konwersja IntStream na Stream<Integer>
        List<Integer> boxedList = IntStream.rangeClosed(1, 5)
                .boxed()
                .toList();
        System.out.println("boxed(): " + boxedList);

        // mapToObj — konwersja każdego int na obiekt
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
    // Sekcja 8: FlatMap i spłaszczanie strumieni
    // ============================================================

    static void flatMapAndFlattening() {
        System.out.println("\n=== FlatMap and Stream Flattening ===");

        // Spłaszczanie listy list
        List<List<String>> nested = List.of(
                List.of("a", "b", "c"),
                List.of("d", "e"),
                List.of("f", "g", "h", "i")
        );
        List<String> flat = nested.stream()
                .flatMap(Collection::stream)
                .toList();
        System.out.println("flatMap (nested lists): " + flat);

        // FlatMap z zamówieniami — pobierz wszystkie elementy ze wszystkich zamówień
        List<Order> orders = List.of(
                new Order("Alice", List.of("laptop", "mouse"), 1200.0),
                new Order("Bob", List.of("keyboard", "monitor", "webcam"), 800.0),
                new Order("Charlie", List.of("headphones"), 150.0)
        );

        List<String> allItems = orders.stream()
                .flatMap(order -> order.items().stream())
                .toList();
        System.out.println("flatMap (all items): " + allItems);

        // FlatMap — podziel słowa na znaki
        List<String> words = List.of("hello", "world");
        List<String> chars = words.stream()
                .flatMap(word -> word.chars().mapToObj(c -> String.valueOf((char) c)))
                .toList();
        System.out.println("flatMap (chars): " + chars);

        // FlatMap z Optional.stream() (Java 9) — odfiltrowanie pustych Optional
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

        // Praktyczny wzorzec: map który może wyprodukować null, połączony z ofNullable + flatMap
        Map<String, String> config = Map.of("host", "localhost", "port", "8080");
        List<String> keys = List.of("host", "port", "timeout");
        List<String> values = keys.stream()
                .flatMap(key -> Stream.ofNullable(config.get(key)))
                .toList();
        System.out.println("ofNullable + flatMap (config lookup): " + values);

        // flatMapToInt — wariant prymitywny
        int totalItems = orders.stream()
                .flatMapToInt(order -> IntStream.of(order.items().size()))
                .sum();
        System.out.println("flatMapToInt (total item count): " + totalItems);
    }

    // ============================================================
    // Sekcja 9: Strumienie równoległe
    // ============================================================

    static void parallelStreams() {
        System.out.println("\n=== Parallel Streams ===");

        List<Integer> numbers = IntStream.rangeClosed(1, 100).boxed().toList();

        // Tworzenie strumienia równoległego z kolekcji
        long parallelSum = numbers.parallelStream()
                .mapToLong(Integer::longValue)
                .sum();
        System.out.println("parallelStream sum(1..100): " + parallelSum);

        // Konwersja sekwencyjnego na równoległy
        long sequentialSum = numbers.stream()
                .parallel()
                .mapToLong(Integer::longValue)
                .sum();
        System.out.println("stream().parallel() sum: " + sequentialSum);

        // forEach vs forEachOrdered w równoległym
        System.out.print("parallel forEach (unordered): ");
        List.of(1, 2, 3, 4, 5).parallelStream()
                .forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.print("parallel forEachOrdered: ");
        List.of(1, 2, 3, 4, 5).parallelStream()
                .forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();

        // Równoległa redukcja — akumulator i combiner muszą być asocjacyjne
        int parallelProduct = IntStream.rangeClosed(1, 10)
                .parallel()
                .reduce(1, (a, b) -> a * b);
        System.out.println("parallel reduce (product 1..10): " + parallelProduct);

        // Problem bezpieczeństwa wątkowego — NIE rób tego (efekty uboczne ze współdzielonym mutowalnym stanem)
        // Poprawne podejście: użyj collect lub reduce
        List<Integer> safeResult = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList()); // bezpieczne wątkowo — Collectors obsługuje synchronizację
        System.out.println("parallel collect (even numbers count): " + safeResult.size());

        // Niestandardowy ForkJoinPool — kontrola poziomu równoległości
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

        // Sprawdzanie czy strumień jest równoległy
        var seqStream = numbers.stream();
        var parStream = numbers.parallelStream();
        System.out.println("sequential isParallel: " + seqStream.isParallel());
        System.out.println("parallel isParallel: " + parStream.isParallel());
        // Oczyszczanie nieużywanych strumieni
        seqStream.close();
        parStream.close();
    }

    // ============================================================
    // Sekcja 10: Typowe wzorce i najlepsze praktyki
    // ============================================================

    static void commonPatternsAndBestPractices() {
        System.out.println("\n=== Common Patterns and Best Practices ===");

        // Wzorzec: znajdź pierwszy pasujący element
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

        // Wzorzec: sprawdzanie istnienia
        boolean hasYoung = people.stream()
                .anyMatch(p -> p.age() < 30);
        System.out.println("has someone under 30: " + hasYoung);

        // Wzorzec: transformacja i zbieranie ze złożoną logiką
        Map<String, List<String>> citiesWithPeople = people.stream()
                .collect(Collectors.groupingBy(Person::city,
                        Collectors.mapping(Person::name, Collectors.toList())));
        System.out.println("cities with people: " + citiesWithPeople);

        // Wzorzec: łączenie Optional z wynikami strumieni
        String result = people.stream()
                .filter(p -> p.city().equals("Gdansk"))
                .map(Person::name)
                .findFirst()
                .map(name -> "Found: " + name)
                .orElse("No one from Gdansk");
        System.out.println("optional chaining: " + result);

        // Wzorzec: nieskończony strumień z limitem
        List<Integer> fibonacci = Stream.iterate(
                        new int[]{0, 1},
                        pair -> new int[]{pair[1], pair[0] + pair[1]}
                )
                .limit(10)
                .map(pair -> pair[0])
                .toList();
        System.out.println("fibonacci (first 10): " + fibonacci);

        // Wzorzec: strumień jako zamiennik pętli opartej na indeksie
        List<String> items = List.of("alpha", "beta", "gamma", "delta");
        String indexed = IntStream.range(0, items.size())
                .mapToObj(i -> i + ": " + items.get(i))
                .collect(Collectors.joining(", "));
        System.out.println("indexed iteration: " + indexed);

        // Wzorzec: mapa częstotliwości
        String sentence = "the quick brown fox jumps over the lazy dog the fox";
        Map<String, Long> wordFrequency = Arrays.stream(sentence.split(" "))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        System.out.println("word frequency: " + wordFrequency);

        // Nie używaj ponownie strumieni — twórz nowy za każdym razem
        // ŹLE: var s = list.stream(); s.count(); s.forEach(...) // rzuca!
        // DOBRZE: wywołaj list.stream() ponownie dla każdej operacji
        List<String> names = List.of("Alice", "Bob", "Charlie");
        long nameCount = names.stream().count();
        String first = names.stream().findFirst().orElse("none");
        System.out.println("separate streams: count=" + nameCount + ", first=" + first);

        // Preferuj referencje do metod dla czytelności
        List<String> upper = names.stream()
                .map(String::toUpperCase) // czystsze niż s -> s.toUpperCase()
                .toList();
        System.out.println("method reference (toUpperCase): " + upper);
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
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
