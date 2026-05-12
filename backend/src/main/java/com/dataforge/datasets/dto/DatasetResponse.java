package com.dataforge.datasets.dto;

import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetStatus;
import java.time.Instant;
import java.util.UUID;

public record DatasetResponse(
        UUID id,
        String name,
        String originalFilename,
        String description,
        Instant uploadTimestamp,
        long rowCount,
        int columnCount,
        long fileSizeBytes,
        DatasetStatus status,
        UploadedByResponse uploadedBy
) {

    public static DatasetResponse from(Dataset dataset) {
        return new DatasetResponse(
                dataset.getId(),
                dataset.getName(),
                dataset.getOriginalFilename(),
                dataset.getDescription(),
                dataset.getUploadTimestamp(),
                dataset.getRowCount(),
                dataset.getColumnCount(),
                dataset.getFileSizeBytes(),
                dataset.getStatus(),
                new UploadedByResponse(
                        dataset.getUploadedBy().getId(),
                        dataset.getUploadedBy().getEmail(),
                        dataset.getUploadedBy().getName()
                )
        );
    }

    public record UploadedByResponse(
            UUID id,
            String email,
            String name
    ) {
    }
}
