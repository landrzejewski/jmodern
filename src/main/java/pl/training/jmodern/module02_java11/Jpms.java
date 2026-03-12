package pl.training.jmodern.module02_java11;

import java.io.IOException;
import java.lang.module.*;
import java.lang.reflect.Method;
import java.nio.charset.spi.CharsetProvider;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Provider;
import java.security.Security;
import java.util.*;
import java.util.spi.ToolProvider;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do JPMS — Dlaczego moduły?
// ============================================================

/*
## Wprowadzenie do JPMS — Dlaczego moduły?

- **Problemy classpath** — ścieżka klas Java była jedynym mechanizmem
  lokalizowania typów przez prawie 20 lat i miała poważne wady:
    - **Piekło JARów**: zduplikowane klasy z różnych JARów, konflikty
      wersji, brakujące zależności przechodnie — wszystko wykrywane dopiero
      w czasie wykonania z `NoClassDefFoundError` lub `ClassNotFoundException`.
    - **Brak enkapsulacji poza public/package-private**: każda publiczna
      klasa w JARze była dostępna dla każdego innego JARa na classpath.
      Wewnętrzne szczegóły implementacji (np. `sun.misc.Unsafe`) wyciekały
      poza granice bibliotek.
    - **Brak jawnych zależności**: JAR nie mógł zadeklarować, których innych
      JARów wymaga. Narzędzia i programiści musieli zgadywać graf
      zależności, co prowadziło do błędów „kolejności classpath".
- **Project Jigsaw** (JSR 376) postawił sobie za cel naprawienie tego za pomocą JPMS:
    - **Niezawodna konfiguracja** — moduły deklarują jawne zależności
      za pomocą dyrektyw `requires`, sprawdzane zarówno w czasie kompilacji, jak i przy
      uruchomieniu JVM. Brakujące zależności powodują szybki błąd zamiast przy pierwszym użyciu.
    - **Silna enkapsulacja** — tylko pakiety jawnie oznaczone jako `exports`
      są dostępne dla innych modułów. Wewnętrzne pakiety są ukryte,
      nawet jeśli ich klasy są publiczne.
    - **Skalowalna platforma** — samo JDK zostało zmodularyzowane na ~70
      modułów (`java.base`, `java.sql`, `java.net.http` itp.). Przed
      Java 9 cały `rt.jar` (~65 MB) był ładowany nawet dla programu
      „Hello World".
    - **Poprawione bezpieczeństwo i wydajność** — mniejsza powierzchnia ataku
      (nieużywane moduły nie są ładowane), szybsze uruchamianie (JVM rozwiązuje
      tylko wymagane moduły), a `jlink` może tworzyć niestandardowe obrazy
      runtime tak małe jak ~30 MB.
- **Oś czasu**:
    - Java 9 (wrzesień 2017) — wprowadzenie systemu modułów.
    - Java 11 (wrzesień 2018) — pierwsze wydanie LTS z w pełni
      dojrzałym i stabilnym JPMS. To tutaj większość przedsiębiorstw przyjęła moduły.
- **Czym jest moduł?** Nazwana, samoopisująca się kolekcja pakietów.
  Deskryptor to specjalny plik `module-info.java` umieszczony w korzeniu
  źródeł (np. `src/module-info.java`). Kompiluje się do
  `module-info.class` w korzeniu JARa.
- **Modularyzacja JDK**: każda klasa JDK należy do nazwanego modułu.
  `java.base` jest modułem fundamentalnym — jest niejawnie wymagany
  przez wszystkie inne moduły (jak `java.lang.Object` będący klasą bazową).

  Przykłady:
    - `java.lang.String` → `java.base`
    - `java.sql.Connection` → `java.sql`
    - `java.net.http.HttpClient` → `java.net.http`
    - `javax.crypto.Cipher` → `java.base`
*/

// ============================================================
// Sekcja 2: Deskryptor modułu (module-info.java) — składnia
// ============================================================

/*
## Deskryptor modułu (module-info.java) — składnia

Deskryptor modułu to plik `module-info.java` w korzeniu źródeł.
Deklaruje nazwę modułu, zależności i eksportowane pakiety.
Poniżej znajduje się kompletna referencyjna składnia z przykładami:

    // Podstawowa deklaracja modułu
    module com.example.myapp {
    }

    // Deklaruje zależność — sprawdzana w czasie kompilacji ORAZ przy uruchomieniu JVM.
    // Jeśli java.sql brakuje w grafie modułów, JVM odmówi uruchomienia.
    module com.example.myapp {
        requires java.sql;
    }

    // Zależność przechodnia (implikowana czytelność).
    // Każdy moduł wymagający com.example.myapp automatycznie uzyskuje
    // dostęp do java.logging — nie trzeba go wymagać osobno.
    module com.example.myapp {
        requires transitive java.logging;
    }

    // Zależność tylko na czas kompilacji (opcjonalna w czasie wykonania).
    // Przydatna dla procesorów adnotacji, sprawdzeń kompilacji itp.
    module com.example.myapp {
        requires static java.compiler;
    }

    // Eksportuje pakiet — czyni go dostępnym dla WSZYSTKICH innych modułów.
    // Tylko eksportowane pakiety są widoczne; wszystko inne jest ukryte.
    module com.example.myapp {
        exports com.example.api;
    }

    // Kwalifikowany eksport — dostępny TYLKO dla wskazanych modułów.
    // Przydatny dla „zaprzyjaźnionego" dostępu między własnymi modułami.
    module com.example.myapp {
        exports com.example.internal to com.example.tests;
    }

    // Otwiera pakiet dla głębokiej refleksji (setAccessible(true)).
    // Wymagane przez frameworki takie jak Jackson, Hibernate, Spring, które używają
    // refleksji do dostępu do prywatnych pól.
    module com.example.myapp {
        opens com.example.model;
    }

    // Kwalifikowane otwarcie — pozwala na refleksję tylko z określonych modułów.
    module com.example.myapp {
        opens com.example.model to com.fasterxml.jackson.databind;
    }

    // Moduł otwarty — otwiera WSZYSTKIE pakiety dla refleksji.
    // Wygodne podczas migracji, ale osłabia enkapsulację.
    open module com.example.myapp {
        exports com.example.api;
    }

    // Dostawca usług — deklaruje, że ten moduł dostarcza
    // implementację interfejsu usługi.
    module com.example.myapp {
        provides com.example.spi.MyService
            with com.example.impl.MyServiceImpl;
    }

    // Konsument usług — deklaruje, że ten moduł używa ServiceLoader
    // do odkrywania implementacji interfejsu usługi.
    module com.example.myapp {
        uses com.example.spi.MyService;
    }

    // Kompletny przykład z praktyki:
    module com.example.myapp {
        requires java.sql;
        requires transitive java.logging;
        requires static java.compiler;

        exports com.example.api;
        exports com.example.internal to com.example.tests;

        opens com.example.model;

        uses com.example.spi.Plugin;
        provides com.example.spi.Plugin
            with com.example.plugins.DefaultPlugin;
    }
*/

