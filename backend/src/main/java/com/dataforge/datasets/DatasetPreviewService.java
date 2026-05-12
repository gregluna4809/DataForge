package com.dataforge.datasets;

import com.dataforge.datasets.dto.DatasetPreviewResponse;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetPreviewService {

    private final DatasetRepository datasetRepository;
    private final DatasetPreviewStorageService datasetPreviewStorageService;
    private final UserRepository userRepository;

    public DatasetPreviewService(
            DatasetRepository datasetRepository,
            DatasetPreviewStorageService datasetPreviewStorageService,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetPreviewStorageService = datasetPreviewStorageService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DatasetPreviewResponse getPreview(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        return new DatasetPreviewResponse(
                DatasetResponse.from(dataset),
                datasetPreviewStorageService.columnNames(dataset),
                datasetPreviewStorageService.rows(dataset)
        );
    }
}
