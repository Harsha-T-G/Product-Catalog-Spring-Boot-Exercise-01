package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleAuthorizationIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanProducts() {
        productRepository.deleteAll();
    }

    @Test
    void givenViewer_whenCreatingProduct_thenReturns403() throws Exception {
        String traceId = "f4c39b16-41d0-42e9-a964-8f87e05dd322";

        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(user("viewer-user").roles("VIEWER"))
                        .header(RequestTraceFilter.TRACE_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("VIEWER-DENIED")))
                .andExpect(status().isForbidden())
                .andExpect(header().string(RequestTraceFilter.TRACE_HEADER, traceId))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access is denied"))
                .andExpect(jsonPath("$.path").value("/api/products"))
                .andExpect(jsonPath("$.traceId").value(traceId))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void givenViewer_whenListingProducts_thenReturns200() throws Exception {
        mockMvc.perform(get("/api/products").with(user("viewer-user").roles("VIEWER")))
                .andExpect(status().isOk());
    }

    @Test
    void givenViewer_whenUpdatingProduct_thenReturns403() throws Exception {
        mockMvc.perform(put("/api/products/{id}", UUID.randomUUID())
                        .with(user("viewer-user").roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("VIEWER-UPDATE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenViewer_whenChangingStock_thenReturns403() throws Exception {
        mockMvc.perform(patch("/api/products/{id}/stock", UUID.randomUUID())
                        .with(user("viewer-user").roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenViewer_whenDeletingProduct_thenReturns403() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", UUID.randomUUID())
                        .with(user("viewer-user").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenEditor_whenDeletingProduct_thenReturns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/{id}", java.util.UUID.randomUUID())
                        .with(user("editor-user").roles("EDITOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenEditor_whenReadingCreatingUpdatingAndChangingStock_thenRequestsSucceed() throws Exception {
        // Arrange
        ProductEntity product = saveProduct("EDITOR-EXISTING");

        // Act & Assert
        mockMvc.perform(get("/api/products").with(user("editor-user").roles("EDITOR")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/products")
                        .with(user("editor-user").roles("EDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("EDITOR-CREATE")))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .with(user("editor-user").roles("EDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("EDITOR-EXISTING")))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/products/{id}/stock", product.getId())
                        .with(user("editor-user").roles("EDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void givenEditor_whenManagingUsers_thenReturns403() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("editor-user").roles("EDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAdmin_whenCreatingProduct_thenReturns201() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/products")
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("ADMIN-ALLOWED")))
                .andExpect(status().isCreated());
    }

    @Test
    void givenAdmin_whenReadingUpdatingChangingStockAndDeleting_thenRequestsSucceed() throws Exception {
        // Arrange
        ProductEntity product = saveProduct("ADMIN-EXISTING");

        // Act & Assert
        mockMvc.perform(get("/api/products").with(user("admin-user").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson("ADMIN-EXISTING")))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/products/{id}/stock", product.getId())
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adjustment\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/products/{id}", product.getId())
                        .with(user("admin-user").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenAdmin_whenRequestingUserManagementRoute_thenPassesSecurityAndReachesValidation() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("admin-user").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenViewer_whenRequestingActuatorInfo_thenReturns200() throws Exception {
        mockMvc.perform(get("/actuator/info").with(user("viewer-user").roles("VIEWER")))
                .andExpect(status().isOk());
    }

    @Test
    void givenAuthenticatedUser_whenRequestingUnknownRoute_thenReturns404() throws Exception {
        mockMvc.perform(get("/new-unmapped-endpoint").with(user("viewer-user").roles("VIEWER")))
                .andExpect(status().isNotFound());
    }

    private ProductEntity saveProduct(String sku) {
        return productRepository.saveAndFlush(new ProductEntity(
                UUID.randomUUID(),
                sku,
                "Authorization Test Product",
                "Security",
                new BigDecimal("19.99"),
                5,
                true));
    }

    private String validProductJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Authorization Test Product",
                  "category": "Security",
                  "price": 19.99,
                  "stockQuantity": 5,
                  "active": true
                }
                """.formatted(sku);
    }
}
