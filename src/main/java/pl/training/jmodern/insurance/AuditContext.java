package pl.training.jmodern.insurance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuditContext {

    public record AuditEntry(String actor, String phase, String claimId, String detail, LocalDateTime timestamp) {

        @Override
        public String toString() {
            return "[%s] %-12s | %-10s | %-8s | %s".formatted(
                    timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")),
                    actor, phase, claimId, detail);
        }
    }

    public static final ScopedValue<String> CURRENT_ACTOR = ScopedValue.newInstance();
    public static final ScopedValue<String> PROCESSING_PHASE = ScopedValue.newInstance();

    private static final CopyOnWriteArrayList<AuditEntry> auditLog = new CopyOnWriteArrayList<>();

    public static void runAs(String actor, String phase, Runnable task) {
        ScopedValue.where(CURRENT_ACTOR, actor)
                .where(PROCESSING_PHASE, phase)
                .run(task);
    }

    public static <T, X extends Throwable> T callAs(String actor, String phase, ScopedValue.CallableOp<T, X> task) throws X {
        return ScopedValue.where(CURRENT_ACTOR, actor)
                .where(PROCESSING_PHASE, phase)
                .call(task);
    }

    public static void log(String claimId, String detail) {
        var actor = CURRENT_ACTOR.isBound() ? CURRENT_ACTOR.get() : "SYSTEM";
        var phase = PROCESSING_PHASE.isBound() ? PROCESSING_PHASE.get() : "UNKNOWN";
        auditLog.add(new AuditEntry(actor, phase, claimId, detail, LocalDateTime.now()));
    }

    public static List<AuditEntry> getLog() {
        return List.copyOf(auditLog);
    }

    public static String formatLog() {
        var entries = List.copyOf(auditLog);
        if (entries.isEmpty()) {
            return "  (no audit entries)";
        }

        var first = entries.getFirst();
        var last = entries.getLast();
        var reversed = entries.reversed();

        var sb = new StringBuilder();
        sb.append("  Audit Trail (%d entries)\n".formatted(entries.size()));
        sb.append("  First: %s\n".formatted(first));
        sb.append("  Last:  %s\n".formatted(last));
        sb.append("  \n  --- All entries (chronological) ---\n");

        for (var entry : entries) {
            sb.append("  ").append(entry).append("\n");
        }

        sb.append("  \n  --- Most recent 5 (reversed) ---\n");
        reversed.stream().limit(5).forEach(e -> sb.append("  ").append(e).append("\n"));

        return sb.toString();
    }

    public static void clear() {
        auditLog.clear();
    }
}
