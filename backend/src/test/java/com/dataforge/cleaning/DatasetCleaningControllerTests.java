package com.dataforge.cleaning;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.cleaning.DatasetCleaningService.CleanedDatasetDownload;
import com.dataforge.cleaning.dto.DatasetCleaningReportResponse;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DatasetCleaningController.class)
class DatasetCleaningControllerTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetCleaningService datasetCleaningService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedCleanDatasetReturnsCleaningReport() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetCleaningService.cleanDataset(USER_EMAIL, datasetId)).thenReturn(report(datasetId));

        mockMvc.perform(post("/api/datasets/{datasetId}/clean", datasetId)
                        .with(user(USER_EMAIL))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(datasetId.toString()))
                .andExpect(jsonPath("$.rowsRead").value(4))
                .andExpect(jsonPath("$.rowsWritten").value(2))
                .andExpect(jsonPath("$.duplicateRowsRemoved").value(1))
                .andExpect(jsonPath("$.emptyRowsRemoved").value(1))
                .andExpect(jsonPath("$.columnsRenamed[0].cleanedName").value("customer_id"))
                .andExpect(jsonPath("$.cleaningRulesApplied[0]").value("TRIM_WHITESPACE"));

        verify(datasetCleaningService).cleanDataset(USER_EMAIL, datasetId);
    }

    @Test
    void authenticatedGetCleaningReportReturnsReport() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetCleaningService.getCleaningReport(USER_EMAIL, datasetId)).thenReturn(report(datasetId));

        mockMvc.perform(get("/api/datasets/{datasetId}/cleaning-report", datasetId).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleanedFilename").value("customers-cleaned.csv"));

        verify(datasetCleaningService).getCleaningReport(USER_EMAIL, datasetId);
    }

    @Test
    void authenticatedDownloadCleanedReturnsCsvAttachment() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        byte[] bytes = "customer_id,name\n1,Ada\n".getBytes();
        when(datasetCleaningService.cleanedDatasetDownload(USER_EMAIL, datasetId))
                .thenReturn(new CleanedDatasetDownload(
                        "customers-cleaned.csv",
                        bytes.length,
                        new ByteArrayResource(bytes)
                ));

        mockMvc.perform(get("/api/datasets/{datasetId}/download-cleaned", datasetId).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"customers-cleaned.csv\""))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, bytes.length));

        verify(datasetCleaningService).cleanedDatasetDownload(USER_EMAIL, datasetId);
    }

    private DatasetCleaningReportResponse report(UUID datasetId) {
        return new DatasetCleaningReportResponse(
                datasetResponse(datasetId),
                "customers-cleaned.csv",
                512,
                4,
                2,
                1,
                1,
                List.of(new ColumnRename("Customer ID", "customer_id")),
                List.of(
                        CleaningRule.TRIM_WHITESPACE,
                        CleaningRule.NORMALIZE_BLANK_VALUES,
                        CleaningRule.NORMALIZE_COLUMN_NAMES_TO_SNAKE_CASE,
                        CleaningRule.REMOVE_FULLY_EMPTY_ROWS,
                        CleaningRule.REMOVE_DUPLICATE_ROWS
                ),
                Instant.parse("2026-05-11T14:00:00Z")
        );
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
                "stored.csv",
                Instant.parse("2026-05-11T12:30:00Z"),
                DatasetStatus.UPLOADED,
                new UploadedByResponse(
                        UUID.fromString("22d91774-dd28-443f-b266-9f0d03820f92"),
                        USER_EMAIL,
                        "DataForge User"
                )
        );
    }
}
