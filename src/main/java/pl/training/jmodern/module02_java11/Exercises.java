package pl.training.jmodern.module02_java11;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

public class Exercises {

    // ---- Typy pomocnicze ----

    record ConfigEntry(String key, String value) {}

    // ============================================================
    // Ćwiczenie 1: Parser pliku konfiguracyjnego
    // ============================================================

    /**
     * Parsuje wieloliniowy ciąg konfiguracyjny do listy rekordów {@link ConfigEntry}.
     *
     * <p>Dane wejściowe to pojedynczy ciąg znaków zawierający wiele linii, każda w formacie
     * {@code klucz=wartość}. Linie puste lub zaczynające się od {@code #} (komentarze)
     * powinny być pomijane. Klucze i wartości powinny mieć usunięte wiodące/końcowe
     * białe znaki.</p>
     *
     * <p>Przykładowe dane wejściowe:
     * <pre>{@code
     * # Database config
     * db.host = localhost
     * db.port = 5432
     *
     * db.name = mydb
     * }</pre>
     *
     * <p><b>Wskazówki:</b> Użyj {@code String.lines()}, {@code String.isBlank()},
     * {@code String.strip()}, {@code Predicate.not()} oraz {@code var}
     * do deklaracji zmiennych lokalnych.</p>
     */
    static List<ConfigEntry> parseConfig(String configText) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 2: Raport inwentaryzacji plików
    // ============================================================

    /**
     * Generuje sformatowany raport tekstowy plików pogrupowanych według rozszerzenia.
     *
     * <p>Na podstawie listy nazw plików (np. {@code "report.pdf"}, {@code "data.csv"})
     * i odpowiadającej im listy rozmiarów w bajtach, grupuje pliki według rozszerzenia
     * i tworzy raport w postaci:</p>
     * <pre>{@code
     * csv
     *   data.csv           1024
     *   sales.csv          2048
     * pdf
     *   report.pdf         4096
     * }</pre>
     *
     * <p>Rozszerzenia powinny być posortowane alfabetycznie. Pliki w każdej grupie powinny
     * występować w oryginalnej kolejności. Użyj {@code String.repeat()} do wyrównania kolumn.</p>
     *
     * <p><b>Wskazówki:</b> Użyj {@code var}, Stream z {@code Collectors.groupingBy},
     * {@code String.repeat()} oraz {@code Predicate.not()} do filtrowania plików
     * bez rozszerzenia.</p>
     */
    static String fileInventoryReport(List<String> fileNames, List<Long> sizes) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 3: Sprawdzanie stanu HTTP
    // ============================================================

    /**
     * Sprawdza stan wielu endpointów HTTP i agreguje wyniki.
     *
     * <p>Na podstawie listy ciągów URL, wysyła żądanie GET do każdego z nich.
     * Zwraca {@code Map<String, String>}, gdzie każdy klucz to URL,
     * a każda wartość to {@code "UP"} (HTTP 2xx) lub {@code "DOWN"}
     * (inny status lub wyjątek).</p>
     *
     * <p><b>Wskazówki:</b> Użyj {@code HttpClient.newHttpClient()},
     * {@code HttpRequest.newBuilder()}, {@code HttpResponse.BodyHandlers.discarding()}
     * oraz {@code var} do wnioskowania typów lokalnych. Owiń każde wywołanie w try-catch,
     * aby obsłużyć błędy połączenia w sposób graceful.</p>
     */
    static Map<String, String> checkEndpoints(List<String> urls) {
        throw new UnsupportedOperationException();
    }
}
