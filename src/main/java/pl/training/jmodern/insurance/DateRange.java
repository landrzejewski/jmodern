package pl.training.jmodern.insurance;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        if (start == null || end == null) throw new IllegalArgumentException("Dates cannot be null");
        if (end.isBefore(start)) throw new IllegalArgumentException("End date cannot be before start date");
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public boolean overlaps(DateRange other) {
        return !start.isAfter(other.end) && !end.isBefore(other.start);
    }

    public long daysCount() {
        return ChronoUnit.DAYS.between(start, end);
    }

    @Override
    public String toString() {
        return "%s to %s (%d days)".formatted(start, end, daysCount());
    }
}
