package com.dataforge.ai;

import com.dataforge.datasets.Dataset;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dataset_ai_insights")
public class DatasetAiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dataset_id", nullable = false, unique = true)
    private Dataset dataset;

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 50)
    private AiInsightGenerationStatus generationStatus;

    @Column(name = "model_name", nullable = false, length = 120)
    private String modelName;

    @Column(name = "dataset_description", nullable = false, columnDefinition = "TEXT")
    private String datasetDescription;

    @Column(name = "potential_issues_json", nullable = false, columnDefinition = "TEXT")
    private String potentialIssuesJson;

    @Column(name = "suggested_analyses_json", nullable = false, columnDefinition = "TEXT")
    private String suggestedAnalysesJson;

    @Column(name = "suggested_visualizations_json", nullable = false, columnDefinition = "TEXT")
    private String suggestedVisualizationsJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected DatasetAiInsight() {
    }

    public DatasetAiInsight(
            Dataset dataset,
            AiInsightGenerationStatus generationStatus,
            String modelName,
            String datasetDescription,
            String potentialIssuesJson,
            String suggestedAnalysesJson,
            String suggestedVisualizationsJson,
            String errorMessage,
            Instant generatedAt
    ) {
        this.dataset = dataset;
        this.generationStatus = generationStatus;
        this.modelName = modelName;
        this.datasetDescription = datasetDescription;
        this.potentialIssuesJson = potentialIssuesJson;
        this.suggestedAnalysesJson = suggestedAnalysesJson;
        this.suggestedVisualizationsJson = suggestedVisualizationsJson;
        this.errorMessage = errorMessage;
        this.generatedAt = generatedAt;
    }

    public void update(
            AiInsightGenerationStatus generationStatus,
            String modelName,
            String datasetDescription,
            String potentialIssuesJson,
            String suggestedAnalysesJson,
            String suggestedVisualizationsJson,
            String errorMessage,
            Instant generatedAt
    ) {
        this.generationStatus = generationStatus;
        this.modelName = modelName;
        this.datasetDescription = datasetDescription;
        this.potentialIssuesJson = potentialIssuesJson;
        this.suggestedAnalysesJson = suggestedAnalysesJson;
        this.suggestedVisualizationsJson = suggestedVisualizationsJson;
        this.errorMessage = errorMessage;
        this.generatedAt = generatedAt;
    }

    public AiInsightGenerationStatus getGenerationStatus() {
        return generationStatus;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDatasetDescription() {
        return datasetDescription;
    }

    public String getPotentialIssuesJson() {
        return potentialIssuesJson;
    }

    public String getSuggestedAnalysesJson() {
        return suggestedAnalysesJson;
    }

    public String getSuggestedVisualizationsJson() {
        return suggestedVisualizationsJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}
