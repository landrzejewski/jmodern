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
// Section 1: Introduction to the HTTP Client API
// ============================================================

/*
## Introduction to the HTTP Client API

- `HttpURLConnection` (since Java 1.1) was the standard HTTP client
  for decades but had many problems:
    - **Verbose**: opening a connection, setting properties, reading
      streams manually, closing resources — even a simple GET required
      ~15 lines of boilerplate.
    - **No HTTP/2 support**: only HTTP/1.1.
    - **Poor error handling**: error responses required reading a
      separate `getErrorStream()`.
    - **No async support**: blocking I/O only.
    - **No builder pattern**: mutable state, easy to misconfigure.
- `java.net.http` — the modern HTTP Client API:
    - **Incubated** in JDK 9 (`jdk.incubator.httpclient`).
    - **Standardized** in JDK 11 (`java.net.http` module).
    - Replaces `HttpURLConnection` as the recommended HTTP client.
- **Three core classes**:
    - `HttpClient` — the engine. Thread-safe, reusable, manages
      connection pools, protocol negotiation, and configuration.
    - `HttpRequest` — an immutable, reusable HTTP request built via
      a fluent builder.
    - `HttpResponse<T>` — a parameterized response. The type parameter
      `T` represents the body type (String, byte[], InputStream, etc.).
- **Design principles**:
    - Fluent builders — readable, chainable configuration.
    - Immutability — requests and clients are immutable once built.
    - Type safety — response body type is known at compile time.
    - HTTP/2 support — with automatic fallback to HTTP/1.1.
    - Async support — `CompletableFuture`-based non-blocking API.
*/

// ============================================================
// Section 2: Creating and Configuring HttpClient
// ============================================================

/*
## Creating and Configuring HttpClient

- `HttpClient.newHttpClient()` — creates a client with default
  settings (HTTP/2 preferred, no redirects, no timeout, default
  executor).
- `HttpClient.newBuilder()` — fluent builder for full configuration:
    - `.version(HttpClient.Version.HTTP_2)` — preferred protocol
      version. HTTP_2 is the default; falls back to HTTP/1.1 if
      the server does not support it.
    - `.followRedirects(HttpClient.Redirect.NORMAL)` — redirect
      policy: `NEVER` (default), `NORMAL` (not HTTPS→HTTP),
      `ALWAYS` (follow all redirects).
    - `.connectTimeout(Duration)` — connection timeout. No default
      timeout (waits indefinitely).
    - `.executor(Executor)` — thread pool for async operations.
      By default uses a common cached thread pool.
    - `.authenticator(Authenticator)` — HTTP Basic/Digest auth.
    - `.proxy(ProxySelector)` — proxy configuration.
    - `.cookieHandler(CookieHandler)` — cookie management.
    - `.sslContext(SSLContext)` / `.sslParameters(SSLParameters)` —
      TLS configuration.
    - `.priority(int)` — HTTP/2 stream priority (1–256).
- **Thread safety**: `HttpClient` is thread-safe and should be
  **reused** across the application (singleton pattern). It manages
  an internal connection pool.
- **AutoCloseable** (Java 21+): `HttpClient` implements
  `AutoCloseable`, so it can be used with try-with-resources to
  cleanly shut down its executor and connection pool.
*/

// ============================================================
// Section 3: Building HTTP Requests
// ============================================================

