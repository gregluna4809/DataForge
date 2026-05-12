package com.dataforge.quality.dto;

import com.dataforge.quality.QualityIssueSummary;
import java.util.List;

public record ColumnQualityResponse(
        String columnName,
        int columnPosition,
        double qualityScore,
        double nullPercentage,
        double uniquenessPercentage,
        double emptyPercentage,
        double typeConsistencyScore,
        List<QualityIssueSummary> issueSummaries
) {
}
