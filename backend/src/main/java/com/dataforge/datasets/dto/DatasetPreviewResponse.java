package com.dataforge.datasets.dto;

import java.util.List;

public record DatasetPreviewResponse(
        DatasetResponse dataset,
        List<String> columnNames,
        List<List<String>> rows
) {
}
