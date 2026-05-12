package com.dataforge.profiling;

import com.dataforge.datasets.Dataset;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DatasetProfileStorageService {

    private static final TypeReference<List<MostCommonValue>> MOST_COMMON_VALUE_LIST_TYPE = new TypeReference<>() {
    };

    private final DatasetColumnProfileRepository datasetColumnProfileRepository;
    private final ObjectMapper objectMapper;

    public DatasetProfileStorageService(
            DatasetColumnProfileRepository datasetColumnProfileRepository,
            ObjectMapper objectMapper
    ) {
        this.datasetColumnProfileRepository = datasetColumnProfileRepository;
        this.objectMapper = objectMapper;
    }

    public List<DatasetColumnProfile> replaceProfile(Dataset dataset, DatasetProfileResult profileResult) {
        datasetColumnProfileRepository.deleteByDataset(dataset);

        Instant profiledAt = Instant.now();
        List<DatasetColumnProfile> profiles = new ArrayList<>(profileResult.columns().size());
        for (ColumnProfileResult columnProfile : profileResult.columns()) {
            profiles.add(new DatasetColumnProfile(
                    dataset,
                    columnProfile,
                    writeMostCommonValues(columnProfile.mostCommonValues()),
                    profiledAt
            ));
        }

        return datasetColumnProfileRepository.saveAll(profiles);
    }

    public List<DatasetColumnProfile> profiles(Dataset dataset) {
        return datasetColumnProfileRepository.findByDatasetOrderByColumnPositionAsc(dataset);
    }

    public List<MostCommonValue> readMostCommonValues(String valuesJson) {
        try {
            return objectMapper.readValue(valuesJson, MOST_COMMON_VALUE_LIST_TYPE);
        } catch (JsonProcessingException exception) {
            throw new ProfileStorageException("Failed to read dataset profile values", exception);
        }
    }

    private String writeMostCommonValues(List<MostCommonValue> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new ProfileStorageException("Failed to store dataset profile values", exception);
        }
    }
}
