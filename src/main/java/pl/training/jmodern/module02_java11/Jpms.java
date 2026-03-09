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
// Section 1: Introduction to JPMS — Why Modules?
// ============================================================

/*
## Introduction to JPMS — Why Modules?

- **Classpath problems** — the Java class path was the only mechanism
  for locating types for nearly 20 years, and it had serious flaws:
    - **JAR hell**: duplicate classes from different JARs, version
      conflicts, missing transitive dependencies — all detected only
      at runtime with `NoClassDefFoundError` or `ClassNotFoundException`.
    - **No encapsulation beyond public/package-private**: any public
      class in a JAR was accessible to every other JAR on the classpath.
      Internal implementation details (e.g., `sun.misc.Unsafe`) leaked
      across library boundaries.
    - **No explicit dependencies**: a JAR could not declare which other
      JARs it required. Tools and developers had to guess the dependency
      graph, leading to "classpath ordering" bugs.
- **Project Jigsaw** (JSR 376) set out to fix these with JPMS:
    - **Reliable configuration** — modules declare explicit dependencies
      via `requires` directives, checked at both compile time and JVM
      startup. Missing dependencies fail fast rather than at first use.
    - **Strong encapsulation** — only packages explicitly `exports`-ed
      are accessible to other modules. Internal packages are hidden,
      even if their classes are public.
    - **Scalable platform** — the JDK itself was modularized into ~70
      modules (`java.base`, `java.sql`, `java.net.http`, etc.). Before
      Java 9, the entire `rt.jar` (~65 MB) was loaded even for a
      "Hello World" program.
    - **Improved security and performance** — smaller attack surface
      (unused modules are not loaded), faster startup (the JVM resolves
      only required modules), and `jlink` can create custom runtime
      images as small as ~30 MB.
- **Timeline**:
    - Java 9 (September 2017) — introduced the module system.
    - Java 11 (September 2018) — first LTS release with JPMS fully
      baked and stable. This is where most enterprises adopted modules.
- **What is a module?** A named, self-describing collection of packages.
  The descriptor is a special file `module-info.java` placed at the
  source root (e.g., `src/module-info.java`). It compiles to
  `module-info.class` in the root of the JAR.
- **JDK modularization**: every JDK class belongs to a named module.
  `java.base` is the foundational module — it is implicitly required
  by all other modules (like `java.lang.Object` being the root class).

  Examples:
    - `java.lang.String` → `java.base`
    - `java.sql.Connection` → `java.sql`
    - `java.net.http.HttpClient` → `java.net.http`
    - `javax.crypto.Cipher` → `java.base`
*/

// ============================================================
// Section 2: Module Descriptor (module-info.java) Syntax
// ============================================================

