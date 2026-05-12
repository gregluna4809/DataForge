package com.dataforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dataforge.datasets.DatasetRepository;
import com.dataforge.datasets.DatasetColumnRepository;
import com.dataforge.datasets.DatasetPreviewRowRepository;
import com.dataforge.users.UserRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class DataForgeApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DatasetRepository datasetRepository;

    @MockBean
    private DatasetColumnRepository datasetColumnRepository;

    @MockBean
    private DatasetPreviewRowRepository datasetPreviewRowRepository;

    @Test
    void contextLoads() {
    }
}
