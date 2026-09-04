package com.codewalnut.productcatalog;

import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductCatalogApplicationTest extends PostgreSqlTestSupport {

    @Test
    void givenApplicationStarts_whenContextLoads_thenSpringContextIsAvailable() {
        // Then — context load is the assertion; failure fails the test
    }
}
