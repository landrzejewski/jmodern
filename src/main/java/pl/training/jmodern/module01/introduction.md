# Modern Java

## Summary of the most important changes in each LTS release

Below is a detailed list of the key language changes introduced in each Long-Term Support (LTS)
version of Java from Java 8 to Java 21

### Java 8 (Released March 2014)

1. Lambda Expressions
- Introduced functional programming capabilities, allowing behavior to be passed as parameters.
- Enabled more concise and readable code by reducing boilerplate associated with anonymous classes.

2. Stream API
- Provided a new abstraction for processing sequences of elements, supporting operations like map, filter, and reduce.
- Facilitated parallel processing of collections, improving performance and scalability.

3. Functional Interfaces
- Defined interfaces with a single abstract method, serving as targets for lambda expressions and method references.
- Enhanced the ability to write more flexible and reusable code components.

4. Method References
- Offered a shorthand notation for lambda expressions that execute existing methods.
- Improved code readability and maintainability by referencing methods directly.

5. Default Methods in Interfaces
- Allowed interfaces to include default method implementations.
- Enhanced backward compatibility by enabling interfaces to evolve without breaking existing implementations.

6. Optional Class
- Addressed the issue of null references by providing a container object that may or may not contain a non-null value.
- Encouraged better handling of potential null values, reducing the likelihood of `NullPointerException`.

7. New Date and Time API (java.time)
- Introduced a comprehensive and consistent API for date and time manipulation.
- Replaced the older `java.util.Date` and `java.util.Calendar` classes with more robust alternatives.

8. Repeating Annotations and Type Annotations
- Enabled multiple annotations of the same type on a single element.
- Improved type checking and clarity in code annotations.

### Java 11 (Released September 2018)

1. Local-Variable Syntax for Lambda Parameters
- Introduced the `var` keyword for lambda parameters, enhancing readability and allowing annotations on parameters.

2. Enhanced String API
- Added new methods such as `isBlank()`, `lines()`, `strip()`, `stripLeading()`, `stripTrailing()`, and `repeat(int)` to facilitate more versatile string manipulations.

3. Unicode 10 Support
- Updated the Java platform to support the latest Unicode standards, improving internationalization and compatibility with diverse character sets.

4. Removal and Deprecation of Features
- Removed the Nashorn JavaScript engine, streamlining the platform.
- Deprecated several older APIs to reduce redundancy and encourage the use of more modern alternatives.

5. HTTP Client Enhancements
- Enhanced the existing HTTP client to support HTTP/2 and WebSocket, providing better performance and modern protocol support.

6. Flight Recorder and Other JVM Enhancements
- Included JVM-level enhancements like Flight Recorder for profiling and monitoring applications with minimal performance overhead.

Note: Java 11 primarily focused on API enhancements, performance improvements, and removal of outdated features rather than introducing major new language constructs.

### Java 17 (Released September 2021)

1. Sealed Classes and Interfaces
- Allowed classes and interfaces to restrict which other classes or interfaces can extend or implement them.
- Facilitated the creation of more controlled and predictable type hierarchies.

2. Pattern Matching for `instanceof`
- Simplified type checks and casting by allowing the extraction of variables within the `instanceof` operator.
- Reduced boilerplate code associated with type casting.

3. Text Blocks
- Introduced multi-line string literals, enhancing the readability and maintainability of code that deals with large blocks of text, such as JSON or SQL queries.

4. Records (Finalized in Java 16)
- Provided a compact syntax for declaring classes that are transparent carriers for immutable data.
- Reduced the boilerplate associated with plain data-holding classes.

5. Enhanced `switch` Statements (Preview Features)
- Introduced more expressive and flexible `switch` constructs, allowing for more concise and readable control flow structures.

6. Enhanced Pseudo-Random Number Generators (PRNGs)
- Expanded the set of PRNG algorithms available in the Java platform, providing developers with more options for generating random numbers.

7. Foreign Function & Memory API (Incubator)
- Introduced APIs to allow Java programs to interoperate with code and data outside of the Java runtime, facilitating integration with native libraries.

