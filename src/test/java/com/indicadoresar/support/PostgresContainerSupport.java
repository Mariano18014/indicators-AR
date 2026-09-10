package com.indicadoresar.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class PostgresContainerSupport {

    @Container
    static final PostgreSQLContainer<?> postgres = createContainer();

    private static PostgreSQLContainer<?> createContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine");
        container.withDatabaseName("indicadoresar_test");
        container.withUsername("test");
        container.withPassword("test");
        container.withReuse(true);
        return container;
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registerDatasourceUrl(registry);
        registerDatasourceUsername(registry);
        registerDatasourcePassword(registry);
    }

    private static void registerDatasourceUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    private static void registerDatasourceUsername(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", postgres::getUsername);
    }

    private static void registerDatasourcePassword(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