/*
## Building HTTP Requests

- `HttpRequest.newBuilder(URI)` — starts building a request for
  the given URI. Alternatively, `.uri(URI)` can be called on the
  builder.
- **HTTP methods**:
    - `.GET()` — the default method (can be omitted).
    - `.POST(BodyPublisher)` — POST with a request body.
    - `.PUT(BodyPublisher)` — PUT with a request body.
    - `.DELETE()` — DELETE (no body).
    - `.method(String, BodyPublisher)` — custom method (e.g. PATCH).
- **Headers**:
    - `.header(name, value)` — adds a header (does not replace).
    - `.headers(name1, value1, name2, value2, ...)` — adds multiple
      headers at once (varargs, alternating name/value).
    - `.setHeader(name, value)` — sets a header, replacing any
      previous values for that name.
- **Other settings**:
    - `.timeout(Duration)` — per-request timeout. Throws
      `HttpTimeoutException` if exceeded.
    - `.version(HttpClient.Version)` — override the client's default
      protocol version for this request.
- **BodyPublishers** — convert Java objects to request bodies:
    - `BodyPublishers.ofString(String)` — string body (UTF-8).
    - `BodyPublishers.ofString(String, Charset)` — string with charset.
    - `BodyPublishers.ofByteArray(byte[])` — raw bytes.
    - `BodyPublishers.ofFile(Path)` — stream from a file.
    - `BodyPublishers.ofInputStream(Supplier<InputStream>)` — lazy
      stream from an InputStream.
    - `BodyPublishers.noBody()` — empty body.
- **Immutability**: once built, `HttpRequest` is immutable and can
  be safely reused and shared across threads.
*/

// ============================================================
// Section 4: Sending Synchronous Requests and Handling Responses
// ============================================================

/*
## Sending Synchronous Requests and Handling Responses

- `client.send(request, BodyHandler)` — sends the request and blocks
  until the response is fully received.
- **BodyHandlers** — determine how the response body is processed:
    - `BodyHandlers.ofString()` — body as `String` (UTF-8).
    - `BodyHandlers.ofString(Charset)` — body as `String` with charset.
    - `BodyHandlers.ofByteArray()` — body as `byte[]`.
    - `BodyHandlers.ofFile(Path)` — save body to a file.
    - `BodyHandlers.ofInputStream()` — body as `InputStream`.
    - `BodyHandlers.ofLines()` — body as `Stream<String>`.
    - `BodyHandlers.discarding()` — discard body (returns `Void`).
    - `BodyHandlers.replacing(U)` — discard body, return given value.
- **HttpResponse<T>** — the response object:
    - `statusCode()` — HTTP status code (200, 404, 500, etc.).
    - `body()` — the response body, typed by the BodyHandler.
    - `headers()` — response headers as `HttpHeaders`.
    - `uri()` — the final URI (may differ from request URI after
      redirects).
    - `version()` — the HTTP version used.
    - `previousResponse()` — `Optional<HttpResponse<T>>` for the
      response before a redirect.
- **Exceptions**:
    - `IOException` — network errors.
    - `InterruptedException` — thread was interrupted while waiting.
    - `HttpTimeoutException` (extends `HttpConnectTimeoutException`
      extends `IOException`) — request timeout exceeded.
*/

// ============================================================
// Section 5: Sending Asynchronous Requests
// ============================================================

/*
## Sending Asynchronous Requests

- `client.sendAsync(request, BodyHandler)` — returns immediately
  with a `CompletableFuture<HttpResponse<T>>`.
- The request is sent in the background using the client's executor.
- **Chaining with CompletableFuture**:
    - `thenApply(Function)` — transform the result.
    - `thenAccept(Consumer)` — consume the result (returns void).
    - `thenCompose(Function)` — chain another async operation.
    - `exceptionally(Function)` — handle errors in the chain.
    - `whenComplete(BiConsumer)` — run action on completion
      (success or failure).
- **Concurrent requests**: launch multiple `sendAsync` calls, then
  use `CompletableFuture.allOf(futures)` to wait for all to complete.
  This is significantly faster than sequential synchronous calls.
- **Virtual threads** (Java 21+) reduce the need for async in simple
  cases — you can use synchronous `send()` on virtual threads without
  blocking platform threads. However, async remains valuable for
  reactive architectures and when you need explicit control over
  concurrency.
*/

// ============================================================
// Section 6: Working with Headers
// ============================================================

