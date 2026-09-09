package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.dto.ProductPageResponse;
import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.dto.ProductSearchCriteria;
import com.codewalnut.productcatalog.dto.StockAdjustmentRequest;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.InsufficientStockException;
import com.codewalnut.productcatalog.exception.InvalidStockAdjustmentException;
import com.codewalnut.productcatalog.exception.ProductLimitReachedException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.mapper.ProductEntityMapper;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductPersistenceSupport productPersistenceSupport;

    private CatalogProperties catalogProperties;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        catalogProperties = new CatalogProperties();
        catalogProperties.setMaximumProducts(500);
        catalogProperties.setLowStockThreshold(5);
        catalogProperties.setDefaultPageSize(20);
        catalogProperties.setMaxPageSize(100);
        productPersistenceSupport = new ProductPersistenceSupport(productRepository);
        productService = new ProductService(
                productRepository,
                new ProductEntityMapper(),
                catalogProperties,
                new ProductPageRequestFactory(catalogProperties),
                productPersistenceSupport);
    }

    @Test
    void givenValidRequest_whenCreate_thenReturnsSavedProductResponse() {
        // Arrange
        ProductRequest request = validRequest("SKU-001");
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.saveAndFlush(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.create(request);

        // Assert
        assertNotNull(response.getId());
        assertEquals("SKU-001", response.getSku());
        verify(productRepository).saveAndFlush(any(ProductEntity.class));
    }

    @Test
    void givenDuplicateSku_whenCreate_thenThrowsDuplicateSkuException() {
        // Arrange
        ProductRequest request = validRequest("ABC-001");
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.existsBySkuIgnoreCase("ABC-001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.create(request));
        verify(productRepository, never()).saveAndFlush(any(ProductEntity.class));
    }

    @Test
    void givenDuplicateSkuDifferentCase_whenCreate_thenThrowsDuplicateSkuException() {
        // Arrange
        ProductRequest request = validRequest("abc-001");
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.existsBySkuIgnoreCase("abc-001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.create(request));
    }

    @Test
    void givenExistingId_whenFindById_thenReturnsProductResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductEntity entity = savedEntity(id, "SKU-001");
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        ProductResponse response = productService.findById(id);

        // Assert
        assertEquals(id, response.getId());
    }

    @Test
    void givenMissingId_whenFindById_thenThrowsProductNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.findById(id));
    }

    @Test
    void givenExistingProduct_whenUpdate_thenPreservesIdAndReturnsUpdatedResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductEntity existing = savedEntity(id, "SKU-001");
        ProductRequest request = validRequest("SKU-002");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("SKU-002", id)).thenReturn(false);
        when(productRepository.saveAndFlush(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.update(id, request);

        // Assert
        assertEquals(id, response.getId());
        assertEquals("SKU-002", response.getSku());

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).saveAndFlush(captor.capture());
        assertEquals(id, captor.getValue().getId());
    }

    @Test
    void givenOtherProductSku_whenUpdate_thenThrowsDuplicateSkuException() {
        // Arrange
        UUID id = UUID.randomUUID();
        ProductEntity existing = savedEntity(id, "SKU-001");
        ProductRequest request = validRequest("sku-999");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("sku-999", id)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.update(id, request));
    }

    @Test
    void givenInactiveProductRequest_whenCreate_thenActiveFlagIsPreserved() {
        // Arrange
        ProductRequest request = validRequest("SKU-INACTIVE");
        request.setActive(false);
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.existsBySkuIgnoreCase("SKU-INACTIVE")).thenReturn(false);
        when(productRepository.saveAndFlush(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.create(request);

        // Assert
        assertFalse(response.isActive());
    }

    @Test
    void givenExistingProduct_whenDelete_thenRemovesProduct() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        // Act
        productService.delete(id);

        // Assert
        verify(productRepository).deleteById(id);
    }

    @Test
    void givenMissingProduct_whenDelete_thenThrowsProductNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.delete(id));
        verify(productRepository, never()).deleteById(id);
    }

    @Test
    void givenStoredProducts_whenFindProducts_thenReturnsPagedResponses() {
        // Arrange
        ProductEntity entity = savedEntity(UUID.randomUUID(), "SKU-001");
        Page<ProductEntity> page = new PageImpl<>(List.of(entity));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        ProductPageResponse response = productService.findProducts(
                new ProductSearchCriteria(0, 5, null, null, null));

        // Assert
        assertEquals(1, response.getContent().size());
        assertEquals("SKU-001", response.getContent().get(0).getSku());
        assertEquals(0, response.getPage());
        assertEquals(1, response.getSize());
    }

    @Test
    void givenProductsBelowThreshold_whenFindLowStock_thenReturnsOnlyActiveProductsWithinThreshold() {
        // Arrange
        catalogProperties.setLowStockThreshold(2);
        ProductEntity lowStockActive = savedEntity(UUID.randomUUID(), "LOW-1");
        when(productRepository.findByActiveTrueAndStockQuantityLessThanEqual(2))
                .thenReturn(List.of(lowStockActive));

        // Act
        List<ProductResponse> responses = productService.findLowStock();

        // Assert
        assertEquals(1, responses.size());
        assertEquals("LOW-1", responses.get(0).getSku());
    }

    @Test
    void givenRepositoryAtMaximum_whenCreate_thenThrowsProductLimitReachedException() {
        // Arrange
        ProductRequest request = validRequest("SKU-MAX");
        catalogProperties.setMaximumProducts(20);
        when(productRepository.count()).thenReturn(20L);

        // Act & Assert
        assertThrows(ProductLimitReachedException.class, () -> productService.create(request));
        verify(productRepository, never()).saveAndFlush(any(ProductEntity.class));
    }

    @Test
    void givenSaveThrowsSkuConstraint_whenCreate_thenThrowsDuplicateSkuException() {
        // Arrange
        ProductRequest request = validRequest("RACE-001");
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.existsBySkuIgnoreCase("RACE-001")).thenReturn(false);
        ConstraintViolationException cause =
                new ConstraintViolationException("duplicate key", null, "products_sku_unique_lower");
        when(productRepository.saveAndFlush(any(ProductEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate", cause));

        // Act & Assert
        DuplicateSkuException exception =
                assertThrows(DuplicateSkuException.class, () -> productService.create(request));
        assertEquals("Product with SKU already exists: RACE-001", exception.getMessage());
    }

    @Test
    void givenOptimisticLockConflict_whenAdjustStock_thenPropagatesConflict() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = savedEntity(id, "STK-OPT");
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(1);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productRepository.saveAndFlush(entity))
                .thenThrow(new ObjectOptimisticLockingFailureException(ProductEntity.class, id));

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> productService.adjustStock(id, request));
    }

    @Test
    void givenMissingProduct_whenAdjustStock_thenThrowsProductNotFoundException() {
        UUID id = UUID.randomUUID();
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(1);
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.adjustStock(id, request));
    }

    @Test
    void givenZeroAdjustment_whenAdjustStock_thenThrowsInvalidStockAdjustmentException() {
        UUID id = UUID.randomUUID();
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(0);

        assertThrows(InvalidStockAdjustmentException.class, () -> productService.adjustStock(id, request));
        verify(productRepository, never()).findById(id);
    }

    @Test
    void givenInsufficientStock_whenAdjustStock_thenThrowsWithoutSaving() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = savedEntity(id, "STK-001");
        entity.applyRequestFields(entity.getSku(), entity.getName(), entity.getCategory(), entity.getPrice(), 2, true);
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(-5);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(InsufficientStockException.class, () -> productService.adjustStock(id, request));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenValidAdjustment_whenAdjustStock_thenPersistsUpdatedQuantity() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = savedEntity(id, "STK-002");
        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setAdjustment(3);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productRepository.saveAndFlush(entity)).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.adjustStock(id, request);

        assertEquals(4, response.getStockQuantity());
        verify(productRepository).saveAndFlush(entity);
    }

    private ProductRequest validRequest(String sku) {
        ProductRequest request = new ProductRequest();
        request.setSku(sku);
        request.setName("Sample Product");
        request.setCategory("General");
        request.setPrice(new BigDecimal("19.99"));
        request.setStockQuantity(10);
        request.setActive(true);
        return request;
    }

    private ProductEntity savedEntity(UUID id, String sku) {
        return new ProductEntity(
                id, sku, "Name", "Cat", new BigDecimal("1.00"), 1, true);
    }
}
