package com.dataforge.datasets.dto;

import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetStatus;
import java.time.Instant;
import java.util.UUID;

public record DatasetUploadResponse(
        UUID datasetId,
        String originalFilename,
        String storedFilename,
        long fileSizeBytes,
        String contentType,
        DatasetStatus status,
        Instant uploadedAt
) {

    public static DatasetUploadResponse from(Dataset dataset) {
        return new DatasetUploadResponse(
                dataset.getId(),
                dataset.getOriginalFilename(),
                dataset.getStoredFilename(),
                dataset.getFileSizeBytes(),
                dataset.getUploadedFileContentType(),
                dataset.getStatus(),
                dataset.getFileUploadedAt()
        );
    }
}
