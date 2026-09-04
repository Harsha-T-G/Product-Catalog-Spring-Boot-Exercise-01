package com.codewalnut.productcatalog.support;

import com.codewalnut.productcatalog.dto.ProductRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class ProductTestFixtures {

    private ProductTestFixtures() {
    }

    public static ProductRequest validProductRequest(String sku) {
        ProductRequest request = new ProductRequest();
        request.setSku(sku);
        request.setName("Sample Product");
        request.setCategory("General");
        request.setPrice(new BigDecimal("19.99"));
        request.setStockQuantity(10);
        request.setActive(true);
        return request;
    }

    public static Map<String, Object> validProductPayload(String sku) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sku", sku);
        payload.put("name", "Integration Product");
        payload.put("category", "General");
        payload.put("price", new BigDecimal("12.50"));
        payload.put("stockQuantity", 10);
        payload.put("active", true);
        return payload;
    }
}
