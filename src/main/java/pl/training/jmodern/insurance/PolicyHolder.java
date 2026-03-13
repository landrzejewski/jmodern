package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.Address;

import java.time.LocalDate;
import java.time.Period;

public record PolicyHolder(String id, String firstName, String lastName, LocalDate dateOfBirth, Address address) {

    public PolicyHolder {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be blank");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("First name cannot be blank");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("Last name cannot be blank");
    }

    public String fullName() {
        return "%s %s".formatted(firstName, lastName);
    }

    public int age() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "%s (age %d)".formatted(fullName(), age());
    }
}
