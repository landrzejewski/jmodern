package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.Claim;
import pl.training.jmodern.insurance.Money;

import java.math.BigDecimal;

public class PayoutCalculator {

    public record PayoutBreakdown(Money requestedAmount, Money deductible, Money afterDeductible,
                                  Money coverageLimit, Money approvedAmount, String explanation) {}

    public static PayoutBreakdown calculate(Claim claim, Money alreadyPaid) {
        var requested = claim.requestedAmount();
        var deductible = claim.policy().deductible();
        var afterDeductible = requested.subtract(deductible);

        if (!afterDeductible.isPositive()) {
            afterDeductible = Money.zero(requested.currency());
        }

        var multiplied = applyTypeMultiplier(claim.type(), afterDeductible);
        var remaining = claim.policy().remainingCoverage(alreadyPaid);
        var approved = multiplied.min(remaining);

        var explanation = """
                Payout Calculation for %s:
                  Requested amount:     %s
                  Deductible applied:   %s
                  After deductible:     %s
                  Type multiplier applied → %s
                  Remaining coverage:   %s
                  Approved amount:      %s"""
                .formatted(claim.claimId(), requested.format(), deductible.format(),
                        afterDeductible.format(), multiplied.format(), remaining.format(), approved.format());

        return new PayoutBreakdown(requested, deductible, afterDeductible, remaining, approved, explanation);
    }

    private static Money applyTypeMultiplier(ClaimType type, Money amount) {
        return switch (type) {
            case ClaimType.AutoClaim(_, _, var thirdParty) when thirdParty ->
                    amount.multiply(BigDecimal.valueOf(0.9));
            case ClaimType.AutoClaim _ ->
                    amount.multiply(BigDecimal.valueOf(0.75));
            case ClaimType.HealthClaim(_, _, var days) when days > 5 ->
                    amount;
            case ClaimType.HealthClaim _ ->
                    amount.multiply(BigDecimal.valueOf(0.8));
            case ClaimType.PropertyClaim(_, _, var area) when area > 100.0 ->
                    amount.multiply(BigDecimal.valueOf(0.95));
            case ClaimType.PropertyClaim _ ->
                    amount.multiply(BigDecimal.valueOf(0.7));
            case ClaimType.TravelClaim(_, var reason, _) when reason.equalsIgnoreCase("medical emergency") ->
                    amount;
            case ClaimType.TravelClaim _ ->
                    amount.multiply(BigDecimal.valueOf(0.6));
        };
    }
}
