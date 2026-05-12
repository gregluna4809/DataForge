package com.dataforge.ai;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dataforge.ai.ollama")
public record OllamaProperties(
        @NotBlank(message = "Ollama endpoint is required")
        String endpoint,

        @NotBlank(message = "Ollama model is required")
        String model,

        @Min(value = 1, message = "Ollama timeout must be positive")
        int timeoutSeconds
) {
}
