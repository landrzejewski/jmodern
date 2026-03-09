package pl.training.jmodern.module02_java17;

import pl.training.jmodern.module02_java17.Exercises.*;

import java.util.*;
import java.util.stream.*;

public class Solutions {

    static String renderReport(List<ApiResponse<?>> responses) {
        var lines = responses.stream()
                .map(r -> switch (r) {
                    case ApiResponse.Success<?> s -> "[OK %d] Body: %s".formatted(s.statusCode(), s.body());
                    case ApiResponse.ClientError<?> e -> "[CLIENT_ERR %d] %s".formatted(e.statusCode(), e.message());
                    case ApiResponse.ServerError<?> e -> "[SERVER_ERR %d] %s".formatted(e.statusCode(), e.message());
                    case ApiResponse.Timeout<?> t -> "[TIMEOUT %dms]".formatted(t.durationMs());
                })
                .toList();

        long successes = responses.stream().filter(r -> r instanceof ApiResponse.Success).count();
        long errors = responses.stream().filter(r -> r instanceof ApiResponse.ClientError || r instanceof ApiResponse.ServerError).count();
        long timeouts = responses.stream().filter(r -> r instanceof ApiResponse.Timeout).count();

        var header = """
                === API Monitoring Report ===
                Total: %d | Success: %d | Errors: %d | Timeouts: %d
                """.formatted(responses.size(), successes, errors, timeouts);

        return header + "\n" + String.join("\n", lines);
    }

    static double evaluate(Expr expr, Map<String, Double> variables) {
        return switch (expr) {
            case Expr.Literal l -> l.value();
            case Expr.Var v -> {
                if (!variables.containsKey(v.name())) {
                    throw new IllegalArgumentException("Unknown variable: " + v.name());
                }
                yield variables.get(v.name());
            }
            case Expr.BinOp b -> {
                var left = evaluate(b.left(), variables);
                var right = evaluate(b.right(), variables);
                yield switch (b.operator()) {
                    case "+" -> left + right;
                    case "-" -> left - right;
                    case "*" -> left * right;
                    case "/" -> left / right;
                    default -> throw new IllegalArgumentException("Unknown operator: " + b.operator());
                };
            }
            case Expr.UnaryOp u -> {
                var operand = evaluate(u.operand(), variables);
                yield switch (u.operator()) {
                    case "-" -> -operand;
                    case "abs" -> Math.abs(operand);
                    default -> throw new IllegalArgumentException("Unknown operator: " + u.operator());
                };
            }
        };
    }

    static String formatDispatchInstruction(Notification notification) {
        return switch (notification) {
            case Notification.Email e ->
                    "SEND EMAIL to %s\nSubject: %s\nBody: %s".formatted(e.to(), e.subject(), e.body());
            case Notification.Sms s -> {
                var result = "SEND SMS to %s\nMessage: %s".formatted(s.phoneNumber(), s.message());
                if (s.message().length() > 160) {
                    result += "\n[WARN: message truncated]";
                }
                yield result;
            }
            case Notification.Push p ->
                    "PUSH to device %s\nTitle: %s\nPayload: %s".formatted(p.deviceToken(), p.title(), p.payload());
            case Notification.Slack s -> {
                var message = s.urgent() ? "[URGENT] " + s.message() : s.message();
                yield "POST to #%s\nMessage: %s".formatted(s.channel(), message);
            }
        };
    }
}