### Java 21 (Released September 2023)

1. Record Patterns (Preview)
- Enabled the deconstruction of record values, allowing more concise and readable code when working with records.
- Facilitated pattern matching with records in conditional statements and expressions.

2. Pattern Matching for `switch` (Second Preview)
- Enhanced `switch` statements to support pattern matching, making them more powerful and expressive.
- Allowed `switch` constructs to handle complex data-oriented queries more naturally.

3. Virtual Threads (Project Loom)
- Introduced lightweight threads managed by the JVM, simplifying concurrent programming.
- Enabled developers to write highly concurrent applications with improved scalability and performance.

4. Sequenced Collections
- Provided ordered versions of collection interfaces, ensuring that elements maintain a defined encounter order.
- Enhanced predictability and consistency when processing collections.

5. Enhanced Switch Expressions and Sealed Interfaces
- Continued improvements to `switch` expressions, making them more robust and versatile.
- Further refined sealed interfaces for more controlled type hierarchies.

6. Improved String Handling and Performance Optimizations
- Enhanced the underlying implementation of strings for better performance and reduced memory footprint.
- Included optimizations that benefit both runtime performance and developer productivity.

7. Deprecation and Removal of Legacy Features
- Continued the process of deprecating and removing outdated APIs and features to streamline the Java platform.
- Encouraged the adoption of more modern and efficient alternatives.

8. Enhanced Pattern Matching and Type Inference
- Expanded capabilities of pattern matching beyond basic types, allowing more complex and nested patterns.
- Improved type inference mechanisms to reduce the need for explicit type declarations.

## The new publishing cycle and its impact on everyday development

The new publishing cycle of Java, established in recent years, has significantly transformed the landscape of everyday
software development. Moving away from the traditional multi-year release intervals, Java now follows a predictable
six-month release cadence, ensuring that new features, enhancements, and performance improvements are
delivered consistently and regularly. Every three years, a Long-Term Support (LTS) version is released,
providing a stable and supported foundation for enterprises and long-term projects. This streamlined cycle allows
developers to access the latest language innovations and API improvements more swiftly, fostering a culture of
continuous improvement and innovation. Consequently, development teams can leverage cutting-edge tools and features
to enhance productivity, code quality, and application performance without waiting for extended periods between major
releases. However, the increased frequency of updates also demands that organizations adopt more agile maintenance
practices, ensuring that their codebases remain compatible and up-to-date with the latest Java versions. Overall,
the new publishing cycle strikes a balance between delivering rapid advancements and maintaining stability through
LTS releases, thereby enhancing the efficiency, flexibility, and responsiveness of everyday Java development.

## JDK and licensing issues

The Java Development Kit (JDK) is an essential toolkit for Java developers, providing the necessary tools to
develop, compile, debug, and run Java applications. It includes the Java Runtime Environment (JRE),
an interpreter/loader (Java), a compiler (javac), an archiver (jar), a documentation generator (javadoc),
and other utilities necessary for Java development. The JDK serves as the foundational platform for building
Java applications, ensuring that developers have access to the latest language features, libraries, and runtime environments.

Historically, Oracle provided the official JDK under the Oracle Binary Code License Agreement, which permitted free
use for personal and development purposes but required a commercial license for production use in organizations.
This licensing model posed challenges for businesses seeking to deploy Java applications at scale, as it introduced
potential costs and legal considerations.

In contrast, OpenJDK emerged as the open-source reference implementation of the Java Platform, Standard Edition (Java SE).
Licensed under the GNU General Public License, version 2, with the Classpath Exception (GPLv2+CE), OpenJDK offered a free
and open alternative for developers and organizations. This allowed broader adoption without the constraints of
proprietary licensing, fostering a more inclusive and collaborative Java ecosystem.

Starting with Java 11, Oracle shifted the licensing model for its JDK distributions. Oracle JDK began to require a
commercial license for production use, aligning it more closely with OpenJDK's licensing terms. This change prompted
many organizations to reconsider their Java deployment strategies, leading to increased adoption of OpenJDK and other
open-source distributions such as Amazon Corretto, AdoptOpenJDK (now part of Eclipse Adoptium), Azul Zulu, and Red Hat OpenJDK.

