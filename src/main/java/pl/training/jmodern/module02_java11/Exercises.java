package pl.training.jmodern.module02_java11;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

public class Exercises {

    // ---- Helper types ----

    record ConfigEntry(String key, String value) {}

    // ============================================================
    // Exercise 1: Config File Parser
    // ============================================================

    /**
     * Parse a multi-line configuration string into a list of {@link ConfigEntry} records.
     *
     * <p>The input is a single string containing multiple lines, each in the format
     * {@code key=value}. Lines that are blank or start with {@code #} (comments)
     * should be ignored. Keys and values should be stripped of leading/trailing
     * whitespace.</p>
     *
     * <p>Example input:
     * <pre>{@code
     * # Database config
     * db.host = localhost
     * db.port = 5432
     *
     * db.name = mydb
     * }</pre>
     *
     * <p><b>Hints:</b> Use {@code String.lines()}, {@code String.isBlank()},
     * {@code String.strip()}, {@code Predicate.not()}, and {@code var}
     * for local variable declarations.</p>
     */
    static List<ConfigEntry> parseConfig(String configText) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 2: File Inventory Report
    // ============================================================

    /**
     * Generate a formatted text report of files grouped by extension.
     *
     * <p>Given a list of file names (e.g. {@code "report.pdf"}, {@code "data.csv"})
     * and a corresponding list of sizes in bytes, group the files by their extension
     * and produce a report string like:</p>
     * <pre>{@code
     * csv
     *   data.csv           1024
     *   sales.csv          2048
     * pdf
     *   report.pdf         4096
     * }</pre>
     *
     * <p>Extensions should be sorted alphabetically. Files within each group should
     * appear in their original order. Use {@code String.repeat()} to align columns.</p>
     *
     * <p><b>Hints:</b> Use {@code var}, streams with {@code Collectors.groupingBy},
     * {@code String.repeat()}, and {@code Predicate.not()} to filter out files
     * without extensions.</p>
     */
    static String fileInventoryReport(List<String> fileNames, List<Long> sizes) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 3: HTTP Health Checker
    // ============================================================

    /**
     * Check the health of multiple HTTP endpoints and aggregate results.
     *
     * <p>Given a list of URL strings, send a GET request to each one.
     * Return a {@code Map<String, String>} where each key is the URL
     * and each value is either {@code "UP"} (HTTP 2xx) or {@code "DOWN"}
     * (any other status or exception).</p>
     *
     * <p><b>Hints:</b> Use {@code HttpClient.newHttpClient()},
     * {@code HttpRequest.newBuilder()}, {@code HttpResponse.BodyHandlers.discarding()},
     * and {@code var} for local type inference. Wrap each call in a try-catch
     * to handle connection failures gracefully.</p>
     */
    static Map<String, String> checkEndpoints(List<String> urls) {
        throw new UnsupportedOperationException();
    }
}
