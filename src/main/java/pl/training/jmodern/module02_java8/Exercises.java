package pl.training.jmodern.module02_java8;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Exercises {

    // ---- Typy pomocnicze ----

    record Transaction(String id, String category, double amount, LocalDate date) {}

    record Employee(String name, Set<DayOfWeek> workingDays, LocalTime startHour, LocalTime endHour) {}

    @FunctionalInterface
    interface Mapper<T, R> {
        R apply(T input);

        default <V> Mapper<T, V> andThen(Mapper<R, V> after) {
            return input -> after.apply(this.apply(input));
        }
    }

    // ============================================================
    // Ćwiczenie 1: Potok analityki transakcji
    // ============================================================

    /**
     * Podsumuj łączne wydatki według kategorii dla transakcji w danym miesiącu.
     *
     * <p>Mając listę transakcji i docelowy {@link YearMonth}, zwróć
     * {@code Map<String, Double>}, gdzie każdy klucz to kategoria, a każda wartość
     * to łączna kwota wydana w tej kategorii w docelowym miesiącu.</p>
     *
     * <p><b>Wskazówki:</b> Użyj {@code Stream.filter} do wybrania miesiąca,
     * {@code Collectors.groupingBy} z {@code Collectors.summingDouble}
     * do agregacji, oraz lambd / referencji do metod tam, gdzie to stosowne.</p>
     */
    static Map<String, Double> spendingByCategory(List<Transaction> transactions, YearMonth month) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 2: Komponowalny procesor logów
    // ============================================================

    /**
     * Zbuduj wielokrotnego użytku potok przetwarzania logów, który wyodrębnia poziomy logów z surowych linii.
     *
     * <p>Każda surowa linia logu może wyglądać jak {@code "2024-01-15 ERROR Something failed"}.
     * Używając niestandardowego interfejsu funkcyjnego {@link Mapper}:</p>
     * <ol>
     *   <li>Utwórz {@code Mapper<String, Optional<String>>}, który zwraca drugi
     *       token (poziom logu) opakowany w {@code Optional}, lub pusty, jeśli linia
     *       ma mniej niż dwa tokeny.</li>
     *   <li>Skomponuj go z innym mapperem, który zamienia wartość na wielkie litery (użyj {@code andThen}).</li>
     *   <li>Zastosuj skomponowany potok do strumienia linii logów i zbierz unikalne,
     *       niepuste poziomy logów do posortowanej {@code List<String>}.</li>
     * </ol>
     *
     * <p><b>Wskazówki:</b> Użyj {@code Stream.map}, {@code Optional.stream} (lub
     * {@code flatMap}) oraz niestandardowej metody domyślnej {@code Mapper.andThen}.</p>
     */
    static List<String> extractLogLevels(List<String> rawLines) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 3: Planowanie spotkań pracowników
    // ============================================================

    /**
     * Znajdź pracowników dostępnych na spotkanie w danym dniu i zakresie czasowym.
     *
     * <p>Pracownik jest dostępny, jeśli:</p>
     * <ul>
     *   <li>{@link DayOfWeek} dnia spotkania znajduje się w jego zbiorze {@code workingDays}.</li>
     *   <li>{@code startTime} i {@code endTime} spotkania mieszczą się w jego
     *       godzinach pracy ({@code startHour} do {@code endHour}).</li>
     * </ul>
     *
     * <p>Zwróć pojedynczy {@code String} z listą dostępnych pracowników oddzielonych
     * {@code ", "} (np. {@code "Alice, Bob, Carol"}).</p>
     *
     * <p><b>Wskazówki:</b> Użyj kompozycji {@code Predicate} ({@code and}),
     * {@code Stream.filter} oraz {@code Collectors.joining} lub {@link StringJoiner}.</p>
     */
    static String findAvailableEmployees(List<Employee> employees,
                                         LocalDate meetingDay,
                                         LocalTime startTime,
                                         LocalTime endTime) {
        throw new UnsupportedOperationException();
    }
}