The Java community has largely embraced the shift towards open-source JDK distributions, recognizing the benefits of
reduced costs, increased transparency, and collaborative innovation. Oracle continues to contribute to OpenJDK, ensuring
that it remains the cornerstone of Java development. Meanwhile, alternative vendors have differentiated themselves by
offering specialized features, extended support, and performance optimizations tailored to various use cases.

Looking forward, organizations must remain vigilant about licensing terms and the evolving landscape of JDK distributions.
As Java continues to evolve with new features and improvements, maintaining compliance with licensing agreements and
leveraging the right JDK distribution will be crucial for sustaining efficient and secure Java development practices.

### Selecting the Implementation and JDK Version

Choosing the right Java Development Kit (JDK) implementation and version is crucial for ensuring the efficiency, security, and maintainability of your Java applications. With multiple JDK distributions and frequent releases, making an informed decision requires understanding the available options and assessing them against your project’s specific needs. Below is a comprehensive guide to help you navigate this selection process.

#### Understanding JDK Implementations

Several JDK implementations are available, each with its own set of features, licensing models, and support options:

- Oracle JDK
    - Description: The original JDK provided by Oracle, historically the standard for Java development.
    - Licensing: As of Java 11, Oracle JDK requires a commercial license for production use, though free for personal and development purposes.
    - Support: Oracle offers commercial support and updates for Oracle JDK.

- OpenJDK
    - Description: The open-source reference implementation of the Java Platform, Standard Edition (Java SE).
    - Licensing: Distributed under the GNU General Public License, version 2, with the Classpath Exception (GPLv2+CE).
    - Support: Supported by the open-source community and various vendors offering commercial support.

- Amazon Corretto
    - Description: A free, multiplatform, production-ready distribution of OpenJDK by Amazon.
    - Licensing: Open-source under the GPLv2+CE.
    - Support: Long-term support with regular updates provided by Amazon.

- Eclipse Temurin (formerly AdoptOpenJDK)
    - Description: A widely adopted OpenJDK distribution managed by the Eclipse Foundation.
    - Licensing: Open-source under GPLv2+CE.
    - Support: Community-driven with commercial support options available through partners.

- Azul Zulu
    - Description: A certified, tested, and supported build of OpenJDK by Azul Systems.
    - Licensing: Offers both open-source (GPLv2+CE) and commercial licenses.
    - Support: Comprehensive support services, including long-term support (LTS).

- Red Hat OpenJDK
    - Description: OpenJDK builds provided by Red Hat, optimized for enterprise use.
    - Licensing: Open-source under GPLv2+CE.
    - Support: Backed by Red Hat’s enterprise support offerings.

- BellSoft Liberica JDK
    - Description: An OpenJDK distribution with additional features like embedded JDK and JavaFX.
    - Licensing: Open-source under GPLv2+CE, with commercial options available.
    - Support: Offers long-term support and commercial support services.

Choosing the right JDK version is equally important, as it impacts the features available, performance, and long-term support.
Here’s how to approach version selection:

- Long-Term Support (LTS) vs. Non-LTS Releases
    - LTS Versions: These versions, released every three years (e.g., Java 8, 11, 17, 21), receive extended support and are ideal for production environments requiring stability.
    - Non-LTS Versions: Released every six months, these versions provide access to the latest features but have a shorter support lifecycle, suitable for experimentation and staying up-to-date with innovations.

- Stability vs. Latest Features
    - Stability Needs: For mission-critical applications, opting for an LTS version ensures a stable and supported foundation.
    - Feature Requirements: If your project benefits from the latest language enhancements or performance improvements, consider adopting a newer non-LTS version, keeping in mind the need for more frequent upgrades.

