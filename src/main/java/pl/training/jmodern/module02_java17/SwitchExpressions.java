package pl.training.jmodern.module02_java17;

import java.util.*;

// ============================================================
// Section 1: Traditional Switch Problems
// ============================================================

/*
## Traditional Switch Problems

- The traditional `switch` statement in Java has been a source of
  subtle bugs since Java 1.0. The most infamous issue is
  **fall-through**: if you forget a `break`, execution silently
  continues into the next case.
- **Fall-through by default**: Unlike `if-else`, switch cases
  fall through unless explicitly terminated with `break`. This
  design was inherited from C/C++ and leads to hard-to-find bugs
  because the compiler gives no warning.
- **Statement-only**: Traditional switch is a **statement**, not
  an **expression**. It cannot produce a value directly. To use
  the result of a switch, you must declare a variable before the
  switch and assign it inside each case — this is verbose and
  error-prone (you might forget to assign in one branch).
- **Limited types** (pre-Java 7): Before Java 7, switch only
  worked with `byte`, `short`, `char`, `int`, and their wrapper
  types. Java 7 added `String`, Java 5 added `enum`.
- **JEP timeline**:
    - JEP 325: Preview in Java 12 (switch expressions)
    - JEP 354: Second preview in Java 13 (introduced `yield`)
    - JEP 361: Finalized in Java 14
- The new switch addresses all these problems: arrow syntax
  eliminates fall-through, expression form returns values, and
  `yield` handles multi-line blocks.
*/

// ============================================================
// Section 2: Arrow Labels and Expression Form
// ============================================================

/*
## Arrow Labels and Expression Form

- **Arrow labels** (`case X ->`) replace the colon-style labels
  (`case X:`). With arrow labels, only the code to the right of
  the arrow executes — there is **no fall-through**, ever.
- **Switch as expression**: The entire switch can now be assigned
  to a variable:
      var result = switch (day) {
          case MONDAY -> "Start of week";
          case FRIDAY -> "Almost weekend";
          default -> "Midweek";
      };
  Note the **semicolon after the closing brace** — this is
  required because the switch is now an expression statement.
- **`yield` for multi-line blocks**: When a case needs multiple
  statements, use a block `{ ... }` and the `yield` keyword to
  produce the value:
      case MONDAY -> {
          logger.info("Monday");
          yield "Start of week";
      }
- **`yield` vs `return`**: `yield` exits the switch expression
  with a value. `return` exits the enclosing method. Do not
  confuse them — using `return` inside a switch expression will
  return from the method, not from the switch.
- **Arrow labels in switch statements**: You can also use arrow
  labels in switch statements (not just expressions). Even without
  returning a value, arrow labels prevent fall-through, making
  the code safer.
- **`var` and type inference**: The result of a switch expression
  can be assigned using `var`, and the compiler infers the type
  from the common type of all branches.
*/

// ============================================================
// Section 3: Multiple Case Labels and Exhaustiveness
// ============================================================

/*
## Multiple Case Labels and Exhaustiveness

- **Multiple labels per case**: You can group multiple constants
  in a single case using commas:
      case MONDAY, TUESDAY, WEDNESDAY -> "Weekday";
  This replaces the old fall-through grouping pattern:
      case MONDAY: case TUESDAY: case WEDNESDAY: // old way
- **Exhaustiveness for enums**: When switching over an enum type
  in an expression, the compiler checks that **all constants are
  covered**. If they are, no `default` branch is needed:
      var label = switch (season) {
          case SPRING -> "Bloom";
          case SUMMER -> "Sun";
          case AUTUMN -> "Leaves";
          case WINTER -> "Snow";
      };
  This is a significant safety advantage — if someone adds a new
  enum constant later, the compiler will flag every switch that
  doesn't handle it.
- **`default` hides missing cases**: If you add a `default`
  branch, the compiler stops checking for exhaustiveness. New
  enum constants silently fall into `default`. For enums, prefer
  covering all constants explicitly — use `default` only when
  you intentionally want a catch-all.
- **Exhaustiveness for non-enum types**: For `String`, `int`,
  and other types, the compiler cannot verify exhaustiveness,
  so a `default` branch is required in switch expressions.
*/

// ============================================================
// Section 4: Switch with Different Types
// ============================================================