// ============================================================
// Sekcja 3: Typy modułów
// ============================================================

/*
## Typy modułów

W JPMS istnieją trzy rodzaje modułów, odzwierciedlające różne
etapy migracji:

1. **Moduły nazwane** (jawne moduły):
    - Mają deskryptor `module-info.java`.
    - Umieszczane na **ścieżce modułów** (`--module-path` / `-p`).
    - Pełna silna enkapsulacja: tylko pakiety oznaczone jako `exports`
      są dostępne z zewnątrz.
    - Jawne `requires` — brakujące zależności powodują szybki błąd przy uruchomieniu.

2. **Moduły automatyczne**:
    - Zwykłe JARy (bez `module-info.java`) umieszczone na
      **ścieżce modułów** (nie classpath).
    - Nazwa modułu jest wyprowadzana z:
        - Atrybutu manifestu `Automatic-Module-Name` (jeśli obecny), lub
        - nazwy pliku JAR (np. `guava-31.1.jar` → `guava`).
    - **Eksportują wszystkie pakiety** — brak enkapsulacji.
    - **Mogą czytać wszystkie inne moduły** (nazwane, automatyczne i nienazwane).
    - Służą jako most podczas migracji z classpath do modułów.

3. **Moduł nienazwany**:
    - Wszystko na **classpath** trafia do modułu nienazwanego.
    - Jest jeden moduł nienazwany na class loader.
    - Może czytać wszystkie moduły nazwane i automatyczne (ma pełny dostęp).
    - **Moduły nazwane NIE MOGĄ wymagać modułu nienazwanego** — jest to
      celowe, aby wymusić właściwą modularyzację.
    - Nie ma `module-info.java`; eksportuje wszystkie swoje pakiety (brak
      enkapsulacji).

- **Zasady interakcji**:
    - Nazwany → Nazwany: OK (poprzez dyrektywy `requires`)
    - Nazwany → Automatyczny: OK (poprzez `requires`)
    - Automatyczny → wszystko: OK (czyta wszystkie moduły)
    - Nazwany → Nienazwany: **ZABRONIONE** (celowo)
    - Nienazwany → Nazwany: OK (może czytać wszystkie nazwane moduły)

- **Strategie migracji**:
    - **Oddolna**: zacznij od modularyzacji bibliotek (zależności liściowych)
      najpierw, pracując w górę do aplikacji. Czyste, ale wolne.
    - **Odgórna**: zmodularyzuj aplikację najpierw, używając modułów
      automatycznych jako mostu dla niemodularyzowanych bibliotek. Szybsze, ale
      polega na zachowaniu modułów automatycznych.

- **Ten projekt** działa na classpath (bez `module-info.java`),
  więc wszystkie nasze klasy żyją w **module nienazwanym**. Możemy nadal
  używać Module API do inspekcji modułów JDK w czasie wykonania.
*/

// ============================================================
// Sekcja 4: Moduły JDK i Module API
// ============================================================

/*
## Moduły JDK i Module API

Java 9+ dostarcza bogate API runtime do inspekcji modułów:

- **`java.lang.Module`** — reprezentuje moduł w czasie wykonania:
    - `getName()` — nazwa modułu (null dla modułu nienazwanego)
    - `isNamed()` — true dla modułów nazwanych, false dla nienazwanych
    - `getDescriptor()` — zwraca `ModuleDescriptor` (null dla nienazwanego)
    - `isExported(String pkg)` — true jeśli pakiet jest eksportowany
    - `isOpen(String pkg)` — true jeśli pakiet jest otwarty dla refleksji
    - `canRead(Module other)` — true jeśli ten moduł może czytać inny
    - `getPackages()` — zbiór wszystkich pakietów w tym module

- **`java.lang.module.ModuleDescriptor`** — informacje o module z czasu kompilacji:
    - `name()` — nazwa modułu
    - `isOpen()` — true jeśli jest to `open module`
    - `isAutomatic()` — true dla modułów automatycznych
    - `exports()` — zbiór `Exports` (nazwa pakietu + cele)
    - `requires()` — zbiór `Requires` (nazwa modułu + modyfikatory)
    - `opens()` — zbiór `Opens` (nazwa pakietu + cele)
    - `provides()` — zbiór `Provides` (usługa + implementacje)
    - `uses()` — zbiór interfejsów usług używanych przez ten moduł

- **`ModuleLayer`** — warstwa modułów rozwiązanych razem:
    - `ModuleLayer.boot()` — warstwa rozruchowa (moduły JDK + aplikacji)
    - Warstwy mogą być nakładane dla systemów wtyczek (izolacja w stylu OSGi)
    - `modules()` — wszystkie moduły w tej warstwie

- **Wewnętrzne typy deskryptora**:
    - `ModuleDescriptor.Exports` — `source()` (pakiet), `targets()`,
      `isQualified()`
    - `ModuleDescriptor.Requires` — `name()` (moduł), `modifiers()`
      (TRANSITIVE, STATIC, MANDATED, SYNTHETIC)
    - `ModuleDescriptor.Provides` — `service()`, `providers()`
    - `ModuleDescriptor.Opens` — `source()`, `targets()`, `isQualified()`
*/

