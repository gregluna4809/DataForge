package com.dataforge.datasets;

import com.dataforge.datasets.dto.DatasetUploadResponse;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DatasetUploadService {

    private static final String CSV_EXTENSION = ".csv";

    private final DatasetRepository datasetRepository;
    private final CsvPreviewParser csvPreviewParser;
    private final DatasetPreviewStorageService datasetPreviewStorageService;
    private final DatasetUploadProperties uploadProperties;
    private final UserRepository userRepository;

    public DatasetUploadService(
            DatasetRepository datasetRepository,
            CsvPreviewParser csvPreviewParser,
            DatasetPreviewStorageService datasetPreviewStorageService,
            DatasetUploadProperties uploadProperties,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.csvPreviewParser = csvPreviewParser;
        this.datasetPreviewStorageService = datasetPreviewStorageService;
        this.uploadProperties = uploadProperties;
        this.userRepository = userRepository;
    }

    @Transactional
    public DatasetUploadResponse uploadCsv(String email, UUID datasetId, MultipartFile file) {
        validateFile(file);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        String originalFilename = cleanOriginalFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + CSV_EXTENSION;
        Path uploadDirectory = resolveUploadDirectory();
        Path storedPath = uploadDirectory.resolve(storedFilename).normalize();

        if (!storedPath.startsWith(uploadDirectory)) {
            throw new FileUploadException("Resolved upload path is invalid");
        }

        try {
            Files.createDirectories(uploadDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new FileUploadException("Failed to store uploaded CSV file", exception);
        }

        CsvPreview preview = csvPreviewParser.parse(storedPath);

        Instant uploadedAt = Instant.now();
        dataset.markUploaded(
                originalFilename,
                storedFilename,
                storedPath.toString(),
                file.getContentType(),
                file.getSize(),
                uploadedAt
        );
        dataset.updateParsedColumnCount(preview.columnNames().size());
        datasetPreviewStorageService.replacePreview(dataset, preview);

        Dataset savedDataset = datasetRepository.save(dataset);
        return DatasetUploadResponse.from(savedDataset);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("CSV file is required");
        }

        if (file.getSize() > uploadProperties.maxFileSizeBytes()) {
            throw new FileUploadException("CSV file exceeds the configured maximum size");
        }

        String originalFilename = cleanOriginalFilename(file.getOriginalFilename());
        if (!originalFilename.toLowerCase(Locale.ROOT).endsWith(CSV_EXTENSION)) {
            throw new FileUploadException("Only .csv files are supported");
        }
    }

    private Path resolveUploadDirectory() {
        return Path.of(uploadProperties.directory()).toAbsolutePath().normalize();
    }

    private String cleanOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new FileUploadException("Uploaded file must have a filename");
        }

        return Path.of(originalFilename).getFileName().toString();
    }
}
