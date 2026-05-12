package com.dataforge.quality;

import java.util.List;

public record DatasetQualityResult(
        double overallScore,
        List<QualityIssueSummary> issueSummaries,
        List<ColumnQualityResult> columns
) {
}
