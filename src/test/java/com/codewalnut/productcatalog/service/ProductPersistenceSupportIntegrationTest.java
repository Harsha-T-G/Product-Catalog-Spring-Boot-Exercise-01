package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ProductPersistenceSupportIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private ProductPersistenceSupport productPersistenceSupport;

    @Autowired
    private com.codewalnut.productcatalog.repository.ProductRepository productRepository;

    @BeforeEach
    void cleanProducts() {
        productRepository.deleteAll();
    }

    @Test
    void givenDuplicateSku_whenSaveAndFlush_thenThrowsDuplicateSkuException() {
        productPersistenceSupport.saveAndFlush(newProduct("SKU-A"), "SKU-A");

        DuplicateSkuException exception = assertThrows(
                DuplicateSkuException.class,
                () -> productPersistenceSupport.saveAndFlush(newProduct("sku-a"), "sku-a"));

        assertEquals("Product with SKU already exists: sku-a", exception.getMessage());
    }

    @Test
    void givenNegativeStock_whenSaveAndFlush_thenRethrowsDataIntegrityViolationException() {
        ProductEntity invalid = new ProductEntity(
                UUID.randomUUID(), "BAD-1", "Bad", "General", new BigDecimal("1.00"), -1, true);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> productPersistenceSupport.saveAndFlush(invalid, "BAD-1"));
    }

    private ProductEntity newProduct(String sku) {
        return new ProductEntity(
                UUID.randomUUID(), sku, "Name", "General", new BigDecimal("19.99"), 5, true);
    }
}
