package com.codewalnut.productcatalog.mapper;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.model.Product;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductMapper {

    public Product toNewProduct(ProductRequest request, UUID id) {
        return new Product(
                id,
                request.getSku(),
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity(),
                request.isActive());
    }

    public Product toUpdatedProduct(UUID id, ProductRequest request) {
        return new Product(
                id,
                request.getSku(),
                request.getName(),
                request.getCategory(),
                request.getPrice(),
                request.getStockQuantity(),
                request.isActive());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive());
    }
}