- Project Requirements and Dependencies
    - Library and Framework Compatibility: Ensure that your project's dependencies are compatible with the chosen JDK version to avoid integration issues.
    - Legacy Code Considerations: For projects with significant legacy code, maintaining consistency with an older JDK version may reduce refactoring efforts.

- Support Timelines and Lifecycle
    - End of Public Updates: Be aware of the support timelines for each JDK version to plan migrations before end-of-life (EOL) dates.
    - Vendor-Specific Support: Different JDK distributions may offer varying support durations, so align your choice with your organization’s maintenance capabilities.

- Security and Compliance
    - Security Patches: Choose a JDK version that receives regular security updates to protect against vulnerabilities.
    - Compliance Requirements: Ensure that the selected JDK complies with your organization’s regulatory and security standards.

## Migration strategies

When transitioning to a different JDK implementation or upgrading to a newer version, follow these steps to ensure a smooth migration:

- Compatibility Testing: Validate that your application runs correctly on the new JDK by conducting comprehensive testing, including unit, integration, and performance tests.
- Dependency Verification: Ensure all third-party libraries and frameworks used in your project are compatible with the target JDK version and implementation.
- Performance Benchmarking: Compare the performance metrics between the current and new JDK to identify any improvements or regressions.
- Gradual Rollout: Implement the new JDK in staging environments before deploying to production to monitor behavior and address issues proactively.
- Backup and Rollback Plans: Maintain backups and establish rollback procedures to revert to the previous JDK version in case of critical issues during migration.
- Documentation and Training: Update project documentation to reflect the new JDK details and provide training to the development team on any new features or changes introduced.

## Managing multiple Java versions

Developers often need to work with multiple versions of Java to test compatibility, leverage new language features, or
maintain legacy applications. Two popular tools for managing and switching between different Java versions are SDKMAN
and JVMS. This article outlines how to use these tools, providing command-line examples and best practices for managing
multiple Java versions on a single system.

### Using SDKMAN

SDKMAN is a popular command-line tool for managing parallel versions of various SDKs, including multiple Java distributions.
It provides an easy way to install, switch, and configure Java environments.

Run the following command in your terminal to install SDKMAN:

```bash
curl -s "https://get.sdkman.io" | bash
```

Follow the instructions displayed (which typically involve restarting your terminal or sourcing the SDKMAN initialization script).

Once installed, you can list all the available Java distributions and versions by running:

```bash
sdk list java
```

This command displays a table with various vendors and version identifiers (for example, OpenJDK, Zulu, Temurin).

To install a specific Java version, use the install command with the version identifier from the list:

```bash
sdk install java 17.0.2-tem
```

After installation, switch to that version with:

```bash
sdk use java 17.0.2-tem
```

You can also set a Java version as the default:

```bash
sdk default java 17.0.2-tem
```

Check the active Java version by running:

```bash
java -version
```

This confirms that the environment reflects your SDKMAN selection.

### Using JVMS

JVMS (Java Version Manager) is another tool designed specifically for switching between multiple Java versions.
While SDKMAN manages various SDKs, JVMS focuses on Java and provides a lightweight approach to switch between
JDKs without altering system paths permanently.

JVMS can typically be installed by cloning its repository and adding it to your shell’s startup script.
For example, if using a Unix-like system:

```bash
git clone https://github.com/patrickfav/jvms.git ~/jvms
echo 'export PATH="$HOME/jvms/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

*Note:* Adjust installation steps according to the instructions provided in the [JVMS repository](https://github.com/patrickfav/jvms) or its documentation.

List the available installed versions managed by JVMS with a command similar to:

```bash
jvms list
```

To add a new Java version, follow the JVMS instructions (this could involve specifying the path to a Java installation
or using integrated download features if available).

Switch between installed versions using a command like:

```bash
jvms use 17.0.2
```

This command temporarily sets the specified version as active in your current terminal session. To check the active version:

```bash
java -version
```

Both SDKMAN and JVMS allow you to switch Java versions on the fly, making it easy to integrate into build scripts,
continuous integration pipelines, or development environments where specific versions are required. You can script
version changes as part of your project setup to ensure consistency across development machines.
