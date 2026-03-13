package pl.training.jmodern.insurance;

import pl.training.jmodern.insurance.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ===============================================
        // SECTION 1: Setup - Policy Holders & Policies
        // ===============================================

        System.out.println("""
                ========================================
                  INSURANCE CLAIMS PROCESSING SYSTEM
                  Java 25 Feature Showcase
                ========================================
                """);

        var address1 = new Address("123 Oak Street", "Springfield", "IL", "62704", "USA");
        var address2 = new Address("45 Elm Avenue", "Portland", "OR", "97201", "USA");
        var address3 = new Address("789 Pine Road", "Austin", "TX", "73301", "USA");
        var address4 = new Address("10 Maple Lane", "Denver", "CO", "80201", "USA");

        var holder1 = new PolicyHolder("PH-001", "Anna", "Kowalski", LocalDate.of(1985, 3, 15), address1);
        var holder2 = new PolicyHolder("PH-002", "Jan", "Nowak", LocalDate.of(1990, 7, 22), address2);
        var holder3 = new PolicyHolder("PH-003", "Maria", "Wisniewska", LocalDate.of(1978, 11, 5), address3);
        var holder4 = new PolicyHolder("PH-004", "Tomek", "Zielinski", LocalDate.of(2000, 1, 30), address4);

        var now = LocalDate.now();
        var activePeriod = new DateRange(now.minusMonths(6), now.plusMonths(6));
        var recentPeriod = new DateRange(now.minusDays(20), now.plusMonths(11));
        var expiredPeriod = new DateRange(now.minusYears(2), now.minusYears(1));

        var policyAuto = new Policy("POL-AUTO-001", PolicyType.AUTO, holder1, activePeriod,
                new Money(1200.0, "USD"), new Money(50000.0, "USD"), new Money(500.0, "USD"));
        var policyHealth = new Policy("POL-HEALTH-001", PolicyType.HEALTH, holder2, activePeriod,
                new Money(3500.0, "USD"), new Money(100000.0, "USD"), new Money(1000.0, "USD"));
        var policyProperty = new Policy("POL-PROP-001", PolicyType.PROPERTY, holder3, activePeriod,
                new Money(2800.0, "USD"), new Money(200000.0, "USD"), new Money(2500.0, "USD"));
        var policyTravel = new Policy("POL-TRAVEL-001", PolicyType.TRAVEL, holder4, activePeriod,
                new Money(600.0, "USD"), new Money(25000.0, "USD"), new Money(200.0, "USD"));
        var policyRecent = new Policy("POL-AUTO-003", PolicyType.AUTO, holder4, recentPeriod,
                new Money(900.0, "USD"), new Money(30000.0, "USD"), new Money(750.0, "USD"));
        var policyExpired = new Policy("POL-AUTO-002", PolicyType.AUTO, holder1, expiredPeriod,
                new Money(1100.0, "USD"), new Money(40000.0, "USD"), new Money(500.0, "USD"));

        System.out.println("--- Policy Summaries ---");
        List.of(policyAuto, policyHealth, policyProperty, policyTravel, policyRecent, policyExpired)
                .forEach(p -> System.out.println(p.summary()));

        // ===============================================
        // SECTION 2: Create Claims
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: CLAIMS CREATION
                ========================================
                """);

        var submitted = new ClaimStatus.Submitted(LocalDateTime.now());

        var claims = List.of(
                new Claim("CLM-001", policyAuto,
                        new ClaimType.AutoClaim("VH-123", "Rear-end collision at intersection", true),
                        new Money(5000.0, "USD"), now, submitted),
                new Claim("CLM-002", policyAuto,
                        new ClaimType.AutoClaim("VH-456", "Parking lot scratch", false),
                        new Money(800.0, "USD"), now, submitted),
                new Claim("CLM-003", policyHealth,
                        new ClaimType.HealthClaim("Appendectomy", "City General Hospital", 7),
                        new Money(15000.0, "USD"), now, submitted),
                new Claim("CLM-004", policyHealth,
                        new ClaimType.HealthClaim("Annual checkup", "Downtown Clinic", 0),
                        new Money(350.0, "USD"), now, submitted),
                new Claim("CLM-005", policyProperty,
                        new ClaimType.PropertyClaim("789 Pine Road", "Fire", 150.0),
                        new Money(45000.0, "USD"), now, submitted),
                new Claim("CLM-006", policyProperty,
                        new ClaimType.PropertyClaim("789 Pine Road", "Water damage", 30.0),
                        new Money(8000.0, "USD"), now, submitted),
                new Claim("CLM-007", policyTravel,
                        new ClaimType.TravelClaim("Tokyo", "medical emergency", LocalDate.of(2026, 1, 15)),
                        new Money(12000.0, "USD"), now, submitted),
                new Claim("CLM-008", policyTravel,
                        new ClaimType.TravelClaim("Paris", "flight cancellation", LocalDate.of(2026, 2, 20)),
                        new Money(2500.0, "USD"), now, submitted),
                new Claim("CLM-009", policyExpired,
                        new ClaimType.AutoClaim("VH-789", "Windshield crack", false),
                        new Money(600.0, "USD"), now, submitted),
                new Claim("CLM-010", policyAuto,
                        new ClaimType.AutoClaim("VH-123", "Theft of stereo system", false),
                        new Money(0.0, "USD"), now, submitted)
        );

        claims.forEach(c -> System.out.println("  Created: " + c));

        // ===============================================
        // SECTION 3: Validation (Structured Concurrency)
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: VALIDATION
                  (Structured Concurrency + ScopedValue)
                ========================================
                """);

        var validationResults = claims.stream()
                .map(ClaimValidator::validate)
                .toList();

        validationResults.forEach(ClaimValidator.logResult);

        var validClaims = validationResults.stream()
                .filter(r -> r instanceof ClaimValidator.ValidationResult.Success)
                .map(r -> ((ClaimValidator.ValidationResult.Success) r).claim())
                .toList();

        System.out.println("\n  Valid claims: %d / %d".formatted(validClaims.size(), claims.size()));

        // ===============================================
        // SECTION 4: Batch Processing (Virtual Threads)
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: BATCH PROCESSING
                  (Virtual Threads + Fraud Detection)
                ========================================
                """);

        var results = ClaimProcessor.processBatch(validClaims);

        // ===============================================
        // SECTION 5: Results - Payout Breakdowns
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: RESULTS
                  (PayoutCalculator + Decision Formatting)
                ========================================
                """);

        for (var result : results) {
            var claim = result.claim();
            System.out.println("  " + claim.claimId() + " (risk score: %d):".formatted(claim.riskScore()));
            System.out.println("    Type: " + ClaimProcessor.describeClaimType(claim.type()));
            System.out.println("    Decision:\n" + ClaimProcessor.formatDecision(claim.status()).indent(6));

            if (result.breakdown() != null) {
                System.out.println(result.breakdown().explanation().indent(4));
            }
        }

        // ===============================================
        // SECTION 6: Analytics (Custom Gatherers)
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: ANALYTICS
                  (Custom Gatherers + Sequenced Collections)
                ========================================
                """);

        var processedClaims = results.stream().map(ClaimProcessor.ProcessingResult::claim).toList();
        var analytics = new ClaimAnalytics(processedClaims);
        System.out.println(analytics.generateReport());

        // ===============================================
        // SECTION 7: Audit Trail (ScopedValue log dump)
        // ===============================================

        System.out.println("""

                ========================================
                  SECTION: AUDIT TRAIL
                  (ScopedValue + Date/Time API)
                ========================================
                """);

        System.out.println(AuditContext.formatLog());

        // Date/Time showcase
        var oldest = holder3;
        var youngest = holder4;
        var ageDifference = Period.between(youngest.dateOfBirth(), oldest.dateOfBirth());
        System.out.println("  Age difference between %s and %s: %d years, %d months, %d days"
                .formatted(youngest.fullName(), oldest.fullName(),
                        Math.abs(ageDifference.getYears()), Math.abs(ageDifference.getMonths()), Math.abs(ageDifference.getDays())));

        var formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        System.out.println("  Today formatted: " + now.format(formatter));
        System.out.println("  Policy expiry: " + activePeriod.end().format(formatter));
        System.out.println("  Coverage period: " + activePeriod);
        System.out.println("  Address example:\n" + address1.prettyPrint().indent(4));

        System.out.println("""

                ========================================
                  DONE - All sections completed
                ========================================
                """);
    }
}
