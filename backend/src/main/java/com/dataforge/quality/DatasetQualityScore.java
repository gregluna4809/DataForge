package com.dataforge.quality;

import com.dataforge.datasets.Dataset;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dataset_quality_scores")
public class DatasetQualityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false, unique = true)
    private Dataset dataset;

    @Column(name = "overall_score", nullable = false)
    private double overallScore;

    @Column(name = "issue_summaries_json", nullable = false, columnDefinition = "TEXT")
    private String issueSummariesJson;

    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt;

    @OneToMany(mappedBy = "datasetQualityScore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatasetColumnQualityScore> columnScores = new ArrayList<>();

    protected DatasetQualityScore() {
    }

    public DatasetQualityScore(
            Dataset dataset,
            double overallScore,
            String issueSummariesJson,
            Instant scoredAt
    ) {
        this.dataset = dataset;
        this.overallScore = overallScore;
        this.issueSummariesJson = issueSummariesJson;
        this.scoredAt = scoredAt;
    }

    public UUID getId() {
        return id;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public String getIssueSummariesJson() {
        return issueSummariesJson;
    }

    public Instant getScoredAt() {
        return scoredAt;
    }

    public List<DatasetColumnQualityScore> getColumnScores() {
        return columnScores;
    }

    public void addColumnScore(DatasetColumnQualityScore columnScore) {
        columnScores.add(columnScore);
    }
}
