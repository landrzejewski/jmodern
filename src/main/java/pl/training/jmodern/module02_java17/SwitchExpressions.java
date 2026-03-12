package pl.training.jmodern.module02_java17;

import java.util.*;

// ============================================================
// Sekcja 1: Problemy z tradycyjnym Switch
// ============================================================

/*
## Problemy z tradycyjnym Switch

- Tradycyjna instrukcja `switch` w Javie była źródłem
  subtelnych błędów od Javy 1.0. Najbardziej znany problem to
  **fall-through**: jeśli zapomnisz `break`, wykonanie cicho
  przechodzi do następnego przypadku.
- **Fall-through domyślnie**: W przeciwieństwie do `if-else`, przypadki switch
  przechodzą dalej, chyba że zostaną jawnie zakończone `break`. Ten
  wzorzec został odziedziczony z C/C++ i prowadzi do trudnych do znalezienia błędów,
  ponieważ kompilator nie wyświetla żadnego ostrzeżenia.
- **Tylko instrukcja**: Tradycyjny switch to **instrukcja**, a nie
  **wyrażenie**. Nie może bezpośrednio produkować wartości. Aby użyć
  wyniku switch, musisz zadeklarować zmienną przed switch
  i przypisać ją wewnątrz każdego przypadku — to jest rozwlekłe i
  podatne na błędy (możesz zapomnieć o przypisaniu w jednej gałęzi).
- **Ograniczone typy** (przed Javą 7): Przed Javą 7 switch działał
  tylko z `byte`, `short`, `char`, `int` i ich typami opakowującymi.
  Java 7 dodała `String`, Java 5 dodała `enum`.
- **Harmonogram JEP**:
    - JEP 325: Podgląd w Javie 12 (wyrażenia switch)
    - JEP 354: Drugi podgląd w Javie 13 (wprowadzono `yield`)
    - JEP 361: Sfinalizowano w Javie 14
- Nowy switch rozwiązuje wszystkie te problemy: składnia strzałkowa
  eliminuje fall-through, forma wyrażeniowa zwraca wartości, a
  `yield` obsługuje wieloliniowe bloki.
*/

// ============================================================
// Sekcja 2: Etykiety strzałkowe i forma wyrażeniowa
// ============================================================

/*
## Etykiety strzałkowe i forma wyrażeniowa

- **Etykiety strzałkowe** (`case X ->`) zastępują etykiety z dwukropkiem
  (`case X:`). Przy etykietach strzałkowych wykonuje się tylko kod
  po prawej stronie strzałki — **nigdy nie ma fall-through**.
- **Switch jako wyrażenie**: Cały switch może być teraz przypisany
  do zmiennej:
      var result = switch (day) {
          case MONDAY -> "Start of week";
          case FRIDAY -> "Almost weekend";
          default -> "Midweek";
      };
  Zwróć uwagę na **średnik po klamrze zamykającej** — jest
  wymagany, ponieważ switch jest teraz instrukcją wyrażeniową.
- **`yield` dla wieloliniowych bloków**: Gdy przypadek wymaga wielu
  instrukcji, użyj bloku `{ ... }` i słowa kluczowego `yield` aby
  wygenerować wartość:
      case MONDAY -> {
          logger.info("Monday");
          yield "Start of week";
      }
- **`yield` vs `return`**: `yield` wychodzi z wyrażenia switch
  z wartością. `return` wychodzi z otaczającej metody. Nie
  myl ich — użycie `return` wewnątrz wyrażenia switch spowoduje
  powrót z metody, nie ze switch.
- **Etykiety strzałkowe w instrukcjach switch**: Możesz również używać
  etykiet strzałkowych w instrukcjach switch (nie tylko wyrażeniach). Nawet bez
  zwracania wartości, etykiety strzałkowe zapobiegają fall-through, co czyni
  kod bezpieczniejszym.
- **`var` i wnioskowanie typów**: Wynik wyrażenia switch
  może być przypisany za pomocą `var`, a kompilator wnioskuje typ
  na podstawie wspólnego typu wszystkich gałęzi.
*/

// ============================================================
// Sekcja 3: Wielokrotne etykiety przypadków i wyczerpywalność
// ============================================================

