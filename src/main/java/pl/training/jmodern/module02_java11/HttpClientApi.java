package pl.training.jmodern.module02_java11;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

// ============================================================
// Sekcja 1: Wprowadzenie do HTTP Client API
// ============================================================

/*
## Wprowadzenie do HTTP Client API

- `HttpURLConnection` (od Java 1.1) był standardowym klientem HTTP
  przez dekady, ale miał wiele problemów:
    - **Rozwlekłość**: otwieranie połączenia, ustawianie właściwości, ręczne
      czytanie strumieni, zamykanie zasobów — nawet prosty GET wymagał
      ~15 linii kodu szablonowego.
    - **Brak wsparcia HTTP/2**: tylko HTTP/1.1.
    - **Słaba obsługa błędów**: odpowiedzi błędów wymagały czytania
      oddzielnego `getErrorStream()`.
    - **Brak wsparcia async**: tylko blokujące I/O.
    - **Brak wzorca budowniczego**: mutowalny stan, łatwo o błędną konfigurację.
- `java.net.http` — nowoczesne HTTP Client API:
    - **Inkubowane** w JDK 9 (`jdk.incubator.httpclient`).
    - **Ustandaryzowane** w JDK 11 (moduł `java.net.http`).
    - Zastępuje `HttpURLConnection` jako zalecany klient HTTP.
- **Trzy główne klasy**:
    - `HttpClient` — silnik. Bezpieczny wątkowo, wielokrotnego użytku, zarządza
      pulami połączeń, negocjacją protokołu i konfiguracją.
    - `HttpRequest` — niemutowalne, wielokrotnego użytku żądanie HTTP zbudowane za pomocą
      płynnego budowniczego.
    - `HttpResponse<T>` — sparametryzowana odpowiedź. Parametr typu
      `T` reprezentuje typ ciała (String, byte[], InputStream itp.).
- **Zasady projektowania**:
    - Płynni budowniczowie — czytelna, łańcuchowa konfiguracja.
    - Niemutowalność — żądania i klienci są niemutowalni po zbudowaniu.
    - Bezpieczeństwo typów — typ ciała odpowiedzi jest znany w czasie kompilacji.
    - Wsparcie HTTP/2 — z automatycznym powrotem do HTTP/1.1.
    - Wsparcie async — nieblokujące API oparte na `CompletableFuture`.
*/

// ============================================================
// Sekcja 2: Tworzenie i konfiguracja HttpClient
// ============================================================

/*
## Tworzenie i konfiguracja HttpClient

- `HttpClient.newHttpClient()` — tworzy klienta z domyślnymi
  ustawieniami (preferowany HTTP/2, brak przekierowań, brak timeout,
  domyślny executor).
- `HttpClient.newBuilder()` — płynny budowniczy dla pełnej konfiguracji:
    - `.version(HttpClient.Version.HTTP_2)` — preferowana wersja
      protokołu. HTTP_2 jest domyślna; powraca do HTTP/1.1, jeśli
      serwer go nie wspiera.
    - `.followRedirects(HttpClient.Redirect.NORMAL)` — polityka
      przekierowań: `NEVER` (domyślna), `NORMAL` (nie HTTPS→HTTP),
      `ALWAYS` (podążaj za wszystkimi przekierowaniami).
    - `.connectTimeout(Duration)` — timeout połączenia. Brak domyślnego
      timeout (czeka nieskończenie).
    - `.executor(Executor)` — pula wątków dla operacji async.
      Domyślnie używa wspólnej buforowanej puli wątków.
    - `.authenticator(Authenticator)` — uwierzytelnianie HTTP Basic/Digest.
    - `.proxy(ProxySelector)` — konfiguracja proxy.
    - `.cookieHandler(CookieHandler)` — zarządzanie ciasteczkami.
    - `.sslContext(SSLContext)` / `.sslParameters(SSLParameters)` —
      konfiguracja TLS.
    - `.priority(int)` — priorytet strumienia HTTP/2 (1–256).
- **Bezpieczeństwo wątkowe**: `HttpClient` jest bezpieczny wątkowo i powinien być
  **wielokrotnie używany** w całej aplikacji (wzorzec singleton). Zarządza
  wewnętrzną pulą połączeń.
- **AutoCloseable** (Java 21+): `HttpClient` implementuje
  `AutoCloseable`, więc może być używany z try-with-resources do
  czystego zamknięcia executora i puli połączeń.
*/

// ============================================================
// Sekcja 3: Budowanie żądań HTTP
// ============================================================

