package com.dataforge.auth.dto;

import com.dataforge.users.User;
import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String tokenType,
        String accessToken,
        UserSummary user
) {

    public static AuthResponse from(User user, String accessToken) {
        return new AuthResponse(
                "Bearer",
                accessToken,
                new UserSummary(user.getId(), user.getEmail(), user.getName(), user.getCreatedAt())
        );
    }

    public record UserSummary(
            UUID id,
            String email,
            String name,
            Instant createdAt
    ) {
    }
}