/*
## Switch with Different Types

- **String switch** (since Java 7): Switch on String values
  using `equals()` semantics. Useful for parsing commands,
  HTTP methods, configuration keys.
- **Integer switch**: Classic switch on `int`/`Integer` values.
  At the bytecode level, the JVM uses `tableswitch` (for dense
  ranges) or `lookupswitch` (for sparse values) — both are
  O(1) or O(log n), much faster than chained if-else.
- **Enum switch**: The most natural fit for switch. Enums have
  a fixed set of constants, enabling exhaustiveness checks.
- **`case null` handling** (Java 21+, JEP 441): Traditionally,
  switching on `null` throws a `NullPointerException`. Starting
  with Java 21, you can explicitly handle null:
      case null -> "No value provided";
  If no `case null` is present, the old NPE behavior is preserved.
- **Type patterns** (Java 21+, JEP 441): Switch can match on
  types, combining `instanceof` and cast in a single step:
      case String s -> "String: " + s;
      case Integer i -> "Integer: " + i;
  This is called **pattern matching for switch** and was previewed
  from Java 17 through Java 20, finalized in Java 21.
*/

// ============================================================
// Section 5: Practical Patterns
// ============================================================

/*
## Practical Patterns

- **Switch in stream pipelines**: Switch expressions work
  beautifully inside `.map()`, `.filter()`, and other stream
  operations because they are expressions that return a value:
      list.stream()
          .map(s -> switch (s.status()) { ... })
          .toList();
- **Factory methods**: Switch expressions are ideal for factory
  patterns — mapping a discriminator to an object:
      static Shape create(String type) {
          return switch (type) { ... };
      }
- **Command dispatch**: Use switch to dispatch on sealed types
  or enums representing commands, events, or messages. This
  replaces verbose if-else chains or the visitor pattern.
- **Mapping/conversion**: Switch expressions naturally express
  value-to-value mappings, like enum-to-string, status-to-color,
  or code-to-message conversions.
- **Nested switch**: Switch expressions can appear inside other
  switch expressions for multi-dimensional dispatch. Use this
  sparingly — if nesting gets deep, consider extracting helper
  methods.
*/

// ============================================================
// Section 6: Switch Expressions vs If-Else
// ============================================================

/*
## Switch Expressions vs If-Else

- **When to use switch**:
    - Matching a single variable against discrete values
    - Enum dispatch (exhaustiveness guaranteed)
    - Replacing long if-else chains that compare the same variable
    - When you need an expression that produces a value
- **When to use if-else**:
    - Range checks (`x > 10 && x < 20`) — switch cannot do ranges
    - Complex boolean conditions involving multiple variables
    - Null checks combined with method calls
    - Conditions that are not equality-based
- **Performance**: At the bytecode level, `switch` on integers
  uses `tableswitch` (O(1) jump table for dense ranges) or
  `lookupswitch` (O(log n) binary search for sparse values).
  Chained if-else compiles to sequential comparisons (O(n)).
  For enums and integers, switch is faster.
- **Readability**: Switch expressions make the structure explicit:
  "this variable can be one of these values, and for each we do
  this." If-else chains obscure this pattern.
- **Migration guidance**: Converting old switch statements to
  new expressions is mostly mechanical:
    1. Remove `break` statements
    2. Replace `:` with `->`
    3. Group fall-through cases with commas
    4. Assign the switch to a variable (expression form)
    5. Replace local variable assignment with `yield` if needed
*/

public class SwitchExpressions {

    // ---- Section 1: Season enum ----

    enum Season { SPRING, SUMMER, AUTUMN, WINTER }

    // ---- Section 2: Day enum ----

    enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

    // ---- Section 3: Priority enum ----

    enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    // ---- Section 4: HttpStatus enum with code field ----

    enum HttpStatus {
        OK(200), CREATED(201), BAD_REQUEST(400), NOT_FOUND(404), INTERNAL_ERROR(500);

        private final int code;
        HttpStatus(int code) { this.code = code; }
        int code() { return code; }
    }

    // ---- Section 5: Command sealed interface ----

    sealed interface Command permits Login, Logout, Purchase, Refund {}
    record Login(String username) implements Command {}
    record Logout(String username) implements Command {}
    record Purchase(String item, double price) implements Command {}
    record Refund(String orderId, double amount) implements Command {}

    // ============================================================
    // Section 1: Traditional Switch Problems
    // ============================================================

