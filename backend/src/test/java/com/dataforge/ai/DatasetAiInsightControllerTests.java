package com.dataforge.ai;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.ai.dto.DatasetAiInsightResponse;
import com.dataforge.datasets.DatasetStatus;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.datasets.dto.DatasetResponse.UploadedByResponse;
import com.dataforge.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DatasetAiInsightController.class)
class DatasetAiInsightControllerTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetAiInsightService datasetAiInsightService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedGetDatasetInsightsReturnsGeneratedInsights() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        DatasetAiInsightResponse response = new DatasetAiInsightResponse(
                datasetResponse(datasetId),
                AiInsightGenerationStatus.GENERATED,
                "llama3.1",
                "Customer import data with identifiers and names.",
                List.of("Possible identifier column"),
                List.of("Analyze customer counts by status"),
                List.of("Bar chart by status"),
                null,
                Instant.parse("2026-05-11T13:15:00Z")
        );
        when(datasetAiInsightService.getInsights(USER_EMAIL, datasetId)).thenReturn(response);

        mockMvc.perform(get("/api/datasets/{datasetId}/insights", datasetId).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(datasetId.toString()))
                .andExpect(jsonPath("$.generationStatus").value("GENERATED"))
                .andExpect(jsonPath("$.modelName").value("llama3.1"))
                .andExpect(jsonPath("$.datasetDescription").value("Customer import data with identifiers and names."))
                .andExpect(jsonPath("$.potentialIssues[0]").value("Possible identifier column"))
                .andExpect(jsonPath("$.suggestedAnalyses[0]").value("Analyze customer counts by status"))
                .andExpect(jsonPath("$.suggestedVisualizations[0]").value("Bar chart by status"));

        verify(datasetAiInsightService).getInsights(USER_EMAIL, datasetId);
    }

    private DatasetResponse datasetResponse(UUID datasetId) {
        return new DatasetResponse(
                datasetId,
                "Customer Imports",
                "customers.csv",
                "Initial customer import",
                Instant.parse("2026-05-11T12:00:00Z"),
                100,
                8,
                4096,
                null,
                null,
                DatasetStatus.UPLOADED,
                new UploadedByResponse(
                        UUID.fromString("22d91774-dd28-443f-b266-9f0d03820f92"),
                        USER_EMAIL,
                        "DataForge User"
                )
        );
    }
}
