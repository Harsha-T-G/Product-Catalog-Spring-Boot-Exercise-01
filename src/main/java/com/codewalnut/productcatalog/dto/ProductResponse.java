package com.codewalnut.productcatalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ProductResponse {

    private final UUID id;
    private final String sku;
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final int stockQuantity;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;

    public ProductResponse(
            UUID id,
            String sku,
            String name,
            String category,
            BigDecimal price,
            int stockQuantity,
            boolean active) {
        this(id, sku, name, category, price, stockQuantity, active, null, null, null);
    }

    public ProductResponse(
            UUID id,
            String sku,
            String name,
            String category,
            BigDecimal price,
            int stockQuantity,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Long version) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
