package com.dataforge.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaInsightClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaInsightClient.class);

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OllamaInsightClient(OllamaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.endpoint())
                .requestFactory(ClientHttpRequestFactories.get(ClientHttpRequestFactorySettings.DEFAULTS
                        .withConnectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
                        .withReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()))))
                .build();
    }

    public AiInsightContent generate(String prompt) {
        try {
            OllamaGenerateResponse response = restClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", properties.model(),
                            "prompt", prompt,
                            "stream", false,
                            "format", "json"
                    ))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (response == null || response.response() == null || response.response().isBlank()) {
                throw new AiInsightGenerationException("Ollama returned an empty response");
            }

            return parseInsightContent(response.response());
        } catch (RestClientException exception) {
            String message = "Ollama request failed for model "
                    + properties.model()
                    + " at "
                    + properties.endpoint()
                    + ": "
                    + exception.getMessage();
            LOGGER.warn(message);
            throw new AiInsightGenerationException(message, exception);
        } catch (JsonProcessingException exception) {
            String message = "Ollama returned invalid insight JSON: " + exception.getOriginalMessage();
            LOGGER.warn(message);
            throw new AiInsightGenerationException(message, exception);
        }
    }

    public String modelName() {
        return properties.model();
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

    private record OllamaGenerateResponse(String response) {
    }
}
