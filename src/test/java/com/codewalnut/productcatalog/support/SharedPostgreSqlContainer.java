package com.codewalnut.productcatalog.support;

import org.testcontainers.containers.PostgreSQLContainer;

final class SharedPostgreSqlContainer {

    private static final PostgreSQLContainer<?> CONTAINER = createAndStart();

    private SharedPostgreSqlContainer() {
    }

    static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }

    private static PostgreSQLContainer<?> createAndStart() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("product_catalog_test")
                .withUsername("product_catalog_test")
                .withPassword("product_catalog_test");
        container.start();
        return container;
    }
}
