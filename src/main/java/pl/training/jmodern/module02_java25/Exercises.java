package pl.training.jmodern.module02_java25;

import module java.base;
import java.util.concurrent.StructuredTaskScope.*;

public class Exercises {

    // ---- Typy pomocnicze do Ćwiczenia 1: Silnik porównywania cen ----

    record PriceQuote(String source, double price, String currency) {}

    // ---- Typy pomocnicze do Ćwiczenia 2: Kontekst zakresu żądania ----

    record RequestContext(String requestId, String userId, String locale) {}

    record ProductInfo(String productId, String name, double price) {}

    record ReviewSummary(String productId, double averageRating, int count) {}

    record ProductPage(ProductInfo product, ReviewSummary reviews) {}

    // ---- Typy pomocnicze do Ćwiczenia 3: Potok danych z okienkowaniem ----

    sealed interface DataChunk permits DataChunk.TextChunk, DataChunk.NumericChunk,
            DataChunk.ErrorChunk {
        record TextChunk(String content) implements DataChunk {}
        record NumericChunk(double value) implements DataChunk {}
        record ErrorChunk(String errorCode, String message) implements DataChunk {}
    }

    // ============================================================
    // Ćwiczenie 1: Silnik porównywania cen
    // ============================================================

    /**
     * Odpytaj wiele źródeł cen współbieżnie i zwróć najniższą ofertę.
     *
     * <p>Mając identyfikator produktu i listę nazw źródeł cen, utwórz virtual thread
     * dla każdego źródła za pomocą {@link java.util.concurrent.StructuredTaskScope}
     * z {@code Joiner.allSuccessfulOrThrow()}. Każde źródło powinno wywołać
     * {@link #fetchQuote(String, String)} aby zasymulować pobieranie ceny.
     * Po zakończeniu wszystkich zadań, znajdź {@link PriceQuote} z najniższą ceną.</p>
     *
     * <p><b>Wskazówki:</b> Użyj {@code StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())},
     * {@code scope.fork()}, {@code scope.join()} oraz {@code Stream.min()}
     * na wynikach.</p>
     */
    static PriceQuote findLowestPrice(String productId, List<String> sources) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Symuluje pobieranie oferty cenowej ze źródła. NIE modyfikuj tej metody.
     */
    static PriceQuote fetchQuote(String productId, String source) {
        var random = new Random(source.hashCode() + productId.hashCode());
        var price = 50 + random.nextDouble() * 150;
        return new PriceQuote(source, Math.round(price * 100.0) / 100.0, "USD");
    }

    // ============================================================
    // Ćwiczenie 2: Propagacja kontekstu zakresu żądania
    // ============================================================

    static final ScopedValue<RequestContext> REQUEST_CTX = ScopedValue.newInstance();

    /**
     * Propaguj {@link RequestContext} przez warstwową architekturę za pomocą {@link ScopedValue}.
     *
     * <p>Zaimplementuj metodę, która:</p>
     * <ol>
     *   <li>Wiąże podany {@code RequestContext} ze scoped value {@code REQUEST_CTX}.</li>
     *   <li>Wewnątrz zakresu używa {@code StructuredTaskScope} do współbieżnego:
     *       <ul>
     *         <li>Pobrania informacji o produkcie przez {@link #fetchProduct(String)}</li>
     *         <li>Pobrania podsumowania recenzji przez {@link #fetchReviews(String)}</li>
     *       </ul>
     *   </li>
     *   <li>Łączy wyniki w {@link ProductPage}.</li>
     * </ol>
     *
     * <p><b>Wskazówki:</b> Użyj {@code ScopedValue.where(REQUEST_CTX, ctx).call(() -> ...)},
     * {@code StructuredTaskScope.open(Joiner.allSuccessfulOrThrow())} oraz
     * {@code scope.fork()}.</p>
     */
    static ProductPage loadProductPage(RequestContext ctx, String productId) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Symuluje pobieranie informacji o produkcie. Odczytuje RequestContext z zakresu. NIE modyfikuj.
     */
    static ProductInfo fetchProduct(String productId) {
        var ctx = REQUEST_CTX.get();
        return new ProductInfo(productId, "Product-" + productId + " [" + ctx.locale() + "]", 99.99);
    }

    /**
     * Symuluje pobieranie recenzji. Odczytuje RequestContext z zakresu. NIE modyfikuj.
     */
    static ReviewSummary fetchReviews(String productId) {
        var ctx = REQUEST_CTX.get();
        return new ReviewSummary(productId, 4.5, 42);
    }

    // ============================================================
    // Ćwiczenie 3: Potok danych z okienkowaniem
    // ============================================================

    /**
     * Przetwórz strumień mieszanych fragmentów danych z agregacją okienkową.
     *
     * <p>Mając listę {@link DataChunk} i rozmiar okna:</p>
     * <ol>
     *   <li>Użyj {@code Stream.Gatherers.windowFixed(windowSize)} aby podzielić
     *       fragmenty na okna o stałym rozmiarze.</li>
     *   <li>Dla każdego okna wygeneruj łańcuch podsumowania:
     *       <ul>
     *         <li>Policz {@code TextChunk}, zsumuj wartości {@code NumericChunk}
     *             i wypisz kody błędów {@code ErrorChunk}.</li>
     *         <li>Użyj wzorców prymitywnych w switch do klasyfikacji wartości liczbowych
     *             (np. {@code case double d when d > 100 -> "high"}).</li>
     *       </ul>
     *   </li>
     *   <li>Zwróć {@code List<String>} z podsumowaniami okien.</li>
     * </ol>
     *
     * <p><b>Wskazówki:</b> Użyj {@code stream.gather(Gatherers.windowFixed(n))},
     * dopasowania wzorców typów zamkniętych na {@code DataChunk} i wzorców prymitywnych.</p>
     */
    static List<String> processWindowed(List<DataChunk> chunks, int windowSize) {
        throw new UnsupportedOperationException();
    }
}
