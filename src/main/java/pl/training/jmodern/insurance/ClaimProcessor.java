package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.Claim;
import pl.training.jmodern.insurance.FraudDetector;
import pl.training.jmodern.insurance.Money;
import pl.training.jmodern.insurance.Policy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

public class ClaimProcessor {

    public record ProcessingResult(Claim claim, PayoutCalculator.PayoutBreakdown breakdown) {}

    public static List<ProcessingResult> processBatch(List<Claim> claims) {
        var results = new CopyOnWriteArrayList<ProcessingResult>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var claim : claims) {
                var allClaims = claims;
                executor.submit(() -> {
                    try {
                        var result = processSingleClaim(claim, allClaims);
                        results.add(result);
                    } catch (Exception _) {
                        AuditContext.log(claim.claimId(), "Processing failed");
                    }
                });
            }
        }

        return List.copyOf(results);
    }

    private static ProcessingResult processSingleClaim(Claim claim, List<Claim> allClaims) {
        return AuditContext.callAs("Processor", "PROCESSING", () -> {
            AuditContext.log(claim.claimId(), "Starting claim processing");

            // Step 1: Fraud detection
            var signals = FraudDetector.detect(claim, allClaims);
            var riskScore = FraudDetector.computeRiskScore(claim, signals);
            var riskCategory = FraudDetector.riskCategory(riskScore);
            var assessed = claim.withRiskScore(riskScore)
                    .withStatus(new ClaimStatus.RiskAssessed(riskScore, riskCategory, LocalDateTime.now()));

            AuditContext.log(claim.claimId(), "Risk assessed: score=%d, category=%s".formatted(riskScore, riskCategory));

            if (!signals.isEmpty()) {
                signals.forEach(s -> AuditContext.log(claim.claimId(), "Fraud signal: " + s.description()));
            }

            // Step 2: Auto-deny if critical risk
            if (riskScore > 85) {
                AuditContext.log(claim.claimId(), "Auto-denied: critical risk score");
                var denied = assessed.withStatus(new ClaimStatus.Denied(
                        "Critical risk score: %d (%s)".formatted(riskScore, riskCategory),
                        LocalDateTime.now()));
                return new ProcessingResult(denied, null);
            }

            // Step 3: Route to reviewer based on risk
            var reviewer = switch (riskCategory) {
                case "HIGH" -> "SeniorReviewer";
                case "MEDIUM" -> "StandardReviewer";
                default -> "AutoProcessor";
            };

            var reviewed = assessed.withStatus(new ClaimStatus.UnderReview(reviewer, LocalDateTime.now()));
            AuditContext.log(claim.claimId(), "Assigned to %s".formatted(reviewer));

            // Step 4: Calculate payout
            var alreadyPaid = calculateAlreadyPaid(claim.policy(), allClaims);
            var breakdown = PayoutCalculator.calculate(reviewed, alreadyPaid);

            Claim finalClaim;
            if (breakdown.approvedAmount().isPositive()) {
                finalClaim = reviewed.withStatus(new ClaimStatus.Approved(
                        breakdown.approvedAmount(), breakdown.deductible(), LocalDateTime.now()));
                AuditContext.log(claim.claimId(), "Approved: %s".formatted(breakdown.approvedAmount().format()));
            } else {
                finalClaim = reviewed.withStatus(new ClaimStatus.Denied(
                        "Payout is zero after deductible and coverage limits", LocalDateTime.now()));
                AuditContext.log(claim.claimId(), "Denied: zero payout after deductible");
            }

            return new ProcessingResult(finalClaim, breakdown);
        });
    }

    static Money calculateAlreadyPaid(Policy policy, List<Claim> allClaims) {
        return allClaims.stream()
                .filter(c -> c.policy().policyNumber().equals(policy.policyNumber()))
                .map(c -> switch (c.status()) {
                    case ClaimStatus.Approved(var amount, _, _) -> amount;
                    case ClaimStatus.Paid(var amount, _, _) -> amount;
                    default -> Money.zero(policy.premium().currency());
                })
                .reduce(Money.zero(policy.premium().currency()), Money::add);
    }

    public static String describeClaimType(ClaimType type) {
        return switch (type) {
            case ClaimType.AutoClaim(var vehicleId, var desc, var thirdParty) ->
                    "Auto claim for vehicle %s: %s (3rd party: %s)".formatted(vehicleId, desc, thirdParty ? "yes" : "no");
            case ClaimType.HealthClaim(var diag, var provider, var days) ->
                    "Health claim - %s at %s (%d days hospitalized)".formatted(diag, provider, days);
            case ClaimType.PropertyClaim(var addr, var dmgType, var area) ->
                    "Property claim - %s damage at %s (%.1f m2)".formatted(dmgType, addr, area);
            case ClaimType.TravelClaim(var dest, var reason, var date) ->
                    "Travel claim - %s to %s on %s".formatted(reason, dest, date);
        };
    }

    public static String formatDecision(ClaimStatus status) {
        return switch (status) {
            case ClaimStatus.Submitted(var at) -> """
                    SUBMITTED
                    Submitted at: %s
                    Awaiting review.""".formatted(at);
            case ClaimStatus.Validating(var at) -> """
                    VALIDATING
                    Started at: %s""".formatted(at);
            case ClaimStatus.RiskAssessed(var score, var category, var at) -> """
                    RISK ASSESSED
                    Score: %d (%s)
                    Assessed at: %s""".formatted(score, category, at);
            case ClaimStatus.UnderReview(var reviewer, var at) -> """
                    UNDER REVIEW
                    Reviewer: %s
                    Review started: %s""".formatted(reviewer, at);
            case ClaimStatus.Approved(var amount, var deductible, var at) -> """
                    APPROVED
                    Approved amount: %s
                    Deductible applied: %s
                    Approved at: %s""".formatted(amount.format(), deductible.format(), at);
            case ClaimStatus.Denied(var reason, var at) -> """
                    DENIED
                    Reason: %s
                    Denied at: %s""".formatted(reason, at);
            case ClaimStatus.Paid(var amount, var at, var txId) -> """
                    PAID
                    Amount: %s
                    Paid at: %s
                    Transaction: %s""".formatted(amount.format(), at, txId);
        };
    }
}
