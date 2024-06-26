package other.function_test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SameTimeOrder {

    private static final String CONTENT_TYPE = "application/json";
    private static final String URL = "http://0.0.0.0:8080/inventory"; // Replace with your actual URL

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws InterruptedException {
        // Create test data
        sendInsertRequest(URL, "{\"type\":\"insert\", \"skuList\":{\"testID\":1}}");

        System.out.println("Wait for 5 secs");
        TimeUnit.SECONDS.sleep(5L);

        // Check for overselling
        sendConcurrentRequests(URL, "{\"type\":\"order\", \"skuList\":{\"testID\":1}}", CONTENT_TYPE);

        // Delete test data
        sendDeleteRequest(URL, "{\"type\":\"delete\", \"skuList\":{\"testID\":0}}");
    }

    public static void sendInsertRequest(String url, String requestBody) throws InterruptedException {
        sendRequest(url, requestBody, CONTENT_TYPE, "Insert");
    }

    public static void sendDeleteRequest(String url, String requestBody) {
        sendRequest(url, requestBody, CONTENT_TYPE, "Delete");
    }

    public static void sendConcurrentRequests(String url, String requestBody, String contentType) {
        // Create two CompletableFutures to send requests concurrently
        CompletableFuture<Void> request1 = sendRequest(url, requestBody, contentType, "Order 1");
        CompletableFuture<Void> request2 = sendRequest(url, requestBody, contentType, "Order 2");

        // Combine the two CompletableFutures to wait for both to complete
        CompletableFuture.allOf(request1, request2).join();

        System.out.println("Both order requests completed.");
    }

    private static CompletableFuture<Void> sendRequest(String url, String requestBody, String contentType, String requestId) {
        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", contentType)
                .POST(BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        // Send the request asynchronously
        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Request " + requestId + " - Status Code: " + response.statusCode());
                    System.out.println("Request " + requestId + " - Response Body: " + response.body());
                    return response;
                })
                .exceptionally(e -> {
                    System.err.println("Request " + requestId + " failed: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                })
                .thenAccept(response -> {
                    if (response != null) {
                        // Additional processing if needed
                    }
                });
    }
}