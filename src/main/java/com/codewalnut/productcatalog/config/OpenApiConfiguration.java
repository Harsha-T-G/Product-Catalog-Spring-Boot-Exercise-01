package com.codewalnut.productcatalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI productCatalogOpenApi(@Value("${info.app.version}") String version) {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Catalog API")
                        .version(version)
                        .description("""
                                REST API for product catalog management.
                                Supports CRUD, pagination, category/active filters, sorting, and stock adjustments.
                                """))
                .addServersItem(new Server().url("/").description("Current host"));
    }
}
