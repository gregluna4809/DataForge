package com.dataforge.quality;

import static org.assertj.core.api.Assertions.assertThat;

import com.dataforge.profiling.ColumnProfileResult;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.InferredDataType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatasetQualityScorerTests {

    private final DatasetQualityScorer scorer = new DatasetQualityScorer();

    @Test
    void scoresColumnsAndCreatesIssueSummariesFromProfiles() {
        DatasetQualityResult result = scorer.score(List.of(
                profile("customer_id", 0, 0, 50, 50, InferredDataType.INTEGER),
                profile("email", 1, 20, 30, 30, InferredDataType.TEXT),
                profile("status", 2, 0, 50, 2, InferredDataType.TEXT),
                profile("notes", 3, 50, 0, 0, InferredDataType.UNKNOWN)
        ));

        assertThat(result.columns()).hasSize(4);
        assertThat(result.overallScore()).isBetween(0.0, 100.0);

        ColumnQualityResult identifier = result.columns().get(0);
        assertThat(identifier.qualityScore()).isEqualTo(98.5);
        assertThat(identifier.issueSummaries())
                .extracting(QualityIssueSummary::type)
                .contains(QualityIssueType.POSSIBLE_IDENTIFIER_COLUMN);

        ColumnQualityResult highNullText = result.columns().get(1);
        assertThat(highNullText.nullPercentage()).isEqualTo(40.0);
        assertThat(highNullText.issueSummaries())
                .extracting(QualityIssueSummary::type)
                .contains(QualityIssueType.HIGH_NULL_RATE, QualityIssueType.INFERRED_TEXT_TYPE);

        ColumnQualityResult lowUniqueness = result.columns().get(2);
        assertThat(lowUniqueness.uniquenessPercentage()).isEqualTo(4.0);
        assertThat(lowUniqueness.issueSummaries())
                .extracting(QualityIssueSummary::type)
                .contains(QualityIssueType.LOW_UNIQUENESS);

        ColumnQualityResult empty = result.columns().get(3);
        assertThat(empty.qualityScore()).isZero();
        assertThat(empty.issueSummaries())
                .extracting(QualityIssueSummary::type)
                .contains(QualityIssueType.EMPTY_COLUMN, QualityIssueType.UNKNOWN_TYPE);
    }

    @Test
    void emptyProfileSetProducesZeroDatasetScore() {
        DatasetQualityResult result = scorer.score(List.of());

        assertThat(result.overallScore()).isZero();
        assertThat(result.columns()).isEmpty();
        assertThat(result.issueSummaries()).isEmpty();
    }

    private DatasetColumnProfile profile(
            String columnName,
            int position,
            long nullCount,
            long nonNullCount,
            long uniqueCount,
            InferredDataType inferredDataType
    ) {
        return new DatasetColumnProfile(
                null,
                new ColumnProfileResult(
                        columnName,
                        position,
                        nullCount,
                        nonNullCount,
                        uniqueCount,
                        inferredDataType,
                        List.of()
                ),
                "[]",
                Instant.parse("2026-05-11T12:45:00Z")
        );
    }
}
