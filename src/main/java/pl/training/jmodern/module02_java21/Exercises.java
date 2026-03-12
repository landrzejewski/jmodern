package pl.training.jmodern.module02_java21;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Exercises {

    // ---- Typy pomocnicze dla ćwiczenia 1: Księga finansowa ----

    record Money(String currency, double amount) {}

    sealed interface FinancialEvent permits FinancialEvent.Payment, FinancialEvent.Refund {
        record Payment(Money money, String merchant) implements FinancialEvent {}
        record Refund(Money money, String reason) implements FinancialEvent {}
    }

    // ---- Typy pomocnicze dla ćwiczenia 2: Klasyfikator strumienia zdarzeń ----

    sealed interface AppEvent permits AppEvent.UserLogin, AppEvent.UserLogout,
            AppEvent.PageView, AppEvent.ApiCall {
        record UserLogin(String userId, String ipAddress, boolean suspicious) implements AppEvent {}
        record UserLogout(String userId) implements AppEvent {}
        record PageView(String userId, String page, int durationMs) implements AppEvent {}
        record ApiCall(String endpoint, int statusCode, long latencyMs) implements AppEvent {}
    }

    // ============================================================
    // Ćwiczenie 1: Uzgadnianie księgi finansowej
    // ============================================================

    /**
     * Uzgodnij listę zdarzeń finansowych do sald w rozbiciu na waluty.
     *
     * <p>Mając {@link SequencedCollection} obiektów {@link FinancialEvent}:</p>
     * <ul>
     *   <li>Płatności <b>dodają</b> do salda danej waluty.</li>
     *   <li>Zwroty <b>odejmują</b> od salda.</li>
     * </ul>
     *
     * <p>Zwróć {@link SequencedMap} (np. {@link LinkedHashMap}) mapującą
     * {@code waluta -> saldo netto}, uporządkowaną według pierwszego wystąpienia
     * każdej waluty. Wypisz również pierwsze i ostatnie przetworzone zdarzenie
     * za pomocą {@code getFirst()} / {@code getLast()}.</p>
     *
     * <p><b>Wskazówki:</b> Użyj wzorców rekordów w switch, aby zdestrukturyzować
     * {@code Payment(Money(currency, amount), _)} z nienazwanymi zmiennymi
     * dla pól, których nie potrzebujesz. Użyj metod kolekcji sekwencyjnych
     * ({@code getFirst}, {@code getLast}).</p>
     */
    static SequencedMap<String, Double> reconcile(SequencedCollection<FinancialEvent> events) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 2: Klasyfikator strumienia zdarzeń
    // ============================================================

    /**
     * Klasyfikuj zdarzenia aplikacji do kategorii ważności przy użyciu szczegółowych reguł.
     *
     * <p>Reguły klasyfikacji:</p>
     * <ul>
     *   <li>{@code UserLogin} z {@code suspicious == true} → {@code "ALERT"}</li>
     *   <li>{@code UserLogin} (normalny) → {@code "INFO"}</li>
     *   <li>{@code UserLogout} → {@code "INFO"}</li>
     *   <li>{@code PageView} z {@code durationMs > 30_000} → {@code "WARNING"} (wolna strona)</li>
     *   <li>{@code PageView} (normalny) → {@code "INFO"}</li>
     *   <li>{@code ApiCall} z {@code statusCode >= 500} → {@code "ALERT"}</li>
     *   <li>{@code ApiCall} z {@code statusCode >= 400} → {@code "WARNING"}</li>
     *   <li>{@code ApiCall} z {@code latencyMs > 5000} → {@code "WARNING"} (wolne API)</li>
     *   <li>{@code ApiCall} (normalny) → {@code "INFO"}</li>
     * </ul>
     *
     * <p>Zwróć {@code Map<String, List<AppEvent>>} grupującą zdarzenia według ich
     * kategorii ważności.</p>
     *
     * <p><b>Wskazówki:</b> Użyj dopasowania wzorców w switch ze wzorcami warunkowanymi
     * ({@code when}), wzorcami rekordów do destrukturyzacji, nienazwanymi zmiennymi
     * ({@code _}) dla nieużywanych komponentów oraz {@code Collectors.groupingBy}.</p>
     */
    static Map<String, List<AppEvent>> classifyEvents(List<AppEvent> events) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 3: Równoległa agregacja czujników
    // ============================================================

    /**
     * Oblicz średnie odczyty per czujnik współbieżnie przy użyciu virtual threads.
     *
     * <p>Mając {@code Map<String, List<Double>>}, gdzie każdy klucz to identyfikator
     * czujnika, a każda wartość to lista surowych odczytów, oblicz średnią dla każdego
     * czujnika. Obliczenia każdego czujnika powinny działać na własnym virtual thread.</p>
     *
     * <p>Zwróć {@link SequencedMap} mapującą {@code identyfikatorCzujnika -> średnia},
     * uporządkowaną alfabetycznie po identyfikatorze czujnika. Użyj {@code getFirst()}
     * i {@code getLast()} na wyniku, aby wypisać pierwszy i ostatni wpis czujnika.</p>
     *
     * <p><b>Wskazówki:</b> Użyj {@code Thread.ofVirtual().start()},
     * {@link ConcurrentHashMap} do bezpiecznej wątkowo akumulacji
     * oraz metod kolekcji sekwencyjnych na końcowym posortowanym wyniku.</p>
     */
    static SequencedMap<String, Double> aggregateSensors(Map<String, List<Double>> sensorData)
            throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
