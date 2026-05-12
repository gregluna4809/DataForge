package com.dataforge.datasets;

import com.dataforge.datasets.dto.CreateDatasetRequest;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final UserRepository userRepository;

    public DatasetService(DatasetRepository datasetRepository, UserRepository userRepository) {
        this.datasetRepository = datasetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DatasetResponse> listDatasetsForUser(String email) {
        User user = findAuthenticatedUser(email);

        return datasetRepository.findByUploadedByOrderByUploadTimestampDesc(user)
                .stream()
                .map(DatasetResponse::from)
                .toList();
    }

    @Transactional
    public DatasetResponse createDataset(String email, CreateDatasetRequest request) {
        User user = findAuthenticatedUser(email);

        Dataset dataset = new Dataset(
                request.name().trim(),
                request.originalFilename().trim(),
                normalizeDescription(request.description()),
                request.rowCount(),
                request.columnCount(),
                request.fileSizeBytes(),
                DatasetStatus.METADATA_CREATED,
                user,
                Instant.now()
        );

        return DatasetResponse.from(datasetRepository.save(dataset));
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
