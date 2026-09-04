package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest extends PostgreSqlTestSupport {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanProducts() {
        productRepository.deleteAll();
    }

    @Test
    void givenNewProduct_whenSaveAndFindById_thenReturnsPersistedEntity() {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductEntity product = newProduct(id, "SKU-001", "Alpha", "General", 10);

        // Act
        productRepository.saveAndFlush(product);
        ProductEntity found = productRepository.findById(id).orElseThrow();

        // Assert
        assertEquals("SKU-001", found.getSku());
        assertEquals("Alpha", found.getName());
    }

    @Test
    void givenExistingSku_whenSaveDuplicateIgnoringCase_thenThrowsDataIntegrityViolationException() {
        // Arrange
        productRepository.saveAndFlush(newProduct(UUID.randomUUID(), "ABC-001", "First", "General", 5));

        // Act & Assert
        assertThrows(
                DataIntegrityViolationException.class,
                () -> productRepository.saveAndFlush(
                        newProduct(UUID.randomUUID(), "abc-001", "Second", "General", 5)));
    }

    @Test
    void givenNonPositivePrice_whenSave_thenThrowsDataIntegrityViolationException() {
        ProductEntity invalidPrice = new ProductEntity(
                UUID.randomUUID(), "PRICE-1", "Bad", "General", new BigDecimal("0.00"), 1, true);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(invalidPrice));
    }

    @Test
    void givenNegativeStock_whenSave_thenThrowsDataIntegrityViolationException() {
        ProductEntity invalidStock = new ProductEntity(
                UUID.randomUUID(), "STOCK-1", "Bad", "General", new BigDecimal("1.00"), -1, true);

        assertThrows(DataIntegrityViolationException.class, () -> productRepository.saveAndFlush(invalidStock));
    }

    @Test
    void givenProductsWithDifferentCategories_whenFindByCategoryIgnoreCase_thenReturnsMatches() {
        // Arrange
        productRepository.save(newProduct(UUID.randomUUID(), "SKU-A", "A", "Electronics", 5));
        productRepository.save(newProduct(UUID.randomUUID(), "SKU-B", "B", "Books", 5));
        productRepository.flush();

        // Act
        List<ProductEntity> matches = productRepository.findByCategoryIgnoreCase("electronics");

        // Assert
        assertEquals(1, matches.size());
        assertEquals("SKU-A", matches.get(0).getSku());
    }

    @Test
    void givenMixedStockLevels_whenFindLowStockActiveProducts_thenReturnsOnlyActiveWithinThreshold() {
        // Arrange
        productRepository.save(newProduct(UUID.randomUUID(), "LOW-1", "Low", "General", 2, true));
        productRepository.save(newProduct(UUID.randomUUID(), "LOW-2", "Inactive", "General", 1, false));
        productRepository.save(newProduct(UUID.randomUUID(), "LOW-3", "Healthy", "General", 10, true));
        productRepository.flush();

        // Act — test profile low-stock threshold is 2
        List<ProductEntity> lowStock = productRepository.findByActiveTrueAndStockQuantityLessThanEqual(2);

        // Assert
        assertEquals(1, lowStock.size());
        assertEquals("LOW-1", lowStock.get(0).getSku());
    }

    @Test
    void givenMultipleProducts_whenFindAllWithPageAndSort_thenReturnsExpectedSlice() {
        // Arrange
        productRepository.save(newProduct(UUID.randomUUID(), "SKU-C", "Charlie", "General", 5));
        productRepository.save(newProduct(UUID.randomUUID(), "SKU-A", "Alpha", "General", 5));
        productRepository.save(newProduct(UUID.randomUUID(), "SKU-B", "Bravo", "General", 5));
        productRepository.flush();

        // Act
        Page<ProductEntity> page = productRepository.findAll(
                PageRequest.of(0, 2, Sort.by("name").ascending()));

        // Assert
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals("Alpha", page.getContent().get(0).getName());
        assertEquals("Bravo", page.getContent().get(1).getName());
    }

    @Test
    void givenNewProduct_whenSavedAndUpdated_thenTimestampsAndVersionAreMaintained() throws InterruptedException {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductEntity product = newProduct(id, "SKU-TIME", "Timed", "General", 5);
        productRepository.saveAndFlush(product);

        // Act
        ProductEntity persisted = productRepository.findById(id).orElseThrow();
        Long versionBeforeUpdate = persisted.getVersion();
        Thread.sleep(5);
        persisted.applyRequestFields(
                persisted.getSku(),
                "Updated",
                persisted.getCategory(),
                persisted.getPrice(),
                persisted.getStockQuantity(),
                persisted.isActive());
        productRepository.saveAndFlush(persisted);
        ProductEntity updated = productRepository.findById(id).orElseThrow();

        // Assert
        assertNotNull(updated.getCreatedAt());
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().compareTo(updated.getCreatedAt()) >= 0);
        assertNotNull(updated.getVersion());
        assertEquals(versionBeforeUpdate + 1, updated.getVersion());
    }

    private ProductEntity newProduct(UUID id, String sku, String name, String category, int stock) {
        return newProduct(id, sku, name, category, stock, true);
    }

    private ProductEntity newProduct(
            UUID id, String sku, String name, String category, int stock, boolean active) {
        return new ProductEntity(id, sku, name, category, new BigDecimal("19.99"), stock, active);
    }
}
