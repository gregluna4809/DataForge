package com.dataforge.datasets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dataforge.security.JwtService;
import com.dataforge.users.User;
import com.dataforge.users.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
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
}
