package com.dataforge.ai;

import com.dataforge.ai.dto.DatasetAiInsightResponse;
import com.dataforge.datasets.AuthenticatedUserNotFoundException;
import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetNotFoundException;
import com.dataforge.datasets.DatasetPreviewStorageService;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.DatasetProfileService;
import com.dataforge.profiling.DatasetProfileStorageService;
import com.dataforge.quality.DatasetQualityScore;
import com.dataforge.quality.DatasetQualityService;
import com.dataforge.quality.DatasetQualityStorageService;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasetAiInsightService {

    private final DatasetRepository datasetRepository;
    private final DatasetPreviewStorageService datasetPreviewStorageService;
    private final DatasetProfileService datasetProfileService;
    private final DatasetProfileStorageService datasetProfileStorageService;
    private final DatasetQualityService datasetQualityService;
    private final DatasetQualityStorageService datasetQualityStorageService;
    private final AiInsightPromptBuilder promptBuilder;
    private final OllamaInsightClient ollamaInsightClient;
    private final DatasetAiInsightStorageService storageService;
    private final UserRepository userRepository;

    public DatasetAiInsightService(
            DatasetRepository datasetRepository,
            DatasetPreviewStorageService datasetPreviewStorageService,
            DatasetProfileService datasetProfileService,
            DatasetProfileStorageService datasetProfileStorageService,
            DatasetQualityService datasetQualityService,
            DatasetQualityStorageService datasetQualityStorageService,
            AiInsightPromptBuilder promptBuilder,
            OllamaInsightClient ollamaInsightClient,
            DatasetAiInsightStorageService storageService,
            UserRepository userRepository
    ) {
        this.datasetRepository = datasetRepository;
        this.datasetPreviewStorageService = datasetPreviewStorageService;
        this.datasetProfileService = datasetProfileService;
        this.datasetProfileStorageService = datasetProfileStorageService;
        this.datasetQualityService = datasetQualityService;
        this.datasetQualityStorageService = datasetQualityStorageService;
        this.promptBuilder = promptBuilder;
        this.ollamaInsightClient = ollamaInsightClient;
        this.storageService = storageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public DatasetAiInsightResponse getInsights(String email, UUID datasetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException(email));
        Dataset dataset = datasetRepository.findByIdAndUploadedBy(datasetId, user)
                .orElseThrow(() -> new DatasetNotFoundException(datasetId));

        DatasetAiInsight insight = storageService.insight(dataset)
                .orElseGet(() -> generateAndStore(dataset));

        return toResponse(dataset, insight);
    }

    private DatasetAiInsight generateAndStore(Dataset dataset) {
        AiInsightContext context = buildContext(dataset);
        String prompt = promptBuilder.build(context);

        try {
            AiInsightContent content = ollamaInsightClient.generate(prompt);
            return storageService.replaceInsight(
                    dataset,
                    AiInsightGenerationStatus.GENERATED,
                    ollamaInsightClient.modelName(),
                    content,
                    null
            );
        } catch (AiInsightGenerationException exception) {
            return storageService.replaceInsight(
                    dataset,
                    AiInsightGenerationStatus.UNAVAILABLE,
                    ollamaInsightClient.modelName(),
                    fallbackContent(context),
                    exception.getMessage()
            );
        }
    }

    private AiInsightContext buildContext(Dataset dataset) {
        List<DatasetColumnProfile> profiles = datasetProfileStorageService.profiles(dataset);
        if (profiles.isEmpty()) {
            profiles = datasetProfileService.profileAndStore(dataset);
        }

        DatasetQualityScore qualityScore = datasetQualityStorageService.qualityScore(dataset).orElse(null);
        if (qualityScore == null) {
            datasetQualityService.scoreAndStore(dataset, profiles);
            qualityScore = datasetQualityStorageService.qualityScore(dataset)
                    .orElseThrow(() -> new AiInsightGenerationException("Dataset quality results could not be generated"));
        }

        return new AiInsightContext(
                dataset,
                datasetPreviewStorageService.columnNames(dataset),
                datasetPreviewStorageService.rows(dataset),
                profiles,
                qualityScore
        );
    }

    private AiInsightContent fallbackContent(AiInsightContext context) {
        List<String> qualityIssues = datasetQualityStorageService
                .readIssues(context.qualityScore().getIssueSummariesJson())
                .stream()
                .map(issue -> issue.type() + ": " + issue.message())
                .toList();

        return new AiInsightContent(
                "AI insight generation is currently unavailable. Deterministic metadata shows "
                        + context.columnNames().size()
                        + " preview columns and "
                        + context.previewRows().size()
                        + " preview rows.",
                qualityIssues.isEmpty() ? List.of("No stored quality issues were available for summary.") : qualityIssues,
                List.of("Review column profiles and quality scores once AI insight generation is available."),
                List.of("Use a table preview and quality score breakdown until AI suggestions can be generated.")
        );
    }

    private DatasetAiInsightResponse toResponse(Dataset dataset, DatasetAiInsight insight) {
        return new DatasetAiInsightResponse(
                DatasetResponse.from(dataset),
                insight.getGenerationStatus(),
                insight.getModelName(),
                insight.getDatasetDescription(),
                storageService.readStrings(insight.getPotentialIssuesJson()),
                storageService.readStrings(insight.getSuggestedAnalysesJson()),
                storageService.readStrings(insight.getSuggestedVisualizationsJson()),
                insight.getErrorMessage(),
                insight.getGeneratedAt()
        );
    }
}