// ============================================================
// Sekcja 5: ServiceLoader z JPMS
// ============================================================

/*
## ServiceLoader z JPMS

- `ServiceLoader` (wprowadzony w Java 6) odkrywa i ładuje implementacje
  usług w czasie wykonania. JPMS jest z nim głęboko zintegrowany.
- **Tryb classpath** (przed modułami):
    - Implementacje są rejestrowane poprzez pliki w
      `META-INF/services/<pełna-kwalifikowana-nazwa-interfejsu>`.
    - Każdy plik wymienia pełne kwalifikowane nazwy klas implementacji.
- **Tryb modułowy** (JPMS):
    - `provides com.example.spi.MyService with com.example.impl.MyImpl;`
      w module-info.java zastępuje plik META-INF/services.
    - `uses com.example.spi.MyService;` jest wymagane w deskryptorze
      modułu konsumenta — bez tego `ServiceLoader.load()` w nazwanym
      module nie zwraca wyników.
    - Dostawcy JPMS są odkrywani z grafu modułów, a nie przez
      skanowanie classpath.
- **Moduł nienazwany** (classpath): `ServiceLoader` nadal działa i
  odkrywa zarówno dostawców META-INF/services, jak i opartych na modułach. Dyrektywa
  `uses` nie jest potrzebna (moduł nienazwany nie ma deskryptora).
- **JDK intensywnie używa ServiceLoader**:
    - `java.util.spi.ToolProvider` — javac, jar, jlink jako usługi
    - `java.nio.charset.spi.CharsetProvider` — dodatkowe zestawy znaków
    - `java.security.Provider` — implementacje bezpieczeństwa/kryptografii
    - `java.sql.Driver` — sterowniki JDBC (automatyczne odkrywanie)
    - `javax.tools.JavaCompiler` — API kompilatora
*/

// ============================================================
// Sekcja 6: Aspekty praktyczne — Narzędzia, migracja i flagi
// ============================================================

/*
## Aspekty praktyczne — Narzędzia, migracja i flagi

- **`jlink`** — tworzy niestandardowe obrazy runtime zawierające tylko
  moduły potrzebne Twojej aplikacji:
    - Pełne JDK: ~300 MB. Niestandardowy obraz dla prostej aplikacji: ~30-40 MB.
    - Polecenie: `jlink --module-path $JAVA_HOME/jmods:mods
      --add-modules com.example.app --output custom-jre`
    - Wynik to samowystarczalny katalog z `bin/java`.
    - Umożliwia „dostarczenie JRE z aplikacją" (brak potrzeby instalacji JRE).

- **`jdeps`** — statyczny analizator zależności:
    - `jdeps --print-module-deps myapp.jar` — wypisuje wymagane moduły
      (dane wejściowe dla jlink).
    - `jdeps --jdk-internals myapp.jar` — znajduje użycie wewnętrznych
      API JDK (np. `sun.misc.Unsafe`).
    - `jdeps -summary myapp.jar` — szybkie podsumowanie zależności modułów.

- **Flagi wiersza poleceń do dostępu do modułów**:
    - `--module-path` (`-p`): określa ścieżkę modułów (jak
      classpath, ale dla modułów).
    - `--add-modules <moduł>`: dodaje moduł do grafu modułów.
      Potrzebne dla modułów niewymaganych przechodnio.
    - `--add-exports <moduł>/<pakiet>=<cel>`: eksportuje pakiet
      w czasie wykonania (omija enkapsulację). Użyj `ALL-UNNAMED` jako celu
      dla kodu na classpath.
    - `--add-opens <moduł>/<pakiet>=<cel>`: otwiera pakiet dla
      głębokiej refleksji w czasie wykonania. Typowa poprawka dla frameworków
      reflektujących na wewnętrznych elementach JDK.
    - `--add-reads <moduł>=<cel>`: dodaje krawędź czytania w czasie wykonania.

- **Typowe problemy migracji**:
    - **Podzielone pakiety**: dwa moduły/JARy zawierające ten sam pakiet.
      System modułów zabrania tego. Poprawka: scal JARy lub zmień nazwy pakietów.
    - **Użycie wewnętrznych API JDK**: `sun.misc.Unsafe`, `com.sun.xml.*`
      itp. Są one enkapsulowane w Java 9+. Poprawka: użyj oficjalnych
      alternatyw (np. `VarHandle` zamiast `Unsafe`).
    - **Refleksja na nieotwartych pakietach**: frameworki takie jak Hibernate,
      Jackson, Spring polegają na `setAccessible(true)`. Poprawka: dodaj dyrektywy `opens`
      lub użyj flag `--add-opens`.
    - **Niestabilność nazw modułów automatycznych**: jeśli JAR biblioteki nie ma
      wpisu manifestu `Automatic-Module-Name`, wyprowadzona nazwa zależy
      od nazwy pliku i może się zmieniać między wersjami.
*/

public class Jpms {

    // ---- Typy pomocnicze dla Sekcji 5 (demonstracja koncepcji ServiceLoader) ----

    interface Greeting {
        String greet(String name);
        String language();
    }

    static class EnglishGreeting implements Greeting {
        @Override
        public String greet(String name) { return "Hello, " + name + "!"; }
        @Override
        public String language() { return "English"; }
    }

    static class PolishGreeting implements Greeting {
        @Override
        public String greet(String name) { return "Cześć, " + name + "!"; }
        @Override
        public String language() { return "Polish"; }
    }

