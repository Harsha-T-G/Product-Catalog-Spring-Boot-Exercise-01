package com.codewalnut.productcatalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void resetCatalog() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/products")).andReturn();
        JsonNode products = objectMapper.readTree(listResult.getResponse().getContentAsString());
        for (JsonNode product : products) {
            mockMvc.perform(delete("/api/products/{id}", product.get("id").asText()));
        }
    }

    @Test
    void givenNewProduct_whenCreateAndRetrieve_thenReturnsPersistedProduct() throws Exception {
        // Arrange
        String payload = objectMapper.writeValueAsString(validProductPayload("INT-001"));

        // Act
        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Assert
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("INT-001"));
    }

    @Test
    void givenExistingProduct_whenUpdate_thenReturnsUpdatedValues() throws Exception {
        // Arrange
        String id = createProduct("INT-200");
        Map<String, Object> update = validProductPayload("INT-201");
        update.put("stockQuantity", 1);

        // Act & Assert
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("INT-201"))
                .andExpect(jsonPath("$.stockQuantity").value(1));
    }

    @Test
    void givenExistingProduct_whenDelete_thenLaterLookupReturns404() throws Exception {
        // Arrange
        String id = createProduct("INT-300");

        // Act
        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        // Assert
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenLowStockProducts_whenQueryLowStock_thenReturnsOnlyActiveProductsWithinThreshold() throws Exception {
        // Arrange
        createProductWithStock("LOW-1", 2, true);
        createProductWithStock("LOW-2", 5, true);
        createProductWithStock("LOW-3", 1, false);

        // Act & Assert — test profile threshold is 2
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sku").value("LOW-1"));
    }

    private String createProduct(String sku) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProductPayload(sku))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void createProductWithStock(String sku, int stock, boolean active) throws Exception {
        Map<String, Object> payload = validProductPayload(sku);
        payload.put("stockQuantity", stock);
        payload.put("active", active);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }

    private Map<String, Object> validProductPayload(String sku) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sku", sku);
        payload.put("name", "Integration Product");
        payload.put("category", "General");
        payload.put("price", new BigDecimal("12.50"));
        payload.put("stockQuantity", 10);
        payload.put("active", true);
        return payload;
    }
}
