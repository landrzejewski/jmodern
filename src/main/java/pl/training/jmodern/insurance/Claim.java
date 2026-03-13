package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.Policy;

import java.time.LocalDate;

public record Claim(String claimId, Policy policy, ClaimType type, Money requestedAmount,
                    LocalDate filedDate, ClaimStatus status, int riskScore) {

    public Claim {
        if (riskScore < 0) riskScore = 0;
    }

    public Claim(String claimId, Policy policy, ClaimType type, Money requestedAmount,
                 LocalDate filedDate, ClaimStatus status) {
        this(claimId, policy, type, requestedAmount, filedDate, status, 0);
    }

    public Claim withStatus(ClaimStatus newStatus) {
        return new Claim(claimId, policy, type, requestedAmount, filedDate, newStatus, riskScore);
    }

    public Claim withRiskScore(int newRiskScore) {
        return new Claim(claimId, policy, type, requestedAmount, filedDate, status, newRiskScore);
    }

    public boolean isResolved() {
        return switch (status) {
            case ClaimStatus.Approved _ -> true;
            case ClaimStatus.Denied _ -> true;
            case ClaimStatus.Paid _ -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return "Claim[%s, %s, %s, status=%s]".formatted(claimId, type.getClass().getSimpleName(), requestedAmount, status.getClass().getSimpleName());
    }
}
