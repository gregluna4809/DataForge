package com.dataforge.ai;

import com.dataforge.ai.dto.ChatHistoryMessage;
import com.dataforge.cleaning.DatasetCleaningReport;
import com.dataforge.datasets.Dataset;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.quality.DatasetColumnQualityScore;
import com.dataforge.quality.DatasetQualityScore;
import com.dataforge.quality.DatasetQualityStorageService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DatasetChatPromptBuilder {

    private static final int PREVIEW_ROW_LIMIT = 10;

    private final DatasetQualityStorageService datasetQualityStorageService;

    public DatasetChatPromptBuilder(DatasetQualityStorageService datasetQualityStorageService) {
        this.datasetQualityStorageService = datasetQualityStorageService;
    }

    public String build(
            Dataset dataset,
            List<String> columnNames,
            List<List<String>> previewRows,
            List<DatasetColumnProfile> profiles,
            DatasetQualityScore qualityScore,
            Optional<DatasetCleaningReport> cleaningReport,
            List<ChatHistoryMessage> history,
            String userMessage
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are DataForge Analyst, an expert data analyst assistant.
                Answer the user's question based ONLY on the deterministic inputs provided below.
                Do not fabricate statistics, row counts, column counts, or business facts not present in the data.
                Provide a concise, professional plain-text answer. Do not return JSON.

                Dataset metadata:
                """);
        prompt.append("- name: ").append(dataset.getName()).append('\n');
        prompt.append("- originalFilename: ").append(dataset.getOriginalFilename()).append('\n');
        prompt.append("- status: ").append(dataset.getStatus()).append('\n');
        prompt.append("- columnCount: ").append(columnNames.size()).append('\n');
        prompt.append("- previewRowCount: ").append(previewRows.size()).append("\n\n");

        appendPreview(prompt, columnNames, previewRows);

        if (!profiles.isEmpty()) {
            appendProfiles(prompt, profiles);
        }

        if (qualityScore != null) {
            appendQuality(prompt, qualityScore);
        }

        cleaningReport.ifPresent(report -> appendCleaningReport(prompt, report));

        if (history != null && !history.isEmpty()) {
            appendHistory(prompt, history);
        }

        prompt.append("\nUser question: ").append(userMessage).append('\n');
        return prompt.toString();
    }

    private void appendPreview(StringBuilder prompt, List<String> columnNames, List<List<String>> rows) {
        prompt.append("Preview columns:\n");
        for (int index = 0; index < columnNames.size(); index++) {
            prompt.append("- ").append(index).append(": ").append(columnNames.get(index)).append('\n');
        }

        prompt.append("\nPreview rows (up to ").append(PREVIEW_ROW_LIMIT).append("):\n");
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

    private void appendQuality(StringBuilder prompt, DatasetQualityScore qualityScore) {
        prompt.append("Quality result:\n");
        prompt.append("- overallScore: ").append(qualityScore.getOverallScore()).append('\n');
        prompt.append("- datasetIssues: ")
                .append(datasetQualityStorageService.readIssues(qualityScore.getIssueSummariesJson()))
                .append('\n');

        prompt.append("Column quality scores:\n");
        for (DatasetColumnQualityScore columnScore : datasetQualityStorageService.orderedColumnScores(qualityScore)) {
            prompt.append("- ")
                    .append(columnScore.getColumnName())
                    .append(": qualityScore=").append(columnScore.getQualityScore())
                    .append(", nullPercentage=").append(columnScore.getNullPercentage())
                    .append(", issues=")
                    .append(datasetQualityStorageService.readIssues(columnScore.getIssueSummariesJson()))
                    .append('\n');
        }
        prompt.append('\n');
    }

    private void appendCleaningReport(StringBuilder prompt, DatasetCleaningReport report) {
        prompt.append("Cleaning report:\n");
        prompt.append("- rowsRead: ").append(report.getRowsRead()).append('\n');
        prompt.append("- rowsWritten: ").append(report.getRowsWritten()).append('\n');
        prompt.append("- duplicateRowsRemoved: ").append(report.getDuplicateRowsRemoved()).append('\n');
        prompt.append("- emptyRowsRemoved: ").append(report.getEmptyRowsRemoved()).append('\n');
        prompt.append("- cleanedFilename: ").append(report.getCleanedFilename()).append("\n\n");
    }

    private void appendHistory(StringBuilder prompt, List<ChatHistoryMessage> history) {
        prompt.append("Conversation history:\n");
        for (ChatHistoryMessage entry : history) {
            prompt.append(entry.role()).append(": ").append(entry.content()).append('\n');
        }
        prompt.append('\n');
    }
}
