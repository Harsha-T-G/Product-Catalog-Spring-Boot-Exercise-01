package com.codewalnut.productcatalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    private static final int MAX_PRICE_INTEGER_DIGITS = 17;
    private static final int MAX_PRICE_FRACTION_DIGITS = 2;

    @NotBlank
    @Size(min = 3, max = 30)
    private String sku;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String category;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(
            integer = MAX_PRICE_INTEGER_DIGITS,
            fraction = MAX_PRICE_FRACTION_DIGITS,
            message = "Price must be greater than zero with at most 17 integer digits and 2 decimal places")
    private BigDecimal price;

    @Min(0)
    private int stockQuantity;

    private boolean active = true;

    private Long version;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
