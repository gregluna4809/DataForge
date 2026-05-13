package com.dataforge.datasets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.ai.AiInsightGenerationStatus;
import com.dataforge.ai.DatasetAiInsight;
import com.dataforge.ai.DatasetAiInsightRepository;
import com.dataforge.cleaning.CleaningRule;
import com.dataforge.cleaning.ColumnRename;
import com.dataforge.cleaning.DatasetCleaningReport;
import com.dataforge.cleaning.DatasetCleaningReportRepository;
import com.dataforge.security.JwtService;
import com.dataforge.profiling.DatasetColumnProfile;
import com.dataforge.profiling.DatasetColumnProfileRepository;
import com.dataforge.profiling.ColumnProfileResult;
import com.dataforge.profiling.InferredDataType;
import com.dataforge.profiling.MostCommonValue;
import com.dataforge.quality.ColumnQualityResult;
import com.dataforge.quality.DatasetColumnQualityScore;
import com.dataforge.quality.DatasetQualityScore;
import com.dataforge.quality.DatasetQualityScoreRepository;
import com.dataforge.quality.QualityIssueSummary;
import com.dataforge.quality.QualityIssueType;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "dataforge.uploads.directory=target/test-uploads",
        "dataforge.uploads.max-file-size-bytes=1048576"
})
@AutoConfigureMockMvc
class DatasetSecurityIntegrationTests {

    private static final String USER_EMAIL = "user@example.com";

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetRepository datasetRepository;

    @MockBean
    private DatasetColumnRepository datasetColumnRepository;

    @MockBean
    private DatasetPreviewRowRepository datasetPreviewRowRepository;

    @MockBean
    private DatasetColumnProfileRepository datasetColumnProfileRepository;

    @MockBean
    private DatasetQualityScoreRepository datasetQualityScoreRepository;

    @MockBean
    private DatasetAiInsightRepository datasetAiInsightRepository;

    @MockBean
    private DatasetCleaningReportRepository datasetCleaningReportRepository;

    @MockBean
    private UserRepository userRepository;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User(
                USER_EMAIL,
                "DataForge User",
                "$2a$10$examplehash",
                "ROLE_USER",
                true,
                Instant.parse("2026-05-11T12:00:00Z")
        );
        ReflectionTestUtils.setField(user, "id", UUID.fromString("22d91774-dd28-443f-b266-9f0d03820f92"));

