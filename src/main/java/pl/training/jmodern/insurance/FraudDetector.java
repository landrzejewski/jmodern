package pl.training.jmodern.insurance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class FraudDetector {

    public sealed interface FraudSignal {
        int score();
        String description();

        record HighFrequency(int claimCount, int score) implements FraudSignal {
            public String description() { return "High frequency: %d claims on same policy in 90 days".formatted(claimCount); }
        }

        record AmountThreshold(double percentOfCoverage, int score) implements FraudSignal {
            public String description() { return "Amount threshold: %.0f%% of max coverage".formatted(percentOfCoverage * 100); }
        }

        record SuspiciousPattern(String pattern, int score) implements FraudSignal {
            public String description() { return "Suspicious pattern: %s".formatted(pattern); }
        }

        record RecentPolicyStart(long daysSinceStart, int score) implements FraudSignal {
            public String description() { return "Recent policy start: %d days ago".formatted(daysSinceStart); }
        }
    }

    public static List<FraudSignal> detect(Claim claim, List<Claim> allClaims) {
        var signals = new ArrayList<FraudSignal>();
        var now = LocalDate.now();

        // >2 claims on same policy in 90 days
        var recentSamePolicyClaims = allClaims.stream()
                .filter(c -> c.policy().policyNumber().equals(claim.policy().policyNumber()))
                .filter(c -> ChronoUnit.DAYS.between(c.filedDate(), now) <= 90)
                .count();
        if (recentSamePolicyClaims > 2) {
            signals.add(new FraudSignal.HighFrequency((int) recentSamePolicyClaims, 25));
        }

        // amount > 80% of maxCoverage
        var maxCov = claim.policy().maxCoverage().amount();
        var requested = claim.requestedAmount().amount();
        if (maxCov.doubleValue() > 0) {
            var percent = requested.doubleValue() / maxCov.doubleValue();
            if (percent > 0.8) {
                signals.add(new FraudSignal.AmountThreshold(percent, 20));
            }
        }

        // auto + "theft" + policy < 60 days
        var daysSincePolicyStart = ChronoUnit.DAYS.between(claim.policy().coveragePeriod().start(), now);
        if (claim.type() instanceof ClaimType.AutoClaim(_, var desc, _)
                && desc.toLowerCase().contains("theft") && daysSincePolicyStart < 60) {
            signals.add(new FraudSignal.SuspiciousPattern("auto theft on policy < 60 days old", 30));
        }

        // policy < 30 days
        if (daysSincePolicyStart < 30) {
            signals.add(new FraudSignal.RecentPolicyStart(daysSincePolicyStart, 15));
        }

        return List.copyOf(signals);
    }

    public static int computeRiskScore(Claim claim, List<FraudSignal> signals) {
        var base = claim.type().baseRiskWeight();
        var signalTotal = signals.stream().mapToInt(FraudSignal::score).sum();
        return base + signalTotal;
    }

    public static String riskCategory(int riskScore) {
        return switch (riskScore) {
            case int s when s <= 30 -> "LOW";
            case int s when s <= 60 -> "MEDIUM";
            case int s when s <= 85 -> "HIGH";
            case int s when s > 85 -> "CRITICAL";
            default -> "UNKNOWN";
        };
    }
}
