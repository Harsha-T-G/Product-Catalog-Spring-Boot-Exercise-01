package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.dto.ProductPageResponse;
import com.codewalnut.productcatalog.dto.ProductRequest;
import com.codewalnut.productcatalog.dto.ProductResponse;
import com.codewalnut.productcatalog.dto.ProductSearchCriteria;
import com.codewalnut.productcatalog.dto.StockAdjustmentRequest;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.exception.InsufficientStockException;
import com.codewalnut.productcatalog.exception.InvalidStockAdjustmentException;
import com.codewalnut.productcatalog.exception.ProductLimitReachedException;
import com.codewalnut.productcatalog.exception.ProductNotFoundException;
import com.codewalnut.productcatalog.mapper.ProductEntityMapper;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.repository.ProductSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEntityMapper productEntityMapper;
    private final CatalogProperties catalogProperties;
    private final ProductPageRequestFactory productPageRequestFactory;
    private final ProductPersistenceSupport productPersistenceSupport;

    public ProductService(
            ProductRepository productRepository,
            ProductEntityMapper productEntityMapper,
            CatalogProperties catalogProperties,
            ProductPageRequestFactory productPageRequestFactory,
            ProductPersistenceSupport productPersistenceSupport) {
        this.productRepository = productRepository;
        this.productEntityMapper = productEntityMapper;
        this.catalogProperties = catalogProperties;
        this.productPageRequestFactory = productPageRequestFactory;
        this.productPersistenceSupport = productPersistenceSupport;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.count() >= catalogProperties.getMaximumProducts()) {
            throw new ProductLimitReachedException(catalogProperties.getMaximumProducts());
        }
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }
        UUID id = UUID.randomUUID();
        ProductEntity entity = productEntityMapper.toNewEntity(id, request);
        ProductEntity saved = productPersistenceSupport.saveAndFlush(entity, request.getSku());
        return productEntityMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductPageResponse findProducts(ProductSearchCriteria criteria) {
        Pageable pageable = productPageRequestFactory.createPageable(
                criteria.page(), criteria.size(), criteria.sort());
        Specification<ProductEntity> specification = ProductSpecifications.withFilters(
                criteria.category(), criteria.active());
        Page<ProductEntity> result = productRepository.findAll(specification, pageable);
        List<ProductResponse> content = result.getContent().stream()
                .map(productEntityMapper::toResponse)
                .toList();
        return new ProductPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        int threshold = catalogProperties.getLowStockThreshold();
        return productRepository.findByActiveTrueAndStockQuantityLessThanEqual(threshold).stream()
                .map(productEntityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productEntityMapper.toResponse(entity);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        assertExpectedVersion(entity, request.getVersion(), id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.getSku(), id)) {
            throw new DuplicateSkuException(request.getSku());
        }
        productEntityMapper.applyUpdate(entity, request);
        ProductEntity saved = productPersistenceSupport.saveAndFlush(entity, request.getSku());
        return productEntityMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse adjustStock(UUID id, StockAdjustmentRequest request) {
        int adjustment = request.getAdjustment();
        if (adjustment == 0) {
            throw new InvalidStockAdjustmentException();
        }

        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        assertExpectedVersion(entity, request.getVersion(), id);

        int newQuantity = entity.getStockQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new InsufficientStockException();
        }

        entity.adjustStockBy(adjustment);
        ProductEntity saved = productPersistenceSupport.saveAndFlush(entity);
        return productEntityMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private void assertExpectedVersion(ProductEntity entity, Long expectedVersion, UUID id) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(ProductEntity.class, id);
        }
    }
}
