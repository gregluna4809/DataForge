package com.dataforge.profiling;

import com.dataforge.datasets.AuthenticatedUserNotFoundException;
import com.dataforge.datasets.CsvPreview;
import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetNotFoundException;
import com.dataforge.datasets.DatasetPreviewStorageService;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.profiling.dto.ColumnProfileResponse;
import com.dataforge.profiling.dto.DatasetProfileResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetProfileService {

    private final DatasetRepository datasetRepository;
    private final DatasetPreviewStorageService datasetPreviewStorageService;
    private final DatasetProfiler datasetProfiler;
    private final DatasetProfileStorageService datasetProfileStorageService;
    private final UserRepository userRepository;

    public DatasetProfileService(
            DatasetRepository datasetRepository,
            DatasetPreviewStorageService datasetPreviewStorageService,
            DatasetProfiler datasetProfiler,
            DatasetProfileStorageService datasetProfileStorageService,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetPreviewStorageService = datasetPreviewStorageService;
        this.datasetProfiler = datasetProfiler;
        this.datasetProfileStorageService = datasetProfileStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void profileAndStore(Dataset dataset) {
        DatasetProfileResult result = datasetProfiler.profile(
                datasetPreviewStorageService.columnNames(dataset),
                datasetPreviewStorageService.rows(dataset)
        );
        datasetProfileStorageService.replaceProfile(dataset, result);
    }

    @Transactional
    public void profileAndStore(Dataset dataset, CsvPreview preview) {
        DatasetProfileResult result = datasetProfiler.profile(preview.columnNames(), preview.rows());
        datasetProfileStorageService.replaceProfile(dataset, result);
    }

    @Transactional
    public DatasetProfileResponse getProfile(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        List<DatasetColumnProfile> profiles = datasetProfileStorageService.profiles(dataset);
        if (profiles.isEmpty()) {
            profileAndStore(dataset);
            profiles = datasetProfileStorageService.profiles(dataset);
        }

        return new DatasetProfileResponse(
                DatasetResponse.from(dataset),
                profiles.stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    private ColumnProfileResponse toResponse(DatasetColumnProfile profile) {
        return new ColumnProfileResponse(
                profile.getColumnName(),
                profile.getColumnPosition(),
                profile.getNullCount(),
                profile.getNonNullCount(),
                profile.getUniqueCount(),
                profile.getInferredDataType(),
                datasetProfileStorageService.readMostCommonValues(profile.getMostCommonValuesJson())
        );
    }
}