/*
## Budowanie żądań HTTP

- `HttpRequest.newBuilder(URI)` — rozpoczyna budowanie żądania dla
  podanego URI. Alternatywnie, `.uri(URI)` może być wywołane na
  budowniczym.
- **Metody HTTP**:
    - `.GET()` — metoda domyślna (może być pominięta).
    - `.POST(BodyPublisher)` — POST z ciałem żądania.
    - `.PUT(BodyPublisher)` — PUT z ciałem żądania.
    - `.DELETE()` — DELETE (bez ciała).
    - `.method(String, BodyPublisher)` — niestandardowa metoda (np. PATCH).
- **Nagłówki**:
    - `.header(name, value)` — dodaje nagłówek (nie zastępuje).
    - `.headers(name1, value1, name2, value2, ...)` — dodaje wiele
      nagłówków naraz (varargs, naprzemiennie nazwa/wartość).
    - `.setHeader(name, value)` — ustawia nagłówek, zastępując wszelkie
      poprzednie wartości dla tej nazwy.
- **Inne ustawienia**:
    - `.timeout(Duration)` — timeout per żądanie. Rzuca
      `HttpTimeoutException` po przekroczeniu.
    - `.version(HttpClient.Version)` — nadpisuje domyślną wersję
      protokołu klienta dla tego żądania.
- **BodyPublishers** — konwertują obiekty Java na ciała żądań:
    - `BodyPublishers.ofString(String)` — ciało jako String (UTF-8).
    - `BodyPublishers.ofString(String, Charset)` — String z zestawem znaków.
    - `BodyPublishers.ofByteArray(byte[])` — surowe bajty.
    - `BodyPublishers.ofFile(Path)` — strumieniowanie z pliku.
    - `BodyPublishers.ofInputStream(Supplier<InputStream>)` — leniwe
      strumieniowanie z InputStream.
    - `BodyPublishers.noBody()` — puste ciało.
- **Niemutowalność**: po zbudowaniu `HttpRequest` jest niemutowalny i może
  być bezpiecznie wielokrotnie używany i współdzielony między wątkami.
*/

// ============================================================
// Sekcja 4: Wysyłanie żądań synchronicznych i obsługa odpowiedzi
// ============================================================

/*
## Wysyłanie żądań synchronicznych i obsługa odpowiedzi

- `client.send(request, BodyHandler)` — wysyła żądanie i blokuje
  do pełnego odebrania odpowiedzi.
- **BodyHandlers** — określają sposób przetwarzania ciała odpowiedzi:
    - `BodyHandlers.ofString()` — ciało jako `String` (UTF-8).
    - `BodyHandlers.ofString(Charset)` — ciało jako `String` z zestawem znaków.
    - `BodyHandlers.ofByteArray()` — ciało jako `byte[]`.
    - `BodyHandlers.ofFile(Path)` — zapis ciała do pliku.
    - `BodyHandlers.ofInputStream()` — ciało jako `InputStream`.
    - `BodyHandlers.ofLines()` — ciało jako `Stream<String>`.
    - `BodyHandlers.discarding()` — odrzucenie ciała (zwraca `Void`).
    - `BodyHandlers.replacing(U)` — odrzucenie ciała, zwrot podanej wartości.
- **HttpResponse<T>** — obiekt odpowiedzi:
    - `statusCode()` — kod statusu HTTP (200, 404, 500 itp.).
    - `body()` — ciało odpowiedzi, typowane przez BodyHandler.
    - `headers()` — nagłówki odpowiedzi jako `HttpHeaders`.
    - `uri()` — końcowy URI (może różnić się od URI żądania po
      przekierowaniach).
    - `version()` — użyta wersja HTTP.
    - `previousResponse()` — `Optional<HttpResponse<T>>` dla
      odpowiedzi przed przekierowaniem.
- **Wyjątki**:
    - `IOException` — błędy sieci.
    - `InterruptedException` — wątek został przerwany podczas oczekiwania.
    - `HttpTimeoutException` (rozszerza `HttpConnectTimeoutException`
      rozszerza `IOException`) — przekroczono timeout żądania.
*/

// ============================================================
// Sekcja 5: Wysyłanie żądań asynchronicznych
// ============================================================

/*
## Wysyłanie żądań asynchronicznych

- `client.sendAsync(request, BodyHandler)` — zwraca natychmiast
  `CompletableFuture<HttpResponse<T>>`.
- Żądanie jest wysyłane w tle przy użyciu executora klienta.
- **Łańcuchowanie z CompletableFuture**:
    - `thenApply(Function)` — transformacja wyniku.
    - `thenAccept(Consumer)` — konsumpcja wyniku (zwraca void).
    - `thenCompose(Function)` — łańcuchowanie kolejnej operacji async.
    - `exceptionally(Function)` — obsługa błędów w łańcuchu.
    - `whenComplete(BiConsumer)` — wykonanie akcji po zakończeniu
      (sukces lub niepowodzenie).
- **Współbieżne żądania**: uruchom wiele wywołań `sendAsync`, a następnie
  użyj `CompletableFuture.allOf(futures)`, aby czekać na wszystkie.
  Jest to znacznie szybsze niż sekwencyjne wywołania synchroniczne.
- **Wątki wirtualne** (Java 21+) zmniejszają potrzebę async w prostych
  przypadkach — można używać synchronicznego `send()` na wątkach wirtualnych bez
  blokowania wątków platformowych. Jednak async pozostaje wartościowe dla
  architektur reaktywnych i gdy potrzebna jest jawna kontrola nad
  współbieżnością.
*/

