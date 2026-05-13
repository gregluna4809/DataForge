package com.dataforge.cleaning;

import com.dataforge.cleaning.dto.DatasetCleaningReportResponse;
import com.dataforge.datasets.AuthenticatedUserNotFoundException;
import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetNotFoundException;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetCleaningService {

    private final DatasetRepository datasetRepository;
    private final DatasetCsvCleaner datasetCsvCleaner;
    private final DatasetCleaningStorageService datasetCleaningStorageService;
    private final UserRepository userRepository;

    public DatasetCleaningService(
            DatasetRepository datasetRepository,
            DatasetCsvCleaner datasetCsvCleaner,
            DatasetCleaningStorageService datasetCleaningStorageService,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetCsvCleaner = datasetCsvCleaner;
        this.datasetCleaningStorageService = datasetCleaningStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public DatasetCleaningReportResponse cleanDataset(String email, UUID datasetId) {
        Dataset dataset = ownedDataset(email, datasetId);
        Path sourcePath = uploadedFilePath(dataset);
        CleanedCsvResult result = datasetCsvCleaner.clean(sourcePath);
        DatasetCleaningReport report = datasetCleaningStorageService.replaceReport(dataset, result);
        return toResponse(dataset, report);
    }

    @Transactional(readOnly = true)
    public DatasetCleaningReportResponse getCleaningReport(String email, UUID datasetId) {
        Dataset dataset = ownedDataset(email, datasetId);
        DatasetCleaningReport report = datasetCleaningStorageService.report(dataset)
                .orElseThrow(() -> new DatasetCleaningReportNotFoundException(datasetId));
        return toResponse(dataset, report);
    }

    @Transactional(readOnly = true)
    public CleanedDatasetDownload cleanedDatasetDownload(String email, UUID datasetId) {
        Dataset dataset = ownedDataset(email, datasetId);
        DatasetCleaningReport report = datasetCleaningStorageService.report(dataset)
                .orElseThrow(() -> new DatasetCleaningReportNotFoundException(datasetId));
        Path cleanedPath = Path.of(report.getCleanedStoragePath()).toAbsolutePath().normalize();
        if (!Files.exists(cleanedPath) || !Files.isRegularFile(cleanedPath)) {
            throw new DatasetCleaningException("Cleaned CSV file was not found");
        }
        Resource resource = new FileSystemResource(cleanedPath);
        return new CleanedDatasetDownload(report.getCleanedFilename(), report.getCleanedFileSizeBytes(), resource);
    }

    private Dataset ownedDataset(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        return datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));
    }

    private Path uploadedFilePath(Dataset dataset) {
        if (dataset.getStoragePath() == null || dataset.getStoragePath().isBlank()) {
            throw new DatasetCleaningException("Dataset does not have an uploaded CSV file to clean");
        }
        return Path.of(dataset.getStoragePath()).toAbsolutePath().normalize();
    }

    private DatasetCleaningReportResponse toResponse(Dataset dataset, DatasetCleaningReport report) {
        return new DatasetCleaningReportResponse(
                DatasetResponse.from(dataset),
                report.getCleanedFilename(),
                report.getCleanedFileSizeBytes(),
                report.getRowsRead(),
                report.getRowsWritten(),
                report.getDuplicateRowsRemoved(),
                report.getEmptyRowsRemoved(),
                datasetCleaningStorageService.readColumnRenames(report.getColumnsRenamedJson()),
                datasetCleaningStorageService.readCleaningRules(report.getCleaningRulesAppliedJson()),
                report.getCleanedAt()
        );
    }

    public record CleanedDatasetDownload(
            String filename,
            long fileSizeBytes,
            Resource resource
    ) {
    }
}
