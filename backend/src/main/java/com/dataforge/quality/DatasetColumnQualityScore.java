package com.dataforge.quality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(
        name = "dataset_column_quality_scores",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dataset_column_quality_scores_score_position",
                columnNames = {"dataset_quality_score_id", "column_position"}
        )
)
public class DatasetColumnQualityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_quality_score_id", nullable = false)
    private DatasetQualityScore datasetQualityScore;

    @Column(name = "column_name", nullable = false, columnDefinition = "TEXT")
    private String columnName;

    @Column(name = "column_position", nullable = false)
    private int columnPosition;

    @Column(name = "quality_score", nullable = false)
    private double qualityScore;

    @Column(name = "null_percentage", nullable = false)
    private double nullPercentage;

    @Column(name = "uniqueness_percentage", nullable = false)
    private double uniquenessPercentage;

    @Column(name = "empty_percentage", nullable = false)
    private double emptyPercentage;

    @Column(name = "type_consistency_score", nullable = false)
    private double typeConsistencyScore;

    @Column(name = "issue_summaries_json", nullable = false, columnDefinition = "TEXT")
    private String issueSummariesJson;

    protected DatasetColumnQualityScore() {
    }

    public DatasetColumnQualityScore(
            DatasetQualityScore datasetQualityScore,
            ColumnQualityResult result,
            String issueSummariesJson
    ) {
        this.datasetQualityScore = datasetQualityScore;
        this.columnName = result.columnName();
        this.columnPosition = result.columnPosition();
        this.qualityScore = result.qualityScore();
        this.nullPercentage = result.nullPercentage();
        this.uniquenessPercentage = result.uniquenessPercentage();
        this.emptyPercentage = result.emptyPercentage();
        this.typeConsistencyScore = result.typeConsistencyScore();
        this.issueSummariesJson = issueSummariesJson;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnPosition() {
        return columnPosition;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public double getNullPercentage() {
        return nullPercentage;
    }

    public double getUniquenessPercentage() {
        return uniquenessPercentage;
    }

    public double getEmptyPercentage() {
        return emptyPercentage;
    }

    public double getTypeConsistencyScore() {
        return typeConsistencyScore;
    }

    public String getIssueSummariesJson() {
        return issueSummariesJson;
    }
}