        token = jwtService.generateToken(user);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(datasetQualityScoreRepository.save(any(DatasetQualityScore.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(datasetCleaningReportRepository.save(any(DatasetCleaningReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void authenticatedListDatasetsWithJwtReturnsUserDatasets() throws Exception {
        Dataset dataset = dataset();
        when(datasetRepository.findByUploadedByOrderByUploadTimestampDesc(user)).thenReturn(List.of(dataset));

        mockMvc.perform(get("/api/datasets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Customer Imports"))
                .andExpect(jsonPath("$[0].uploadedBy.email").value(USER_EMAIL));
    }

    @Test
    void authenticatedCreateDatasetWithJwtCreatesMetadata() throws Exception {
        when(datasetRepository.save(any(Dataset.class))).thenAnswer(invocation -> {
            Dataset dataset = invocation.getArgument(0);
            ReflectionTestUtils.setField(dataset, "id", UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55"));
            return dataset;
        });

        mockMvc.perform(post("/api/datasets")
                        .header("Authorization", "Bearer " + token)
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
                .andExpect(jsonPath("$.name").value("Customer Imports"))
                .andExpect(jsonPath("$.status").value("METADATA_CREATED"))
                .andExpect(jsonPath("$.uploadedBy.email").value(USER_EMAIL));
    }

    @Test
    void authenticatedUploadCsvWithJwtStoresFileAndUpdatesDataset() throws Exception {
        Dataset dataset = dataset();
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetRepository.save(any(Dataset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.csv",
                "text/csv",
                "id,name\n1,Ada\n".getBytes()
        );

        mockMvc.perform(multipart("/api/datasets/{datasetId}/upload", dataset.getId())
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.originalFilename").value("customers.csv"))
                .andExpect(jsonPath("$.storedFilename").isNotEmpty())
                .andExpect(jsonPath("$.fileSizeBytes").value(14))
                .andExpect(jsonPath("$.status").value("UPLOADED"));
    }

    @Test
    void authenticatedPreviewWithJwtReturnsOnlyOwnedDatasetPreview() throws Exception {
        Dataset dataset = dataset();
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetColumnRepository.findByDatasetOrderByPositionAsc(dataset)).thenReturn(List.of(
                new DatasetColumn(dataset, "id", 0),
                new DatasetColumn(dataset, "name", 1)
        ));
        when(datasetPreviewRowRepository.findByDatasetOrderByPositionAsc(dataset)).thenReturn(List.of(
                new DatasetPreviewRow(dataset, 0, "[\"1\",\"Ada\"]")
        ));

        mockMvc.perform(get("/api/datasets/{datasetId}/preview", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.columnNames[0]").value("id"))
                .andExpect(jsonPath("$.rows[0][1]").value("Ada"));
    }

    @Test
    void authenticatedProfileWithJwtReturnsOnlyOwnedDatasetProfile() throws Exception {
        Dataset dataset = dataset();
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetColumnProfileRepository.findByDatasetOrderByColumnPositionAsc(dataset)).thenReturn(List.of(
                new DatasetColumnProfile(
                        dataset,
                        new ColumnProfileResult(
                                "name",
                                1,
                                0,
                                1,
                                1,
                                InferredDataType.TEXT,
                                List.of(new MostCommonValue("Ada", 1))
                        ),
                        "[{\"value\":\"Ada\",\"count\":1}]",
                        Instant.parse("2026-05-11T12:45:00Z")
                )
        ));

        mockMvc.perform(get("/api/datasets/{datasetId}/profile", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.columns[0].columnName").value("name"))
                .andExpect(jsonPath("$.columns[0].inferredDataType").value("TEXT"))
                .andExpect(jsonPath("$.columns[0].mostCommonValues[0].value").value("Ada"));
    }

    @Test
    void authenticatedQualityWithJwtReturnsOnlyOwnedDatasetQuality() throws Exception {
        Dataset dataset = dataset();
        DatasetQualityScore qualityScore = new DatasetQualityScore(
                dataset,
                98.5,
                "[{\"type\":\"POSSIBLE_IDENTIFIER_COLUMN\",\"message\":\"Column values are nearly all unique and may represent an identifier\"}]",
                Instant.parse("2026-05-11T13:00:00Z")
        );
        qualityScore.addColumnScore(new DatasetColumnQualityScore(
                qualityScore,
                new ColumnQualityResult(
                        "id",
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
                ),
                "[{\"type\":\"POSSIBLE_IDENTIFIER_COLUMN\",\"message\":\"Column values are nearly all unique and may represent an identifier\"}]"
        ));

        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetQualityScoreRepository.findByDataset(dataset)).thenReturn(Optional.of(qualityScore));

        mockMvc.perform(get("/api/datasets/{datasetId}/quality", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.overallScore").value(98.5))
                .andExpect(jsonPath("$.issueSummaries[0].type").value("POSSIBLE_IDENTIFIER_COLUMN"))
                .andExpect(jsonPath("$.columns[0].columnName").value("id"));
    }

    @Test
    void authenticatedInsightsWithJwtReturnsOnlyOwnedDatasetInsights() throws Exception {
        Dataset dataset = dataset();
        DatasetAiInsight insight = new DatasetAiInsight(
                dataset,
                AiInsightGenerationStatus.GENERATED,
                "llama3.1",
                "Customer import data with identifiers and names.",
                "[\"Possible identifier column\"]",
                "[\"Analyze customer counts by status\"]",
                "[\"Bar chart by status\"]",
                null,
                Instant.parse("2026-05-11T13:15:00Z")
        );

        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetAiInsightRepository.findByDataset(dataset)).thenReturn(Optional.of(insight));

        mockMvc.perform(get("/api/datasets/{datasetId}/insights", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.generationStatus").value("GENERATED"))
                .andExpect(jsonPath("$.datasetDescription").value("Customer import data with identifiers and names."))
                .andExpect(jsonPath("$.potentialIssues[0]").value("Possible identifier column"));
    }

    @Test
    void authenticatedCleanWithJwtReturnsOnlyOwnedDatasetCleaningReport() throws Exception {
        Dataset dataset = dataset();
        Path uploadPath = Path.of("target/test-uploads/security-clean-source.csv").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath.getParent());
        Files.writeString(uploadPath, "Customer ID,Name\n 1 , Ada \n1,Ada\n,\n");
        dataset.markUploaded(
                "customers.csv",
                "security-clean-source.csv",
                uploadPath.toString(),
                "text/csv",
                Files.size(uploadPath),
                Instant.parse("2026-05-11T13:30:00Z")
        );

        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));

        mockMvc.perform(post("/api/datasets/{datasetId}/clean", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.rowsRead").value(3))
                .andExpect(jsonPath("$.rowsWritten").value(1))
                .andExpect(jsonPath("$.duplicateRowsRemoved").value(1))
                .andExpect(jsonPath("$.emptyRowsRemoved").value(1))
                .andExpect(jsonPath("$.columnsRenamed[0].cleanedName").value("customer_id"));
    }

    @Test
    void authenticatedCleaningReportWithJwtReturnsOnlyOwnedDatasetReport() throws Exception {
        Dataset dataset = dataset();
        DatasetCleaningReport report = cleaningReport(dataset);
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetCleaningReportRepository.findByDataset(dataset)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/datasets/{datasetId}/cleaning-report", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataset.id").value(dataset.getId().toString()))
                .andExpect(jsonPath("$.cleanedFilename").value("customers-cleaned.csv"))
                .andExpect(jsonPath("$.cleaningRulesApplied[0]").value("TRIM_WHITESPACE"));
    }

    @Test
    void authenticatedDownloadCleanedWithJwtReturnsOnlyOwnedCleanedCsv() throws Exception {
        Dataset dataset = dataset();
        Path cleanedPath = Path.of("target/test-uploads/security-cleaned.csv").toAbsolutePath().normalize();
        Files.createDirectories(cleanedPath.getParent());
        Files.writeString(cleanedPath, "customer_id,name\n1,Ada\n");
        DatasetCleaningReport report = new DatasetCleaningReport(
                dataset,
                "customers-cleaned.csv",
                cleanedPath.toString(),
                Files.size(cleanedPath),
                1,
                1,
                0,
                0,
                "[]",
                "[\"TRIM_WHITESPACE\"]",
                Instant.parse("2026-05-11T14:00:00Z")
        );
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));
        when(datasetCleaningReportRepository.findByDataset(dataset)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/datasets/{datasetId}/download-cleaned", dataset.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"customers-cleaned.csv\""));
    }

    @Test
    void insightsRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/insights", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void cleanRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/datasets/{datasetId}/clean", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void cleaningReportRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/cleaning-report", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadCleanedRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/download-cleaned", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void qualityRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/quality", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void profileRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/profile", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void previewRejectsDatasetOwnedByAnotherUser() throws Exception {
        UUID datasetId = UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55");
        when(datasetRepository.findByIdAndUploadedBy(datasetId, user)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/datasets/{datasetId}/preview", datasetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadRejectsNonCsvFiles() throws Exception {
        Dataset dataset = dataset();
        when(datasetRepository.findByIdAndUploadedBy(dataset.getId(), user)).thenReturn(Optional.of(dataset));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.txt",
                "text/plain",
                "not csv".getBytes()
        );

        mockMvc.perform(multipart("/api/datasets/{datasetId}/upload", dataset.getId())
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only .csv files are supported"));
    }

    @Test
    void unauthenticatedListDatasetsIsRejected() throws Exception {
        mockMvc.perform(get("/api/datasets"))
                .andExpect(status().isUnauthorized());
    }

    private Dataset dataset() {
        Dataset dataset = new Dataset(
                "Customer Imports",
                "customers.csv",
                "Initial customer import",
                100,
                8,
                4096,
                DatasetStatus.METADATA_CREATED,
                user,
                Instant.parse("2026-05-11T12:30:00Z")
        );
        ReflectionTestUtils.setField(dataset, "id", UUID.fromString("3a28a4a5-3137-4a67-a7d4-379cc1efbd55"));
        return dataset;
    }

    private DatasetCleaningReport cleaningReport(Dataset dataset) {
        return new DatasetCleaningReport(
                dataset,
                "customers-cleaned.csv",
                "target/test-uploads/customers-cleaned.csv",
                512,
                4,
                2,
                1,
                1,
                "[{\"originalName\":\"Customer ID\",\"cleanedName\":\"customer_id\"}]",
                "[\"TRIM_WHITESPACE\",\"NORMALIZE_BLANK_VALUES\",\"NORMALIZE_COLUMN_NAMES_TO_SNAKE_CASE\",\"REMOVE_FULLY_EMPTY_ROWS\",\"REMOVE_DUPLICATE_ROWS\"]",
                Instant.parse("2026-05-11T14:00:00Z")
        );
    }
}