    // ============================================================
    // Sekcja 1: Wprowadzenie do JPMS — Dlaczego moduły?
    // ============================================================

    static void introductionToJpms() {
        System.out.println("=== Introduction to JPMS — Why Modules? ===");

        // Bieżąca klasa jest w module nienazwanym (jesteśmy na classpath)
        Module currentModule = Jpms.class.getModule();
        System.out.println("Current class module:");
        System.out.println("  getModule():  " + currentModule);
        System.out.println("  isNamed():    " + currentModule.isNamed());
        System.out.println("  getName():    " + currentModule.getName());

        // Klasy JDK żyją w nazwanych modułach
        Module stringModule = String.class.getModule();
        System.out.println("\njava.lang.String module:");
        System.out.println("  isNamed(): " + stringModule.isNamed());
        System.out.println("  getName(): " + stringModule.getName());

        Module httpClientModule = java.net.http.HttpClient.class.getModule();
        System.out.println("\njava.net.http.HttpClient module:");
        System.out.println("  isNamed(): " + httpClientModule.isNamed());
        System.out.println("  getName(): " + httpClientModule.getName());

        Module sqlModule = java.sql.Connection.class.getModule();
        System.out.println("\njava.sql.Connection module:");
        System.out.println("  isNamed(): " + sqlModule.isNamed());
        System.out.println("  getName(): " + sqlModule.getName());

        // Zliczanie wszystkich modułów w warstwie rozruchowej
        ModuleLayer bootLayer = ModuleLayer.boot();
        long totalModules = bootLayer.modules().size();
        System.out.println("\nBoot layer total modules: " + totalModules);

        // Wypisanie wszystkich nazw modułów java.* (publiczne moduły API)
        var javaModules = bootLayer.modules().stream()
                .map(Module::getName)
                .filter(name -> name.startsWith("java."))
                .sorted()
                .collect(Collectors.toList());
        System.out.println("\njava.* modules (" + javaModules.size() + "):");
        javaModules.forEach(name -> System.out.println("  " + name));
    }

    // ============================================================
    // Sekcja 2: Deskryptor modułu (module-info.java) — składnia
    // ============================================================

