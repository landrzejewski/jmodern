package pl.training.jmodern.module02_java25;

import module java.base;
import java.util.concurrent.StructuredTaskScope.*;

import pl.training.jmodern.module02_java25.Exercises.*;

public class Solutions {

    static PriceQuote findLowestPrice(String productId, List<String> sources) throws Exception {
        try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())) {
            var subtasks = sources.stream()
                    .map(source -> scope.fork(() -> Exercises.fetchQuote(productId, source)))
                    .toList();
            scope.join();
            return subtasks.stream()
                    .map(Subtask::get)
                    .min(Comparator.comparingDouble(PriceQuote::price))
                    .orElseThrow();
        }
    }

    static ProductPage loadProductPage(RequestContext ctx, String productId) throws Exception {
        return ScopedValue.where(Exercises.REQUEST_CTX, ctx).call(() -> {
            try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())) {
                var productTask = scope.fork(() -> Exercises.fetchProduct(productId));
                var reviewsTask = scope.fork(() -> Exercises.fetchReviews(productId));
                scope.join();
                return new ProductPage(productTask.get(), reviewsTask.get());
            }
        });
    }

    static List<String> processWindowed(List<DataChunk> chunks, int windowSize) {
        return chunks.stream()
                .gather(Gatherers.windowFixed(windowSize))
                .map(Solutions::summarizeWindow)
                .toList();
    }

    private static String summarizeWindow(List<DataChunk> window) {
        int textCount = 0;
        double numericSum = 0.0;
        var errorCodes = new ArrayList<String>();

        for (var chunk : window) {
            switch (chunk) {
                case DataChunk.TextChunk _ -> textCount++;
                case DataChunk.NumericChunk(double value) -> numericSum += value;
                case DataChunk.ErrorChunk(var code, _) -> errorCodes.add(code);
            }
        }

        var classification = switch (numericSum) {
            case double d when d > 100 -> "high";
            case double d when d > 0 -> "low";
            default -> "none";
        };

        return "texts=%d, numericSum=%.1f (%s), errors=%s".formatted(
                textCount, numericSum, classification, errorCodes);
    }
}
