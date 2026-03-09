package pl.training.jmodern.module02_java8;

import pl.training.jmodern.module02_java8.Exercises.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Solutions {

    static Map<String, Double> spendingByCategory(List<Transaction> transactions, YearMonth month) {
        return transactions.stream()
                .filter(t -> YearMonth.from(t.date()).equals(month))
                .collect(Collectors.groupingBy(Transaction::category, Collectors.summingDouble(Transaction::amount)));
    }

    static List<String> extractLogLevels(List<String> rawLines) {
        Mapper<String, Optional<String>> extractLevel = line -> {
            var tokens = line.split("\\s+");
            return tokens.length >= 2 ? Optional.of(tokens[1]) : Optional.empty();
        };
        var pipeline = extractLevel.andThen(opt -> opt.map(String::toUpperCase));
        return rawLines.stream()
                .map(pipeline::apply)
                .flatMap(Optional::stream)
                .distinct()
                .sorted()
                .toList();
    }

    static String findAvailableEmployees(List<Employee> employees,
                                         LocalDate meetingDay,
                                         LocalTime startTime,
                                         LocalTime endTime) {
        Predicate<Employee> worksOnDay = e -> e.workingDays().contains(meetingDay.getDayOfWeek());
        Predicate<Employee> availableAtTime = e ->
                !e.startHour().isAfter(startTime) && !e.endHour().isBefore(endTime);
        return employees.stream()
                .filter(worksOnDay.and(availableAtTime))
                .map(Employee::name)
                .collect(Collectors.joining(", "));
    }
}
