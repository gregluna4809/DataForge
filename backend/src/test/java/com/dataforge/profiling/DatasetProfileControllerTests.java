package com.dataforge.profiling;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.datasets.DatasetStatus;
import com.dataforge.datasets.dto.DatasetResponse;
import com.dataforge.datasets.dto.DatasetResponse.UploadedByResponse;
import com.dataforge.profiling.dto.ColumnProfileResponse;
import com.dataforge.profiling.dto.DatasetProfileResponse;
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

@WebMvcTest(DatasetProfileController.class)
class DatasetProfileControllerTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetProfileService datasetProfileService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void authenticatedGetDatasetProfileReturnsStoredProfile() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        DatasetProfileResponse response = new DatasetProfileResponse(
                datasetResponse(datasetId),
                List.of(new ColumnProfileResponse(
                        "status",
                        1,
                        1,
                        49,
                        2,
                        InferredDataType.BOOLEAN,
                        List.of(new MostCommonValue("true", 40), new MostCommonValue("false", 9))
                ))
        );
        when(datasetProfileService.getProfile(USER_EMAIL, datasetId)).thenReturn(response);

        mockMvc.perform(get("/api/datasets/{datasetId}/profile", datasetId).with(user(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(datasetId.toString()))
                .andExpect(jsonPath("$.columns[0].columnName").value("status"))
                .andExpect(jsonPath("$.columns[0].nullCount").value(1))
                .andExpect(jsonPath("$.columns[0].inferredDataType").value("BOOLEAN"))
                .andExpect(jsonPath("$.columns[0].mostCommonValues[0].value").value("true"))
                .andExpect(jsonPath("$.columns[0].mostCommonValues[0].count").value(40));

        verify(datasetProfileService).getProfile(USER_EMAIL, datasetId);
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
