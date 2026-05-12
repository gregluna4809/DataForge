package com.dataforge.quality;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.datasets.DatasetStatus;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.datasets.dto.DatasetResponse.UploadedByResponse;
import com.dataforge.quality.dto.ColumnQualityResponse;
import com.dataforge.quality.dto.DatasetQualityResponse;
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

@WebMvcTest(DatasetQualityController.class)
class DatasetQualityControllerTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetQualityService datasetQualityService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedGetDatasetQualityReturnsStoredScores() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        DatasetQualityResponse response = new DatasetQualityResponse(
                datasetResponse(datasetId),
                88.25,
                List.of(new QualityIssueSummary(
                        QualityIssueType.POSSIBLE_IDENTIFIER_COLUMN,
                        "Column values are nearly all unique and may represent an identifier"
                )),
                Instant.parse("2026-05-11T13:00:00Z"),
                List.of(new ColumnQualityResponse(
                        "customer_id",
                        0,
                        98.5,
                        0.0,
                        100.0,
                        0.0,
                        100.0,
                        List.of(new QualityIssueSummary(
                                QualityIssueType.POSSIBLE_IDENTIFIER_COLUMN,
                                "Column values are nearly all unique and may represent an identifier"
                        ))
                ))
        );
        when(datasetQualityService.getQuality(USER_EMAIL, datasetId)).thenReturn(response);

        mockMvc.perform(get("/api/datasets/{datasetId}/quality", datasetId).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(datasetId.toString()))
                .andExpect(jsonPath("$.overallScore").value(88.25))
                .andExpect(jsonPath("$.issueSummaries[0].type").value("POSSIBLE_IDENTIFIER_COLUMN"))
                .andExpect(jsonPath("$.columns[0].columnName").value("customer_id"))
                .andExpect(jsonPath("$.columns[0].qualityScore").value(98.5))
                .andExpect(jsonPath("$.columns[0].uniquenessPercentage").value(100.0));

        verify(datasetQualityService).getQuality(USER_EMAIL, datasetId);
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