/*
## Module Descriptor (module-info.java) Syntax

A module descriptor is a `module-info.java` file at the source root.
It declares the module's name, dependencies, and exported packages.
Below is a comprehensive syntax reference with examples:

    // Basic module declaration
    module com.example.myapp {
    }

    // Declares a dependency — checked at compile time AND JVM startup.
    // If java.sql is missing from the module graph, the JVM refuses to start.
    module com.example.myapp {
        requires java.sql;
    }

    // Transitive dependency (implied readability).
    // Any module that requires com.example.myapp automatically gets
    // access to java.logging — no need to require it separately.
    module com.example.myapp {
        requires transitive java.logging;
    }

    // Compile-time only dependency (optional at runtime).
    // Useful for annotation processors, compile-time checks, etc.
    module com.example.myapp {
        requires static java.compiler;
    }

    // Exports a package — makes it accessible to ALL other modules.
    // Only exported packages are visible; everything else is hidden.
    module com.example.myapp {
        exports com.example.api;
    }

    // Qualified export — accessible ONLY to specified modules.
    // Useful for "friend" access between your own modules.
    module com.example.myapp {
        exports com.example.internal to com.example.tests;
    }

    // Opens a package for deep reflection (setAccessible(true)).
    // Needed by frameworks like Jackson, Hibernate, Spring that use
    // reflection to access private fields.
    module com.example.myapp {
        opens com.example.model;
    }

    // Qualified opens — allows reflection only from specific modules.
    module com.example.myapp {
        opens com.example.model to com.fasterxml.jackson.databind;
    }

    // Open module — opens ALL packages for reflection.
    // Convenient during migration but weakens encapsulation.
    open module com.example.myapp {
        exports com.example.api;
    }

    // Service provider — declares that this module provides an
    // implementation for a service interface.
    module com.example.myapp {
        provides com.example.spi.MyService
            with com.example.impl.MyServiceImpl;
    }

    // Service consumer — declares that this module uses ServiceLoader
    // to discover implementations of a service interface.
    module com.example.myapp {
        uses com.example.spi.MyService;
    }

    // Complete real-world example:
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
// Section 3: Types of Modules
// ============================================================

/*
## Types of Modules

There are three kinds of modules in JPMS, reflecting different
migration stages:

1. **Named modules** (explicit modules):
    - Have a `module-info.java` descriptor.
    - Placed on the **module path** (`--module-path` / `-p`).
    - Full strong encapsulation: only `exports`-ed packages are
      accessible from outside.
    - Explicit `requires` — missing dependencies fail fast at startup.

2. **Automatic modules**:
    - Regular JARs (without `module-info.java`) placed on the
      **module path** (not classpath).
    - The module name is derived from:
        - `Automatic-Module-Name` manifest attribute (if present), or
        - the JAR filename (e.g., `guava-31.1.jar` → `guava`).
    - **Export all packages** — no encapsulation.
    - **Can read all other modules** (named, automatic, and unnamed).
    - Serve as a bridge during migration from classpath to modules.

3. **Unnamed module**:
    - Everything on the **classpath** goes into the unnamed module.
    - There is one unnamed module per class loader.
    - Can read all named and automatic modules (has full access).
    - **Named modules CANNOT require the unnamed module** — this is
      by design, to force proper modularization.
    - Has no `module-info.java`; exports all its packages (no
      encapsulation).

- **Interaction rules**:
    - Named → Named: OK (via `requires` directives)
    - Named → Automatic: OK (via `requires`)
    - Automatic → everything: OK (reads all modules)
    - Named → Unnamed: **FORBIDDEN** (by design)
    - Unnamed → Named: OK (can read all named modules)

- **Migration strategies**:
    - **Bottom-up**: start by modularizing libraries (leaf dependencies)
      first, working up to the application. Clean but slow.
    - **Top-down**: modularize the application first, using automatic
      modules as a bridge for un-modularized libraries. Faster but
      relies on automatic module behavior.

- **This project** runs on the classpath (no `module-info.java`),
  so all our classes live in the **unnamed module**. We can still
  use the Module API to inspect JDK modules at runtime.
*/

// ============================================================
// Section 4: JDK Modules and the Module API
// ============================================================

/*
## JDK Modules and the Module API

Java 9+ provides a rich runtime API for inspecting modules:

- **`java.lang.Module`** — represents a runtime module:
    - `getName()` — module name (null for unnamed module)
    - `isNamed()` — true for named modules, false for unnamed
    - `getDescriptor()` — returns `ModuleDescriptor` (null for unnamed)
    - `isExported(String pkg)` — true if the package is exported
    - `isOpen(String pkg)` — true if the package is open for reflection
    - `canRead(Module other)` — true if this module can read other
    - `getPackages()` — set of all packages in this module

- **`java.lang.module.ModuleDescriptor`** — compile-time module info:
    - `name()` — module name
    - `isOpen()` — true if it is an `open module`
    - `isAutomatic()` — true for automatic modules
    - `exports()` — set of `Exports` (package name + targets)
    - `requires()` — set of `Requires` (module name + modifiers)
    - `opens()` — set of `Opens` (package name + targets)
    - `provides()` — set of `Provides` (service + implementations)
    - `uses()` — set of service interfaces this module uses

- **`ModuleLayer`** — a layer of modules resolved together:
    - `ModuleLayer.boot()` — the boot layer (JDK + application modules)
    - Layers can be stacked for plugin systems (OSGi-like isolation)
    - `modules()` — all modules in this layer

- **Inner descriptor types**:
    - `ModuleDescriptor.Exports` — `source()` (package), `targets()`,
      `isQualified()`
    - `ModuleDescriptor.Requires` — `name()` (module), `modifiers()`
      (TRANSITIVE, STATIC, MANDATED, SYNTHETIC)
    - `ModuleDescriptor.Provides` — `service()`, `providers()`
    - `ModuleDescriptor.Opens` — `source()`, `targets()`, `isQualified()`
*/

