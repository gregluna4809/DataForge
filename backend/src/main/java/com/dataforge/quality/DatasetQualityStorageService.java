package com.dataforge.quality;

import com.dataforge.datasets.Dataset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DatasetQualityStorageService {

    private static final TypeReference<List<QualityIssueSummary>> ISSUE_LIST_TYPE = new TypeReference<>() {
    };

    private final DatasetQualityScoreRepository datasetQualityScoreRepository;
    private final ObjectMapper objectMapper;

    public DatasetQualityStorageService(
            DatasetQualityScoreRepository datasetQualityScoreRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetQualityScoreRepository = datasetQualityScoreRepository;
        this.objectMapper = objectMapper;
    }

    public DatasetQualityScore replaceQuality(Dataset dataset, DatasetQualityResult result) {
        datasetQualityScoreRepository.deleteByDataset(dataset);

        DatasetQualityScore score = new DatasetQualityScore(
                dataset,
                result.overallScore(),
                writeIssues(result.issueSummaries()),
                Instant.now()
        );

        for (ColumnQualityResult column : result.columns()) {
            score.addColumnScore(new DatasetColumnQualityScore(score, column, writeIssues(column.issueSummaries())));
        }

        return datasetQualityScoreRepository.save(score);
    }

    public Optional<DatasetQualityScore> qualityScore(Dataset dataset) {
        return datasetQualityScoreRepository.findByDataset(dataset);
    }

    public List<DatasetColumnQualityScore> orderedColumnScores(DatasetQualityScore score) {
        return score.getColumnScores()
                .stream()
                .sorted(Comparator.comparingInt(DatasetColumnQualityScore::getColumnPosition))
                .toList();
    }

    public List<QualityIssueSummary> readIssues(String issuesJson) {
        try {
            return objectMapper.readValue(issuesJson, ISSUE_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new QualityStorageException("Failed to read dataset quality issues", exception);
        }
    }

    private String writeIssues(List<QualityIssueSummary> issues) {
        try {
            return objectMapper.writeValueAsString(issues);
        } catch (JsonProcessingException exception) {
            throw new QualityStorageException("Failed to store dataset quality issues", exception);
        }
    }
}
