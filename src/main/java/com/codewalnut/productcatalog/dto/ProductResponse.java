package com.codewalnut.productcatalog.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
public class ProductResponse {

    UUID id;
    String sku;
    String name;
    String category;
    BigDecimal price;
    int stockQuantity;
    boolean active;
    Instant createdAt;
    Instant updatedAt;
    Long version;
}