// ============================================================
// Section 5: ServiceLoader with JPMS
// ============================================================

/*
## ServiceLoader with JPMS

- `ServiceLoader` (introduced in Java 6) discovers and loads service
  implementations at runtime. JPMS integrates deeply with it.
- **Classpath mode** (pre-modules):
    - Implementations are registered via files in
      `META-INF/services/<fully-qualified-interface-name>`.
    - Each file lists the fully-qualified class names of implementations.
- **Module mode** (JPMS):
    - `provides com.example.spi.MyService with com.example.impl.MyImpl;`
      in module-info.java replaces the META-INF/services file.
    - `uses com.example.spi.MyService;` is required in the consuming
      module's descriptor — without it, `ServiceLoader.load()` in a
      named module returns no results.
    - JPMS providers are discovered from the module graph, not by
      scanning the classpath.
- **Unnamed module** (classpath): `ServiceLoader` still works and
  discovers both META-INF/services and module-based providers. No
  `uses` directive is needed (unnamed module has no descriptor).
- **JDK uses ServiceLoader extensively**:
    - `java.util.spi.ToolProvider` — javac, jar, jlink as services
    - `java.nio.charset.spi.CharsetProvider` — additional charsets
    - `java.security.Provider` — security/crypto implementations
    - `java.sql.Driver` — JDBC drivers (automatic discovery)
    - `javax.tools.JavaCompiler` — compiler API
*/

// ============================================================
// Section 6: Practical Aspects — Tools, Migration, and Flags
// ============================================================

/*
## Practical Aspects — Tools, Migration, and Flags

- **`jlink`** — creates custom runtime images containing only the
  modules your application needs:
    - Full JDK: ~300 MB. Custom image for a simple app: ~30-40 MB.
    - Command: `jlink --module-path $JAVA_HOME/jmods:mods
      --add-modules com.example.app --output custom-jre`
    - The output is a self-contained directory with `bin/java`.
    - Enables "ship the JRE with the app" (no JRE installation needed).

- **`jdeps`** — static dependency analyzer:
    - `jdeps --print-module-deps myapp.jar` — lists required modules
      (input for jlink).
    - `jdeps --jdk-internals myapp.jar` — finds usage of internal
      JDK APIs (e.g., `sun.misc.Unsafe`).
    - `jdeps -summary myapp.jar` — quick summary of module dependencies.

- **Command-line flags for module access**:
    - `--module-path` (`-p`): specifies the module path (like
      classpath but for modules).
    - `--add-modules <module>`: adds a module to the module graph.
      Needed for modules not required transitively.
    - `--add-exports <module>/<package>=<target>`: exports a package
      at runtime (bypasses encapsulation). Use `ALL-UNNAMED` as target
      for classpath code.
    - `--add-opens <module>/<package>=<target>`: opens a package for
      deep reflection at runtime. Common fix for frameworks that
      reflect on JDK internals.
    - `--add-reads <module>=<target>`: adds a read edge at runtime.

- **Common migration issues**:
    - **Split packages**: two modules/JARs containing the same package.
      The module system forbids this. Fix: merge JARs or rename packages.
    - **Internal JDK API usage**: `sun.misc.Unsafe`, `com.sun.xml.*`,
      etc. These are encapsulated in Java 9+. Fix: use official
      alternatives (e.g., `VarHandle` instead of `Unsafe`).
    - **Reflection on non-open packages**: frameworks like Hibernate,
      Jackson, Spring rely on `setAccessible(true)`. Fix: add `opens`
      directives or use `--add-opens` flags.
    - **Automatic module name instability**: if a library JAR has no
      `Automatic-Module-Name` manifest entry, the derived name depends
      on the filename and may change between versions.
*/

