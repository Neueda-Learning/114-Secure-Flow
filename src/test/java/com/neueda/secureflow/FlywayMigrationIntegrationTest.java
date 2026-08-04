package com.neueda.secureflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:secureflow-migration;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class FlywayMigrationIntegrationTest {
    @Test
    void flywaySchemaMatchesJpaModel() {
        // If Flyway SQL and JPA mappings disagree, the application context cannot start.
    }
}