// ============================================================
// Sekcja 6: Praca z nagłówkami
// ============================================================

/*
## Praca z nagłówkami

- **HttpHeaders** — obiekt nagłówków odpowiedzi:
    - `map()` — zwraca niemodyfikowalną `Map<String, List<String>>`
      ze wszystkimi nagłówkami.
    - `firstValue(name)` — zwraca `Optional<String>` z pierwszą
      wartością dla podanej nazwy nagłówka.
    - `allValues(name)` — zwraca `List<String>` ze wszystkimi wartościami dla
      podanej nazwy nagłówka.
    - `firstValueAsLong(name)` — zwraca `OptionalLong` sparsowany jako
      long (przydatne dla Content-Length, Age itp.).
- **Ustawianie nagłówków żądania**:
    - `.header(name, value)` — **dodaje** wartość (wiele wartości
      dla tego samego nagłówka jest dozwolone w HTTP).
    - `.setHeader(name, value)` — **zastępuje** wszystkie poprzednie wartości
      dla nagłówka.
    - `.headers(n1, v1, n2, v2, ...)` — dodaje wiele nagłówków
      naraz.
- **Typowe nagłówki**:
    - `Content-Type` — typ mediów ciała żądania/odpowiedzi.
    - `Accept` — typy mediów, które klient może obsłużyć.
    - `Authorization` — dane uwierzytelniające (np. `Bearer <token>`).
    - `User-Agent` — identyfikacja klienta.
- **Brak rozróżniania wielkości liter**: nazwy nagłówków HTTP nie rozróżniają
  wielkości liter. API `HttpHeaders` obsługuje to — `firstValue("content-type")`
  i `firstValue("Content-Type")` zwracają ten sam wynik.
*/

// ============================================================
// Sekcja 7: Obsługa JSON (bez zewnętrznych bibliotek)
// ============================================================

/*
## Obsługa JSON (bez zewnętrznych bibliotek)

- JDK **nie zawiera** biblioteki JSON. W produkcji
  używaj Jackson, Gson lub Jakarta JSON-B do właściwej
  serializacji/deserializacji JSON.
- **Wysyłanie JSON**: użyj `BodyPublishers.ofString(jsonString)` z
  nagłówkiem `Content-Type: application/json`.
- **Odbieranie JSON**: ciało odpowiedzi przychodzi jako `String` (przez
  `BodyHandlers.ofString()`). Parsuj je przy użyciu wybranej biblioteki JSON.
- **Wzorzec**: metoda pomocnicza, która prekonfiguruje nagłówek Content-Type
  dla żądań JSON, redukując kod szablonowy.
- **Integracja z Jackson** (nie pokazana — brak zależności):
  ```
  ObjectMapper mapper = new ObjectMapper();
  String json = mapper.writeValueAsString(myObject);
  MyObject result = mapper.readValue(responseBody, MyObject.class);
  ```
*/

// ============================================================
// Sekcja 8: Typowe wzorce i najlepsze praktyki
// ============================================================

/*
## Typowe wzorce i najlepsze praktyki

- **HttpClient jako singleton**: klient jest bezpieczny wątkowo i zarządza
  pulą połączeń. Utwórz jedną instancję i używaj jej wielokrotnie w całej
  aplikacji. Tworzenie nowego klienta na żądanie marnuje zasoby.
- **Strategia timeout**: ustaw `connectTimeout` na kliencie (nawiązywanie
  połączenia) i `timeout` na każdym żądaniu (całkowity czas żądania).
  Zawsze ustawiaj timeout w produkcji — domyślna wartość to nieskończoność.
- **Sprawdzanie kodu statusu**: sprawdź `statusCode()` przed przetwarzaniem
  ciała. Typowe zakresy:
    - `2xx` — sukces.
    - `3xx` — przekierowanie (obsługiwane automatycznie, jeśli skonfigurowane).
    - `4xx` — błąd klienta (złe żądanie, nie znaleziono, brak autoryzacji).
    - `5xx` — błąd serwera.
- **Zalety HTTP/2**: multipleksowanie (wiele żądań przez jedno
  połączenie), kompresja nagłówków (HPACK), ramkowanie binarne (bardziej
  wydajne parsowanie), server push. Klient negocjuje HTTP/2
  automatycznie, gdy serwer go wspiera.
- **AutoCloseable** (Java 21+): `HttpClient` implementuje
  `AutoCloseable`. Użyj try-with-resources do czystego zamknięcia
  executora klienta i puli połączeń.
- **Executor z wątkami wirtualnymi**: skonfiguruj klienta z executorem
  wątków wirtualnych dla skalowalnych operacji async:
  `HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor())`
- **Wzorzec logowania/interceptora**: nie ma wbudowanego API interceptora.
  Owiń klienta w metodę pomocniczą, która loguje szczegóły żądania/odpowiedzi
  przed i po wysłaniu.
*/