    static void moduleDescriptorSyntax() {
        System.out.println("\n=== Module Descriptor (module-info.java) Syntax ===");

        // Inspekcja java.base — moduł fundamentalny
        Module javaBase = String.class.getModule();
        ModuleDescriptor baseDescriptor = javaBase.getDescriptor();

        System.out.println("java.base descriptor:");
        System.out.println("  name:   " + baseDescriptor.name());
        System.out.println("  isOpen: " + baseDescriptor.isOpen());
        System.out.println("  isAutomatic: " + baseDescriptor.isAutomatic());

        // Pierwsze 15 eksportów z java.base
        var baseExports = baseDescriptor.exports().stream()
                .filter(e -> !e.isQualified())  // tylko niekwalifikowane (publiczne) eksporty
                .map(ModuleDescriptor.Exports::source)
                .sorted()
                .limit(15)
                .collect(Collectors.toList());
        System.out.println("\n  first 15 (unqualified) exports:");
        baseExports.forEach(pkg -> System.out.println("    exports " + pkg));

        // Inspekcja java.sql — pokazuje przechodnie requires
        Module javaSql = java.sql.Connection.class.getModule();
        ModuleDescriptor sqlDescriptor = javaSql.getDescriptor();

        System.out.println("\njava.sql requires:");
        sqlDescriptor.requires().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
                .forEach(req -> {
                    var mods = req.modifiers().isEmpty() ? "" : " " + req.modifiers();
                    System.out.println("  requires " + req.name() + mods);
                });

        // Kwalifikowane eksporty z java.base (exports ... to ...)
        var qualifiedExports = baseDescriptor.exports().stream()
                .filter(ModuleDescriptor.Exports::isQualified)
                .sorted(Comparator.comparing(ModuleDescriptor.Exports::source))
                .limit(10)
                .collect(Collectors.toList());
        System.out.println("\njava.base qualified exports (first 10):");
        qualifiedExports.forEach(exp ->
                System.out.println("  exports " + exp.source() + " to " + exp.targets()));

        // Dostawcy usług zadeklarowani przez java.base
        var baseProvides = baseDescriptor.provides();
        System.out.println("\njava.base provides (" + baseProvides.size() + " services):");
        baseProvides.stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Provides::service))
                .limit(10)
                .forEach(p -> System.out.println("  provides " + p.service()
                        + "\n    with " + p.providers()));
    }

    // ============================================================
    // Sekcja 3: Typy modułów
    // ============================================================

    static void typesOfModules() {
        System.out.println("\n=== Types of Modules ===");

        // Potwierdzenie, że bieżąca klasa jest w module nienazwanym
        Module currentModule = Jpms.class.getModule();
        System.out.println("Current class module: " + currentModule);
        System.out.println("  isNamed(): " + currentModule.isNamed());
        System.out.println("  This is the UNNAMED module (classpath)");

        // Moduł nienazwany ma dostęp do klas modułów nazwanych
        Module javaBase = String.class.getModule();
        System.out.println("\nUnnamed module can access java.base classes:");
        System.out.println("  String.class loaded OK: " + (String.class != null));
        System.out.println("  currentModule.canRead(java.base): " + currentModule.canRead(javaBase));

        // Pobranie modułu nienazwanego class loadera
        Module classLoaderUnnamed = Jpms.class.getClassLoader().getUnnamedModule();
        System.out.println("\nClassLoader's unnamed module: " + classLoaderUnnamed);
        System.out.println("  same as our module: " + (currentModule == classLoaderUnnamed));

        // Podział modułów warstwy rozruchowej na grupy java.* vs jdk.*
        ModuleLayer bootLayer = ModuleLayer.boot();
        var modulesByPrefix = bootLayer.modules().stream()
                .collect(Collectors.groupingBy(m -> {
                    String name = m.getName();
                    if (name.startsWith("java.")) return "java.*";
                    if (name.startsWith("jdk.")) return "jdk.*";
                    return "other";
                }, Collectors.counting()));
        System.out.println("\nBoot layer module groups:");
        modulesByPrefix.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue() + " modules"));

        // Test relacji canRead()
        Module javaSql = java.sql.Connection.class.getModule();
        Module javaNetHttp = java.net.http.HttpClient.class.getModule();
        System.out.println("\ncanRead() relationships:");
        System.out.println("  unnamed → java.base:     " + currentModule.canRead(javaBase));
        System.out.println("  unnamed → java.sql:      " + currentModule.canRead(javaSql));
        System.out.println("  java.sql → java.base:    " + javaSql.canRead(javaBase));
        System.out.println("  java.sql → java.net.http:" + javaSql.canRead(javaNetHttp));

        // Wyświetlanie pakietów w naszym module nienazwanym
        var ourPackages = currentModule.getPackages();
        System.out.println("\nPackages in unnamed module (" + ourPackages.size() + "):");
        ourPackages.stream().sorted().limit(10)
                .forEach(pkg -> System.out.println("  " + pkg));
        if (ourPackages.size() > 10) {
            System.out.println("  ... and " + (ourPackages.size() - 10) + " more");
        }
    }

    // ============================================================
    // Sekcja 4: Moduły JDK i Module API
    // ============================================================

    static void jdkModulesAndApi() {
        System.out.println("\n=== JDK Modules and the Module API ===");

        // Dogłębna analiza: java.base
        Module javaBase = String.class.getModule();
        ModuleDescriptor baseDesc = javaBase.getDescriptor();

        long exportCount = baseDesc.exports().stream().filter(e -> !e.isQualified()).count();
        long qualifiedExportCount = baseDesc.exports().stream().filter(ModuleDescriptor.Exports::isQualified).count();
        int packageCount = javaBase.getPackages().size();
        int usesCount = baseDesc.uses().size();

        System.out.println("java.base deep-dive:");
        System.out.println("  unqualified exports: " + exportCount);
        System.out.println("  qualified exports:   " + qualifiedExportCount);
        System.out.println("  total packages:      " + packageCount);
        System.out.println("  services it uses:    " + usesCount);

        // Niektóre usługi, których java.base używa
        System.out.println("  uses (first 10):");
        baseDesc.uses().stream().sorted().limit(10)
                .forEach(svc -> System.out.println("    uses " + svc));

        // Dogłębna analiza: java.sql
        Module javaSql = java.sql.Connection.class.getModule();
        ModuleDescriptor sqlDesc = javaSql.getDescriptor();

        System.out.println("\njava.sql deep-dive:");
        System.out.println("  requires:");
        sqlDesc.requires().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
                .forEach(req -> System.out.println("    requires " + req.name()
                        + (req.modifiers().isEmpty() ? "" : " " + req.modifiers())));
        System.out.println("  exports:");
        sqlDesc.exports().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Exports::source))
                .forEach(exp -> System.out.println("    exports " + exp.source()
                        + (exp.isQualified() ? " to " + exp.targets() : "")));

        // Sprawdzanie widoczności eksportów w java.base
        System.out.println("\nExport visibility checks on java.base:");
        System.out.println("  isExported(\"java.lang\"):          " + javaBase.isExported("java.lang"));
        System.out.println("  isExported(\"java.util\"):           " + javaBase.isExported("java.util"));
        System.out.println("  isExported(\"sun.security.ssl\"):    " + javaBase.isExported("sun.security.ssl"));
        System.out.println("  isExported(\"jdk.internal.misc\"):   " + javaBase.isExported("jdk.internal.misc"));

        // Znalezienie 5 największych modułów JDK według liczby niekwalifikowanych eksportów
        ModuleLayer bootLayer = ModuleLayer.boot();
        System.out.println("\nTop 5 JDK modules by unqualified export count:");
        bootLayer.modules().stream()
                .filter(m -> m.getDescriptor() != null)
                .sorted(Comparator.<Module>comparingLong(m ->
                        m.getDescriptor().exports().stream()
                                .filter(e -> !e.isQualified()).count())
                        .reversed())
                .limit(5)
                .forEach(m -> {
                    long count = m.getDescriptor().exports().stream()
                            .filter(e -> !e.isQualified()).count();
                    System.out.println("  " + m.getName() + ": " + count + " exports");
                });

        // Sprawdzanie isOpen w java.base
        System.out.println("\nReflection openness checks on java.base:");
        System.out.println("  isOpen(\"java.lang\"): " + javaBase.isOpen("java.lang"));
        System.out.println("  isOpen(\"java.util\"): " + javaBase.isOpen("java.util"));
        System.out.println("  (java.base is NOT an open module — deep reflection is restricted)");
    }

    // ============================================================
    // Sekcja 5: ServiceLoader z JPMS
    // ============================================================

    static void serviceLoaderWithJpms() {
        System.out.println("\n=== ServiceLoader with JPMS ===");

        // Odkrywanie dostawców narzędzi JDK poprzez ServiceLoader
        System.out.println("--- JDK ToolProvider services ---");
        ServiceLoader<ToolProvider> toolProviders = ServiceLoader.load(ToolProvider.class);
        System.out.println("Discovered tool providers:");
        toolProviders.forEach(tp ->
                System.out.println("  " + tp.name() + " (" + tp.getClass().getModule().getName() + ")"));

        // Znalezienie konkretnego narzędzia — javac
        var javacTool = ToolProvider.findFirst("javac");
        System.out.println("\nToolProvider.findFirst(\"javac\"): " + javacTool.map(ToolProvider::name).orElse("not found"));

        var jarTool = ToolProvider.findFirst("jar");
        System.out.println("ToolProvider.findFirst(\"jar\"):   " + jarTool.map(ToolProvider::name).orElse("not found"));

        var jlinkTool = ToolProvider.findFirst("jlink");
        System.out.println("ToolProvider.findFirst(\"jlink\"): " + jlinkTool.map(ToolProvider::name).orElse("not found"));

        // Odkrywanie dostawców zestawów znaków
        System.out.println("\n--- CharsetProvider services ---");
        ServiceLoader<CharsetProvider> charsetProviders = ServiceLoader.load(CharsetProvider.class);
        var charsetProviderList = charsetProviders.stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        System.out.println("CharsetProvider count: " + charsetProviderList.size());
        charsetProviderList.forEach(cp ->
                System.out.println("  " + cp.getClass().getName()
                        + " (module: " + cp.getClass().getModule().getName() + ")"));

        // Dostawcy bezpieczeństwa (ładowani inaczej, ale ilustrują koncepcję)
        System.out.println("\n--- Security Providers ---");
        var securityProviders = Security.getProviders();
        System.out.println("Security provider count: " + securityProviders.length);
        Arrays.stream(securityProviders).limit(5).forEach(p ->
                System.out.println("  " + p.getName() + " v" + p.getVersionStr()
                        + " (module: " + p.getClass().getModule().getName() + ")"));
        if (securityProviders.length > 5) {
            System.out.println("  ... and " + (securityProviders.length - 5) + " more");
        }

        // Demonstracja koncepcji ServiceLoader z klasami wewnętrznymi
        System.out.println("\n--- ServiceLoader concept (inner class demo) ---");
        // W rzeczywistym module użyłbyś: provides Greeting with EnglishGreeting, PolishGreeting;
        // Ponieważ jesteśmy na classpath, symulujemy koncepcję:
        List<Greeting> greetings = List.of(new EnglishGreeting(), new PolishGreeting());
        System.out.println("Simulated service implementations:");
        for (var greeting : greetings) {
            System.out.println("  [" + greeting.language() + "] " + greeting.greet("Java"));
        }
        System.out.println("  (in a modular app, ServiceLoader.load(Greeting.class) would discover these)");

        // Wypisanie deklaracji provides z java.base
        System.out.println("\n--- java.base provides declarations ---");
        ModuleDescriptor baseDesc = String.class.getModule().getDescriptor();
        baseDesc.provides().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Provides::service))
                .forEach(p -> System.out.println("  provides " + p.service()
                        + "\n    with " + p.providers()));
    }

    // ============================================================
    // Sekcja 6: Aspekty praktyczne — Narzędzia, migracja i flagi
    // ============================================================

    static void practicalAspects() {
        System.out.println("\n=== Practical Aspects — Tools, Migration, and Flags ===");

        // Wersja runtime
        Runtime.Version version = Runtime.version();
        System.out.println("Runtime.version(): " + version);
        System.out.println("  feature: " + version.feature());
        System.out.println("  interim: " + version.interim());
        System.out.println("  update:  " + version.update());

        // Budowanie mapowania klasa-moduł dla typowych klas JDK
        System.out.println("\n--- Class-to-module mapping ---");
        var classModuleMap = new LinkedHashMap<String, String>();
        classModuleMap.put("java.lang.String", String.class.getModule().getName());
        classModuleMap.put("java.util.List", List.class.getModule().getName());
        classModuleMap.put("java.sql.Connection", java.sql.Connection.class.getModule().getName());
        classModuleMap.put("java.net.http.HttpClient", java.net.http.HttpClient.class.getModule().getName());
        classModuleMap.put("javax.crypto.Cipher", javax.crypto.Cipher.class.getModule().getName());
        classModuleMap.put("java.util.logging.Logger", java.util.logging.Logger.class.getModule().getName());
        classModuleMap.put("javax.xml.parsers.DocumentBuilder", javax.xml.parsers.DocumentBuilderFactory.class.getModule().getName());
        classModuleMap.put("java.lang.management.ManagementFactory", java.lang.management.ManagementFactory.class.getModule().getName());

        classModuleMap.forEach((cls, mod) ->
                System.out.printf("  %-45s → %s%n", cls, mod));

        // Obliczanie minimalnego zestawu modułów (symulacja wyniku jdeps)
        System.out.println("\n--- Minimal module set (simulating jdeps) ---");
        var usedModules = new TreeSet<String>();
        usedModules.add(String.class.getModule().getName());                   // java.base
        usedModules.add(java.sql.Connection.class.getModule().getName());      // java.sql
        usedModules.add(java.net.http.HttpClient.class.getModule().getName()); // java.net.http
        System.out.println("Modules used by this application:");
        usedModules.forEach(m -> System.out.println("  " + m));
        System.out.println("jdeps equivalent: --add-modules " + String.join(",", usedModules));

        // Próba refleksyjnego dostępu do wewnętrznej klasy JDK → przechwycenie błędu enkapsulacji
        System.out.println("\n--- Encapsulation in action ---");
        try {
            // sun.security.ssl.SSLContextImpl jest klasą wewnętrzną
            Class<?> internalClass = Class.forName("sun.security.ssl.SSLContextImpl");
            var constructor = internalClass.getDeclaredConstructor();
            constructor.setAccessible(true);  // To powinno się nie powieść z InaccessibleObjectException
            System.out.println("  (unexpected) created internal class instance");
        } catch (Exception e) {
            System.out.println("  Attempted: reflective access to sun.security.ssl.SSLContextImpl");
            System.out.println("  Result:    " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  Fix:       --add-opens java.base/sun.security.ssl=ALL-UNNAMED");
        }

        // Podsumowanie flag wiersza poleceń
        System.out.println("\n--- JPMS command-line flags summary ---");
        System.out.println("  --module-path (-p) <path>          Set the module path");
        System.out.println("  --add-modules <module>(,<module>)* Add root modules");
        System.out.println("  --add-exports <mod>/<pkg>=<target> Export package at runtime");
        System.out.println("  --add-opens <mod>/<pkg>=<target>   Open package for reflection");
        System.out.println("  --add-reads <mod>=<target>         Add read edge at runtime");
        System.out.println("  --patch-module <mod>=<path>        Override module contents");
        System.out.println("  --list-modules                     List observable modules");
        System.out.println("  --describe-module (-d) <mod>       Describe a module");
        System.out.println("  --show-module-resolution           Show module resolution log");
        System.out.println("\nCommon targets:");
        System.out.println("  ALL-UNNAMED  — all code on the classpath");
        System.out.println("  ALL-MODULE-PATH — all modules on the module path");

        // Podsumowanie jlink
        System.out.println("\n--- jlink: custom runtime images ---");
        ModuleLayer bootLayer = ModuleLayer.boot();
        long totalJdkModules = bootLayer.modules().size();
        System.out.println("  Full JDK has " + totalJdkModules + " modules (~300 MB)");
        System.out.println("  A minimal app might need only 3 modules (~30-40 MB)");
        System.out.println("  jlink command:");
        System.out.println("    jlink --module-path $JAVA_HOME/jmods:mods \\");
        System.out.println("      --add-modules " + String.join(",", usedModules) + " \\");
        System.out.println("      --output custom-jre");
    }

    // ============================================================
    // Sekcja 7: Praktyczne tworzenie modułów — kompilacja, pakowanie, ładowanie
    // ============================================================

    /*
    ## Praktyczne tworzenie modułów — kompilacja, pakowanie, ładowanie

    Wszystkie poprzednie sekcje inspekowały moduły JDK z classpath.
    Ta sekcja idzie dalej: **programistycznie tworzy, kompiluje,
    pakuje i ładuje** trzy moduły Java w całości w czasie wykonania.

    **Przykład trzech modułów**:
        com.training.api       — eksportuje interfejs `MessageService`
        com.training.provider  — wymaga api, dostarcza MessageService
                                 z EnglishMessageService + PolishMessageService
        com.training.app       — wymaga api, używa MessageService przez ServiceLoader

    **Kluczowe dyrektywy JPMS zademonstrowane**:
        exports, requires, provides...with, uses

    **Kroki wykonywane programistycznie**:
    1. Tworzenie katalogu tymczasowego z podkatalogami `src/`, `out/`, `mods/`
    2. Zapis plików źródłowych (module-info.java + klasy Java) przy użyciu bloków tekstowych
    3. Kompilacja wszystkich modułów za pomocą `ToolProvider("javac")` i `--module-source-path`
    4. Pakowanie każdego modułu do modularnego JARa za pomocą `ToolProvider("jar")`
    5. Ładowanie modułów w czasie wykonania przez `ModuleFinder` + `Configuration` + `ModuleLayer`
    6. Inspekcja wynikowych deskryptorów modułów (exports, requires, provides, uses)
    7. Refleksyjne wywołanie `Main.run()` — odkrywa implementacje usług
       przez `ServiceLoader`, udowadniając, że `provides...with` + `uses` działają od początku do końca
    8. Czyszczenie plików tymczasowych

    To jest **pełny cykl życia Jigsaw** bez opuszczania JVM.
    */

    private static void writeSource(Path srcRoot, String moduleName, String packagePath,
                                     String fileName, String source) throws IOException {
        Path dir = srcRoot.resolve(moduleName);
        if (!packagePath.isEmpty()) {
            dir = dir.resolve(packagePath.replace('.', '/'));
        }
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), source);
    }

    static void practicalModuleCreation() {
        System.out.println("\n=== Practical Module Creation — Compile, Package, Load ===");

        Path tempDir = null;
        try {
            // Krok 1: Tworzenie struktury katalogu tymczasowego
            tempDir = Files.createTempDirectory("jpms-demo-");
            Path srcDir = Files.createDirectory(tempDir.resolve("src"));
            Path outDir = Files.createDirectory(tempDir.resolve("out"));
            Path modsDir = Files.createDirectory(tempDir.resolve("mods"));
            System.out.println("Temp directory: " + tempDir);

            // Krok 2: Zapis plików źródłowych dla trzech modułów

            // --- com.training.api ---
            writeSource(srcDir, "com.training.api", "",
                    "module-info.java", """
                    module com.training.api {
                        exports com.training.api;
                    }
                    """);

            writeSource(srcDir, "com.training.api", "com.training.api",
                    "MessageService.java", """
                    package com.training.api;

                    public interface MessageService {
                        String getMessage();
                        String language();
                    }
                    """);

            // --- com.training.provider ---
            writeSource(srcDir, "com.training.provider", "",
                    "module-info.java", """
                    module com.training.provider {
                        requires com.training.api;
                        provides com.training.api.MessageService
                            with com.training.provider.EnglishMessageService,
                                 com.training.provider.PolishMessageService;
                    }
                    """);

            writeSource(srcDir, "com.training.provider", "com.training.provider",
                    "EnglishMessageService.java", """
                    package com.training.provider;

                    import com.training.api.MessageService;

                    public class EnglishMessageService implements MessageService {
                        @Override
                        public String getMessage() { return "Hello from the modular world!"; }
                        @Override
                        public String language() { return "English"; }
                    }
                    """);

            writeSource(srcDir, "com.training.provider", "com.training.provider",
                    "PolishMessageService.java", """
                    package com.training.provider;

                    import com.training.api.MessageService;

                    public class PolishMessageService implements MessageService {
                        @Override
                        public String getMessage() { return "Witaj ze świata modułów!"; }
                        @Override
                        public String language() { return "Polish"; }
                    }
                    """);

            // --- com.training.app ---
            // Eksportujemy com.training.app, abyśmy mogli refleksyjnie wywołać Main.run() z tej demonstracji.
            // W rzeczywistej aplikacji moduł app byłby punktem wejścia (--module com.training.app/...)
            // i NIE musiałby eksportować swojego pakietu.
            writeSource(srcDir, "com.training.app", "",
                    "module-info.java", """
                    module com.training.app {
                        requires com.training.api;
                        uses com.training.api.MessageService;
                        exports com.training.app;
                    }
                    """);

            writeSource(srcDir, "com.training.app", "com.training.app",
                    "Main.java", """
                    package com.training.app;

                    import com.training.api.MessageService;
                    import java.lang.ModuleLayer;
                    import java.util.ServiceLoader;

                    public class Main {
                        public static String run(ModuleLayer layer) {
                            StringBuilder sb = new StringBuilder();
                            // Użycie ServiceLoader.load(layer, service) do odkrycia dostawców w naszej niestandardowej warstwie
                            ServiceLoader<MessageService> loader = ServiceLoader.load(layer, MessageService.class);
                            loader.forEach(svc ->
                                sb.append("  [").append(svc.language()).append("] ").append(svc.getMessage()).append("\\n")
                            );
                            if (sb.isEmpty()) {
                                sb.append("  (no implementations found)\\n");
                            }
                            return sb.toString();
                        }
                    }
                    """);

            System.out.println("Source files written (3 modules, 7 files)");

            // Krok 3: Kompilacja wszystkich modułów za pomocą javac --module-source-path
            var javac = ToolProvider.findFirst("javac")
                    .orElseThrow(() -> new RuntimeException("javac ToolProvider not found"));

            int compileResult = javac.run(System.out, System.err,
                    "--module-source-path", srcDir.toString(),
                    "-d", outDir.toString(),
                    "--module", "com.training.api,com.training.provider,com.training.app");

            if (compileResult != 0) {
                System.out.println("Compilation FAILED (exit code " + compileResult + ")");
                return;
            }
            System.out.println("Compilation successful (all 3 modules compiled)");

            // Krok 4: Pakowanie każdego modułu do modularnego JARa
            var jar = ToolProvider.findFirst("jar")
                    .orElseThrow(() -> new RuntimeException("jar ToolProvider not found"));

            String[] moduleNames = {"com.training.api", "com.training.provider", "com.training.app"};
            for (String moduleName : moduleNames) {
                Path jarFile = modsDir.resolve(moduleName + ".jar");
                Path moduleOut = outDir.resolve(moduleName);
                int jarResult = jar.run(System.out, System.err,
                        "--create",
                        "--file", jarFile.toString(),
                        "-C", moduleOut.toString(), ".");
                if (jarResult != 0) {
                    System.out.println("JAR packaging FAILED for " + moduleName);
                    return;
                }
            }
            System.out.println("JAR packaging successful (3 modular JARs created)");

            // Krok 5: Ładowanie modułów w czasie wykonania przez ModuleFinder + Configuration + ModuleLayer
            ModuleFinder finder = ModuleFinder.of(modsDir);

            // Pokazanie co finder odkrył
            System.out.println("\nModuleFinder discovered:");
            finder.findAll().stream()
                    .sorted(Comparator.comparing(ref -> ref.descriptor().name()))
                    .forEach(ref -> System.out.println("  " + ref.descriptor().name()
                            + " (" + ref.location().map(Object::toString).orElse("?") + ")"));

            // Rozwiązywanie grafu modułów
            ModuleLayer parentLayer = ModuleLayer.boot();
            Configuration parentConfig = parentLayer.configuration();
            Configuration config = parentConfig.resolve(
                    finder,
                    ModuleFinder.of(),  // pusty after-finder
                    Set.of("com.training.app", "com.training.provider", "com.training.api"));

            // Tworzenie nowej ModuleLayer z własnym class loaderem
            ModuleLayer layer = parentLayer.defineModulesWithOneLoader(
                    config, ClassLoader.getSystemClassLoader());

            System.out.println("\nCustom ModuleLayer created with " + layer.modules().size() + " modules");

            // Krok 6: Inspekcja deskryptorów modułów
            System.out.println("\n--- Module descriptors ---");
            layer.modules().stream()
                    .sorted(Comparator.comparing(Module::getName))
                    .forEach(mod -> {
                        ModuleDescriptor desc = mod.getDescriptor();
                        System.out.println("\n  module " + desc.name() + " {");
                        desc.requires().stream()
                                .filter(r -> !r.modifiers().contains(ModuleDescriptor.Requires.Modifier.MANDATED))
                                .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
                                .forEach(r -> System.out.println("      requires " + r.name() + ";"));
                        desc.exports().forEach(e ->
                                System.out.println("      exports " + e.source() + ";"));
                        desc.uses().forEach(u ->
                                System.out.println("      uses " + u + ";"));
                        desc.provides().forEach(p ->
                                System.out.println("      provides " + p.service()
                                        + " with " + String.join(", ", p.providers()) + ";"));
                        System.out.println("  }");
                    });

            // Krok 7: Refleksyjne wywołanie Main.run(layer) w celu demonstracji ServiceLoader
            System.out.println("\n--- Invoking com.training.app.Main.run(layer) ---");
            Class<?> mainClass = layer.findLoader("com.training.app")
                    .loadClass("com.training.app.Main");
            Method runMethod = mainClass.getMethod("run", ModuleLayer.class);
            String output = (String) runMethod.invoke(null, layer);
            System.out.println("ServiceLoader discovered implementations:");
            System.out.print(output);

            System.out.println("\nFull Jigsaw lifecycle completed: write → compile → package → load → invoke");

        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        } finally {
            // Krok 8: Czyszczenie plików tymczasowych
            if (tempDir != null) {
                try {
                    Path dir = tempDir;
                    Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
                            Files.delete(d);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    System.out.println("Temp directory cleaned up");
                } catch (IOException e) {
                    System.out.println("Cleanup warning: " + e.getMessage());
                }
            }
        }
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) {
        introductionToJpms();
        moduleDescriptorSyntax();
        typesOfModules();
        jdkModulesAndApi();
        serviceLoaderWithJpms();
        practicalAspects();
        practicalModuleCreation();
    }
}
