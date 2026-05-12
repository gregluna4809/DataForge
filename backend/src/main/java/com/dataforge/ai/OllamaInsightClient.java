package com.dataforge.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OllamaInsightClient {

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

            return objectMapper.readValue(response.response(), AiInsightContent.class);
        } catch (RestClientException exception) {
            throw new AiInsightGenerationException("Ollama is unavailable", exception);
        } catch (JsonProcessingException exception) {
            throw new AiInsightGenerationException("Ollama returned invalid insight JSON", exception);
        }
    }

    public String modelName() {
        return properties.model();
    }

    private record OllamaGenerateResponse(String response) {
    }
}