public class HttpClientApi {

    // --- Wbudowana infrastruktura serwera testowego ---

    private static String BASE;
    private static HttpClient sharedClient;

    static HttpServer startTestServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        // GET /hello → powitanie JSON
        server.createContext("/hello", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String json = "{\"message\":\"Hello, World!\"}";
                sendResponse(exchange, 200, json, "application/json");
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        // GET /users → tablica JSON użytkowników
        server.createContext("/users", exchange -> {
            String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25},{\"name\":\"Charlie\",\"age\":35}]";
            sendResponse(exchange, 200, json, "application/json");
        });

        // POST/PUT/DELETE /echo → zwraca echo ciała żądania
        server.createContext("/echo", exchange -> {
            String method = exchange.getRequestMethod();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            switch (method) {
                case "POST" -> {
                    exchange.getResponseHeaders().add("Location", "/echo/1");
                    sendResponse(exchange, 201, body, "application/json");
                }
                case "PUT" -> sendResponse(exchange, 200, body, "application/json");
                case "DELETE" -> {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                }
                default -> {
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                }
            }
        });

        // GET /slow → 3-sekundowe opóźnienie
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sendResponse(exchange, 200, "{\"message\":\"slow response\"}", "application/json");
        });

        // GET /redirect → przekierowanie 301 do /hello
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", BASE + "/hello");
            exchange.sendResponseHeaders(301, -1);
            exchange.close();
        });

        // GET /headers → zwraca echo nagłówków żądania jako JSON
        server.createContext("/headers", exchange -> {
            var requestHeaders = exchange.getRequestHeaders();
            var sb = new StringBuilder("{");
            var entries = new ArrayList<>(requestHeaders.entrySet());
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                sb.append("\"").append(entry.getKey().toLowerCase()).append("\":\"")
                        .append(String.join(", ", entry.getValue())).append("\"");
                if (i < entries.size() - 1) sb.append(",");
            }
            sb.append("}");
            sendResponse(exchange, 200, sb.toString(), "application/json");
        });

        // GET /status/CODE → zwraca podany kod statusu
        server.createContext("/status/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            int code = Integer.parseInt(path.substring("/status/".length()));
            if (code >= 200 && code < 300) {
                sendResponse(exchange, code, "{\"status\":" + code + "}", "application/json");
            } else {
                String body = "{\"error\":\"Status " + code + "\"}";
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(code, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        return server;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode,
                                     String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    // --- Pomocnik: budowanie żądania JSON POST/PUT z nagłówkiem Content-Type ---

    static HttpRequest jsonRequest(String url, String method, String jsonBody) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(method, BodyPublishers.ofString(jsonBody))
                .build();
    }

    // --- Pomocnik: sprawdzanie czy kod statusu jest w zakresie sukcesu ---

    static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    // ============================================================
    // Sekcja 1: Wprowadzenie do HTTP Client API
    // ============================================================

    static void introductionToHttpClientApi() throws Exception {
        System.out.println("=== Introduction to the HTTP Client API ===");

        // Stary sposób z HttpURLConnection (zakomentowany dla porównania):
        //
        // URL url = new URL("http://localhost:8080/hello");
        // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setRequestMethod("GET");
        // conn.setRequestProperty("Accept", "application/json");
        // int status = conn.getResponseCode();
        // BufferedReader reader = new BufferedReader(
        //         new InputStreamReader(conn.getInputStream()));
        // StringBuilder response = new StringBuilder();
        // String line;
        // while ((line = reader.readLine()) != null) {
        //     response.append(line);
        // }
        // reader.close();
        // conn.disconnect();
        // System.out.println(status + ": " + response);

        // Nowy sposób — minimalny GET w ~5 liniach:
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/hello")).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println("status: " + response.statusCode());
        System.out.println("body: " + response.body());

        // Trzy główne klasy w akcji:
        // 1. HttpClient   — silnik wysyłający żądanie
        // 2. HttpRequest  — niemutowalny opis tego, co wysłać
        // 3. HttpResponse — sparametryzowany kontener na odpowiedź (tutaj: String)
        System.out.println("client class: " + client.getClass().getSimpleName());
        System.out.println("request URI: " + request.uri());
        System.out.println("request method: " + request.method());
        System.out.println("response type: HttpResponse<String>");
    }

    // ============================================================
    // Sekcja 2: Tworzenie i konfiguracja HttpClient
    // ============================================================

    static void creatingAndConfiguringHttpClient() throws Exception {
        System.out.println("\n=== Creating and Configuring HttpClient ===");

        // Domyślny klient — preferowany HTTP/2, brak przekierowań, brak timeout
        HttpClient defaultClient = HttpClient.newHttpClient();
        System.out.println("default version: " + defaultClient.version());
        System.out.println("default redirect: " + defaultClient.followRedirects());
        System.out.println("default connect timeout: " + defaultClient.connectTimeout());

        // W pełni skonfigurowany klient za pomocą budowniczego
        HttpClient configuredClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
        System.out.println("configured version: " + configuredClient.version());
        System.out.println("configured redirect: " + configuredClient.followRedirects());
        System.out.println("configured connect timeout: " + configuredClient.connectTimeout());

        // Zachowanie przekierowań: NEVER vs ALWAYS
        // Z NEVER (domyślnie) — odpowiedzi z przekierowaniem są zwracane bez zmian
        HttpClient noRedirectClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest redirectRequest = HttpRequest.newBuilder(URI.create(BASE + "/redirect")).build();

        HttpResponse<String> noFollowResponse = noRedirectClient.send(redirectRequest, BodyHandlers.ofString());
        System.out.println("NEVER redirect — status: " + noFollowResponse.statusCode());
        System.out.println("NEVER redirect — Location header: " +
                noFollowResponse.headers().firstValue("Location").orElse("none"));

        // Z ALWAYS — przekierowania są podążane automatycznie
        HttpClient alwaysRedirectClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpResponse<String> followedResponse = alwaysRedirectClient.send(redirectRequest, BodyHandlers.ofString());
        System.out.println("ALWAYS redirect — status: " + followedResponse.statusCode());
        System.out.println("ALWAYS redirect — final URI: " + followedResponse.uri());
        System.out.println("ALWAYS redirect — body: " + followedResponse.body());
        System.out.println("ALWAYS redirect — previous response: " + followedResponse.previousResponse());
    }

    // ============================================================
    // Sekcja 3: Budowanie żądań HTTP
    // ============================================================

    static void buildingHttpRequests() throws Exception {
        System.out.println("\n=== Building HTTP Requests ===");

        // GET z niestandardowymi nagłówkami
        HttpRequest getRequest = HttpRequest.newBuilder(URI.create(BASE + "/headers"))
                .header("Accept", "application/json")
                .header("X-Custom-Header", "custom-value")
                .header("User-Agent", "JModern-HttpClient/1.0")
                .timeout(Duration.ofSeconds(5))
                .GET() // GET jest domyślne, ale tutaj podane jawnie dla jasności
                .build();
        HttpResponse<String> getResponse = sharedClient.send(getRequest, BodyHandlers.ofString());
        System.out.println("GET /headers — status: " + getResponse.statusCode());
        System.out.println("GET /headers — body: " + getResponse.body());

        // Inspekcja zbudowanego obiektu żądania
        System.out.println("request URI: " + getRequest.uri());
        System.out.println("request method: " + getRequest.method());
        System.out.println("request timeout: " + getRequest.timeout());
        System.out.println("request headers: " + getRequest.headers().map());

        // POST z ciałem JSON
        String postJson = "{\"name\":\"Diana\",\"age\":28}";
        HttpRequest postRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(postJson))
                .build();
        HttpResponse<String> postResponse = sharedClient.send(postRequest, BodyHandlers.ofString());
        System.out.println("POST /echo — status: " + postResponse.statusCode());
        System.out.println("POST /echo — body: " + postResponse.body());
        System.out.println("POST /echo — Location: " +
                postResponse.headers().firstValue("Location").orElse("none"));

        // PUT z ciałem JSON
        String putJson = "{\"name\":\"Diana\",\"age\":29}";
        HttpRequest putRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(putJson))
                .build();
        HttpResponse<String> putResponse = sharedClient.send(putRequest, BodyHandlers.ofString());
        System.out.println("PUT /echo — status: " + putResponse.statusCode());
        System.out.println("PUT /echo — body: " + putResponse.body());

        // DELETE — bez ciała
        HttpRequest deleteRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = sharedClient.send(deleteRequest, BodyHandlers.ofString());
        System.out.println("DELETE /echo — status: " + deleteResponse.statusCode());

        // Niestandardowa metoda (PATCH) za pomocą .method()
        HttpRequest patchRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .header("Content-Type", "application/json")
                .method("PATCH", BodyPublishers.ofString("{\"age\":30}"))
                .build();
        System.out.println("PATCH request method: " + patchRequest.method());

        // Przegląd BodyPublishers
        // BodyPublishers.ofString("text")                — ciało String
        // BodyPublishers.ofByteArray(bytes)               — ciało byte[]
        // BodyPublishers.ofFile(Path.of("data.json"))     — strumieniowanie z pliku
        // BodyPublishers.ofInputStream(() -> inputStream)  — leniwy InputStream
        // BodyPublishers.noBody()                         — puste ciało

        // Żądania są niemutowalne — mogą być przechowywane i wielokrotnie używane
        System.out.println("requests are immutable: same object can be sent multiple times");
    }

    // ============================================================
    // Sekcja 4: Wysyłanie żądań synchronicznych i obsługa odpowiedzi
    // ============================================================

    static void sendingSynchronousRequests() throws Exception {
        System.out.println("\n=== Sending Synchronous Requests and Handling Responses ===");

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/users")).build();

        // BodyHandlers.ofString() — ciało jako String
        HttpResponse<String> stringResponse = sharedClient.send(request, BodyHandlers.ofString());
        System.out.println("ofString — status: " + stringResponse.statusCode());
        System.out.println("ofString — body: " + stringResponse.body());

        // BodyHandlers.ofByteArray() — ciało jako byte[]
        HttpResponse<byte[]> byteResponse = sharedClient.send(request, BodyHandlers.ofByteArray());
        System.out.println("ofByteArray — length: " + byteResponse.body().length + " bytes");

        // BodyHandlers.discarding() — całkowite odrzucenie ciała
        HttpResponse<Void> discardedResponse = sharedClient.send(request, BodyHandlers.discarding());
        System.out.println("discarding — status: " + discardedResponse.statusCode());
        System.out.println("discarding — body: " + discardedResponse.body()); // null

        // BodyHandlers.ofLines() — ciało jako Stream<String>
        HttpResponse<Stream<String>> linesResponse = sharedClient.send(request, BodyHandlers.ofLines());
        List<String> lines = linesResponse.body().toList();
        System.out.println("ofLines — lines: " + lines);

        // Szczegóły HttpResponse
        System.out.println("response URI: " + stringResponse.uri());
        System.out.println("response version: " + stringResponse.version());
        System.out.println("response headers (Content-Type): " +
                stringResponse.headers().firstValue("Content-Type").orElse("unknown"));

        // Sprawdzanie kodu statusu
        HttpResponse<String> okResponse = sharedClient.send(
                HttpRequest.newBuilder(URI.create(BASE + "/status/200")).build(),
                BodyHandlers.ofString());
        HttpResponse<String> notFoundResponse = sharedClient.send(
                HttpRequest.newBuilder(URI.create(BASE + "/status/404")).build(),
                BodyHandlers.ofString());
        HttpResponse<String> serverErrorResponse = sharedClient.send(
                HttpRequest.newBuilder(URI.create(BASE + "/status/500")).build(),
                BodyHandlers.ofString());

        System.out.println("200 OK — success: " + isSuccess(okResponse.statusCode()));
        System.out.println("404 Not Found — success: " + isSuccess(notFoundResponse.statusCode()));
        System.out.println("404 Not Found — body: " + notFoundResponse.body());
        System.out.println("500 Server Error — success: " + isSuccess(serverErrorResponse.statusCode()));

        // Wyjątek timeout — timeout żądania 1s z /slow (3s opóźnienie)
        HttpRequest slowRequest = HttpRequest.newBuilder(URI.create(BASE + "/slow"))
                .timeout(Duration.ofSeconds(1))
                .build();
        try {
            sharedClient.send(slowRequest, BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            System.out.println("HttpTimeoutException: " + e.getMessage());
        }
    }

    // ============================================================
    // Sekcja 5: Wysyłanie żądań asynchronicznych
    // ============================================================

    static void sendingAsynchronousRequests() throws Exception {
        System.out.println("\n=== Sending Asynchronous Requests ===");

        // Pojedynczy async GET z łańcuchowaniem
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/hello")).build();

        CompletableFuture<String> future = sharedClient
                .sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body);

        // Żądanie jest wysyłane w tle — tutaj możemy wykonywać inną pracę
        System.out.println("request sent asynchronously...");

        // Blokowanie i pobranie wyniku
        String body = future.join();
        System.out.println("async result: " + body);

        // Łańcuchowanie z thenApply i thenAccept
        sharedClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(String::toUpperCase)
                .thenAccept(result -> System.out.println("chained async result: " + result))
                .join(); // oczekiwanie na zakończenie

        // Obsługa błędów z exceptionally
        HttpRequest badRequest = HttpRequest.newBuilder(URI.create(BASE + "/slow"))
                .timeout(Duration.ofSeconds(1))
                .build();

        String errorResult = sharedClient.sendAsync(badRequest, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> "error: " + ex.getCause().getMessage())
                .join();
        System.out.println("async with error handling: " + errorResult);

        // Współbieżne żądania — 5 żądań async równolegle
        System.out.println("--- concurrent async requests ---");
        List<String> endpoints = List.of("/hello", "/users", "/status/200", "/status/201", "/status/404");

        List<CompletableFuture<String>> futures = endpoints.stream()
                .map(endpoint -> sharedClient
                        .sendAsync(HttpRequest.newBuilder(URI.create(BASE + endpoint)).build(),
                                BodyHandlers.ofString())
                        .thenApply(resp -> endpoint + " → " + resp.statusCode()))
                .toList();

        // Oczekiwanie na zakończenie wszystkich
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        // Zebranie wyników
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        results.forEach(r -> System.out.println("  " + r));
    }

    // ============================================================
    // Sekcja 6: Praca z nagłówkami
    // ============================================================

    static void workingWithHeaders() throws Exception {
        System.out.println("\n=== Working with Headers ===");

        // Wysyłanie żądania z niestandardowymi nagłówkami do endpointu /headers
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/headers"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.example-token")
                .header("X-Request-Id", "req-12345")
                .header("User-Agent", "JModern-HttpClient/1.0")
                .header("Accept-Language", "en-US")
                .header("Accept-Language", "pl-PL") // dodaje drugą wartość
                .build();

        HttpResponse<String> response = sharedClient.send(request, BodyHandlers.ofString());
        System.out.println("echoed headers: " + response.body());

        // Odczyt nagłówków odpowiedzi
        HttpHeaders responseHeaders = response.headers();

        // map() — wszystkie nagłówki jako Map<String, List<String>>
        System.out.println("all response headers:");
        responseHeaders.map().forEach((name, values) ->
                System.out.println("  " + name + ": " + values));

        // firstValue() — pierwsza wartość dla nagłówka
        System.out.println("Content-Type: " +
                responseHeaders.firstValue("Content-Type").orElse("not present"));
        System.out.println("content-type (lowercase): " +
                responseHeaders.firstValue("content-type").orElse("not present"));

        // firstValueAsLong() — parsowanie jako long
        responseHeaders.firstValueAsLong("Content-Length")
                .ifPresent(len -> System.out.println("Content-Length: " + len));

        // allValues() — wszystkie wartości dla nagłówka
        System.out.println("all Content-Type values: " +
                responseHeaders.allValues("Content-Type"));

        // Demonstracja .header() vs .setHeader()
        // .header() DODAJE — wielokrotne wywołania kumulują się
        HttpRequest multiHeader = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .header("X-Tag", "first")
                .header("X-Tag", "second") // dodaje, nie zastępuje
                .build();
        System.out.println("header() adds — X-Tag values: " +
                multiHeader.headers().allValues("X-Tag"));

        // .setHeader() ZASTĘPUJE — ostatnie wywołanie wygrywa
        HttpRequest setHeader = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .header("X-Tag", "first")
                .setHeader("X-Tag", "replaced") // zastępuje poprzednią wartość
                .build();
        System.out.println("setHeader() replaces — X-Tag values: " +
                setHeader.headers().allValues("X-Tag"));

        // .headers() — dodanie wielu nagłówków naraz
        HttpRequest multiHeaders = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .headers("Accept", "application/json",
                        "X-Trace-Id", "trace-001",
                        "X-Span-Id", "span-001")
                .build();
        System.out.println("headers() varargs — all headers: " +
                multiHeaders.headers().map());
    }

    // ============================================================
    // Sekcja 7: Obsługa JSON (bez zewnętrznych bibliotek)
    // ============================================================

    static void handlingJson() throws Exception {
        System.out.println("\n=== Handling JSON (Without External Libraries) ===");

        // POST JSON do /echo
        String postJson = "{\"name\":\"Eve\",\"email\":\"eve@example.com\",\"role\":\"admin\"}";
        HttpRequest postRequest = jsonRequest(BASE + "/echo", "POST", postJson);
        HttpResponse<String> postResponse = sharedClient.send(postRequest, BodyHandlers.ofString());
        System.out.println("POST JSON — status: " + postResponse.statusCode());
        System.out.println("POST JSON — echoed body: " + postResponse.body());
        System.out.println("POST JSON — Content-Type sent: " +
                postRequest.headers().firstValue("Content-Type").orElse("none"));

        // PUT JSON do /echo
        String putJson = "{\"name\":\"Eve\",\"email\":\"eve-updated@example.com\",\"role\":\"superadmin\"}";
        HttpRequest putRequest = jsonRequest(BASE + "/echo", "PUT", putJson);
        HttpResponse<String> putResponse = sharedClient.send(putRequest, BodyHandlers.ofString());
        System.out.println("PUT JSON — status: " + putResponse.statusCode());
        System.out.println("PUT JSON — echoed body: " + putResponse.body());

        // GET JSON i prosta ekstrakcja oparta na ciągu znaków (edukacyjna — nie do produkcji)
        HttpResponse<String> usersResponse = sharedClient.send(
                HttpRequest.newBuilder(URI.create(BASE + "/users")).build(),
                BodyHandlers.ofString());
        String usersJson = usersResponse.body();
        System.out.println("GET /users JSON: " + usersJson);

        // Prosta ekstrakcja: zliczanie wystąpień "name" (tylko edukacyjnie)
        long nameCount = usersJson.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .split("\"name\"").length - 1;
        System.out.println("number of users (naive count): " + nameCount);

        // Metoda pomocnicza jsonRequest redukuje kod szablonowy:
        // Zamiast:
        //   HttpRequest.newBuilder(URI.create(url))
        //       .header("Content-Type", "application/json")
        //       .header("Accept", "application/json")
        //       .POST(BodyPublishers.ofString(json))
        //       .build();
        // Użyj:
        //   jsonRequest(url, "POST", json);

        // W produkcji z Jackson:
        // ObjectMapper mapper = new ObjectMapper();
        // record User(String name, String email, String role) {}
        // User user = mapper.readValue(response.body(), User.class);
        // String json = mapper.writeValueAsString(new User("Eve", "eve@example.com", "admin"));
    }

    // ============================================================
    // Sekcja 8: Typowe wzorce i najlepsze praktyki
    // ============================================================

    static void commonPatternsAndBestPractices() throws Exception {
        System.out.println("\n=== Common Patterns and Best Practices ===");

        // Wzorzec: HttpClient jako singleton (bezpieczny wątkowo, wielokrotnie używa połączeń)
        // W rzeczywistej aplikacji:
        //   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        //       .connectTimeout(Duration.ofSeconds(10))
        //       .followRedirects(HttpClient.Redirect.NORMAL)
        //       .build();
        System.out.println("shared client instance: " + sharedClient);
        System.out.println("shared client is reused across all demos (singleton pattern)");

        // Wzorzec: strategia timeout — poziom klienta + poziom żądania
        HttpClient clientWithTimeout = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))  // timeout nawiązywania połączenia
                .build();
        HttpRequest requestWithTimeout = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .timeout(Duration.ofSeconds(10))         // całkowity timeout żądania
                .build();
        HttpResponse<String> response = clientWithTimeout.send(requestWithTimeout, BodyHandlers.ofString());
        System.out.println("timeout strategy — status: " + response.statusCode());
        System.out.println("client connectTimeout: " + clientWithTimeout.connectTimeout());
        System.out.println("request timeout: " + requestWithTimeout.timeout());

        // Wzorzec: pomocnik kodu statusu do sprawdzania kategorii odpowiedzi
        System.out.println("isSuccess(200): " + isSuccess(200));
        System.out.println("isSuccess(201): " + isSuccess(201));
        System.out.println("isSuccess(301): " + isSuccess(301));
        System.out.println("isSuccess(404): " + isSuccess(404));
        System.out.println("isSuccess(500): " + isSuccess(500));

        // Wzorzec: HTTP/2 — klient negocjuje automatycznie
        HttpClient http2Client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        HttpResponse<String> http2Response = http2Client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                BodyHandlers.ofString());
        System.out.println("requested version: HTTP_2");
        System.out.println("actual version used: " + http2Response.version());
        // Uwaga: nasz serwer testowy wspiera tylko HTTP/1.1, więc klient powraca do niego

        // Wzorzec: AutoCloseable (Java 21+) — try-with-resources
        try (HttpClient autoCloseClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            HttpResponse<String> autoCloseResponse = autoCloseClient.send(
                    HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                    BodyHandlers.ofString());
            System.out.println("try-with-resources client — status: " + autoCloseResponse.statusCode());
        } // executor klienta i pula połączeń są tutaj zamykane

        // Wzorzec: executor z wątkami wirtualnymi dla skalowalnego async
        try (HttpClient virtualClient = HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {
            HttpResponse<String> vResponse = virtualClient.send(
                    HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                    BodyHandlers.ofString());
            System.out.println("virtual threads client — status: " + vResponse.statusCode());
        }

        // Wzorzec: wrapper logujący
        System.out.println("--- logging wrapper demo ---");
        loggedSend(sharedClient,
                HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                BodyHandlers.ofString());
    }

    // --- Pomocnik: wrapper logujący dla żądań ---

    static <T> HttpResponse<T> loggedSend(HttpClient client, HttpRequest request,
                                          BodyHandler<T> handler) throws Exception {
        System.out.println("[LOG] → " + request.method() + " " + request.uri());
        long start = System.nanoTime();
        HttpResponse<T> response = client.send(request, handler);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.println("[LOG] ← " + response.statusCode() + " (" + elapsed + " ms)");
        return response;
    }

    // ============================================================
    // Main — uruchomienie wszystkich sekcji
    // ============================================================

    public static void main(String[] args) throws Exception {
        // Uruchomienie wbudowanego serwera testowego na losowym dostępnym porcie
        HttpServer server = startTestServer();
        int port = server.getAddress().getPort();
        BASE = "http://localhost:" + port;
        System.out.println("test server started on port " + port);
        System.out.println();

        // Utworzenie współdzielonego klienta używanego we wszystkich demonstracjach
        sharedClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        try {
            introductionToHttpClientApi();
            creatingAndConfiguringHttpClient();
            buildingHttpRequests();
            sendingSynchronousRequests();
            sendingAsynchronousRequests();
            workingWithHeaders();
            handlingJson();
            commonPatternsAndBestPractices();
        } finally {
            server.stop(0);
            System.out.println("\ntest server stopped");
        }
    }
}