    static void traditionalSwitchProblems() {
        System.out.println("=== Section 1: Traditional Switch Problems ===");

        // Old-style switch with break — counting season days
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

        // Fall-through bug demonstration
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

        // Old-style: returning a value requires a variable
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

        // Teaser: same logic with arrow syntax (preview of Section 2)
        System.out.println("\n--- New arrow syntax teaser ---");
        var dayTypeNew = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
            case SATURDAY, SUNDAY -> "Weekend";
        };
        System.out.println(day + " is a " + dayTypeNew + " (arrow syntax)");
    }

    // ============================================================
    // Section 2: Arrow Labels and Expression Form
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

        // Day → description mapping with arrow syntax
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

        // Switch expression with var
        System.out.println("\n--- Switch expression assigned to var ---");
        Season season = Season.WINTER;
        var avgTemp = switch (season) {
            case SPRING -> 15.0;
            case SUMMER -> 28.0;
            case AUTUMN -> 12.0;
            case WINTER -> -2.0;
        };
        System.out.println(season + " average temperature: " + avgTemp + "°C");

        // yield in multi-line block
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

        // Switch expression in return statement of helper method
        System.out.println("\n--- Switch expression in return statement ---");
        for (Day day : Day.values()) {
            System.out.println("  " + day + " → " + dayCategory(day));
        }
    }

    // ============================================================
    // Section 3: Multiple Case Labels and Exhaustiveness
    // ============================================================

    static void multipleCaseLabelsAndExhaustiveness() {
        System.out.println("\n=== Section 3: Multiple Case Labels and Exhaustiveness ===");

        // Priority grouping: LOW+MEDIUM / HIGH+CRITICAL
        System.out.println("--- Priority grouping ---");
        for (Priority p : Priority.values()) {
            var response = switch (p) {
                case LOW, MEDIUM -> "Standard handling";
                case HIGH, CRITICAL -> "Immediate escalation";
            };
            System.out.println("  " + p + " → " + response);
        }

        // Day weekday/weekend with comma grouping
        System.out.println("\n--- Weekday/weekend with comma grouping ---");
        for (Day day : Day.values()) {
            var type = switch (day) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
                case SATURDAY, SUNDAY -> "Weekend";
            };
            System.out.println("  " + day + " → " + type);
        }

        // Exhaustive Season switch — no default needed
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
        // If someone adds a 5th season, the compiler will flag every
        // switch expression that doesn't handle it — this is the key
        // advantage over using a default branch.

        // Priority with detailed handling
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
    // Section 4: Switch with Different Types
    // ============================================================

    static void switchWithDifferentTypes() {
        System.out.println("\n=== Section 4: Switch with Different Types ===");

        // String switch for HTTP methods
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

        // Integer switch for month → quarter
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

        // Enum switch: HttpStatus → message
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

        // Null handling with case null (Java 21+)
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

        // Type pattern switch on Object (Java 21+)
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
    // Section 5: Practical Patterns
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

        // Switch in stream .map()
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

        // Stream with switch for filtering and mapping
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

        // Command dispatch on sealed interface
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

        // Season → activity conversion helper
        System.out.println("\n--- Season activity suggestions ---");
        for (Season s : Season.values()) {
            System.out.println("  " + s + " → " + seasonActivity(s));
        }

        // Nested switch: season + priority → scheduling
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
    // Section 6: Switch Expressions vs If-Else
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

        // Side-by-side: if-else vs switch for the same logic
        System.out.println("--- Side-by-side comparison ---");
        int statusCode = 404;

        // If-else approach
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

        // Switch approach
        var messageSwitch = switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
        System.out.println("  switch:  " + statusCode + " → " + messageSwitch);

        // Example where if-else is better: range check
        System.out.println("\n--- Where if-else wins: range checks ---");
        List<Integer> scores = List.of(95, 82, 71, 55, 38);
        for (int score : scores) {
            // Switch cannot express ranges — if-else is the right tool here
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

        // Example where switch wins: enum dispatch
        System.out.println("\n--- Where switch wins: enum dispatch ---");
        for (Season s : Season.values()) {
            System.out.println("  " + s + " → " + classifySeason(s));
        }
        for (Priority p : Priority.values()) {
            System.out.println("  " + p + " → " + priorityLabel(p));
        }

        // Old → new refactoring demonstration
        System.out.println("\n--- Old-style to new-style refactoring ---");
        Day day = Day.WEDNESDAY;

        // OLD style (verbose, error-prone)
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

        // NEW style (concise, safe)
        var newResult = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Workday";
            case SATURDAY, SUNDAY -> "Weekend";
        };

        System.out.println("  Old style: " + day + " → " + oldResult);
        System.out.println("  New style: " + day + " → " + newResult);
        System.out.println("  Both produce the same result: " + oldResult.equals(newResult));

        // Performance note
        System.out.println("\n--- Performance note ---");
        System.out.println("  Switch on int/enum uses tableswitch/lookupswitch bytecode");
        System.out.println("  → O(1) jump table for dense values, O(log n) binary search for sparse");
        System.out.println("  If-else chains compile to sequential comparisons → O(n)");
        System.out.println("  For enum dispatch and discrete values, switch is both faster and clearer");
    }

    // ============================================================
    // Main — run all sections
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
