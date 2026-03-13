package pl.training.jmodern.insurance;

import java.time.LocalDate;

public record Policy(String policyNumber, PolicyType type, PolicyHolder holder, DateRange coveragePeriod,
                     Money premium, Money maxCoverage, Money deductible) {

    public boolean isActive(LocalDate date) {
        return coveragePeriod.contains(date);
    }

    public Money remainingCoverage(Money alreadyPaid) {
        var remaining = maxCoverage.subtract(alreadyPaid);
        return remaining.isPositive() ? remaining : Money.zero(maxCoverage.currency());
    }

    public String summary() {
        return """
                Policy: %s
                Type:   %s
                Holder: %s
                Period: %s
                Premium: %s
                Max Coverage: %s
                Deductible: %s
                Active:  %s"""
                .formatted(policyNumber, type, holder.fullName(), coveragePeriod,
                        premium.format(), maxCoverage.format(), deductible.format(),
                        isActive(LocalDate.now()));
    }

    @Override
    public String toString() {
        return "Policy[%s, %s, %s]".formatted(policyNumber, type, holder.fullName());
    }
}
