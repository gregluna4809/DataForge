package com.dataforge.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OllamaInsightClientTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsToGenerateEndpointAndParsesOllamaResponseField() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        String insightJson = objectMapper.writeValueAsString(Map.of(
                "datasetDescription", "Customer import data with profile and quality context.",
                "potentialIssues", List.of("Possible identifier column"),
                "suggestedAnalyses", List.of("Analyze customer counts by status"),
                "suggestedVisualizations", List.of("Bar chart by status")
        ));
        byte[] responseBody = objectMapper.writeValueAsBytes(Map.of(
                "model", "llama3:latest",
                "created_at", "2026-05-12T12:00:00Z",
                "response", insightJson,
                "done", true
        ));

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            assertThat(exchange.getRequestHeaders().getFirst("Accept")).contains("application/json");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();
        });
        server.start();

        OllamaInsightClient client = new OllamaInsightClient(
                new OllamaProperties("http://localhost:" + server.getAddress().getPort(), "llama3:latest", 5),
                objectMapper
        );

        AiInsightContent content = client.generate("structured prompt");

        assertThat(content.datasetDescription()).isEqualTo("Customer import data with profile and quality context.");
        assertThat(content.potentialIssues()).containsExactly("Possible identifier column");
        assertThat(content.suggestedAnalyses()).containsExactly("Analyze customer counts by status");
        assertThat(content.suggestedVisualizations()).containsExactly("Bar chart by status");

        JsonNode requestJson = objectMapper.readTree(requestBody.get());
        assertThat(requestJson.get("model").asText()).isEqualTo("llama3:latest");
        assertThat(requestJson.get("prompt").asText()).isEqualTo("structured prompt");
        assertThat(requestJson.get("stream").asBoolean()).isFalse();
    }

    @Test
    void parsesOllamaJsonWhenResponseContentTypeIsOctetStream() throws IOException {
        String insightJson = objectMapper.writeValueAsString(Map.of(
                "datasetDescription", "Octet stream response was parsed.",
                "potentialIssues", List.of("Large generated payload"),
                "suggestedAnalyses", List.of("Inspect AI insight persistence"),
                "suggestedVisualizations", List.of("Insight summary panel")
        ));
        byte[] responseBody = objectMapper.writeValueAsBytes(Map.of(
                "model", "llama3:latest",
                "response", insightJson,
                "done", true
        ));

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            exchange.getRequestBody().readAllBytes();
            assertThat(exchange.getRequestHeaders().getFirst("Accept")).contains("application/json");
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();
        });
        server.start();

        OllamaInsightClient client = new OllamaInsightClient(
                new OllamaProperties("http://localhost:" + server.getAddress().getPort(), "llama3:latest", 5),
                objectMapper
        );

        AiInsightContent content = client.generate("structured prompt");

        assertThat(content.datasetDescription()).isEqualTo("Octet stream response was parsed.");
        assertThat(content.potentialIssues()).containsExactly("Large generated payload");
    }

    @Test
    void parsesJsonObjectWhenOllamaAddsTextAroundResponse() throws IOException {
        String insightJson = objectMapper.writeValueAsString(Map.of(
                "datasetDescription", "Profile summary.",
                "potentialIssues", List.of(),
                "suggestedAnalyses", List.of("Review null-heavy columns"),
                "suggestedVisualizations", List.of("Quality issue table")
        ));
        byte[] responseBody = objectMapper.writeValueAsBytes(Map.of(
                "response", "Here is the JSON:\n" + insightJson,
                "done", true
        ));

        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();
        });
        server.start();

        OllamaInsightClient client = new OllamaInsightClient(
                new OllamaProperties("http://localhost:" + server.getAddress().getPort(), "llama3:latest", 5),
                objectMapper
        );

        AiInsightContent content = client.generate("structured prompt");

        assertThat(content.datasetDescription()).isEqualTo("Profile summary.");
        assertThat(content.suggestedAnalyses()).containsExactly("Review null-heavy columns");
    }
}
