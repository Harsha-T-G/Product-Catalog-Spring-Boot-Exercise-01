package com.codewalnut.productcatalog;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.config.SecuritySeedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CatalogProperties.class, SecuritySeedProperties.class})
public class ProductCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductCatalogApplication.class, args);
    }
}