public class Jpms {

    // ---- Helper types for Section 5 (ServiceLoader concept demo) ----

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
    // Section 1: Introduction to JPMS — Why Modules?
    // ============================================================

    static void introductionToJpms() {
        System.out.println("=== Introduction to JPMS — Why Modules? ===");

        // Current class is in the unnamed module (we're on the classpath)
        Module currentModule = Jpms.class.getModule();
        System.out.println("Current class module:");
        System.out.println("  getModule():  " + currentModule);
        System.out.println("  isNamed():    " + currentModule.isNamed());
        System.out.println("  getName():    " + currentModule.getName());

        // JDK classes live in named modules
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

        // Count total modules in the boot layer
        ModuleLayer bootLayer = ModuleLayer.boot();
        long totalModules = bootLayer.modules().size();
        System.out.println("\nBoot layer total modules: " + totalModules);

        // List all java.* module names (the public API modules)
        var javaModules = bootLayer.modules().stream()
                .map(Module::getName)
                .filter(name -> name.startsWith("java."))
                .sorted()
                .collect(Collectors.toList());
        System.out.println("\njava.* modules (" + javaModules.size() + "):");
        javaModules.forEach(name -> System.out.println("  " + name));
    }

    // ============================================================
    // Section 2: Module Descriptor (module-info.java) Syntax
    // ============================================================