/*
## Wielokrotne etykiety przypadków i wyczerpywalność

- **Wiele etykiet na przypadek**: Możesz grupować wiele stałych
  w jednym przypadku używając przecinków:
      case MONDAY, TUESDAY, WEDNESDAY -> "Weekday";
  To zastępuje stary wzorzec grupowania przez fall-through:
      case MONDAY: case TUESDAY: case WEDNESDAY: // stary sposób
- **Wyczerpywalność dla enum**: Przy przełączaniu na typie enum
  w wyrażeniu, kompilator sprawdza, czy **wszystkie stałe są
  pokryte**. Jeśli tak, gałąź `default` nie jest potrzebna:
      var label = switch (season) {
          case SPRING -> "Bloom";
          case SUMMER -> "Sun";
          case AUTUMN -> "Leaves";
          case WINTER -> "Snow";
      };
  To jest znacząca zaleta bezpieczeństwa — jeśli ktoś doda nową
  stałą enum później, kompilator oznaczy każdy switch, który
  jej nie obsługuje.
- **`default` ukrywa brakujące przypadki**: Jeśli dodasz gałąź `default`,
  kompilator przestaje sprawdzać wyczerpywalność. Nowe
  stałe enum cicho trafiają do `default`. Dla enum preferuj
  jawne pokrycie wszystkich stałych — używaj `default` tylko gdy
  celowo chcesz mieć przypadek ogólny.
- **Wyczerpywalność dla typów nie-enum**: Dla `String`, `int`
  i innych typów kompilator nie może zweryfikować wyczerpywalności,
  więc gałąź `default` jest wymagana w wyrażeniach switch.
*/

// ============================================================
// Sekcja 4: Switch z różnymi typami
// ============================================================

/*
## Switch z różnymi typami

- **Switch na String** (od Javy 7): Przełączanie na wartościach String
  używając semantyki `equals()`. Przydatne do parsowania poleceń,
  metod HTTP, kluczy konfiguracji.
- **Switch na liczbach całkowitych**: Klasyczny switch na wartościach `int`/`Integer`.
  Na poziomie kodu bajtowego JVM używa `tableswitch` (dla gęstych
  zakresów) lub `lookupswitch` (dla rzadkich wartości) — oba mają
  złożoność O(1) lub O(log n), znacznie szybsze niż łańcuch if-else.
- **Switch na enum**: Najbardziej naturalne zastosowanie switch. Enum mają
  stały zbiór stałych, umożliwiając sprawdzanie wyczerpywalności.
- **Obsługa `case null`** (Java 21+, JEP 441): Tradycyjnie,
  przełączanie na `null` rzuca `NullPointerException`. Począwszy
  od Javy 21, możesz jawnie obsłużyć null:
      case null -> "No value provided";
  Jeśli nie ma `case null`, zachowane jest stare zachowanie NPE.
- **Wzorce typów** (Java 21+, JEP 441): Switch może dopasowywać
  typy, łącząc `instanceof` i rzutowanie w jednym kroku:
      case String s -> "String: " + s;
      case Integer i -> "Integer: " + i;
  Nazywa się to **dopasowywaniem wzorców dla switch** i było
  w podglądzie od Javy 17 do Javy 20, sfinalizowane w Javie 21.
*/

// ============================================================
// Sekcja 5: Wzorce praktyczne
// ============================================================

/*
## Wzorce praktyczne

- **Switch w potokach Stream**: Wyrażenia switch działają
  doskonale wewnątrz `.map()`, `.filter()` i innych operacji
  Stream, ponieważ są wyrażeniami zwracającymi wartość:
      list.stream()
          .map(s -> switch (s.status()) { ... })
          .toList();
- **Metody fabrykujące**: Wyrażenia switch są idealne do wzorców
  fabrykujących — mapowania dyskryminatora na obiekt:
      static Shape create(String type) {
          return switch (type) { ... };
      }
- **Dispatch poleceń**: Użyj switch do dispatchowania na sealed typach
  lub enum reprezentujących polecenia, zdarzenia lub wiadomości. To
  zastępuje rozwlekłe łańcuchy if-else lub wzorzec visitor.
- **Mapowanie/konwersja**: Wyrażenia switch naturalnie wyrażają
  mapowania wartość-na-wartość, jak enum-na-string, status-na-kolor,
  czy konwersje kod-na-wiadomość.
- **Zagnieżdżony switch**: Wyrażenia switch mogą pojawiać się wewnątrz innych
  wyrażeń switch dla wielowymiarowego dispatchowania. Używaj tego
  oszczędnie — jeśli zagnieżdżanie staje się głębokie, rozważ wyodrębnienie
  metod pomocniczych.
*/

