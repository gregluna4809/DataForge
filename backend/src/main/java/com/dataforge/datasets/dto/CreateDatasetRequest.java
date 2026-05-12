package com.dataforge.datasets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateDatasetRequest(
        @NotBlank(message = "Dataset name is required")
        @Size(max = 150, message = "Dataset name must be 150 characters or fewer")
        String name,

        @NotBlank(message = "Original filename is required")
        @Size(max = 255, message = "Original filename must be 255 characters or fewer")
        String originalFilename,

        @Size(max = 1000, message = "Description must be 1000 characters or fewer")
        String description,

        @NotNull(message = "Row count is required")
        @PositiveOrZero(message = "Row count must be zero or greater")
        Long rowCount,

        @NotNull(message = "Column count is required")
        @PositiveOrZero(message = "Column count must be zero or greater")
        Integer columnCount,

        @NotNull(message = "File size is required")
        @PositiveOrZero(message = "File size must be zero or greater")
        Long fileSizeBytes
) {
}
