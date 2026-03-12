package pl.training.jmodern.module02_java11;

import pl.training.jmodern.module02_java11.Exercises.ConfigEntry;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.*;
import java.util.stream.*;

public class Solutions {

    static List<ConfigEntry> parseConfig(String configText) {
        return configText.lines()
                .filter(line -> !line.strip().startsWith("#") && line.contains("="))
                .map(line -> {
                    var parts = line.split("=", 2);
                    return new ConfigEntry(parts[0].strip(), parts[1].strip());
                })
                .toList();
    }

    static String fileInventoryReport(List<String> fileNames, List<Long> sizes) {
        record FileEntry(String name, long size) {}

        var filesWithExtensions = IntStream.range(0, fileNames.size())
                .mapToObj(i -> new FileEntry(fileNames.get(i), sizes.get(i)))
                .filter(f -> f.name().contains("."))
                .toList();

        var grouped = filesWithExtensions.stream()
                .collect(Collectors.groupingBy(
                        f -> f.name().substring(f.name().lastIndexOf('.') + 1),
                        TreeMap::new,
                        Collectors.toList()
                ));

        var sb = new StringBuilder();
        for (var entry : grouped.entrySet()) {
            sb.append(entry.getKey()).append("\n");
            for (var file : entry.getValue()) {
                var padding = " ".repeat(Math.max(1, 20 - file.name().length()));
                sb.append("  ").append(file.name()).append(padding).append(file.size()).append("\n");
            }
        }
        return sb.toString().stripTrailing();
    }

    static Map<String, String> checkEndpoints(List<String> urls) {
        var client = HttpClient.newHttpClient();
        var results = new LinkedHashMap<String, String>();
        for (var url : urls) {
            try {
                var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                var response = client.send(request, BodyHandlers.discarding());
                results.put(url, response.statusCode() / 100 == 2 ? "UP" : "DOWN");
            } catch (Exception e) {
                results.put(url, "DOWN");
            }
        }
        return results;
    }
}