    static void moduleDescriptorSyntax() {
        System.out.println("\n=== Module Descriptor (module-info.java) Syntax ===");

        // Inspect java.base — the foundational module
        Module javaBase = String.class.getModule();
        ModuleDescriptor baseDescriptor = javaBase.getDescriptor();

        System.out.println("java.base descriptor:");
        System.out.println("  name:   " + baseDescriptor.name());
        System.out.println("  isOpen: " + baseDescriptor.isOpen());
        System.out.println("  isAutomatic: " + baseDescriptor.isAutomatic());

        // First 15 exports from java.base
        var baseExports = baseDescriptor.exports().stream()
                .filter(e -> !e.isQualified())  // only unqualified (public) exports
                .map(ModuleDescriptor.Exports::source)
                .sorted()
                .limit(15)
                .collect(Collectors.toList());
        System.out.println("\n  first 15 (unqualified) exports:");
        baseExports.forEach(pkg -> System.out.println("    exports " + pkg));

        // Inspect java.sql — shows transitive requires
        Module javaSql = java.sql.Connection.class.getModule();
        ModuleDescriptor sqlDescriptor = javaSql.getDescriptor();

        System.out.println("\njava.sql requires:");
        sqlDescriptor.requires().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Requires::name))
                .forEach(req -> {
                    var mods = req.modifiers().isEmpty() ? "" : " " + req.modifiers();
                    System.out.println("  requires " + req.name() + mods);
                });

        // Qualified exports from java.base (exports ... to ...)
        var qualifiedExports = baseDescriptor.exports().stream()
                .filter(ModuleDescriptor.Exports::isQualified)
                .sorted(Comparator.comparing(ModuleDescriptor.Exports::source))
                .limit(10)
                .collect(Collectors.toList());
        System.out.println("\njava.base qualified exports (first 10):");
        qualifiedExports.forEach(exp ->
                System.out.println("  exports " + exp.source() + " to " + exp.targets()));

        // Service providers declared by java.base
        var baseProvides = baseDescriptor.provides();
        System.out.println("\njava.base provides (" + baseProvides.size() + " services):");
        baseProvides.stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Provides::service))
                .limit(10)
                .forEach(p -> System.out.println("  provides " + p.service()
                        + "\n    with " + p.providers()));
    }

    // ============================================================
    // Section 3: Types of Modules
    // ============================================================

    static void typesOfModules() {
        System.out.println("\n=== Types of Modules ===");

        // Confirm current class is in the unnamed module
        Module currentModule = Jpms.class.getModule();
        System.out.println("Current class module: " + currentModule);
        System.out.println("  isNamed(): " + currentModule.isNamed());
        System.out.println("  This is the UNNAMED module (classpath)");

        // Unnamed module can access named module classes
        Module javaBase = String.class.getModule();
        System.out.println("\nUnnamed module can access java.base classes:");
        System.out.println("  String.class loaded OK: " + (String.class != null));
        System.out.println("  currentModule.canRead(java.base): " + currentModule.canRead(javaBase));

        // Get the class loader's unnamed module
        Module classLoaderUnnamed = Jpms.class.getClassLoader().getUnnamedModule();
        System.out.println("\nClassLoader's unnamed module: " + classLoaderUnnamed);
        System.out.println("  same as our module: " + (currentModule == classLoaderUnnamed));

        // Partition boot layer modules into java.* vs jdk.* groups
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

        // Test canRead() relationships
        Module javaSql = java.sql.Connection.class.getModule();
        Module javaNetHttp = java.net.http.HttpClient.class.getModule();
        System.out.println("\ncanRead() relationships:");
        System.out.println("  unnamed → java.base:     " + currentModule.canRead(javaBase));
        System.out.println("  unnamed → java.sql:      " + currentModule.canRead(javaSql));
        System.out.println("  java.sql → java.base:    " + javaSql.canRead(javaBase));
        System.out.println("  java.sql → java.net.http:" + javaSql.canRead(javaNetHttp));

        // Show packages in our unnamed module
        var ourPackages = currentModule.getPackages();
        System.out.println("\nPackages in unnamed module (" + ourPackages.size() + "):");
        ourPackages.stream().sorted().limit(10)
                .forEach(pkg -> System.out.println("  " + pkg));
        if (ourPackages.size() > 10) {
            System.out.println("  ... and " + (ourPackages.size() - 10) + " more");
        }
    }

    // ============================================================
    // Section 4: JDK Modules and the Module API
    // ============================================================

    static void jdkModulesAndApi() {
        System.out.println("\n=== JDK Modules and the Module API ===");

        // Deep-dive: java.base
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

        // Some services java.base uses
        System.out.println("  uses (first 10):");
        baseDesc.uses().stream().sorted().limit(10)
                .forEach(svc -> System.out.println("    uses " + svc));

        // Deep-dive: java.sql
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

        // Check export visibility on java.base
        System.out.println("\nExport visibility checks on java.base:");
        System.out.println("  isExported(\"java.lang\"):          " + javaBase.isExported("java.lang"));
        System.out.println("  isExported(\"java.util\"):           " + javaBase.isExported("java.util"));
        System.out.println("  isExported(\"sun.security.ssl\"):    " + javaBase.isExported("sun.security.ssl"));
        System.out.println("  isExported(\"jdk.internal.misc\"):   " + javaBase.isExported("jdk.internal.misc"));

        // Find top 5 largest JDK modules by (unqualified) export count
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

        // Check isOpen on java.base
        System.out.println("\nReflection openness checks on java.base:");
        System.out.println("  isOpen(\"java.lang\"): " + javaBase.isOpen("java.lang"));
        System.out.println("  isOpen(\"java.util\"): " + javaBase.isOpen("java.util"));
        System.out.println("  (java.base is NOT an open module — deep reflection is restricted)");
    }

    // ============================================================
    // Section 5: ServiceLoader with JPMS
    // ============================================================

    static void serviceLoaderWithJpms() {
        System.out.println("\n=== ServiceLoader with JPMS ===");

        // Discover JDK tool providers via ServiceLoader
        System.out.println("--- JDK ToolProvider services ---");
        ServiceLoader<ToolProvider> toolProviders = ServiceLoader.load(ToolProvider.class);
        System.out.println("Discovered tool providers:");
        toolProviders.forEach(tp ->
                System.out.println("  " + tp.name() + " (" + tp.getClass().getModule().getName() + ")"));

        // Find specific tool — javac
        var javacTool = ToolProvider.findFirst("javac");
        System.out.println("\nToolProvider.findFirst(\"javac\"): " + javacTool.map(ToolProvider::name).orElse("not found"));

        var jarTool = ToolProvider.findFirst("jar");
        System.out.println("ToolProvider.findFirst(\"jar\"):   " + jarTool.map(ToolProvider::name).orElse("not found"));

        var jlinkTool = ToolProvider.findFirst("jlink");
        System.out.println("ToolProvider.findFirst(\"jlink\"): " + jlinkTool.map(ToolProvider::name).orElse("not found"));

        // Discover charset providers
        System.out.println("\n--- CharsetProvider services ---");
        ServiceLoader<CharsetProvider> charsetProviders = ServiceLoader.load(CharsetProvider.class);
        var charsetProviderList = charsetProviders.stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        System.out.println("CharsetProvider count: " + charsetProviderList.size());
        charsetProviderList.forEach(cp ->
                System.out.println("  " + cp.getClass().getName()
                        + " (module: " + cp.getClass().getModule().getName() + ")"));

        // Security providers (these are loaded differently but illustrate the concept)
        System.out.println("\n--- Security Providers ---");
        var securityProviders = Security.getProviders();
        System.out.println("Security provider count: " + securityProviders.length);
        Arrays.stream(securityProviders).limit(5).forEach(p ->
                System.out.println("  " + p.getName() + " v" + p.getVersionStr()
                        + " (module: " + p.getClass().getModule().getName() + ")"));
        if (securityProviders.length > 5) {
            System.out.println("  ... and " + (securityProviders.length - 5) + " more");
        }

        // Demonstrate the ServiceLoader concept with inner classes
        System.out.println("\n--- ServiceLoader concept (inner class demo) ---");
        // In a real module, you'd use: provides Greeting with EnglishGreeting, PolishGreeting;
        // Since we're on the classpath, we simulate the concept:
        List<Greeting> greetings = List.of(new EnglishGreeting(), new PolishGreeting());
        System.out.println("Simulated service implementations:");
        for (var greeting : greetings) {
            System.out.println("  [" + greeting.language() + "] " + greeting.greet("Java"));
        }
        System.out.println("  (in a modular app, ServiceLoader.load(Greeting.class) would discover these)");

        // Print provides declarations from java.base
        System.out.println("\n--- java.base provides declarations ---");
        ModuleDescriptor baseDesc = String.class.getModule().getDescriptor();
        baseDesc.provides().stream()
                .sorted(Comparator.comparing(ModuleDescriptor.Provides::service))
                .forEach(p -> System.out.println("  provides " + p.service()
                        + "\n    with " + p.providers()));
    }

    // ============================================================
    // Section 6: Practical Aspects — Tools, Migration, and Flags
    // ============================================================

    static void practicalAspects() {
        System.out.println("\n=== Practical Aspects — Tools, Migration, and Flags ===");

        // Runtime version
        Runtime.Version version = Runtime.version();
        System.out.println("Runtime.version(): " + version);
        System.out.println("  feature: " + version.feature());
        System.out.println("  interim: " + version.interim());
        System.out.println("  update:  " + version.update());

        // Build class-to-module mapping for common JDK classes
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

        // Compute minimal module set (simulating jdeps output)
        System.out.println("\n--- Minimal module set (simulating jdeps) ---");
        var usedModules = new TreeSet<String>();
        usedModules.add(String.class.getModule().getName());                   // java.base
        usedModules.add(java.sql.Connection.class.getModule().getName());      // java.sql
        usedModules.add(java.net.http.HttpClient.class.getModule().getName()); // java.net.http
        System.out.println("Modules used by this application:");
        usedModules.forEach(m -> System.out.println("  " + m));
        System.out.println("jdeps equivalent: --add-modules " + String.join(",", usedModules));

        // Try reflective access to internal JDK class → catch encapsulation error
        System.out.println("\n--- Encapsulation in action ---");
        try {
            // sun.security.ssl.SSLContextImpl is an internal class
            Class<?> internalClass = Class.forName("sun.security.ssl.SSLContextImpl");
            var constructor = internalClass.getDeclaredConstructor();
            constructor.setAccessible(true);  // This should fail with InaccessibleObjectException
            System.out.println("  (unexpected) created internal class instance");
        } catch (Exception e) {
            System.out.println("  Attempted: reflective access to sun.security.ssl.SSLContextImpl");
            System.out.println("  Result:    " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  Fix:       --add-opens java.base/sun.security.ssl=ALL-UNNAMED");
        }

        // Summary of command-line flags
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

        // jlink summary
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
    // Section 7: Practical Module Creation — Compile, Package, Load
    // ============================================================

    /*
    ## Practical Module Creation — Compile, Package, Load

    All previous sections inspected JDK modules from the classpath.
    This section goes further: it **programmatically creates, compiles,
    packages, and loads** three Java modules entirely at runtime.

    **Three-module example**:
        com.training.api       — exports a `MessageService` interface
        com.training.provider  — requires api, provides MessageService
                                 with EnglishMessageService + PolishMessageService
        com.training.app       — requires api, uses MessageService via ServiceLoader

    **Key JPMS directives demonstrated**:
        exports, requires, provides...with, uses

    **Steps performed programmatically**:
    1. Create a temp directory with `src/`, `out/`, `mods/` subdirectories
    2. Write source files (module-info.java + Java classes) using text blocks
    3. Compile all modules with `ToolProvider("javac")` and `--module-source-path`
    4. Package each module into a modular JAR with `ToolProvider("jar")`
    5. Load modules at runtime via `ModuleFinder` + `Configuration` + `ModuleLayer`
    6. Inspect the resulting module descriptors (exports, requires, provides, uses)
    7. Invoke `Main.run()` reflectively — it discovers service implementations
       via `ServiceLoader`, proving that `provides...with` + `uses` work end-to-end
    8. Clean up temp files

    This is the **full Jigsaw lifecycle** without leaving the JVM.
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
            // Step 1: Create temp directory structure
            tempDir = Files.createTempDirectory("jpms-demo-");
            Path srcDir = Files.createDirectory(tempDir.resolve("src"));
            Path outDir = Files.createDirectory(tempDir.resolve("out"));
            Path modsDir = Files.createDirectory(tempDir.resolve("mods"));
            System.out.println("Temp directory: " + tempDir);

            // Step 2: Write source files for three modules

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
            // We export com.training.app so we can invoke Main.run() reflectively from this demo.
            // In a real application, the app module would be the entry point (--module com.training.app/...)
            // and would NOT need to export its package.
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
                            // Use ServiceLoader.load(layer, service) to discover providers in our custom layer
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

            // Step 3: Compile all modules with javac --module-source-path
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

            // Step 4: Package each module into a modular JAR
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

            // Step 5: Load modules at runtime via ModuleFinder + Configuration + ModuleLayer
            ModuleFinder finder = ModuleFinder.of(modsDir);

            // Show what the finder discovered
            System.out.println("\nModuleFinder discovered:");
            finder.findAll().stream()
                    .sorted(Comparator.comparing(ref -> ref.descriptor().name()))
                    .forEach(ref -> System.out.println("  " + ref.descriptor().name()
                            + " (" + ref.location().map(Object::toString).orElse("?") + ")"));

            // Resolve the module graph
            ModuleLayer parentLayer = ModuleLayer.boot();
            Configuration parentConfig = parentLayer.configuration();
            Configuration config = parentConfig.resolve(
                    finder,
                    ModuleFinder.of(),  // empty after-finder
                    Set.of("com.training.app", "com.training.provider", "com.training.api"));

            // Create a new ModuleLayer with its own class loader
            ModuleLayer layer = parentLayer.defineModulesWithOneLoader(
                    config, ClassLoader.getSystemClassLoader());

            System.out.println("\nCustom ModuleLayer created with " + layer.modules().size() + " modules");

            // Step 6: Inspect module descriptors
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

            // Step 7: Invoke Main.run(layer) reflectively to demonstrate ServiceLoader
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
            // Step 8: Clean up temp files
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
    // Main — run all sections
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
