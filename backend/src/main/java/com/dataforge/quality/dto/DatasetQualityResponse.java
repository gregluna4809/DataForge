package com.dataforge.quality.dto;

import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.quality.QualityIssueSummary;
import java.time.Instant;
import java.util.List;

public record DatasetQualityResponse(
        DatasetResponse dataset,
        double overallScore,
        List<QualityIssueSummary> issueSummaries,
        Instant scoredAt,
        List<ColumnQualityResponse> columns
) {
}
