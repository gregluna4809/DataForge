package com.dataforge.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatasetChatRequest(
        @NotBlank @Size(max = 1000) String message
) {
}
