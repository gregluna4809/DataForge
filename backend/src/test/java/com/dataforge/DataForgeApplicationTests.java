package com.dataforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.dataforge.datasets.DatasetRepository;
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

    @Test
    void contextLoads() {
    }
}
