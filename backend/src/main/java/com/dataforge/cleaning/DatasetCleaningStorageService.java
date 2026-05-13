package com.dataforge.cleaning;

import com.dataforge.datasets.Dataset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DatasetCleaningStorageService {

    private static final TypeReference<List<ColumnRename>> COLUMN_RENAME_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<CleaningRule>> CLEANING_RULE_LIST_TYPE = new TypeReference<>() {
    };

    private final DatasetCleaningReportRepository datasetCleaningReportRepository;
    private final ObjectMapper objectMapper;

    public DatasetCleaningStorageService(
            DatasetCleaningReportRepository datasetCleaningReportRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetCleaningReportRepository = datasetCleaningReportRepository;
        this.objectMapper = objectMapper;
    }

    public DatasetCleaningReport replaceReport(Dataset dataset, CleanedCsvResult result) {
        datasetCleaningReportRepository.deleteByDataset(dataset);
        DatasetCleaningReport report = new DatasetCleaningReport(
                dataset,
                result.cleanedFilename(),
                result.cleanedPath().toString(),
                result.cleanedFileSizeBytes(),
                result.rowsRead(),
                result.rowsWritten(),
                result.duplicateRowsRemoved(),
                result.emptyRowsRemoved(),
                writeColumnRenames(result.columnsRenamed()),
                writeCleaningRules(result.cleaningRulesApplied()),
                Instant.now()
        );
        return datasetCleaningReportRepository.save(report);
    }

    public Optional<DatasetCleaningReport> report(Dataset dataset) {
        return datasetCleaningReportRepository.findByDataset(dataset);
    }

    public List<ColumnRename> readColumnRenames(String valuesJson) {
        try {
            return objectMapper.readValue(valuesJson, COLUMN_RENAME_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new CleaningStorageException("Failed to read cleaning column rename metadata", exception);
        }
    }

    public List<CleaningRule> readCleaningRules(String valuesJson) {
        try {
            return objectMapper.readValue(valuesJson, CLEANING_RULE_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new CleaningStorageException("Failed to read cleaning rule metadata", exception);
        }
    }

    private String writeColumnRenames(List<ColumnRename> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new CleaningStorageException("Failed to store cleaning column rename metadata", exception);
        }
    }

    private String writeCleaningRules(List<CleaningRule> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new CleaningStorageException("Failed to store cleaning rule metadata", exception);
        }
    }
}
