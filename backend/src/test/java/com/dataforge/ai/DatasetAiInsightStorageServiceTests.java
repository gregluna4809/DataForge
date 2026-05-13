package com.dataforge.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dataforge.datasets.Dataset;
import com.dataforge.datasets.DatasetStatus;
import com.dataforge.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatasetAiInsightStorageServiceTests {

    @Mock
    private DatasetAiInsightRepository repository;

    private DatasetAiInsightStorageService service;
    private Dataset dataset;

    @BeforeEach
    void setUp() {
        service = new DatasetAiInsightStorageService(repository, new ObjectMapper());
        User user = new User(
                "user@example.com",
                "DataForge User",
                "$2a$10$examplehash",
                "ROLE_USER",
                true,
                Instant.parse("2026-05-11T12:00:00Z")
        );
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
    }

    @Test
    void replaceInsightUpdatesExistingUnavailableRowInsteadOfDeletingAndInserting() {
        DatasetAiInsight existingInsight = new DatasetAiInsight(
                dataset,
                AiInsightGenerationStatus.UNAVAILABLE,
                "llama3:latest",
                "Previous fallback.",
                "[]",
                "[]",
                "[]",
                "Previous Ollama failure",
                Instant.parse("2026-05-11T13:15:00Z")
        );
        AiInsightContent content = new AiInsightContent(
                "Fresh generated insight.",
                List.of("Potential issue"),
                List.of("Review completeness"),
                List.of("Null-rate table")
        );

        when(repository.findByDataset(dataset)).thenReturn(Optional.of(existingInsight));
        when(repository.save(same(existingInsight))).thenReturn(existingInsight);

        DatasetAiInsight savedInsight = service.replaceInsight(
                dataset,
                AiInsightGenerationStatus.GENERATED,
                "llama3:latest",
                content,
                null
        );

        assertThat(savedInsight).isSameAs(existingInsight);
        assertThat(savedInsight.getGenerationStatus()).isEqualTo(AiInsightGenerationStatus.GENERATED);
        assertThat(savedInsight.getDatasetDescription()).isEqualTo("Fresh generated insight.");
        assertThat(savedInsight.getPotentialIssuesJson()).contains("Potential issue");
        assertThat(savedInsight.getSuggestedAnalysesJson()).contains("Review completeness");
        assertThat(savedInsight.getSuggestedVisualizationsJson()).contains("Null-rate table");
        assertThat(savedInsight.getErrorMessage()).isNull();

        verify(repository, never()).deleteByDataset(dataset);
        verify(repository).save(existingInsight);
    }
}
