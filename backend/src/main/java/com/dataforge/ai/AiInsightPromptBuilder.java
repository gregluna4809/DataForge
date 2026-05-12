package com.dataforge.ai;

import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.quality.DatasetColumnQualityScore;
import com.dataforge.quality.DatasetQualityStorageService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiInsightPromptBuilder {

    private static final int PREVIEW_ROW_LIMIT = 10;

    private final DatasetQualityStorageService datasetQualityStorageService;

    public AiInsightPromptBuilder(DatasetQualityStorageService datasetQualityStorageService) {
        this.datasetQualityStorageService = datasetQualityStorageService;
    }

    public String build(AiInsightContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are DataForge's dataset insight assistant.
                Use only the deterministic inputs provided below.
                Do not fabricate statistics, row counts, column counts, or business facts.
                Return strict JSON with exactly these keys:
                {
                  "datasetDescription": "one concise paragraph",
                  "potentialIssues": ["issue summary"],
                  "suggestedAnalyses": ["analysis suggestion"],
                  "suggestedVisualizations": ["visualization suggestion"]
                }

                Dataset metadata:
                """);
        prompt.append("- name: ").append(context.dataset().getName()).append('\n');
        prompt.append("- originalFilename: ").append(context.dataset().getOriginalFilename()).append('\n');
        prompt.append("- status: ").append(context.dataset().getStatus()).append('\n');
        prompt.append("- previewColumnCount: ").append(context.columnNames().size()).append('\n');
        prompt.append("- previewRowCount: ").append(context.previewRows().size()).append("\n\n");

        appendPreview(prompt, context.columnNames(), context.previewRows());
        appendProfiles(prompt, context.profiles());
        appendQuality(prompt, context);

        return prompt.toString();
    }

    private void appendPreview(StringBuilder prompt, List<String> columnNames, List<List<String>> rows) {
        prompt.append("Preview columns:\n");
        for (int index = 0; index < columnNames.size(); index++) {
            prompt.append("- ").append(index).append(": ").append(columnNames.get(index)).append('\n');
        }

        prompt.append("\nPreview rows, limited to first ").append(PREVIEW_ROW_LIMIT).append(" rows:\n");
        rows.stream()
                .limit(PREVIEW_ROW_LIMIT)
                .forEach(row -> prompt.append("- ").append(row).append('\n'));
        prompt.append('\n');
    }

    private void appendProfiles(StringBuilder prompt, List<DatasetColumnProfile> profiles) {
        prompt.append("Column profiles:\n");
        for (DatasetColumnProfile profile : profiles) {
            prompt.append("- ")
                    .append(profile.getColumnName())
                    .append(": nullCount=").append(profile.getNullCount())
                    .append(", nonNullCount=").append(profile.getNonNullCount())
                    .append(", uniqueCount=").append(profile.getUniqueCount())
                    .append(", inferredDataType=").append(profile.getInferredDataType())
                    .append('\n');
        }
        prompt.append('\n');
    }

    private void appendQuality(StringBuilder prompt, AiInsightContext context) {
        prompt.append("Quality result:\n");
        prompt.append("- overallScore: ").append(context.qualityScore().getOverallScore()).append('\n');
        prompt.append("- datasetIssues: ")
                .append(datasetQualityStorageService.readIssues(context.qualityScore().getIssueSummariesJson()))
                .append('\n');

        prompt.append("Column quality scores:\n");
        for (DatasetColumnQualityScore columnScore : datasetQualityStorageService.orderedColumnScores(context.qualityScore())) {
            prompt.append("- ")
                    .append(columnScore.getColumnName())
                    .append(": qualityScore=").append(columnScore.getQualityScore())
                    .append(", nullPercentage=").append(columnScore.getNullPercentage())
                    .append(", uniquenessPercentage=").append(columnScore.getUniquenessPercentage())
                    .append(", typeConsistencyScore=").append(columnScore.getTypeConsistencyScore())
                    .append(", issues=").append(datasetQualityStorageService.readIssues(columnScore.getIssueSummariesJson()))
                    .append('\n');
        }
    }
}
