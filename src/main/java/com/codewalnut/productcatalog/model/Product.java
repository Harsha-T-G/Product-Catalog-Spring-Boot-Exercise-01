package com.codewalnut.productcatalog.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String sku;
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final int stockQuantity;
    private final boolean active;

    public Product(
            UUID id,
            String sku,
            String name,
            String category,
            BigDecimal price,
            int stockQuantity,
            boolean active) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isActive() {
        return active;
    }
}
