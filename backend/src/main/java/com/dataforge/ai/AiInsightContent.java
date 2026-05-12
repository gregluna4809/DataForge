package com.dataforge.ai;

import java.util.List;

public record AiInsightContent(
        String datasetDescription,
        List<String> potentialIssues,
        List<String> suggestedAnalyses,
        List<String> suggestedVisualizations
) {
}
