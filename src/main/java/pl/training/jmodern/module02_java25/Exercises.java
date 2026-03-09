package pl.training.jmodern.module02_java25;

import module java.base;
import java.util.concurrent.StructuredTaskScope.*;

public class Exercises {

    // ---- Helper types for Exercise 1: Competitive Pricing Engine ----

    record PriceQuote(String source, double price, String currency) {}

    // ---- Helper types for Exercise 2: Request-Scoped Context ----

    record RequestContext(String requestId, String userId, String locale) {}

    record ProductInfo(String productId, String name, double price) {}

    record ReviewSummary(String productId, double averageRating, int count) {}

    record ProductPage(ProductInfo product, ReviewSummary reviews) {}

    // ---- Helper types for Exercise 3: Windowed Data Pipeline ----

    sealed interface DataChunk permits DataChunk.TextChunk, DataChunk.NumericChunk,
            DataChunk.ErrorChunk {
        record TextChunk(String content) implements DataChunk {}
        record NumericChunk(double value) implements DataChunk {}
        record ErrorChunk(String errorCode, String message) implements DataChunk {}
    }

    // ============================================================
    // Exercise 1: Competitive Pricing Engine
    // ============================================================

    /**
     * Query multiple price sources concurrently and return the lowest quote.
     *
     * <p>Given a product ID and a list of price-source names, fork a virtual
     * thread for each source using {@link java.util.concurrent.StructuredTaskScope}
     * with {@code Joiner.allSuccessfulOrThrow()}. Each source should call
     * {@link #fetchQuote(String, String)} to simulate fetching a price.
     * After all tasks complete, find the minimum-priced {@link PriceQuote}.</p>
     *
     * <p><b>Hints:</b> Use {@code StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())},
     * {@code scope.fork()}, {@code scope.join()}, and {@code Stream.min()}
     * on the results.</p>
     */
    static PriceQuote findLowestPrice(String productId, List<String> sources) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Simulates fetching a price quote from a source. Do NOT modify this method.
     */
    static PriceQuote fetchQuote(String productId, String source) {
        var random = new Random(source.hashCode() + productId.hashCode());
        var price = 50 + random.nextDouble() * 150;
        return new PriceQuote(source, Math.round(price * 100.0) / 100.0, "USD");
    }

    // ============================================================
    // Exercise 2: Request-Scoped Context Propagation
    // ============================================================

    static final ScopedValue<RequestContext> REQUEST_CTX = ScopedValue.newInstance();

    /**
     * Propagate a {@link RequestContext} through a layered architecture using {@link ScopedValue}.
     *
     * <p>Implement a method that:</p>
     * <ol>
     *   <li>Binds the given {@code RequestContext} to the {@code REQUEST_CTX} scoped value.</li>
     *   <li>Inside the scope, uses {@code StructuredTaskScope} to concurrently:
     *       <ul>
     *         <li>Fetch product info via {@link #fetchProduct(String)}</li>
     *         <li>Fetch review summary via {@link #fetchReviews(String)}</li>
     *       </ul>
     *   </li>
     *   <li>Combines the results into a {@link ProductPage}.</li>
     * </ol>
     *
     * <p><b>Hints:</b> Use {@code ScopedValue.where(REQUEST_CTX, ctx).call(() -> ...)},
     * {@code StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())}, and
     * {@code scope.fork()}.</p>
     */
    static ProductPage loadProductPage(RequestContext ctx, String productId) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Simulates fetching product info. Reads the scoped RequestContext. Do NOT modify.
     */
    static ProductInfo fetchProduct(String productId) {
        var ctx = REQUEST_CTX.get();
        return new ProductInfo(productId, "Product-" + productId + " [" + ctx.locale() + "]", 99.99);
    }

    /**
     * Simulates fetching reviews. Reads the scoped RequestContext. Do NOT modify.
     */
    static ReviewSummary fetchReviews(String productId) {
        var ctx = REQUEST_CTX.get();
        return new ReviewSummary(productId, 4.5, 42);
    }

    // ============================================================
    // Exercise 3: Windowed Data Pipeline
    // ============================================================

    /**
     * Process a stream of mixed data chunks with windowed aggregation.
     *
     * <p>Given a list of {@link DataChunk}s and a window size:</p>
     * <ol>
     *   <li>Use {@code Stream.Gatherers.windowFixed(windowSize)} to split the
     *       chunks into fixed-size windows.</li>
     *   <li>For each window, produce a summary string:
     *       <ul>
     *         <li>Count the {@code TextChunk}s, sum the {@code NumericChunk} values,
     *             and list the {@code ErrorChunk} error codes.</li>
     *         <li>Use primitive patterns in switch to classify numeric values
     *             (e.g., {@code case double d when d > 100 -> "high"}).</li>
     *       </ul>
     *   </li>
     *   <li>Return a {@code List<String>} of window summaries.</li>
     * </ol>
     *
     * <p><b>Hints:</b> Use {@code stream.gather(Gatherers.windowFixed(n))},
     * sealed type pattern matching on {@code DataChunk}, and primitive patterns.</p>
     */
    static List<String> processWindowed(List<DataChunk> chunks, int windowSize) {
        throw new UnsupportedOperationException();
    }
}
