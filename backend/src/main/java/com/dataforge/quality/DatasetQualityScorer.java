package com.dataforge.quality;

import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.InferredDataType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DatasetQualityScorer {

    private static final double HIGH_NULL_RATE_THRESHOLD = 25.0;
    private static final double POSSIBLE_IDENTIFIER_THRESHOLD = 95.0;
    private static final double LOW_UNIQUENESS_THRESHOLD = 10.0;

    public DatasetQualityResult score(List<DatasetColumnProfile> profiles) {
        List<ColumnQualityResult> columnResults = profiles.stream()
                .map(this::scoreColumn)
                .toList();

        double overallScore = columnResults.isEmpty()
                ? 0.0
                : columnResults.stream()
                .mapToDouble(ColumnQualityResult::qualityScore)
                .average()
                .orElse(0.0);

        return new DatasetQualityResult(
                round(overallScore),
                datasetIssues(columnResults),
                columnResults
        );
    }

    private ColumnQualityResult scoreColumn(DatasetColumnProfile profile) {
        long totalValues = profile.getNullCount() + profile.getNonNullCount();
        double nullPercentage = percentage(profile.getNullCount(), totalValues);
        double emptyPercentage = nullPercentage;
        double uniquenessPercentage = percentage(profile.getUniqueCount(), profile.getNonNullCount());
        double typeConsistencyScore = typeConsistencyScore(profile.getInferredDataType());

        double completenessScore = 100.0 - nullPercentage;
        double uniquenessScore = uniquenessScore(profile, uniquenessPercentage);
        double qualityScore = round(
                (completenessScore * 0.45)
                        + (uniquenessScore * 0.30)
                        + (typeConsistencyScore * 0.15)
                        + ((100.0 - emptyPercentage) * 0.10)
        );

        return new ColumnQualityResult(
                profile.getColumnName(),
                profile.getColumnPosition(),
                qualityScore,
                round(nullPercentage),
                round(uniquenessPercentage),
                round(emptyPercentage),
                round(typeConsistencyScore),
                columnIssues(profile, nullPercentage, uniquenessPercentage)
        );
    }

    private double uniquenessScore(DatasetColumnProfile profile, double uniquenessPercentage) {
        if (profile.getNonNullCount() == 0) {
            return 0.0;
        }

        if (uniquenessPercentage >= POSSIBLE_IDENTIFIER_THRESHOLD) {
            return 95.0;
        }

        return uniquenessPercentage;
    }

    private double typeConsistencyScore(InferredDataType inferredDataType) {
        return switch (inferredDataType) {
            case BOOLEAN, INTEGER, DECIMAL, DATE, DATETIME -> 100.0;
            case TEXT -> 80.0;
            case UNKNOWN -> 0.0;
        };
    }

    private List<QualityIssueSummary> columnIssues(
            DatasetColumnProfile profile,
            double nullPercentage,
            double uniquenessPercentage
    ) {
        List<QualityIssueSummary> issues = new ArrayList<>();

        if (profile.getNonNullCount() == 0) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.EMPTY_COLUMN,
                    "Column contains no non-empty preview values"
            ));
        } else if (nullPercentage >= HIGH_NULL_RATE_THRESHOLD) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.HIGH_NULL_RATE,
                    "Column has a high null or empty value rate"
            ));
        }

        if (profile.getNonNullCount() > 0 && uniquenessPercentage >= POSSIBLE_IDENTIFIER_THRESHOLD) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.POSSIBLE_IDENTIFIER_COLUMN,
                    "Column values are nearly all unique and may represent an identifier"
            ));
        }

        if (profile.getNonNullCount() > 1 && uniquenessPercentage <= LOW_UNIQUENESS_THRESHOLD) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.LOW_UNIQUENESS,
                    "Column has low uniqueness across preview values"
            ));
        }

        if (profile.getInferredDataType() == InferredDataType.TEXT) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.INFERRED_TEXT_TYPE,
                    "Column values were inferred as free-form text"
            ));
        }

        if (profile.getInferredDataType() == InferredDataType.UNKNOWN) {
            issues.add(new QualityIssueSummary(
                    QualityIssueType.UNKNOWN_TYPE,
                    "Column type could not be inferred from preview values"
            ));
        }

        return issues;
    }

    private List<QualityIssueSummary> datasetIssues(List<ColumnQualityResult> columns) {
        List<QualityIssueSummary> issues = new ArrayList<>();
        for (ColumnQualityResult column : columns) {
            issues.addAll(column.issueSummaries());
        }
        return issues.stream()
                .distinct()
                .toList();
    }

    private double percentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }

        return ((double) numerator / (double) denominator) * 100.0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
