package com.dataforge.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DatasetChatRequest(
        @NotBlank @Size(max = 1000) String message,
        @Valid @Size(max = 10) List<@Valid ChatHistoryMessage> history
) {
}
