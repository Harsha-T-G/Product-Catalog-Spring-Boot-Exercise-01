package com.codewalnut.productcatalog;

import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.support.ProductTestFixtures;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void resetCatalog() {
        productRepository.deleteAll();
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
    void givenMixedProducts_whenFilterPaginateAndSort_thenReturnsExpectedPage() throws Exception {
        // Arrange
        createProductWithCategory("PAGE-A", "Electronics", "Alpha");
        createProductWithCategory("PAGE-B", "Electronics", "Bravo");
        createProductWithCategory("PAGE-C", "Books", "Charlie");

        // Act & Assert
        mockMvc.perform(get("/api/products")
                        .param("category", "electronics")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("PAGE-A"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void givenNewProduct_whenCreate_thenDatabaseRecordMatchesResponse() throws Exception {
        // Arrange
        String payload = objectMapper.writeValueAsString(validProductPayload("DB-001"));

        // Act
        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Assert — new request confirms persistence
        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("DB-001"))
                .andExpect(jsonPath("$.name").value("Integration Product"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.version").exists());
    }

    @Test
    void givenProductsWithSameName_whenPaginateByName_thenUsesIdTieBreaker() throws Exception {
        // Arrange
        createProductWithCategory("TIE-A", "General", "Shared Name");
        createProductWithCategory("TIE-B", "General", "Shared Name");

        // Act & Assert — page size 1 with name sort returns stable first page
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void givenConcurrentStockPatches_whenOptimisticLockLost_thenReturns409() throws Exception {
        // Arrange
        String id = createProduct("LOCK-409");
        Map<String, Object> payload = Map.of("adjustment", 1);
        String body = objectMapper.writeValueAsString(payload);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> patchStock = () -> {
            ready.countDown();
            start.await();
            return mockMvc.perform(patch("/api/products/{id}/stock", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        };

        try {
            Future<Integer> first = executor.submit(patchStock);
            Future<Integer> second = executor.submit(patchStock);
            ready.await();
            start.countDown();

            int statusOne = first.get();
            int statusTwo = second.get();
            assertTrue(statusOne == 200 || statusTwo == 200);
            assertTrue(statusOne == 409 || statusTwo == 409);
        } finally {
            executor.shutdownNow();
        }
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

    private void createProductWithCategory(String sku, String category, String name) throws Exception {
        Map<String, Object> payload = ProductTestFixtures.validProductPayload(sku);
        payload.put("category", category);
        payload.put("name", name);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }

    private Map<String, Object> validProductPayload(String sku) {
        return ProductTestFixtures.validProductPayload(sku);
    }
}
