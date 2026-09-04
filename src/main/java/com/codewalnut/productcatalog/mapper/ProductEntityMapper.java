package com.codewalnut.productcatalog.mapper;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductEntityMapper {

    public ProductEntity toNewEntity(UUID id, ProductRequest request) {
        return new ProductEntity(
                id,
                request.getSku(),
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity(),
                request.isActive());
    }

    public void applyUpdate(ProductEntity entity, ProductRequest request) {
        entity.applyRequestFields(
                request.getSku(),
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity(),
                request.isActive());
    }

    public ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }
}
