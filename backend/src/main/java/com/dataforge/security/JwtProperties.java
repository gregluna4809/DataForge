package com.dataforge.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dataforge.security.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret is required")
        String secret,

        @Min(value = 1, message = "JWT expiration must be at least 1 minute")
        long expirationMinutes
) {
}