/*
## Working with Headers

- **HttpHeaders** — the response headers object:
    - `map()` — returns an unmodifiable `Map<String, List<String>>`
      of all headers.
    - `firstValue(name)` — returns `Optional<String>` with the first
      value for the given header name.
    - `allValues(name)` — returns `List<String>` of all values for
      the given header name.
    - `firstValueAsLong(name)` — returns `OptionalLong` parsed as
      a long (useful for Content-Length, Age, etc.).
- **Setting request headers**:
    - `.header(name, value)` — **adds** a value (multiple values
      for the same header are allowed in HTTP).
    - `.setHeader(name, value)` — **replaces** all previous values
      for the header.
    - `.headers(n1, v1, n2, v2, ...)` — adds multiple headers at
      once.
- **Common headers**:
    - `Content-Type` — media type of the request/response body.
    - `Accept` — media types the client can handle.
    - `Authorization` — credentials (e.g., `Bearer <token>`).
    - `User-Agent` — client identification.
- **Case insensitivity**: HTTP header names are case-insensitive.
  The `HttpHeaders` API handles this — `firstValue("content-type")`
  and `firstValue("Content-Type")` return the same result.
*/

// ============================================================
// Section 7: Handling JSON (Without External Libraries)
// ============================================================

/*
## Handling JSON (Without External Libraries)

- The JDK does **not** include a JSON library. In production,
  use Jackson, Gson, or Jakarta JSON-B for proper JSON
  serialization/deserialization.
- **Sending JSON**: use `BodyPublishers.ofString(jsonString)` with
  the `Content-Type: application/json` header.
- **Receiving JSON**: the response body comes as a `String` (via
  `BodyHandlers.ofString()`). Parse it with your JSON library of
  choice.
- **Pattern**: a helper method that pre-configures the Content-Type
  header for JSON requests, reducing boilerplate.
- **Jackson integration** (not shown — no dependency):
  ```
  ObjectMapper mapper = new ObjectMapper();
  String json = mapper.writeValueAsString(myObject);
  MyObject result = mapper.readValue(responseBody, MyObject.class);
  ```
*/

// ============================================================
// Section 8: Common Patterns and Best Practices
// ============================================================

/*
## Common Patterns and Best Practices

- **HttpClient as singleton**: the client is thread-safe and manages
  a connection pool. Create one instance and reuse it throughout
  the application. Creating a new client per request wastes resources.
- **Timeout strategy**: set `connectTimeout` on the client (connection
  establishment) and `timeout` on each request (total request time).
  Always set timeouts in production — the default is infinite.
- **Status code checking**: check `statusCode()` before processing
  the body. Common ranges:
    - `2xx` — success.
    - `3xx` — redirect (handled automatically if configured).
    - `4xx` — client error (bad request, not found, unauthorized).
    - `5xx` — server error.
- **HTTP/2 benefits**: multiplexing (multiple requests over one
  connection), header compression (HPACK), binary framing (more
  efficient parsing), server push. The client negotiates HTTP/2
  automatically when the server supports it.
- **AutoCloseable** (Java 21+): `HttpClient` implements
  `AutoCloseable`. Use try-with-resources to cleanly shut down
  the client's executor and connection pool.
- **Virtual threads executor**: configure the client with a virtual
  thread executor for scalable async operations:
  `HttpClient.newBuilder().executor(Executors.newVirtualThreadPerTaskExecutor())`
- **Logging/interceptor pattern**: there is no built-in interceptor
  API. Wrap the client in a helper method that logs request/response
  details before and after sending.
*/

public class HttpClientApi {

    // --- Embedded test server infrastructure ---

    private static String BASE;
    private static HttpClient sharedClient;

