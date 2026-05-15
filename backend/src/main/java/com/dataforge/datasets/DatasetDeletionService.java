package com.dataforge.datasets;

import com.dataforge.ai.DatasetAiInsightRepository;
import com.dataforge.cleaning.DatasetCleaningReport;
import com.dataforge.cleaning.DatasetCleaningReportRepository;
import com.dataforge.profiling.DatasetColumnProfileRepository;
import com.dataforge.quality.DatasetQualityScoreRepository;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetDeletionService {

    private static final Logger log = LoggerFactory.getLogger(DatasetDeletionService.class);

    private final DatasetRepository datasetRepository;
    private final DatasetColumnRepository datasetColumnRepository;
    private final DatasetPreviewRowRepository datasetPreviewRowRepository;
    private final DatasetColumnProfileRepository datasetColumnProfileRepository;
    private final DatasetQualityScoreRepository datasetQualityScoreRepository;
    private final DatasetAiInsightRepository datasetAiInsightRepository;
    private final DatasetCleaningReportRepository datasetCleaningReportRepository;
    private final UserRepository userRepository;

    public DatasetDeletionService(
            DatasetRepository datasetRepository,
            DatasetColumnRepository datasetColumnRepository,
            DatasetPreviewRowRepository datasetPreviewRowRepository,
            DatasetColumnProfileRepository datasetColumnProfileRepository,
            DatasetQualityScoreRepository datasetQualityScoreRepository,
            DatasetAiInsightRepository datasetAiInsightRepository,
            DatasetCleaningReportRepository datasetCleaningReportRepository,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetColumnRepository = datasetColumnRepository;
        this.datasetPreviewRowRepository = datasetPreviewRowRepository;
        this.datasetColumnProfileRepository = datasetColumnProfileRepository;
        this.datasetQualityScoreRepository = datasetQualityScoreRepository;
        this.datasetAiInsightRepository = datasetAiInsightRepository;
        this.datasetCleaningReportRepository = datasetCleaningReportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteDataset(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        String uploadedFilePath = dataset.getStoragePath();
        Optional<String> cleanedFilePath = datasetCleaningReportRepository.findByDataset(dataset)
                .map(DatasetCleaningReport::getCleanedStoragePath);

        datasetAiInsightRepository.deleteByDataset(dataset);
        datasetCleaningReportRepository.deleteByDataset(dataset);
        datasetQualityScoreRepository.deleteByDataset(dataset);
        datasetColumnProfileRepository.deleteByDataset(dataset);
        datasetPreviewRowRepository.deleteByDataset(dataset);
        datasetColumnRepository.deleteByDataset(dataset);
        datasetRepository.delete(dataset);

        deleteFile(uploadedFilePath);
        cleanedFilePath.ifPresent(this::deleteFile);
    }

    private void deleteFile(String pathString) {
        if (pathString == null || pathString.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(pathString).toAbsolutePath().normalize());
        } catch (IOException exception) {
            log.warn("Failed to delete file during dataset deletion: {}", pathString, exception);
        }
    }
}
