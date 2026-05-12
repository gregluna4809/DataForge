package com.dataforge.quality;

public record QualityIssueSummary(
        QualityIssueType type,
        String message
) {
}
