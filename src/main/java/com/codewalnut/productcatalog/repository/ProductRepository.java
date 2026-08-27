package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findAll();

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseExcludingId(String sku, UUID excludeId);

    void deleteById(UUID id);

    int count();
}
