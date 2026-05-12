package com.dataforge.quality;

import java.util.List;

public record ColumnQualityResult(
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
