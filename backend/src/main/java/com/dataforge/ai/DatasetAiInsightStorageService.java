package com.dataforge.ai;

import com.dataforge.datasets.Dataset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DatasetAiInsightStorageService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final DatasetAiInsightRepository datasetAiInsightRepository;
    private final ObjectMapper objectMapper;

    public DatasetAiInsightStorageService(
            DatasetAiInsightRepository datasetAiInsightRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetAiInsightRepository = datasetAiInsightRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<DatasetAiInsight> insight(Dataset dataset) {
        return datasetAiInsightRepository.findByDataset(dataset);
    }

    public DatasetAiInsight replaceInsight(
            Dataset dataset,
            AiInsightGenerationStatus status,
            String modelName,
            AiInsightContent content,
            String errorMessage
    ) {
        datasetAiInsightRepository.deleteByDataset(dataset);
        DatasetAiInsight insight = new DatasetAiInsight(
                dataset,
                status,
                modelName,
                normalize(content.datasetDescription()),
                writeStrings(content.potentialIssues()),
                writeStrings(content.suggestedAnalyses()),
                writeStrings(content.suggestedVisualizations()),
                errorMessage,
                Instant.now()
        );
        return datasetAiInsightRepository.save(insight);
    }

    public List<String> readStrings(String valuesJson) {
        try {
            return objectMapper.readValue(valuesJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new AiInsightStorageException("Failed to read AI insight values", exception);
        }
    }

    private String writeStrings(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException exception) {
            throw new AiInsightStorageException("Failed to store AI insight values", exception);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "AI insight generation did not return a dataset description.";
        }

        return value.trim();
    }
}
