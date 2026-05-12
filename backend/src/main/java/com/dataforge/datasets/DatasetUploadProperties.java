package com.dataforge.datasets;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dataforge.uploads")
public record DatasetUploadProperties(
        @NotBlank(message = "Upload directory is required")
        String directory,

        @Min(value = 1, message = "Upload max file size must be positive")
        long maxFileSizeBytes
) {
}
