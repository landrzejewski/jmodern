package pl.training.jmodern.module02_java17;

import java.util.*;
import java.util.stream.*;

public class Exercises {

    // ---- Helper types for Exercise 1: API Response Report ----

    sealed interface ApiResponse<T> permits ApiResponse.Success, ApiResponse.ClientError,
            ApiResponse.ServerError, ApiResponse.Timeout {
        record Success<T>(int statusCode, T body) implements ApiResponse<T> {}
        record ClientError<T>(int statusCode, String message) implements ApiResponse<T> {}
        record ServerError<T>(int statusCode, String message) implements ApiResponse<T> {}
        record Timeout<T>(long durationMs) implements ApiResponse<T> {}
    }

    // ---- Helper types for Exercise 2: Expression Tree Evaluator ----

    sealed interface Expr permits Expr.Literal, Expr.Var, Expr.BinOp, Expr.UnaryOp {
        record Literal(double value) implements Expr {}
        record Var(String name) implements Expr {}
        record BinOp(String operator, Expr left, Expr right) implements Expr {}
        record UnaryOp(String operator, Expr operand) implements Expr {}
    }

    // ---- Helper types for Exercise 3: Notification Dispatcher ----

    sealed interface Notification permits Notification.Email, Notification.Sms,
            Notification.Push, Notification.Slack {
        record Email(String to, String subject, String body) implements Notification {}
        record Sms(String phoneNumber, String message) implements Notification {}
        record Push(String deviceToken, String title, String payload) implements Notification {}
        record Slack(String channel, String message, boolean urgent) implements Notification {}
    }

    // ============================================================
    // Exercise 1: API Response Report Renderer
    // ============================================================

    /**
     * Render a list of API responses into a formatted monitoring report.
     *
     * <p>For each {@link ApiResponse}, produce a line describing it:</p>
     * <ul>
     *   <li>{@code Success}: {@code "[OK 200] Body: ..."}</li>
     *   <li>{@code ClientError}: {@code "[CLIENT_ERR 404] ..."}</li>
     *   <li>{@code ServerError}: {@code "[SERVER_ERR 500] ..."}</li>
     *   <li>{@code Timeout}: {@code "[TIMEOUT 1500ms]"}</li>
     * </ul>
     *
     * <p>Combine all lines into a report using a text block header:</p>
     * <pre>{@code
     * === API Monitoring Report ===
     * Total: 4 | Success: 2 | Errors: 1 | Timeouts: 1
     *
     * [OK 200] Body: {...}
     * [TIMEOUT 1500ms]
     * ...
     * }</pre>
     *
     * <p><b>Hints:</b> Use a switch expression over the sealed {@code ApiResponse}
     * hierarchy, records for pattern matching, text blocks with {@code .formatted()},
     * and streams to count categories.</p>
     */
    static String renderReport(List<ApiResponse<?>> responses) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 2: Expression Tree Evaluator
    // ============================================================

    /**
     * Evaluate a recursive expression tree with variable bindings.
     *
     * <p>Given an {@link Expr} tree and a {@code Map<String, Double>} of variable
     * bindings, compute the numeric result. Supported operations:</p>
     * <ul>
     *   <li>{@code Literal}: return its value directly</li>
     *   <li>{@code Var}: look up the variable name in the bindings map;
     *       throw {@code IllegalArgumentException} if not found</li>
     *   <li>{@code BinOp}: supports {@code "+"}, {@code "-"}, {@code "*"}, {@code "/"}</li>
     *   <li>{@code UnaryOp}: supports {@code "-"} (negation) and {@code "abs"}</li>
     * </ul>
     *
     * <p><b>Hints:</b> Use an exhaustive switch expression over the sealed
     * {@code Expr} hierarchy. Use {@code yield} for multi-line cases.
     * Recurse for {@code BinOp} and {@code UnaryOp}.</p>
     */
    static double evaluate(Expr expr, Map<String, Double> variables) {
        throw new UnsupportedOperationException();
    }

    // ============================================================
    // Exercise 3: Notification Dispatcher
    // ============================================================

    /**
     * Format delivery instructions for a notification.
     *
     * <p>Given a {@link Notification}, return a formatted instruction string:</p>
     * <ul>
     *   <li>{@code Email}: {@code "SEND EMAIL to <to>\nSubject: <subject>\nBody: <body>"}</li>
     *   <li>{@code Sms}: {@code "SEND SMS to <phone>\nMessage: <message>"};
     *       if the message exceeds 160 characters, append {@code "\n[WARN: message truncated]"}</li>
     *   <li>{@code Push}: {@code "PUSH to device <token>\nTitle: <title>\nPayload: <payload>"}</li>
     *   <li>{@code Slack}: {@code "POST to #<channel>\nMessage: <message>"};
     *       if {@code urgent}, prepend {@code "[URGENT] "} to the message</li>
     * </ul>
     *
     * <p><b>Hints:</b> Use a switch expression over the sealed hierarchy,
     * pattern matching for instanceof for the truncation/urgency checks,
     * text blocks for multi-line output, and records for deconstruction.</p>
     */
    static String formatDispatchInstruction(Notification notification) {
        throw new UnsupportedOperationException();
    }
}
