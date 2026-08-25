package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.mapper.ProductMapper;
import com.codewalnut.productcatalog.model.Product;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, new ProductMapper());
    }

    @Test
    void givenValidRequest_whenCreate_thenReturnsSavedProductResponse() {
        // Arrange
        ProductRequest request = validRequest("SKU-001");
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.create(request);

        // Assert
        assertNotNull(response.getId());
        assertEquals("SKU-001", response.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void givenDuplicateSku_whenCreate_thenThrowsDuplicateSkuException() {
        // Arrange
        ProductRequest request = validRequest("ABC-001");
        when(productRepository.existsBySkuIgnoreCase("ABC-001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.create(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void givenDuplicateSkuDifferentCase_whenCreate_thenThrowsDuplicateSkuException() {
        // Arrange
        ProductRequest request = validRequest("abc-001");
        when(productRepository.existsBySkuIgnoreCase("abc-001")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.create(request));
    }

    @Test
    void givenExistingId_whenFindById_thenReturnsProductResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "SKU-001", "Name", "Cat", new BigDecimal("1.00"), 1, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

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
        Product existing = new Product(id, "SKU-001", "Old", "Cat", new BigDecimal("1.00"), 1, true);
        ProductRequest request = validRequest("SKU-002");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuIgnoreCaseExcludingId("SKU-002", id)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.update(id, request);

        // Assert
        assertEquals(id, response.getId());
        assertEquals("SKU-002", response.getSku());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals(id, captor.getValue().getId());
    }

    @Test
    void givenOtherProductSku_whenUpdate_thenThrowsDuplicateSkuException() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product existing = new Product(id, "SKU-001", "Old", "Cat", new BigDecimal("1.00"), 1, true);
        ProductRequest request = validRequest("sku-999");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySkuIgnoreCaseExcludingId("sku-999", id)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateSkuException.class, () -> productService.update(id, request));
    }

    @Test
    void givenInactiveProductRequest_whenCreate_thenActiveFlagIsPreserved() {
        // Arrange
        ProductRequest request = validRequest("SKU-INACTIVE");
        request.setActive(false);
        when(productRepository.existsBySkuIgnoreCase("SKU-INACTIVE")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.create(request);

        // Assert
        assertFalse(response.isActive());
    }

    @Test
    void givenExistingProduct_whenDelete_thenRemovesProduct() {
        // Arrange
        UUID id = UUID.randomUUID();
        Product product = new Product(id, "SKU-001", "Name", "Cat", new BigDecimal("1.00"), 1, true);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // Act
        productService.delete(id);

        // Assert
        verify(productRepository).deleteById(id);
    }

    @Test
    void givenMissingProduct_whenDelete_thenThrowsProductNotFoundException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.delete(id));
        verify(productRepository, never()).deleteById(id);
    }

    @Test
    void givenStoredProducts_whenFindAll_thenReturnsMappedResponses() {
        // Arrange
        Product product = new Product(UUID.randomUUID(), "SKU-001", "Name", "Cat", new BigDecimal("2.00"), 3, true);
        when(productRepository.findAll()).thenReturn(List.of(product));

        // Act
        List<ProductResponse> responses = productService.findAll();

        // Assert
        assertEquals(1, responses.size());
        assertEquals("SKU-001", responses.get(0).getSku());
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
}
