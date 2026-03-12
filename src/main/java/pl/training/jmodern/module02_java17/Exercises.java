package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.stream.*;

public class Exercises {

    // ---- Typy pomocnicze do Ćwiczenia 1: Raport odpowiedzi API ----

    sealed interface ApiResponse<T> permits ApiResponse.Success, ApiResponse.ClientError,
            ApiResponse.ServerError, ApiResponse.Timeout {
        record Success<T>(int statusCode, T body) implements ApiResponse<T> {}
        record ClientError<T>(int statusCode, String message) implements ApiResponse<T> {}
        record ServerError<T>(int statusCode, String message) implements ApiResponse<T> {}
        record Timeout<T>(long durationMs) implements ApiResponse<T> {}
    }

    // ---- Typy pomocnicze do Ćwiczenia 2: Ewaluator drzewa wyrażeń ----

    sealed interface Expr permits Expr.Literal, Expr.Var, Expr.BinOp, Expr.UnaryOp {
        record Literal(double value) implements Expr {}
        record Var(String name) implements Expr {}
        record BinOp(String operator, Expr left, Expr right) implements Expr {}
        record UnaryOp(String operator, Expr operand) implements Expr {}
    }

    // ---- Typy pomocnicze do Ćwiczenia 3: Dispatcher powiadomień ----

    sealed interface Notification permits Notification.Email, Notification.Sms,
            Notification.Push, Notification.Slack {
        record Email(String to, String subject, String body) implements Notification {}
        record Sms(String phoneNumber, String message) implements Notification {}
        record Push(String deviceToken, String title, String payload) implements Notification {}
        record Slack(String channel, String message, boolean urgent) implements Notification {}
    }

    // ============================================================
    // Ćwiczenie 1: Renderer raportu odpowiedzi API
    // ============================================================

    /**
     * Renderuje listę odpowiedzi API w sformatowany raport monitoringu.
     *
     * <p>Dla każdego {@link ApiResponse} generuje linię opisującą go:</p>
     * <ul>
     *   <li>{@code Success}: {@code "[OK 200] Body: ..."}</li>
     *   <li>{@code ClientError}: {@code "[CLIENT_ERR 404] ..."}</li>
     *   <li>{@code ServerError}: {@code "[SERVER_ERR 500] ..."}</li>
     *   <li>{@code Timeout}: {@code "[TIMEOUT 1500ms]"}</li>
     * </ul>
     *
     * <p>Połącz wszystkie linie w raport używając nagłówka z bloku tekstowego:</p>
     * <pre>{@code
     * === API Monitoring Report ===
     * Total: 4 | Success: 2 | Errors: 1 | Timeouts: 1
     *
     * [OK 200] Body: {...}
     * [TIMEOUT 1500ms]
     * ...
     * }</pre>
     *
     * <p><b>Wskazówki:</b> Użyj wyrażenia switch na zapieczętowanej hierarchii {@code ApiResponse},
     * record do dopasowywania wzorców, bloków tekstowych z {@code .formatted()},
     * oraz Stream do zliczania kategorii.</p>
     */
    static String renderReport(List<ApiResponse<?>> responses) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 2: Ewaluator drzewa wyrażeń
    // ============================================================

    /**
     * Oblicza rekurencyjne drzewo wyrażeń z wiązaniami zmiennych.
     *
     * <p>Dla danego drzewa {@link Expr} i {@code Map<String, Double>} wiązań
     * zmiennych, oblicza wynik numeryczny. Obsługiwane operacje:</p>
     * <ul>
     *   <li>{@code Literal}: zwraca wartość bezpośrednio</li>
     *   <li>{@code Var}: wyszukuje nazwę zmiennej w mapie wiązań;
     *       rzuca {@code IllegalArgumentException} jeśli nie znaleziono</li>
     *   <li>{@code BinOp}: obsługuje {@code "+"}, {@code "-"}, {@code "*"}, {@code "/"}</li>
     *   <li>{@code UnaryOp}: obsługuje {@code "-"} (negacja) i {@code "abs"}</li>
     * </ul>
     *
     * <p><b>Wskazówki:</b> Użyj wyczerpującego wyrażenia switch na zapieczętowanej
     * hierarchii {@code Expr}. Użyj {@code yield} dla wieloliniowych przypadków.
     * Wywołuj rekurencyjnie dla {@code BinOp} i {@code UnaryOp}.</p>
     */
    static double evaluate(Expr expr, Map<String, Double> variables) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Ćwiczenie 3: Dispatcher powiadomień
    // ============================================================

    /**
     * Formatuje instrukcje dostarczenia dla powiadomienia.
     *
     * <p>Dla danego {@link Notification}, zwraca sformatowany ciąg instrukcji:</p>
     * <ul>
     *   <li>{@code Email}: {@code "SEND EMAIL to <to>\nSubject: <subject>\nBody: <body>"}</li>
     *   <li>{@code Sms}: {@code "SEND SMS to <phone>\nMessage: <message>"};
     *       jeśli wiadomość przekracza 160 znaków, dołącz {@code "\n[WARN: message truncated]"}</li>
     *   <li>{@code Push}: {@code "PUSH to device <token>\nTitle: <title>\nPayload: <payload>"}</li>
     *   <li>{@code Slack}: {@code "POST to #<channel>\nMessage: <message>"};
     *       jeśli {@code urgent}, dodaj {@code "[URGENT] "} przed wiadomością</li>
     * </ul>
     *
     * <p><b>Wskazówki:</b> Użyj wyrażenia switch na zapieczętowanej hierarchii,
     * dopasowywania wzorców dla instanceof do sprawdzania obcinania/pilności,
     * bloków tekstowych dla wieloliniowego wyjścia, oraz record do dekonstrukcji.</p>
     */
    static String formatDispatchInstruction(Notification notification) {
        throw new UnsupportedOperationException();
    }
}
