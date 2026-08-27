package com.codewalnut.productcatalog.controller;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void clearProducts() {
        productRepository.findAll().forEach(product -> productRepository.deleteById(product.getId()));
    }

    @Test
    void givenValidRequest_whenCreateProduct_thenReturns201WithLocationHeader() throws Exception {
        // Act
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("SKU-001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andReturn();

        // Assert
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = body.get("id").asText();
        assertEquals("/api/products/" + id, result.getResponse().getHeader("Location"));
    }

    @Test
    void givenExistingProduct_whenGetById_thenReturns200() throws Exception {
        // Arrange
        String id = createProduct("SKU-100");

        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-100"));
    }

    @Test
    void givenInvalidRequest_whenCreateProduct_thenReturns400WithFieldErrors() throws Exception {
        // Arrange
        ProductRequest request = validRequest("SKU-001");
        request.setSku("");

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("sku"))
                .andExpect(jsonPath("$.path").value("/api/products"));
        assertEquals(0, productRepository.count());
    }

    @Test
    void givenMissingProduct_whenGetById_thenReturns404ErrorEnvelope() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void givenDuplicateSku_whenCreateProduct_thenReturns409ErrorEnvelope() throws Exception {
        // Arrange
        createProduct("ABC-001");

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest("abc-001"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void givenInvalidUuid_whenGetProduct_thenReturns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void givenUnsupportedMethod_whenPatchProducts_thenReturns405() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/products"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void givenExistingProduct_whenDelete_thenReturns204() throws Exception {
        // Arrange
        String id = createProduct("SKU-200");

        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
        assertEquals(0, productRepository.count());
    }

    @Test
    void givenExistingProduct_whenUpdate_thenReturns200() throws Exception {
        // Arrange
        String id = createProduct("SKU-300");
        ProductRequest update = validRequest("SKU-301");

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-301"))
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void givenNoProducts_whenListProducts_thenReturnsEmptyArray() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void givenMissingProduct_whenDelete_thenReturns404() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenInactiveProduct_whenCreateAndGet_thenActiveFlagIsPreserved() throws Exception {
        // Arrange
        ProductRequest request = validRequest("SKU-INACTIVE");
        request.setActive(false);

        // Act
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = body.get("id").asText();

        // Assert
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        assertFalse(body.get("active").asBoolean());
    }

    private String createProduct(String sku) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest(sku))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
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
