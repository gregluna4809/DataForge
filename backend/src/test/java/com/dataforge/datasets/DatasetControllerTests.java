package com.dataforge.datasets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.datasets.dto.CreateDatasetRequest;
import com.dataforge.datasets.dto.DatasetPreviewResponse;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.datasets.dto.DatasetResponse.UploadedByResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.dataforge.security.JwtService;

@WebMvcTest(DatasetController.class)
class DatasetControllerTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private DatasetPreviewService datasetPreviewService;

    @MockBean
    private DatasetUploadService datasetUploadService;

    @MockBean
    private DatasetDeletionService datasetDeletionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedListDatasetsReturnsUserDatasets() throws Exception {
        DatasetResponse dataset = datasetResponse();
        when(datasetService.listDatasetsForUser(USER_EMAIL)).thenReturn(List.of(dataset));

        mockMvc.perform(get("/api/datasets").with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dataset.id().toString()))
                .andExpect(jsonPath("$[0].name").value("Customer Imports"))
                .andExpect(jsonPath("$[0].uploadedBy.email").value(USER_EMAIL));

        verify(datasetService).listDatasetsForUser(USER_EMAIL);
    }

    @Test
    void authenticatedCreateDatasetCreatesMetadataForUser() throws Exception {
        DatasetResponse dataset = datasetResponse();
        when(datasetService.createDataset(eq(USER_EMAIL), any(CreateDatasetRequest.class))).thenReturn(dataset);

        mockMvc.perform(post("/api/datasets")
                        .with(user(USER_EMAIL))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Customer Imports",
                                  "originalFilename": "customers.csv",
                                  "description": "Initial customer import",
                                  "rowCount": 100,
                                  "columnCount": 8,
                                  "fileSizeBytes": 4096
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(dataset.id().toString()))
                .andExpect(jsonPath("$.status").value("METADATA_CREATED"))
                .andExpect(jsonPath("$.uploadedBy.email").value(USER_EMAIL));

        verify(datasetService).createDataset(eq(USER_EMAIL), any(CreateDatasetRequest.class));
    }

    @Test
    void authenticatedGetDatasetPreviewReturnsMetadataColumnsAndRows() throws Exception {
        DatasetResponse dataset = datasetResponse();
        DatasetPreviewResponse preview = new DatasetPreviewResponse(
                dataset,
                List.of("id", "name", "notes"),
                List.of(List.of("1", "Ada Lovelace", "first, customer"))
        );
        when(datasetPreviewService.getPreview(USER_EMAIL, dataset.id())).thenReturn(preview);

        mockMvc.perform(get("/api/datasets/{datasetId}/preview", dataset.id()).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.id().toString()))
                .andExpect(jsonPath("$.dataset.name").value("Customer Imports"))
                .andExpect(jsonPath("$.columnNames[0]").value("id"))
                .andExpect(jsonPath("$.columnNames[2]").value("notes"))
                .andExpect(jsonPath("$.rows[0][2]").value("first, customer"));

        verify(datasetPreviewService).getPreview(USER_EMAIL, dataset.id());
    }

    @Test
    void authenticatedDeleteDatasetReturnsNoContent() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");

        mockMvc.perform(delete("/api/datasets/{datasetId}", datasetId)
                        .with(user(USER_EMAIL))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(datasetDeletionService).deleteDataset(USER_EMAIL, datasetId);
    }

    @Test
    void unauthenticatedDeleteDatasetIsRejected() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        mockMvc.perform(delete("/api/datasets/{datasetId}", datasetId).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedListDatasetsIsRejected() throws Exception {
        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isUnauthorized());
    }

    private DatasetResponse datasetResponse() {
        return new DatasetResponse(
                UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55"),
                "Customer Imports",
                "customers.csv",
                "Initial customer import",
                Instant.parse("2026-05-11T12:00:00Z"),
                100,
                8,
                4096,
                null,
                null,
                DatasetStatus.METADATA_CREATED,
                new UploadedByResponse(
                        UUID.fromString("22d91774-dd28-443f-b266-9f0d03820f92"),
                        USER_EMAIL,
                        "DataForge User"
                )
        );
    }
}
