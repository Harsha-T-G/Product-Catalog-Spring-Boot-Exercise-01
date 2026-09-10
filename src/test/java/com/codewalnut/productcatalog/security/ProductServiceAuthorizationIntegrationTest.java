package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.service.ProductService;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceAuthorizationIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private ProductService productService;

    @Test
    @WithMockUser(roles = "EDITOR")
    void givenEditor_whenDeletingThroughService_thenDeniedBeforeBusinessLogic() {
        assertThrows(AccessDeniedException.class, () -> productService.delete(UUID.randomUUID()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void givenAdmin_whenDeletingThroughService_thenReachesBusinessLogic() {
        assertThrows(ProductNotFoundException.class, () -> productService.delete(UUID.randomUUID()));
    }
}
