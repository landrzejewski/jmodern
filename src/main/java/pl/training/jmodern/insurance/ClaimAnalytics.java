package pl.training.jmodern.insurance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Gatherer;

public class ClaimAnalytics {

    record ClaimSummary(String claimId, String type, Money amount, String status, int riskScore) {}

    private final List<Claim> claims;

    public ClaimAnalytics(List<Claim> claims) {
        this.claims = claims;
    }

    // Custom Gatherer: sliding window using Gatherer.ofSequential + Sequenced Collections (removeFirst)
    public static <T> Gatherer<T, ?, List<T>> slidingWindow(int size) {
        return Gatherer.ofSequential(
                () -> new ArrayList<T>(),
                (window, element, downstream) -> {
                    window.add(element);
                    if (window.size() > size) {
                        window.removeFirst();
                    }
                    if (window.size() == size) {
                        downstream.push(List.copyOf(window));
                    }
                    return true;
                }
        );
    }

    // Custom Gatherer: running total of Money values
    public static Gatherer<Money, ?, Money> runningTotal() {
        return Gatherer.ofSequential(
                () -> new Money[]{Money.zero("USD")},
                (state, element, downstream) -> {
                    state[0] = state[0].add(element);
                    downstream.push(state[0]);
                    return true;
                }
        );
    }

    public List<List<Claim>> slidingWindowRiskAnalysis(int windowSize) {
        return claims.stream()
                .sorted(Comparator.comparing(Claim::filedDate))
                .gather(slidingWindow(windowSize))
                .toList();
    }

    public List<Money> runningPayoutTotals() {
        return claims.stream()
                .filter(c -> c.status() instanceof ClaimStatus.Approved)
                .map(c -> switch (c.status()) {
                    case ClaimStatus.Approved(var amount, _, _) -> amount;
                    default -> Money.zero("USD");
                })
                .gather(runningTotal())
                .toList();
    }

    public String generateReport() {
        var sorted = claims.stream()
                .sorted(Comparator.comparing(Claim::filedDate))
                .toList();

        var summaries = sorted.stream()
                .map(c -> new ClaimSummary(
                        c.claimId(),
                        c.type().getClass().getSimpleName(),
                        c.requestedAmount(),
                        c.status().getClass().getSimpleName(),
                        c.riskScore()))
                .toList();

        var first = sorted.isEmpty() ? "N/A" : sorted.getFirst().claimId();
        var last = sorted.isEmpty() ? "N/A" : sorted.getLast().claimId();

        var reversed = sorted.reversed();
        var recentThree = reversed.stream().limit(3)
                .map(c -> "    %s - %s (%s)".formatted(c.claimId(), c.requestedAmount().format(), c.type().getClass().getSimpleName()))
                .collect(Collectors.joining("\n"));

        var approvedCount = claims.stream().filter(c -> c.status() instanceof ClaimStatus.Approved).count();
        var deniedCount = claims.stream().filter(c -> c.status() instanceof ClaimStatus.Denied).count();

        // Sliding window analysis
        var windows = slidingWindowRiskAnalysis(3);
        var windowAnalysis = windows.stream()
                .map(w -> {
                    var avgRisk = w.stream().mapToInt(Claim::riskScore).average().orElse(0);
                    var ids = w.stream().map(Claim::claimId).collect(Collectors.joining(", "));
                    return "    [%s] avg risk: %.1f".formatted(ids, avgRisk);
                })
                .collect(Collectors.joining("\n"));

        // Running payout totals
        var payoutTotals = runningPayoutTotals();
        var payoutStr = payoutTotals.stream()
                .map(m -> "    " + m.format())
                .collect(Collectors.joining("\n"));

        return """
                ========================================
                   CLAIMS ANALYTICS REPORT
                ========================================
                Total claims: %d
                Approved: %d | Denied: %d

                First filed: %s
                Last filed:  %s

                Most recent 3 claims (reversed):
                %s

                Sliding window risk analysis (window=3):
                %s

                Running payout totals:
                %s

                All summaries:
                %s
                ========================================"""
                .formatted(
                        claims.size(),
                        approvedCount, deniedCount,
                        first, last,
                        recentThree,
                        windowAnalysis.isEmpty() ? "    (insufficient data)" : windowAnalysis,
                        payoutStr.isEmpty() ? "    (no approved claims)" : payoutStr,
                        summaries.stream()
                                .map(s -> "    [%s] %s - %s (%s, risk=%d)".formatted(s.status(), s.claimId(), s.amount().format(), s.type(), s.riskScore()))
                                .collect(Collectors.joining("\n"))
                );
    }
}
