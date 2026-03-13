package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.Money;

import java.time.LocalDateTime;

public sealed interface ClaimStatus {

    record Submitted(LocalDateTime submittedAt) implements ClaimStatus {}

    record Validating(LocalDateTime startedAt) implements ClaimStatus {}

    record RiskAssessed(int riskScore, String riskCategory, LocalDateTime assessedAt) implements ClaimStatus {}

    record UnderReview(String reviewerName, LocalDateTime reviewStartedAt) implements ClaimStatus {}

    record Approved(Money approvedAmount, Money deductibleApplied, LocalDateTime approvedAt) implements ClaimStatus {}

    record Denied(String reason, LocalDateTime deniedAt) implements ClaimStatus {}

    record Paid(Money paidAmount, LocalDateTime paidAt, String transactionId) implements ClaimStatus {}
}
