package com.codewalnut.productcatalog.controller;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.dto.StockAdjustmentRequest;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.GlobalExceptionHandler;
import com.codewalnut.productcatalog.exception.ProductLimitReachedException;
import com.codewalnut.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerWebMvcTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void givenValidRequest_whenCreateProduct_thenReturns201WithLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.create(any(ProductRequest.class))).thenReturn(
                new ProductResponse(id, "SKU-001", "Sample", "General", new BigDecimal("19.99"), 10, true, null, null, 0L));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("SKU-001"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/" + id));
    }

    @Test
    void givenOptimisticLockConflict_whenAdjustStock_thenReturns409ErrorEnvelope() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.adjustStock(eq(id), any(StockAdjustmentRequest.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(ProductEntity.class, id));

        mockMvc.perform(patch("/api/products/{id}/stock", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Product was updated by another transaction"));
    }

    @Test
    void givenProductLimitReached_whenCreateProduct_thenReturns409ErrorEnvelope() throws Exception {
        when(productService.create(any(ProductRequest.class)))
                .thenThrow(new ProductLimitReachedException(20));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("SKU-LIMIT"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/products"));
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
