package com.dataforge.datasets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DatasetPreviewStorageService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final DatasetColumnRepository datasetColumnRepository;
    private final DatasetPreviewRowRepository datasetPreviewRowRepository;
    private final ObjectMapper objectMapper;

    public DatasetPreviewStorageService(
            DatasetColumnRepository datasetColumnRepository,
            DatasetPreviewRowRepository datasetPreviewRowRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetColumnRepository = datasetColumnRepository;
        this.datasetPreviewRowRepository = datasetPreviewRowRepository;
        this.objectMapper = objectMapper;
    }

    public void replacePreview(Dataset dataset, CsvPreview preview) {
        datasetPreviewRowRepository.deleteByDataset(dataset);
        datasetColumnRepository.deleteByDataset(dataset);

        List<DatasetColumn> columns = new ArrayList<>(preview.columnNames().size());
        for (int index = 0; index < preview.columnNames().size(); index++) {
            columns.add(new DatasetColumn(dataset, preview.columnNames().get(index), index));
        }
        datasetColumnRepository.saveAll(columns);

        List<DatasetPreviewRow> rows = new ArrayList<>(preview.rows().size());
        for (int index = 0; index < preview.rows().size(); index++) {
            rows.add(new DatasetPreviewRow(dataset, index, writeRowValues(preview.rows().get(index))));
        }
        datasetPreviewRowRepository.saveAll(rows);
    }

    public List<String> columnNames(Dataset dataset) {
        return datasetColumnRepository.findByDatasetOrderByPositionAsc(dataset)
                .stream()
                .map(DatasetColumn::getName)
                .toList();
    }

    public List<List<String>> rows(Dataset dataset) {
        return datasetPreviewRowRepository.findByDatasetOrderByPositionAsc(dataset)
                .stream()
                .map(row -> readRowValues(row.getValuesJson()))
                .toList();
    }

    private String writeRowValues(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new FileUploadException("Failed to store CSV preview row", exception);
        }
    }

    private List<String> readRowValues(String valuesJson) {
        try {
            return objectMapper.readValue(valuesJson, STRING_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new FileUploadException("Failed to read CSV preview row", exception);
        }
    }
}
