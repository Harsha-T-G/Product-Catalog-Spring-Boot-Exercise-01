package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.dto.StockAdjustmentRequest;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.InsufficientStockException;
import com.codewalnut.productcatalog.exception.ProductLimitReachedException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanProducts() {
        productRepository.deleteAll();
    }

    @Test
    void givenValidRequest_whenCreateUpdateAndDelete_thenPersistsExpectedState() {
        // Arrange
        ProductRequest createRequest = validRequest("SVC-001");

        // Act
        ProductResponse created = productService.create(createRequest);
        ProductResponse retrieved = productService.findById(created.getId());

        ProductRequest updateRequest = validRequest("SVC-002");
        updateRequest.setName("Updated Name");
        ProductResponse updated = productService.update(created.getId(), updateRequest);

        productService.delete(created.getId());

        // Assert
        assertEquals("SVC-001", retrieved.getSku());
        assertEquals("SVC-002", updated.getSku());
        assertEquals("Updated Name", updated.getName());
        assertThrows(ProductNotFoundException.class, () -> productService.findById(created.getId()));
    }

    @Test
    void givenExistingSku_whenCreateDuplicateIgnoringCase_thenThrowsDuplicateSkuException() {
        // Arrange
        productService.create(validRequest("DUP-001"));

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.create(validRequest("dup-001")));
        assertEquals(1, productRepository.count());
    }

    @Test
    void givenCatalogAtMaximum_whenCreateAnotherProduct_thenThrowsProductLimitReachedException() {
        // Arrange — test profile maximum-products is 20
        for (int index = 0; index < 20; index++) {
            productService.create(validRequest("MAX-" + index));
        }

        // Act & Assert
        assertThrows(ProductLimitReachedException.class, () -> productService.create(validRequest("MAX-OVER")));
        assertEquals(20, productRepository.count());
    }

    @Test
    void givenExistingProduct_whenAdjustStockUp_thenPersistsIncreasedQuantity() {
        // Arrange
        ProductResponse created = productService.create(validRequest("STK-ADD"));
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(5);

        // Act
        ProductResponse updated = productService.adjustStock(created.getId(), request);

        // Assert
        assertEquals(15, updated.getStockQuantity());
        assertEquals(15, productRepository.findById(created.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    void givenExistingProduct_whenAdjustStockDown_thenPersistsDecreasedQuantity() {
        // Arrange
        ProductResponse created = productService.create(validRequest("STK-REM"));
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(-3);

        // Act
        ProductResponse updated = productService.adjustStock(created.getId(), request);

        // Assert
        assertEquals(7, updated.getStockQuantity());
        assertEquals(7, productRepository.findById(created.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    void givenInsufficientStock_whenAdjustStockDown_thenThrowsAndPreservesStoredQuantity() {
        // Arrange
        ProductResponse created = productService.create(validRequest("STK-FAIL"));
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(-100);

        // Act & Assert
        assertThrows(
                InsufficientStockException.class,
                () -> productService.adjustStock(created.getId(), request));
        assertEquals(10, productRepository.findById(created.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    void givenMissingProduct_whenFindById_thenThrowsProductNotFoundException() {
        assertThrows(ProductNotFoundException.class, () -> productService.findById(UUID.randomUUID()));
    }

    @Test
    void givenStaleVersion_whenSecondSaveCommits_thenThrowsOptimisticLockingFailureException() {
        ProductResponse created = productService.create(validRequest("OPT-001"));
        ProductEntity current = productRepository.findById(created.getId()).orElseThrow();
        ProductEntity stale = productRepository.findById(created.getId()).orElseThrow();

        current.applyRequestFields(
                current.getSku(), "Updated", current.getCategory(), current.getPrice(), current.getStockQuantity(), true);
        productRepository.saveAndFlush(current);

        stale.adjustStockBy(-1);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> productRepository.saveAndFlush(stale));
    }

    private ProductRequest validRequest(String sku) {
        ProductRequest request = new ProductRequest();
        request.setSku(sku);
        request.setName("Service Product");
        request.setCategory("General");
        request.setPrice(new BigDecimal("12.50"));
        request.setStockQuantity(10);
        request.setActive(true);
        return request;
    }
}
