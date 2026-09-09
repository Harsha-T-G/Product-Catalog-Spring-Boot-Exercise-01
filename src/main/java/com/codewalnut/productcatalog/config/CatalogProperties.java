package com.codewalnut.productcatalog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "catalog")
@Validated
@Getter
@Setter
public class CatalogProperties {

    @Min(0)
    private int lowStockThreshold;
    @Min(1)
    private int maximumProducts;
    @NotBlank
    private String defaultCategory;
    @Min(1)
    private int defaultPageSize;
    @Min(1)
    private int maxPageSize;

    @jakarta.annotation.PostConstruct
    void validatePageSizes() {
        if (defaultPageSize > maxPageSize) {
            throw new IllegalStateException(
                    "catalog.default-page-size (" + defaultPageSize + ") must be <= catalog.max-page-size ("
                            + maxPageSize + ")");
        }
    }
}
