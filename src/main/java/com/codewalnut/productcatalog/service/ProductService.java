package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.mapper.ProductMapper;
import com.codewalnut.productcatalog.model.Product;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        UUID id = UUID.randomUUID();
        Product product = productMapper.toNewProduct(request, id);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponse(product);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (productRepository.existsBySkuIgnoreCaseExcludingId(request.getSku(), id)) {
            throw new DuplicateSkuException(request.getSku());
        }
        Product updated = productMapper.toUpdatedProduct(id, request);
        Product saved = productRepository.save(updated);
        return productMapper.toResponse(saved);
    }

    public void delete(UUID id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        productRepository.deleteById(id);
    }
}
