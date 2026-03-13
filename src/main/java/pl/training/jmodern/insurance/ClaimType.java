package pl.training.jmodern.insurance;

import java.time.LocalDate;

public sealed interface ClaimType {

    record AutoClaim(String vehicleId, String accidentDescription, boolean thirdPartyInvolved) implements ClaimType {}

    record HealthClaim(String diagnosis, String providerName, int daysHospitalized) implements ClaimType {}

    record PropertyClaim(String propertyAddress, String damageType, double damageAreaSqMeters) implements ClaimType {}

    record TravelClaim(String destination, String reason, LocalDate travelDate) implements ClaimType {}

    default int baseRiskWeight() {
        return switch (this) {
            case AutoClaim(_, _, var thirdParty) when thirdParty -> 30;
            case AutoClaim _ -> 15;
            case HealthClaim(_, _, var days) when days > 5 -> 20;
            case HealthClaim _ -> 10;
            case PropertyClaim(_, _, var area) when area > 100.0 -> 25;
            case PropertyClaim _ -> 12;
            case TravelClaim(_, var reason, _) when reason.equalsIgnoreCase("medical emergency") -> 18;
            case TravelClaim _ -> 8;
        };
    }
}
