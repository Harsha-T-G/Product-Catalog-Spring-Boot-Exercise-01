package com.codewalnut.productcatalog;

import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest extends PostgreSqlTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void givenPostgreSqlDatabase_whenApplicationStarts_thenFlywayAppliesProductsMigration() {
        // Assert — Hibernate ddl-auto=validate requires schema created by Flyway
        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1'", Integer.class);
        assertEquals(1, migrationCount);

        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'products'",
                Integer.class);
        assertEquals(1, tableCount);

        Integer skuIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'products' AND indexname = 'products_sku_unique_lower'",
                Integer.class);
        assertEquals(1, skuIndexCount);
    }

    @Test
    void givenAppliedMigration_whenInspectingProductsTable_thenRequiredColumnsExist() {
        Integer columnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'products'
                  AND column_name IN (
                    'id', 'sku', 'name', 'category', 'price', 'stock_quantity',
                    'active', 'created_at', 'updated_at', 'version'
                  )
                """,
                Integer.class);
        assertEquals(10, columnCount);

        Integer priceCheckCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.check_constraints
                WHERE constraint_name = 'products_price_positive'
                """,
                Integer.class);
        assertTrue(priceCheckCount >= 1);
    }
}