// ============================================================
// Sekcja 6: Wyrażenia Switch vs If-Else
// ============================================================

/*
## Wyrażenia Switch vs If-Else

- **Kiedy używać switch**:
    - Dopasowywanie pojedynczej zmiennej do dyskretnych wartości
    - Dispatch na enum (gwarantowana wyczerpywalność)
    - Zastępowanie długich łańcuchów if-else porównujących tę samą zmienną
    - Gdy potrzebujesz wyrażenia produkującego wartość
- **Kiedy używać if-else**:
    - Sprawdzanie zakresów (`x > 10 && x < 20`) — switch nie obsługuje zakresów
    - Złożone warunki logiczne obejmujące wiele zmiennych
    - Sprawdzanie null w połączeniu z wywołaniami metod
    - Warunki nieoparte na równości
- **Wydajność**: Na poziomie kodu bajtowego `switch` na liczbach całkowitych
  używa `tableswitch` (tablica skoków O(1) dla gęstych zakresów) lub
  `lookupswitch` (wyszukiwanie binarne O(log n) dla rzadkich wartości).
  Łańcuchy if-else kompilują się do sekwencyjnych porównań (O(n)).
  Dla enum i liczb całkowitych switch jest szybszy.
- **Czytelność**: Wyrażenia switch czynią strukturę jawną:
  "ta zmienna może mieć jedną z tych wartości i dla każdej robimy
  to." Łańcuchy if-else zaciemniają ten wzorzec.
- **Wskazówki migracyjne**: Konwersja starych instrukcji switch na
  nowe wyrażenia jest w większości mechaniczna:
    1. Usuń instrukcje `break`
    2. Zamień `:` na `->`
    3. Zgrupuj przypadki fall-through przecinkami
    4. Przypisz switch do zmiennej (forma wyrażeniowa)
    5. Zamień przypisanie zmiennej lokalnej na `yield` jeśli potrzeba
*/

public class SwitchExpressions {

    // ---- Sekcja 1: Enum Season ----

    enum Season { SPRING, SUMMER, AUTUMN, WINTER }

    // ---- Sekcja 2: Enum Day ----

    enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

    // ---- Sekcja 3: Enum Priority ----

    enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    // ---- Sekcja 4: Enum HttpStatus z polem code ----

    enum HttpStatus {
        OK(200), CREATED(201), BAD_REQUEST(400), NOT_FOUND(404), INTERNAL_ERROR(500);

        private final int code;
        HttpStatus(int code) { this.code = code; }
        int code() { return code; }
    }

    // ---- Sekcja 5: Zapieczętowany interfejs Command ----

    sealed interface Command permits Login, Logout, Purchase, Refund {}
    record Login(String username) implements Command {}
    record Logout(String username) implements Command {}
    record Purchase(String item, double price) implements Command {}
    record Refund(String orderId, double amount) implements Command {}

    // ============================================================
    // Sekcja 1: Problemy z tradycyjnym Switch
    // ============================================================

    static void traditionalSwitchProblems() {
        System.out.println("=== Section 1: Traditional Switch Problems ===");

        // Stary styl switch z break — zliczanie dni pory roku
        System.out.println("--- Old-style switch with break ---");
        Season season = Season.SUMMER;
        int days;
        switch (season) {
            case SPRING:
                days = 92;
                break;
            case SUMMER:
                days = 93;
                break;
            case AUTUMN:
                days = 91;
                break;
            case WINTER:
                days = 90;
                break;
            default:
                days = 0;
                break;
        }
        System.out.println(season + " has " + days + " days");

        // Demonstracja błędu fall-through
        System.out.println("\n--- Fall-through bug demonstration ---");
        System.out.println("Intentional fall-through (missing break on SPRING and SUMMER):");
        for (Season s : Season.values()) {
            System.out.print("  " + s + " → ");
            switch (s) {
                case SPRING:
                case SUMMER:
                    System.out.println("warm season (SPRING/SUMMER grouped via fall-through)");
                    break;
                case AUTUMN:
                case WINTER:
                    System.out.println("cold season (AUTUMN/WINTER grouped via fall-through)");
                    break;
            }
        }

        // Stary styl: zwracanie wartości wymaga zmiennej
        System.out.println("\n--- Old-style: variable before switch ---");
        Day day = Day.SATURDAY;
        String dayType;
        switch (day) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                dayType = "Weekday";
                break;
            case SATURDAY:
            case SUNDAY:
                dayType = "Weekend";
                break;
            default:
                dayType = "Unknown";
                break;
        }
        System.out.println(day + " is a " + dayType);