    static HttpServer startTestServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        // GET /hello → JSON greeting
        server.createContext("/hello", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String json = "{\"message\":\"Hello, World!\"}";
                sendResponse(exchange, 200, json, "application/json");
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        });

        // GET /users → JSON array of users
        server.createContext("/users", exchange -> {
            String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25},{\"name\":\"Charlie\",\"age\":35}]";
            sendResponse(exchange, 200, json, "application/json");
        });

        // POST/PUT/DELETE /echo → echoes request body
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

        // GET /slow → 3-second delay
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sendResponse(exchange, 200, "{\"message\":\"slow response\"}", "application/json");
        });

        // GET /redirect → 301 redirect to /hello
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", BASE + "/hello");
            exchange.sendResponseHeaders(301, -1);
            exchange.close();
        });

        // GET /headers → echoes request headers as JSON
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

        // GET /status/CODE → returns the given status code
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

    // --- Helper: build a JSON POST/PUT request with Content-Type header ---

    static HttpRequest jsonRequest(String url, String method, String jsonBody) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(method, BodyPublishers.ofString(jsonBody))
                .build();
    }

    // --- Helper: check if status code is in the success range ---

    static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    // ============================================================
    // Section 1: Introduction to the HTTP Client API
    // ============================================================

    static void introductionToHttpClientApi() throws Exception {
        System.out.println("=== Introduction to the HTTP Client API ===");

        // The old way with HttpURLConnection (commented for comparison):
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

        // The new way — minimal GET in ~5 lines:
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/hello")).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println("status: " + response.statusCode());
        System.out.println("body: " + response.body());

        // Three core classes demonstrated:
        // 1. HttpClient   — the engine that sends the request
        // 2. HttpRequest  — immutable description of what to send
        // 3. HttpResponse — parameterized container for the response (here: String)
        System.out.println("client class: " + client.getClass().getSimpleName());
        System.out.println("request URI: " + request.uri());
        System.out.println("request method: " + request.method());
        System.out.println("response type: HttpResponse<String>");
    }

    // ============================================================
    // Section 2: Creating and Configuring HttpClient
    // ============================================================

    static void creatingAndConfiguringHttpClient() throws Exception {
        System.out.println("\n=== Creating and Configuring HttpClient ===");

        // Default client — HTTP/2 preferred, no redirect following, no timeout
        HttpClient defaultClient = HttpClient.newHttpClient();
        System.out.println("default version: " + defaultClient.version());
        System.out.println("default redirect: " + defaultClient.followRedirects());
        System.out.println("default connect timeout: " + defaultClient.connectTimeout());

        // Fully configured client via builder
        HttpClient configuredClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
        System.out.println("configured version: " + configuredClient.version());
        System.out.println("configured redirect: " + configuredClient.followRedirects());
        System.out.println("configured connect timeout: " + configuredClient.connectTimeout());

        // Redirect behavior: NEVER vs ALWAYS
        // With NEVER (default) — redirect responses are returned as-is
        HttpClient noRedirectClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest redirectRequest = HttpRequest.newBuilder(URI.create(BASE + "/redirect")).build();

        HttpResponse<String> noFollowResponse = noRedirectClient.send(redirectRequest, BodyHandlers.ofString());
        System.out.println("NEVER redirect — status: " + noFollowResponse.statusCode());
        System.out.println("NEVER redirect — Location header: " +
                noFollowResponse.headers().firstValue("Location").orElse("none"));

        // With ALWAYS — redirects are followed automatically
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
    // Section 3: Building HTTP Requests
    // ============================================================

    static void buildingHttpRequests() throws Exception {
        System.out.println("\n=== Building HTTP Requests ===");

        // GET with custom headers
        HttpRequest getRequest = HttpRequest.newBuilder(URI.create(BASE + "/headers"))
                .header("Accept", "application/json")
                .header("X-Custom-Header", "custom-value")
                .header("User-Agent", "JModern-HttpClient/1.0")
                .timeout(Duration.ofSeconds(5))
                .GET() // GET is the default, but explicit here for clarity
                .build();
        HttpResponse<String> getResponse = sharedClient.send(getRequest, BodyHandlers.ofString());
        System.out.println("GET /headers — status: " + getResponse.statusCode());
        System.out.println("GET /headers — body: " + getResponse.body());

        // Inspecting the built request object
        System.out.println("request URI: " + getRequest.uri());
        System.out.println("request method: " + getRequest.method());
        System.out.println("request timeout: " + getRequest.timeout());
        System.out.println("request headers: " + getRequest.headers().map());

        // POST with JSON body
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

        // PUT with JSON body
        String putJson = "{\"name\":\"Diana\",\"age\":29}";
        HttpRequest putRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(putJson))
                .build();
        HttpResponse<String> putResponse = sharedClient.send(putRequest, BodyHandlers.ofString());
        System.out.println("PUT /echo — status: " + putResponse.statusCode());
        System.out.println("PUT /echo — body: " + putResponse.body());

        // DELETE — no body
        HttpRequest deleteRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = sharedClient.send(deleteRequest, BodyHandlers.ofString());
        System.out.println("DELETE /echo — status: " + deleteResponse.statusCode());

        // Custom method (PATCH) using .method()
        HttpRequest patchRequest = HttpRequest.newBuilder(URI.create(BASE + "/echo"))
                .header("Content-Type", "application/json")
                .method("PATCH", BodyPublishers.ofString("{\"age\":30}"))
                .build();
        System.out.println("PATCH request method: " + patchRequest.method());

        // BodyPublishers overview
        // BodyPublishers.ofString("text")                — String body
        // BodyPublishers.ofByteArray(bytes)               — byte[] body
        // BodyPublishers.ofFile(Path.of("data.json"))     — stream from file
        // BodyPublishers.ofInputStream(() -> inputStream)  — lazy InputStream
        // BodyPublishers.noBody()                         — empty body

        // Requests are immutable — can be stored and reused
        System.out.println("requests are immutable: same object can be sent multiple times");
    }

    // ============================================================
    // Section 4: Sending Synchronous Requests and Handling Responses
    // ============================================================

    static void sendingSynchronousRequests() throws Exception {
        System.out.println("\n=== Sending Synchronous Requests and Handling Responses ===");

        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/users")).build();

        // BodyHandlers.ofString() — body as String
        HttpResponse<String> stringResponse = sharedClient.send(request, BodyHandlers.ofString());
        System.out.println("ofString — status: " + stringResponse.statusCode());
        System.out.println("ofString — body: " + stringResponse.body());

        // BodyHandlers.ofByteArray() — body as byte[]
        HttpResponse<byte[]> byteResponse = sharedClient.send(request, BodyHandlers.ofByteArray());
        System.out.println("ofByteArray — length: " + byteResponse.body().length + " bytes");

        // BodyHandlers.discarding() — discard the body entirely
        HttpResponse<Void> discardedResponse = sharedClient.send(request, BodyHandlers.discarding());
        System.out.println("discarding — status: " + discardedResponse.statusCode());
        System.out.println("discarding — body: " + discardedResponse.body()); // null

        // BodyHandlers.ofLines() — body as Stream<String>
        HttpResponse<Stream<String>> linesResponse = sharedClient.send(request, BodyHandlers.ofLines());
        List<String> lines = linesResponse.body().toList();
        System.out.println("ofLines — lines: " + lines);

        // HttpResponse details
        System.out.println("response URI: " + stringResponse.uri());
        System.out.println("response version: " + stringResponse.version());
        System.out.println("response headers (Content-Type): " +
                stringResponse.headers().firstValue("Content-Type").orElse("unknown"));

        // Status code checking
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

        // Timeout exception — request timeout of 1s with /slow (3s delay)
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
    // Section 5: Sending Asynchronous Requests
    // ============================================================

    static void sendingAsynchronousRequests() throws Exception {
        System.out.println("\n=== Sending Asynchronous Requests ===");

        // Single async GET with chaining
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/hello")).build();

        CompletableFuture<String> future = sharedClient
                .sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body);

        // The request is sent in the background — we can do other work here
        System.out.println("request sent asynchronously...");

        // Block and get the result
        String body = future.join();
        System.out.println("async result: " + body);

        // Chaining with thenApply and thenAccept
        sharedClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(String::toUpperCase)
                .thenAccept(result -> System.out.println("chained async result: " + result))
                .join(); // wait for completion

        // Error handling with exceptionally
        HttpRequest badRequest = HttpRequest.newBuilder(URI.create(BASE + "/slow"))
                .timeout(Duration.ofSeconds(1))
                .build();

        String errorResult = sharedClient.sendAsync(badRequest, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> "error: " + ex.getCause().getMessage())
                .join();
        System.out.println("async with error handling: " + errorResult);

        // Concurrent requests — 5 async requests in parallel
        System.out.println("--- concurrent async requests ---");
        List<String> endpoints = List.of("/hello", "/users", "/status/200", "/status/201", "/status/404");

        List<CompletableFuture<String>> futures = endpoints.stream()
                .map(endpoint -> sharedClient
                        .sendAsync(HttpRequest.newBuilder(URI.create(BASE + endpoint)).build(),
                                BodyHandlers.ofString())
                        .thenApply(resp -> endpoint + " → " + resp.statusCode()))
                .toList();

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        // Collect results
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        results.forEach(r -> System.out.println("  " + r));
    }

    // ============================================================
    // Section 6: Working with Headers
    // ============================================================

    static void workingWithHeaders() throws Exception {
        System.out.println("\n=== Working with Headers ===");

        // Send request with custom headers to /headers endpoint
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + "/headers"))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.example-token")
                .header("X-Request-Id", "req-12345")
                .header("User-Agent", "JModern-HttpClient/1.0")
                .header("Accept-Language", "en-US")
                .header("Accept-Language", "pl-PL") // adds second value
                .build();

        HttpResponse<String> response = sharedClient.send(request, BodyHandlers.ofString());
        System.out.println("echoed headers: " + response.body());

        // Reading response headers
        HttpHeaders responseHeaders = response.headers();

        // map() — all headers as Map<String, List<String>>
        System.out.println("all response headers:");
        responseHeaders.map().forEach((name, values) ->
                System.out.println("  " + name + ": " + values));

        // firstValue() — first value for a header
        System.out.println("Content-Type: " +
                responseHeaders.firstValue("Content-Type").orElse("not present"));
        System.out.println("content-type (lowercase): " +
                responseHeaders.firstValue("content-type").orElse("not present"));

        // firstValueAsLong() — parse as long
        responseHeaders.firstValueAsLong("Content-Length")
                .ifPresent(len -> System.out.println("Content-Length: " + len));

        // allValues() — all values for a header
        System.out.println("all Content-Type values: " +
                responseHeaders.allValues("Content-Type"));

        // Demonstrating .header() vs .setHeader()
        // .header() ADDS — multiple calls accumulate
        HttpRequest multiHeader = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .header("X-Tag", "first")
                .header("X-Tag", "second") // adds, does not replace
                .build();
        System.out.println("header() adds — X-Tag values: " +
                multiHeader.headers().allValues("X-Tag"));

        // .setHeader() REPLACES — last call wins
        HttpRequest setHeader = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .header("X-Tag", "first")
                .setHeader("X-Tag", "replaced") // replaces previous value
                .build();
        System.out.println("setHeader() replaces — X-Tag values: " +
                setHeader.headers().allValues("X-Tag"));

        // .headers() — add multiple headers at once
        HttpRequest multiHeaders = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .headers("Accept", "application/json",
                        "X-Trace-Id", "trace-001",
                        "X-Span-Id", "span-001")
                .build();
        System.out.println("headers() varargs — all headers: " +
                multiHeaders.headers().map());
    }

    // ============================================================
    // Section 7: Handling JSON (Without External Libraries)
    // ============================================================

    static void handlingJson() throws Exception {
        System.out.println("\n=== Handling JSON (Without External Libraries) ===");

        // POST JSON to /echo
        String postJson = "{\"name\":\"Eve\",\"email\":\"eve@example.com\",\"role\":\"admin\"}";
        HttpRequest postRequest = jsonRequest(BASE + "/echo", "POST", postJson);
        HttpResponse<String> postResponse = sharedClient.send(postRequest, BodyHandlers.ofString());
        System.out.println("POST JSON — status: " + postResponse.statusCode());
        System.out.println("POST JSON — echoed body: " + postResponse.body());
        System.out.println("POST JSON — Content-Type sent: " +
                postRequest.headers().firstValue("Content-Type").orElse("none"));

        // PUT JSON to /echo
        String putJson = "{\"name\":\"Eve\",\"email\":\"eve-updated@example.com\",\"role\":\"superadmin\"}";
        HttpRequest putRequest = jsonRequest(BASE + "/echo", "PUT", putJson);
        HttpResponse<String> putResponse = sharedClient.send(putRequest, BodyHandlers.ofString());
        System.out.println("PUT JSON — status: " + putResponse.statusCode());
        System.out.println("PUT JSON — echoed body: " + putResponse.body());

        // GET JSON and simple string-based extraction (educational — not for production)
        HttpResponse<String> usersResponse = sharedClient.send(
                HttpRequest.newBuilder(URI.create(BASE + "/users")).build(),
                BodyHandlers.ofString());
        String usersJson = usersResponse.body();
        System.out.println("GET /users JSON: " + usersJson);

        // Simple extraction: count occurrences of "name" (educational only)
        long nameCount = usersJson.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())
                .split("\"name\"").length - 1;
        System.out.println("number of users (naive count): " + nameCount);

        // The jsonRequest helper method reduces boilerplate:
        // Instead of:
        //   HttpRequest.newBuilder(URI.create(url))
        //       .header("Content-Type", "application/json")
        //       .header("Accept", "application/json")
        //       .POST(BodyPublishers.ofString(json))
        //       .build();
        // Use:
        //   jsonRequest(url, "POST", json);

        // In production with Jackson:
        // ObjectMapper mapper = new ObjectMapper();
        // record User(String name, String email, String role) {}
        // User user = mapper.readValue(response.body(), User.class);
        // String json = mapper.writeValueAsString(new User("Eve", "eve@example.com", "admin"));
    }

    // ============================================================
    // Section 8: Common Patterns and Best Practices
    // ============================================================

    static void commonPatternsAndBestPractices() throws Exception {
        System.out.println("\n=== Common Patterns and Best Practices ===");

        // Pattern: HttpClient as singleton (thread-safe, reuses connections)
        // In a real application:
        //   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        //       .connectTimeout(Duration.ofSeconds(10))
        //       .followRedirects(HttpClient.Redirect.NORMAL)
        //       .build();
        System.out.println("shared client instance: " + sharedClient);
        System.out.println("shared client is reused across all demos (singleton pattern)");

        // Pattern: timeout strategy — client-level + request-level
        HttpClient clientWithTimeout = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))  // connection establishment timeout
                .build();
        HttpRequest requestWithTimeout = HttpRequest.newBuilder(URI.create(BASE + "/hello"))
                .timeout(Duration.ofSeconds(10))         // total request timeout
                .build();
        HttpResponse<String> response = clientWithTimeout.send(requestWithTimeout, BodyHandlers.ofString());
        System.out.println("timeout strategy — status: " + response.statusCode());
        System.out.println("client connectTimeout: " + clientWithTimeout.connectTimeout());
        System.out.println("request timeout: " + requestWithTimeout.timeout());

        // Pattern: status code helper for checking response categories
        System.out.println("isSuccess(200): " + isSuccess(200));
        System.out.println("isSuccess(201): " + isSuccess(201));
        System.out.println("isSuccess(301): " + isSuccess(301));
        System.out.println("isSuccess(404): " + isSuccess(404));
        System.out.println("isSuccess(500): " + isSuccess(500));

        // Pattern: HTTP/2 — the client negotiates automatically
        HttpClient http2Client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        HttpResponse<String> http2Response = http2Client.send(
                HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                BodyHandlers.ofString());
        System.out.println("requested version: HTTP_2");
        System.out.println("actual version used: " + http2Response.version());
        // Note: our test server only supports HTTP/1.1, so the client falls back

        // Pattern: AutoCloseable (Java 21+) — try-with-resources
        try (HttpClient autoCloseClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            HttpResponse<String> autoCloseResponse = autoCloseClient.send(
                    HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                    BodyHandlers.ofString());
            System.out.println("try-with-resources client — status: " + autoCloseResponse.statusCode());
        } // client's executor and connection pool are shut down here

        // Pattern: virtual threads executor for scalable async
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

        // Pattern: logging wrapper
        System.out.println("--- logging wrapper demo ---");
        loggedSend(sharedClient,
                HttpRequest.newBuilder(URI.create(BASE + "/hello")).build(),
                BodyHandlers.ofString());
    }

    // --- Helper: logging wrapper for requests ---

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
    // Main — run all sections
    // ============================================================

    public static void main(String[] args) throws Exception {
        // Start embedded test server on a random available port
        HttpServer server = startTestServer();
        int port = server.getAddress().getPort();
        BASE = "http://localhost:" + port;
        System.out.println("test server started on port " + port);
        System.out.println();

        // Create shared client used across demos
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
