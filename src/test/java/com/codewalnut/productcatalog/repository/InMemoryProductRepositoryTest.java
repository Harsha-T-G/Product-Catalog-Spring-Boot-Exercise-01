package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryProductRepositoryTest {

    private InMemoryProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    @Test
    void givenNewProduct_whenSave_thenFindByIdReturnsSavedProduct() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product product = sampleProduct(id, "SKU-001");

        // Act
        repository.save(product);

        // Assert
        Optional<Product> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("SKU-001", found.get().getSku());
    }

    @Test
    void givenSavedProducts_whenFindAll_thenReturnsUnmodifiableCopy() {
        // Arrange
        repository.save(sampleProduct(UUID.randomUUID(), "SKU-001"));
        repository.save(sampleProduct(UUID.randomUUID(), "SKU-002"));

        // Act
        List<Product> products = repository.findAll();

        // Assert
        assertEquals(2, products.size());
        assertNotSame(products, repository.findAll());
    }

    @Test
    void givenExistingSku_whenExistsBySkuIgnoreCase_thenReturnsTrueForDifferentCasing() {
        // Arrange
        repository.save(sampleProduct(UUID.randomUUID(), "ABC-001"));

        // Act & Assert
        assertTrue(repository.existsBySkuIgnoreCase("abc-001"));
    }

    @Test
    void givenExistingProduct_whenDeleteById_thenProductIsRemoved() {
        // Arrange
        UUID id = UUID.randomUUID();
        repository.save(sampleProduct(id, "SKU-001"));

        // Act
        repository.deleteById(id);

        // Assert
        assertFalse(repository.findById(id).isPresent());
        assertEquals(0, repository.count());
    }

    @Test
    void givenExistingSku_whenExistsBySkuIgnoreCaseExcludingId_thenIgnoresSameProduct() {
        // Arrange
        UUID id = UUID.randomUUID();
        repository.save(sampleProduct(id, "ABC-001"));

        // Act & Assert
        assertFalse(repository.existsBySkuIgnoreCaseExcludingId("abc-001", id));
        assertTrue(repository.existsBySkuIgnoreCaseExcludingId("abc-001", UUID.randomUUID()));
    }

    private Product sampleProduct(UUID id, String sku) {
        return new Product(id, sku, "Sample", "General", new BigDecimal("9.99"), 5, true);
    }
}