        // Zapowiedź: ta sama logika ze składnią strzałkową (podgląd Sekcji 2)
        System.out.println("\n--- New arrow syntax teaser ---");
        var dayTypeNew = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
            case SATURDAY, SUNDAY -> "Weekend";
        };
        System.out.println(day + " is a " + dayTypeNew + " (arrow syntax)");
    }

    // ============================================================
    // Sekcja 2: Etykiety strzałkowe i forma wyrażeniowa
    // ============================================================

    static String dayCategory(Day day) {
        return switch (day) {
            case MONDAY -> "Start of work week";
            case TUESDAY, WEDNESDAY, THURSDAY -> "Midweek";
            case FRIDAY -> "End of work week";
            case SATURDAY, SUNDAY -> "Weekend";
        };
    }

    static void arrowLabelsAndExpressionForm() {
        System.out.println("\n=== Section 2: Arrow Labels and Expression Form ===");

        // Mapowanie Day → opis ze składnią strzałkową
        System.out.println("--- Day descriptions with arrow syntax ---");
        for (Day day : Day.values()) {
            var description = switch (day) {
                case MONDAY -> "Back to work";
                case TUESDAY -> "Getting into rhythm";
                case WEDNESDAY -> "Hump day";
                case THURSDAY -> "Almost there";
                case FRIDAY -> "TGIF!";
                case SATURDAY -> "Freedom!";
                case SUNDAY -> "Rest day";
            };
            System.out.println("  " + day + " → " + description);
        }

        // Wyrażenie switch z var
        System.out.println("\n--- Switch expression assigned to var ---");
        Season season = Season.WINTER;
        var avgTemp = switch (season) {
            case SPRING -> 15.0;
            case SUMMER -> 28.0;
            case AUTUMN -> 12.0;
            case WINTER -> -2.0;
        };
        System.out.println(season + " average temperature: " + avgTemp + "°C");

        // yield w wieloliniowym bloku
        System.out.println("\n--- yield in multi-line block ---");
        var seasonReport = switch (season) {
            case SPRING -> {
                var temp = "mild";
                var activity = "gardening";
                yield temp + " weather, perfect for " + activity;
            }
            case SUMMER -> {
                var temp = "hot";
                var activity = "swimming";
                yield temp + " weather, perfect for " + activity;
            }
            case AUTUMN -> {
                var temp = "cool";
                var activity = "hiking";
                yield temp + " weather, perfect for " + activity;
            }
            case WINTER -> {
                var temp = "cold";
                var activity = "skiing";
                yield temp + " weather, perfect for " + activity;
            }
        };
        System.out.println(season + ": " + seasonReport);

        // Wyrażenie switch w instrukcji return metody pomocniczej
        System.out.println("\n--- Switch expression in return statement ---");
        for (Day day : Day.values()) {
            System.out.println("  " + day + " → " + dayCategory(day));
        }
    }

    // ============================================================
    // Sekcja 3: Wielokrotne etykiety przypadków i wyczerpywalność
    // ============================================================

    static void multipleCaseLabelsAndExhaustiveness() {
        System.out.println("\n=== Section 3: Multiple Case Labels and Exhaustiveness ===");

        // Grupowanie priorytetów: LOW+MEDIUM / HIGH+CRITICAL
        System.out.println("--- Priority grouping ---");
        for (Priority p : Priority.values()) {
            var response = switch (p) {
                case LOW, MEDIUM -> "Standard handling";
                case HIGH, CRITICAL -> "Immediate escalation";
            };
            System.out.println("  " + p + " → " + response);
        }

        // Dzień roboczy/weekend z grupowaniem przecinkowym
        System.out.println("\n--- Weekday/weekend with comma grouping ---");
        for (Day day : Day.values()) {
            var type = switch (day) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
                case SATURDAY, SUNDAY -> "Weekend";
            };
            System.out.println("  " + day + " → " + type);
        }

        // Wyczerpujący switch na Season — nie potrzeba default
        System.out.println("\n--- Exhaustive Season switch (no default) ---");
        for (Season s : Season.values()) {
            var emoji = switch (s) {
                case SPRING -> "flowers";
                case SUMMER -> "sun";
                case AUTUMN -> "leaves";
                case WINTER -> "snowflake";
            };
            System.out.println("  " + s + " → " + emoji);
        }
        // Jeśli ktoś doda 5. porę roku, kompilator oznaczy każde
        // wyrażenie switch, które jej nie obsługuje — to jest kluczowa
        // zaleta w porównaniu z użyciem gałęzi default.

        // Priorytet ze szczegółową obsługą
        System.out.println("\n--- Priority with detailed response times ---");
        for (Priority p : Priority.values()) {
            var responseTime = switch (p) {
                case LOW -> "Within 5 business days";
                case MEDIUM -> "Within 24 hours";
                case HIGH -> "Within 4 hours";
                case CRITICAL -> "Immediate response required";
            };
            System.out.println("  " + p + " → " + responseTime);
        }
    }

    // ============================================================
    // Sekcja 4: Switch z różnymi typami
    // ============================================================

    static void switchWithDifferentTypes() {
        System.out.println("\n=== Section 4: Switch with Different Types ===");

        // Switch na String dla metod HTTP
        System.out.println("--- String switch: HTTP methods ---");
        List<String> methods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        for (String method : methods) {
            var description = switch (method) {
                case "GET" -> "Retrieve resource";
                case "POST" -> "Create resource";
                case "PUT" -> "Replace resource";
                case "DELETE" -> "Remove resource";
                case "PATCH" -> "Partial update";
                default -> "Other method: " + method;
            };
            System.out.println("  " + method + " → " + description);
        }

        // Switch na liczbie całkowitej: miesiąc → kwartał
        System.out.println("\n--- Integer switch: month to quarter ---");
        for (int month = 1; month <= 12; month++) {
            var quarter = switch (month) {
                case 1, 2, 3 -> "Q1";
                case 4, 5, 6 -> "Q2";
                case 7, 8, 9 -> "Q3";
                case 10, 11, 12 -> "Q4";
                default -> "Invalid";
            };
            System.out.println("  Month " + month + " → " + quarter);
        }

        // Switch na enum: HttpStatus → wiadomość
        System.out.println("\n--- Enum switch: HttpStatus to message ---");
        for (HttpStatus status : HttpStatus.values()) {
            var message = switch (status) {
                case OK -> "Success";
                case CREATED -> "Resource created";
                case BAD_REQUEST -> "Client error: bad request";
                case NOT_FOUND -> "Resource not found";
                case INTERNAL_ERROR -> "Server error";
            };
            System.out.println("  " + status.code() + " " + status + " → " + message);
        }

        // Obsługa null z case null (Java 21+)
        System.out.println("\n--- Null handling with case null (Java 21+) ---");
        List<String> values = new ArrayList<>();
        values.add("hello");
        values.add(null);
        values.add("world");
        for (String value : values) {
            var result = switch (value) {
                case null -> "<null value>";
                case "hello" -> "greeting detected";
                case "world" -> "planet detected";
                default -> "other: " + value;
            };
            System.out.println("  \"" + value + "\" → " + result);
        }

        // Switch ze wzorcem typów na Object (Java 21+)
        System.out.println("\n--- Type pattern switch on Object (Java 21+) ---");
        List<Object> objects = List.of(42, "Hello", 3.14, true, List.of(1, 2, 3));
        for (Object obj : objects) {
            var description = switch (obj) {
                case Integer i -> "Integer: " + i + " (doubled: " + (i * 2) + ")";
                case String s -> "String: \"" + s + "\" (length: " + s.length() + ")";
                case Double d -> "Double: " + d + " (rounded: " + Math.round(d) + ")";
                case Boolean b -> "Boolean: " + b + " (negated: " + !b + ")";
                default -> "Unknown type: " + obj.getClass().getSimpleName();
            };
            System.out.println("  " + description);
        }
    }

    // ============================================================
    // Sekcja 5: Wzorce praktyczne
    // ============================================================

    static String seasonActivity(Season season) {
        return switch (season) {
            case SPRING -> "plant a garden";
            case SUMMER -> "go to the beach";
            case AUTUMN -> "go apple picking";
            case WINTER -> "build a snowman";
        };
    }

    static String processCommand(Command command) {
        return switch (command) {
            case Login l -> "Logging in user: " + l.username();
            case Logout l -> "Logging out user: " + l.username();
            case Purchase p -> String.format("Processing purchase: %s ($%.2f)", p.item(), p.price());
            case Refund r -> String.format("Processing refund: order %s ($%.2f)", r.orderId(), r.amount());
        };
    }

    static void practicalPatterns() {
        System.out.println("\n=== Section 5: Practical Patterns ===");

        // Switch w potoku Stream .map()
        System.out.println("--- Switch in stream pipeline ---");
        var seasons = List.of(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER);
        var activities = seasons.stream()
                .map(s -> s + ": " + switch (s) {
                    case SPRING -> "plant flowers";
                    case SUMMER -> "go swimming";
                    case AUTUMN -> "rake leaves";
                    case WINTER -> "drink hot cocoa";
                })
                .toList();
        activities.forEach(a -> System.out.println("  " + a));

        // Stream z switch do filtrowania i mapowania
        System.out.println("\n--- Stream filter + switch mapping ---");
        var days = List.of(Day.values());
        var workdaySchedule = days.stream()
                .filter(d -> switch (d) {
                    case SATURDAY, SUNDAY -> false;
                    default -> true;
                })
                .map(d -> d + " → " + switch (d) {
                    case MONDAY -> "Team standup at 9:00";
                    case TUESDAY -> "Deep work block";
                    case WEDNESDAY -> "Code review session";
                    case THURSDAY -> "Architecture meeting";
                    case FRIDAY -> "Demo and retrospective";
                    default -> "Unknown";
                })
                .toList();
        workdaySchedule.forEach(s -> System.out.println("  " + s));

        // Dispatch poleceń na zapieczętowanym interfejsie
        System.out.println("\n--- Command dispatch ---");
        List<Command> commands = List.of(
                new Login("alice"),
                new Purchase("Laptop", 999.99),
                new Purchase("Mouse", 29.99),
                new Logout("alice"),
                new Refund("ORD-42", 29.99)
        );
        for (Command cmd : commands) {
            System.out.println("  " + processCommand(cmd));
        }

        // Konwersja Season → aktywność (metoda pomocnicza)
        System.out.println("\n--- Season activity suggestions ---");
        for (Season s : Season.values()) {
            System.out.println("  " + s + " → " + seasonActivity(s));
        }

        // Zagnieżdżony switch: pora roku + priorytet → planowanie
        System.out.println("\n--- Nested switch: season + priority scheduling ---");
        var testCases = List.of(
                new Object[]{Season.SUMMER, Priority.CRITICAL},
                new Object[]{Season.WINTER, Priority.LOW},
                new Object[]{Season.SPRING, Priority.HIGH},
                new Object[]{Season.AUTUMN, Priority.MEDIUM}
        );
        for (var tc : testCases) {
            Season s = (Season) tc[0];
            Priority p = (Priority) tc[1];
            var schedule = switch (s) {
                case SUMMER -> switch (p) {
                    case LOW, MEDIUM -> "Schedule after summer break";
                    case HIGH, CRITICAL -> "Handle before vacation";
                };
                case WINTER -> switch (p) {
                    case LOW -> "Defer to January";
                    case MEDIUM -> "Fit into December sprint";
                    case HIGH, CRITICAL -> "Handle before holidays";
                };
                case SPRING, AUTUMN -> switch (p) {
                    case LOW -> "Add to backlog";
                    case MEDIUM -> "Next sprint";
                    case HIGH -> "This sprint";
                    case CRITICAL -> "Drop everything";
                };
            };
            System.out.println("  " + s + " + " + p + " → " + schedule);
        }
    }

    // ============================================================
    // Sekcja 6: Wyrażenia Switch vs If-Else
    // ============================================================

    static String classifySeason(Season season) {
        return switch (season) {
            case SPRING, SUMMER -> "Warm half";
            case AUTUMN, WINTER -> "Cold half";
        };
    }

    static String priorityLabel(Priority priority) {
        return switch (priority) {
            case LOW -> "Low priority";
            case MEDIUM -> "Medium priority";
            case HIGH -> "High priority";
            case CRITICAL -> "Critical priority";
        };
    }

    static void switchExpressionsVsIfElse() {
        System.out.println("\n=== Section 6: Switch Expressions vs If-Else ===");

        // Porównanie obok siebie: if-else vs switch dla tej samej logiki
        System.out.println("--- Side-by-side comparison ---");
        int statusCode = 404;

        // Podejście if-else
        String messageIfElse;
        if (statusCode == 200) {
            messageIfElse = "OK";
        } else if (statusCode == 201) {
            messageIfElse = "Created";
        } else if (statusCode == 400) {
            messageIfElse = "Bad Request";
        } else if (statusCode == 404) {
            messageIfElse = "Not Found";
        } else if (statusCode == 500) {
            messageIfElse = "Internal Server Error";
        } else {
            messageIfElse = "Unknown";
        }
        System.out.println("  if-else: " + statusCode + " → " + messageIfElse);

        // Podejście switch
        var messageSwitch = switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
        System.out.println("  switch:  " + statusCode + " → " + messageSwitch);

        // Przykład, gdzie if-else jest lepszy: sprawdzanie zakresów
        System.out.println("\n--- Where if-else wins: range checks ---");
        List<Integer> scores = List.of(95, 82, 71, 55, 38);
        for (int score : scores) {
            // Switch nie obsługuje zakresów — if-else jest tu właściwym narzędziem
            String grade;
            if (score >= 90) {
                grade = "A";
            } else if (score >= 80) {
                grade = "B";
            } else if (score >= 70) {
                grade = "C";
            } else if (score >= 60) {
                grade = "D";
            } else {
                grade = "F";
            }
            System.out.println("  Score " + score + " → Grade " + grade);
        }

        // Przykład, gdzie switch wygrywa: dispatch na enum
        System.out.println("\n--- Where switch wins: enum dispatch ---");
        for (Season s : Season.values()) {
            System.out.println("  " + s + " → " + classifySeason(s));
        }
        for (Priority p : Priority.values()) {
            System.out.println("  " + p + " → " + priorityLabel(p));
        }

        // Demonstracja refaktoryzacji ze starego na nowy styl
        System.out.println("\n--- Old-style to new-style refactoring ---");
        Day day = Day.WEDNESDAY;

        // STARY styl (rozwlekły, podatny na błędy)
        String oldResult;
        switch (day) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                oldResult = "Workday";
                break;
            case SATURDAY:
            case SUNDAY:
                oldResult = "Weekend";
                break;
            default:
                oldResult = "Unknown";
                break;
        }

        // NOWY styl (zwięzły, bezpieczny)
        var newResult = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Workday";
            case SATURDAY, SUNDAY -> "Weekend";
        };

        System.out.println("  Old style: " + day + " → " + oldResult);
        System.out.println("  New style: " + day + " → " + newResult);
        System.out.println("  Both produce the same result: " + oldResult.equals(newResult));

        // Uwaga o wydajności
        System.out.println("\n--- Performance note ---");
        System.out.println("  Switch on int/enum uses tableswitch/lookupswitch bytecode");
        System.out.println("  → O(1) jump table for dense values, O(log n) binary search for sparse");
        System.out.println("  If-else chains compile to sequential comparisons → O(n)");
        System.out.println("  For enum dispatch and discrete values, switch is both faster and clearer");
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        traditionalSwitchProblems();
        arrowLabelsAndExpressionForm();
        multipleCaseLabelsAndExhaustiveness();
        switchWithDifferentTypes();
        practicalPatterns();
        switchExpressionsVsIfElse();
    }
}
