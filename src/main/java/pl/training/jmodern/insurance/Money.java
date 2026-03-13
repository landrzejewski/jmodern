package pl.training.jmodern.insurance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public record Money(BigDecimal amount, String currency) implements Comparable<Money> {

    public Money {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency cannot be blank");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase();
    }

    public Money(double amount, String currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    public Money min(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) <= 0 ? this : other;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String format() {
        var currencyInstance = Currency.getInstance(currency);
        var formatter = NumberFormat.getCurrencyInstance(Locale.US);
        formatter.setCurrency(currencyInstance);
        return formatter.format(amount);
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on different currencies: %s vs %s".formatted(currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return format();
    }
}
