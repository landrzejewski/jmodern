package pl.training.jmodern.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Consumer;

public class ClaimValidator {

    public sealed interface ValidationResult {
        record Success(Claim claim) implements ValidationResult {}
        record Failure(Claim claim, List<String> errors) implements ValidationResult {}
    }

    public static ValidationResult validate(Claim claim) {
        var validatingClaim = claim.withStatus(new ClaimStatus.Validating(LocalDateTime.now()));

        try {
            List<String> errors = AuditContext.callAs("Validator", "VALIDATION", () -> {
                AuditContext.log(claim.claimId(), "Starting parallel validation checks");

                try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())) {

                    var checkPolicy = scope.fork(() -> checkActivePolicy(claim));
                    var checkAmount = scope.fork(() -> checkPositiveAmount(claim));
                    var checkResolved = scope.fork(() -> checkNotResolved(claim));
                    var checkFiling = scope.fork(() -> checkFilingPeriod(claim));

                    scope.join();

                    var collectedErrors = java.util.stream.Stream.of(
                                    checkPolicy.get(), checkAmount.get(),
                                    checkResolved.get(), checkFiling.get())
                            .flatMap(Optional::stream)
                            .toList();

                    AuditContext.log(claim.claimId(), collectedErrors.isEmpty()
                            ? "Validation passed" : "Validation failed: %d errors".formatted(collectedErrors.size()));

                    return collectedErrors;
                }
            });

            return errors.isEmpty()
                    ? new ValidationResult.Success(validatingClaim)
                    : new ValidationResult.Failure(validatingClaim, errors);

        } catch (Exception _) {
            return new ValidationResult.Failure(validatingClaim, List.of("Validation interrupted"));
        }
    }

    private static Optional<String> checkActivePolicy(Claim claim) {
        return claim.policy().isActive(LocalDate.now())
                ? Optional.empty()
                : Optional.of("Policy is not active");
    }

    private static Optional<String> checkPositiveAmount(Claim claim) {
        return claim.requestedAmount().amount().compareTo(BigDecimal.ZERO) > 0
                ? Optional.empty()
                : Optional.of("Requested amount must be positive");
    }

    private static Optional<String> checkNotResolved(Claim claim) {
        return !claim.isResolved()
                ? Optional.empty()
                : Optional.of("Claim is already resolved");
    }

    private static Optional<String> checkFilingPeriod(Claim claim) {
        return claim.filedDate().isAfter(LocalDate.now().minusYears(1))
                ? Optional.empty()
                : Optional.of("Claim filed outside the allowed filing period");
    }

    public static final Consumer<ValidationResult> logResult = result -> {
        switch (result) {
            case ValidationResult.Success(var claim) ->
                    System.out.println("  + Claim %s passed validation".formatted(claim.claimId()));
            case ValidationResult.Failure(var claim, var errors) -> {
                System.out.println("  x Claim %s failed validation:".formatted(claim.claimId()));
                errors.forEach(e -> System.out.println("    - " + e));
            }
        }
    };
}
