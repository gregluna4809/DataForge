package com.dataforge.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetPreviewStorageService;
import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.DatasetStatus;
import com.dataforge.profiling.ColumnProfileResult;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.DatasetProfileService;
import com.dataforge.profiling.DatasetProfileStorageService;
import com.dataforge.profiling.InferredDataType;
import com.dataforge.quality.DatasetQualityScore;
import com.dataforge.quality.DatasetQualityService;
import com.dataforge.quality.DatasetQualityStorageService;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DatasetAiInsightServiceTests {

    private static final String USER_EMAIL = "user@example.com";
    private static final UUID DATASET_ID = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");

    @Mock
    private DatasetRepository datasetRepository;

    @Mock
    private DatasetPreviewStorageService datasetPreviewStorageService;

    @Mock
    private DatasetProfileService datasetProfileService;

    @Mock
    private DatasetProfileStorageService datasetProfileStorageService;

    @Mock
    private DatasetQualityService datasetQualityService;

    @Mock
    private DatasetQualityStorageService datasetQualityStorageService;

    @Mock
    private AiInsightPromptBuilder promptBuilder;

    @Mock
    private OllamaInsightClient ollamaInsightClient;

    @Mock
    private DatasetAiInsightStorageService storageService;

    @Mock
    private UserRepository userRepository;

    private DatasetAiInsightService service;
    private User user;
    private Dataset dataset;
    private DatasetQualityScore qualityScore;

    @BeforeEach
    void setUp() {
        service = new DatasetAiInsightService(
                datasetRepository,
                datasetPreviewStorageService,
                datasetProfileService,
                datasetProfileStorageService,
                datasetQualityService,
                datasetQualityStorageService,
                promptBuilder,
                ollamaInsightClient,
                storageService,
                userRepository
        );

        user = new User(
                USER_EMAIL,
                "DataForge User",
                "$2a$10$examplehash",
                "ROLE_USER",
                true,
                Instant.parse("2026-05-11T12:00:00Z")
        );
        ReflectionTestUtils.setField(user, "id", UUID.fromString("22d91774-dd28-443f-b266-9f0d03820f92"));

        dataset = new Dataset(
                "Customer Imports",
                "customers.csv",
                "Initial customer import",
                100,
                2,
                4096,
                DatasetStatus.UPLOADED,
                user,
                Instant.parse("2026-05-11T12:30:00Z")
        );
        ReflectionTestUtils.setField(dataset, "id", DATASET_ID);

        qualityScore = new DatasetQualityScore(
                dataset,
                91.25,
                "[]",
                Instant.parse("2026-05-11T13:00:00Z")
        );
    }

    @Test
    void generatesAndPersistsInsightsFromOllama() {
        AiInsightContent content = new AiInsightContent(
                "Customer import data with identifiers and names.",
                List.of("Possible identifier column"),
                List.of("Analyze customer counts by status"),
                List.of("Bar chart by status")
        );
        DatasetAiInsight storedInsight = generatedInsight(content, AiInsightGenerationStatus.GENERATED, null);

        arrangeOwnedDataset();
        arrangeContext();
        when(promptBuilder.build(any(AiInsightContext.class))).thenReturn("structured prompt");
        when(ollamaInsightClient.generate("structured prompt")).thenReturn(content);
        when(ollamaInsightClient.modelName()).thenReturn("llama3.1");
        when(storageService.replaceInsight(
                eq(dataset),
                eq(AiInsightGenerationStatus.GENERATED),
                eq("llama3.1"),
                eq(content),
                eq(null)
        )).thenReturn(storedInsight);
        arrangeStoredJsonReads(content);

        var response = service.getInsights(USER_EMAIL, DATASET_ID);

        assertThat(response.generationStatus()).isEqualTo(AiInsightGenerationStatus.GENERATED);
        assertThat(response.datasetDescription()).isEqualTo("Customer import data with identifiers and names.");
        assertThat(response.potentialIssues()).containsExactly("Possible identifier column");
        verify(ollamaInsightClient).generate("structured prompt");
    }

    @Test
    void returnsPersistedFallbackWhenOllamaIsUnavailable() {
        AiInsightContent fallback = new AiInsightContent(
                "AI insight generation is currently unavailable. Deterministic metadata shows 2 preview columns and 1 preview rows.",
                List.of("No stored quality issues were available for summary."),
                List.of("Review column profiles and quality scores once AI insight generation is available."),
                List.of("Use a table preview and quality score breakdown until AI suggestions can be generated.")
        );
        DatasetAiInsight storedInsight = generatedInsight(fallback, AiInsightGenerationStatus.UNAVAILABLE, "Ollama is unavailable");

        arrangeOwnedDataset();
        arrangeContext();
        when(promptBuilder.build(any(AiInsightContext.class))).thenReturn("structured prompt");
        when(ollamaInsightClient.generate("structured prompt")).thenThrow(new AiInsightGenerationException("Ollama is unavailable"));
        when(ollamaInsightClient.modelName()).thenReturn("llama3.1");
        when(datasetQualityStorageService.readIssues("[]")).thenReturn(List.of());
        when(storageService.replaceInsight(
                eq(dataset),
                eq(AiInsightGenerationStatus.UNAVAILABLE),
                eq("llama3.1"),
                any(AiInsightContent.class),
                eq("Ollama is unavailable")
        )).thenReturn(storedInsight);
        arrangeStoredJsonReads(fallback);

        var response = service.getInsights(USER_EMAIL, DATASET_ID);

        assertThat(response.generationStatus()).isEqualTo(AiInsightGenerationStatus.UNAVAILABLE);
        assertThat(response.errorMessage()).isEqualTo("Ollama is unavailable");
        assertThat(response.datasetDescription()).contains("AI insight generation is currently unavailable");
    }

    @Test
    void retriesGenerationWhenStoredInsightWasUnavailable() {
        AiInsightContent previousFallback = new AiInsightContent(
                "AI insight generation is currently unavailable. Deterministic metadata shows 2 preview columns and 1 preview rows.",
                List.of("No stored quality issues were available for summary."),
                List.of("Review column profiles and quality scores once AI insight generation is available."),
                List.of("Use a table preview and quality score breakdown until AI suggestions can be generated.")
        );
        DatasetAiInsight unavailableInsight = generatedInsight(
                previousFallback,
                AiInsightGenerationStatus.UNAVAILABLE,
                "Previous Ollama failure"
        );
        AiInsightContent generatedContent = new AiInsightContent(
                "Fresh AI insight generated after Ollama became available.",
                List.of("Potential quality issue"),
                List.of("Review completeness by column"),
                List.of("Null-rate table")
        );
        DatasetAiInsight generatedInsight = generatedInsight(generatedContent, AiInsightGenerationStatus.GENERATED, null);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(datasetRepository.findByIdAndUploadedBy(DATASET_ID, user)).thenReturn(Optional.of(dataset));
        when(storageService.insight(dataset)).thenReturn(Optional.of(unavailableInsight));
        arrangeContext();
        when(promptBuilder.build(any(AiInsightContext.class))).thenReturn("structured prompt");
        when(ollamaInsightClient.generate("structured prompt")).thenReturn(generatedContent);
        when(ollamaInsightClient.modelName()).thenReturn("llama3.1");
        when(storageService.replaceInsight(
                eq(dataset),
                eq(AiInsightGenerationStatus.GENERATED),
                eq("llama3.1"),
                eq(generatedContent),
                eq(null)
        )).thenReturn(generatedInsight);
        arrangeStoredJsonReads(generatedContent);

        var response = service.getInsights(USER_EMAIL, DATASET_ID);

        assertThat(response.generationStatus()).isEqualTo(AiInsightGenerationStatus.GENERATED);
        assertThat(response.datasetDescription()).isEqualTo("Fresh AI insight generated after Ollama became available.");
        verify(ollamaInsightClient).generate("structured prompt");
    }

    private void arrangeOwnedDataset() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(datasetRepository.findByIdAndUploadedBy(DATASET_ID, user)).thenReturn(Optional.of(dataset));
        when(storageService.insight(dataset)).thenReturn(Optional.empty());
    }

    private void arrangeContext() {
        DatasetColumnProfile profile = new DatasetColumnProfile(
                dataset,
                new ColumnProfileResult(
                        "customer_id",
                        0,
                        0,
                        1,
                        1,
                        InferredDataType.INTEGER,
                        List.of()
                ),
                "[]",
                Instant.parse("2026-05-11T12:45:00Z")
        );

        when(datasetProfileStorageService.profiles(dataset)).thenReturn(List.of(profile));
        when(datasetQualityStorageService.qualityScore(dataset)).thenReturn(Optional.of(qualityScore));
        when(datasetPreviewStorageService.columnNames(dataset)).thenReturn(List.of("customer_id", "name"));
        when(datasetPreviewStorageService.rows(dataset)).thenReturn(List.of(List.of("1", "Ada")));
    }

    private DatasetAiInsight generatedInsight(
            AiInsightContent content,
            AiInsightGenerationStatus status,
            String errorMessage
    ) {
        return new DatasetAiInsight(
                dataset,
                status,
                "llama3.1",
                content.datasetDescription(),
                "potentialIssuesJson",
                "suggestedAnalysesJson",
                "suggestedVisualizationsJson",
                errorMessage,
                Instant.parse("2026-05-11T13:15:00Z")
        );
    }

    private void arrangeStoredJsonReads(AiInsightContent content) {
        when(storageService.readStrings("potentialIssuesJson")).thenReturn(content.potentialIssues());
        when(storageService.readStrings("suggestedAnalysesJson")).thenReturn(content.suggestedAnalyses());
        when(storageService.readStrings("suggestedVisualizationsJson")).thenReturn(content.suggestedVisualizations());
    }
}
