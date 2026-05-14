package com.dataforge.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OllamaInsightClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaInsightClient.class);

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaInsightClient(OllamaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .build();
    }

    public AiInsightContent generate(String prompt) {
        try {
            return parseInsightContent(sendRawResponse(prompt));
        } catch (JsonProcessingException exception) {
            String message = "Ollama returned invalid insight JSON: " + exception.getOriginalMessage();
            LOGGER.warn(message);
            throw new AiInsightGenerationException(message, exception);
        }
    }

    public String generateText(String prompt) {
        return sendRawResponse(prompt).trim();
    }

    public String modelName() {
        return properties.model();
    }

    private String sendRawResponse(String prompt) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", properties.model(),
                    "prompt", prompt,
                    "stream", false
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint() + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            String responseText = response.body();
            if (responseText == null || responseText.isBlank()) {
                throw new AiInsightGenerationException("Ollama returned an empty response");
            }

            return parseGenerateResponse(responseText).response();
        } catch (JsonProcessingException exception) {
            String message = "Ollama returned an unreadable response: " + exception.getOriginalMessage();
            LOGGER.warn(message);
            throw new AiInsightGenerationException(message, exception);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            String message = "Ollama request failed for model "
                    + properties.model()
                    + " at "
                    + properties.endpoint()
                    + ": "
                    + exception.getMessage();
            LOGGER.warn(message);
            throw new AiInsightGenerationException(message, exception);
        }
    }

    private AiInsightContent parseInsightContent(String responseText) throws JsonProcessingException {
        String trimmed = responseText.trim();
        try {
            return objectMapper.readValue(trimmed, AiInsightContent.class);
        } catch (JsonProcessingException exception) {
            String extractedJson = extractJsonObject(trimmed);
            if (extractedJson == null || extractedJson.equals(trimmed)) {
                throw exception;
            }
            return objectMapper.readValue(extractedJson, AiInsightContent.class);
        }
    }

    private String extractJsonObject(String value) {
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return value.substring(start, end + 1);
    }

    private OllamaGenerateResponse parseGenerateResponse(String responseBody) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, OllamaGenerateResponse.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaGenerateResponse(String response) {
    }
}
