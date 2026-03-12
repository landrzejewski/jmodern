package pl.training.jmodern.module02_java21;

import pl.training.jmodern.module02_java21.Exercises.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Solutions {

    static SequencedMap<String, Double> reconcile(SequencedCollection<FinancialEvent> events) {
        System.out.println("First event: " + events.getFirst());
        System.out.println("Last event: " + events.getLast());

        var balances = new LinkedHashMap<String, Double>();
        for (var event : events) {
            switch (event) {
                case FinancialEvent.Payment(Money(var currency, var amount), _) ->
                        balances.merge(currency, amount, Double::sum);
                case FinancialEvent.Refund(Money(var currency, var amount), _) ->
                        balances.merge(currency, -amount, Double::sum);
            }
        }
        return balances;
    }

    static Map<String, List<AppEvent>> classifyEvents(List<AppEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(Solutions::classify));
    }

    private static String classify(AppEvent event) {
        return switch (event) {
            case AppEvent.UserLogin(_, _, var suspicious) when suspicious -> "ALERT";
            case AppEvent.UserLogin _ -> "INFO";
            case AppEvent.UserLogout _ -> "INFO";
            case AppEvent.PageView(_, _, var duration) when duration > 30_000 -> "WARNING";
            case AppEvent.PageView _ -> "INFO";
            case AppEvent.ApiCall(_, var status, _) when status >= 500 -> "ALERT";
            case AppEvent.ApiCall(_, var status, _) when status >= 400 -> "WARNING";
            case AppEvent.ApiCall(_, _, var latency) when latency > 5000 -> "WARNING";
            case AppEvent.ApiCall _ -> "INFO";
        };
    }

    static SequencedMap<String, Double> aggregateSensors(Map<String, List<Double>> sensorData)
            throws InterruptedException {
        var results = new ConcurrentHashMap<String, Double>();
        var threads = new ArrayList<Thread>();

        for (var entry : sensorData.entrySet()) {
            var thread = Thread.ofVirtual().start(() -> {
                var avg = entry.getValue().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                results.put(entry.getKey(), avg);
            });
            threads.add(thread);
        }

        for (var thread : threads) {
            thread.join();
        }

        var sorted = new TreeMap<>(results);
        System.out.println("First sensor: " + sorted.firstEntry());
        System.out.println("Last sensor: " + sorted.lastEntry());
        return Collections.unmodifiableSequencedMap(sorted);
    }
}
