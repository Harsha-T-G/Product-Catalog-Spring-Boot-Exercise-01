package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final ConcurrentHashMap<UUID, Product> products = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(new ArrayList<>(products.values()));
    }

    @Override
    public boolean existsBySkuIgnoreCase(String sku) {
        return existsBySkuIgnoreCaseExcludingId(sku, null);
    }

    @Override
    public boolean existsBySkuIgnoreCaseExcludingId(String sku, UUID excludeId) {
        return products.values().stream()
                .anyMatch(product -> product.getSku().equalsIgnoreCase(sku)
                        && (excludeId == null || !product.getId().equals(excludeId)));
    }

    @Override
    public void deleteById(UUID id) {
        products.remove(id);
    }

    @Override
    public int count() {
        return products.size();
    }
}
