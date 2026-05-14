package com.dataforge.ai;

import com.dataforge.ai.dto.DatasetChatResponse;
import com.dataforge.cleaning.DatasetCleaningReport;
import com.dataforge.cleaning.DatasetCleaningStorageService;
import com.dataforge.datasets.AuthenticatedUserNotFoundException;
import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetNotFoundException;
import com.dataforge.datasets.DatasetPreviewStorageService;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.DatasetProfileStorageService;
import com.dataforge.quality.DatasetQualityScore;
import com.dataforge.quality.DatasetQualityStorageService;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetChatService.class);

    private final DatasetRepository datasetRepository;
    private final DatasetPreviewStorageService datasetPreviewStorageService;
    private final DatasetProfileStorageService datasetProfileStorageService;
    private final DatasetQualityStorageService datasetQualityStorageService;
    private final DatasetCleaningStorageService datasetCleaningStorageService;
    private final DatasetChatPromptBuilder promptBuilder;
    private final OllamaInsightClient ollamaInsightClient;
    private final UserRepository userRepository;

    public DatasetChatService(
            DatasetRepository datasetRepository,
            DatasetPreviewStorageService datasetPreviewStorageService,
            DatasetProfileStorageService datasetProfileStorageService,
            DatasetQualityStorageService datasetQualityStorageService,
            DatasetCleaningStorageService datasetCleaningStorageService,
            DatasetChatPromptBuilder promptBuilder,
            OllamaInsightClient ollamaInsightClient,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetPreviewStorageService = datasetPreviewStorageService;
        this.datasetProfileStorageService = datasetProfileStorageService;
        this.datasetQualityStorageService = datasetQualityStorageService;
        this.datasetCleaningStorageService = datasetCleaningStorageService;
        this.promptBuilder = promptBuilder;
        this.ollamaInsightClient = ollamaInsightClient;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DatasetChatResponse chat(String email, UUID datasetId, String message) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        List<String> columnNames = datasetPreviewStorageService.columnNames(dataset);
        List<List<String>> previewRows = datasetPreviewStorageService.rows(dataset);
        List<DatasetColumnProfile> profiles = datasetProfileStorageService.profiles(dataset);
        DatasetQualityScore qualityScore = datasetQualityStorageService.qualityScore(dataset).orElse(null);
        Optional<DatasetCleaningReport> cleaningReport = datasetCleaningStorageService.report(dataset);

        String prompt = promptBuilder.build(dataset, columnNames, previewRows, profiles, qualityScore, cleaningReport, message);

        try {
            String answer = ollamaInsightClient.generateText(prompt);
            return new DatasetChatResponse(answer);
        } catch (AiInsightGenerationException exception) {
            LOGGER.warn("DataForge Analyst unavailable for dataset {}: {}", datasetId, exception.getMessage());
            return new DatasetChatResponse(fallbackAnswer(dataset, qualityScore));
        }
    }

    private String fallbackAnswer(Dataset dataset, DatasetQualityScore qualityScore) {
        String scoreText = qualityScore != null
                ? "The overall quality score is " + qualityScore.getOverallScore() + "."
                : "Quality scoring has not yet been completed for this dataset.";
        return "DataForge Analyst is temporarily unavailable. "
                + "Based on the dataset metadata: \""
                + dataset.getName()
                + "\" contains "
                + dataset.getColumnCount()
                + " column(s) and "
                + dataset.getRowCount()
                + " row(s). "
                + scoreText
                + " Please review the analytics panels for detailed information.";
    }
}
