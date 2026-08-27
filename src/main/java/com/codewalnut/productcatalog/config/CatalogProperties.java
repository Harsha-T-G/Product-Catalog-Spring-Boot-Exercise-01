package com.codewalnut.productcatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog")
public class CatalogProperties {

    private int lowStockThreshold = 5;
    private int maximumProducts = 500;
    private String defaultCategory = "General";

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
}
