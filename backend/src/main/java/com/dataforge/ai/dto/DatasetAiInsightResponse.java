package com.dataforge.ai.dto;

import com.dataforge.ai.AiInsightGenerationStatus;
import com.dataforge.datasets.dto.DatasetResponse;
import java.time.Instant;
import java.util.List;

public record DatasetAiInsightResponse(
        DatasetResponse dataset,
        AiInsightGenerationStatus generationStatus,
        String modelName,
        String datasetDescription,
        List<String> potentialIssues,
        List<String> suggestedAnalyses,
        List<String> suggestedVisualizations,
        String errorMessage,
        Instant generatedAt
) {
}
