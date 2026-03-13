package pl.training.jmodern.insurance;

public record Address(String street, String city, String state, String zipCode, String country) {

    public Address {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street cannot be blank");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City cannot be blank");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country cannot be blank");
    }

    public String prettyPrint() {
        return """
                %s
                %s, %s %s
                %s""".formatted(street, city, state != null ? state : "", zipCode != null ? zipCode : "", country);
    }

    @Override
    public String toString() {
        return "%s, %s, %s".formatted(street, city, country);
    }
}
