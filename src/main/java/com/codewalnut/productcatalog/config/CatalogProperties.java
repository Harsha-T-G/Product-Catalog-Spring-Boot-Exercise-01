package com.codewalnut.productcatalog.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "catalog")
@Validated
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

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getMaximumProducts() {
        return maximumProducts;
    }

    public void setMaximumProducts(int maximumProducts) {
        this.maximumProducts = maximumProducts;
    }

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
}
