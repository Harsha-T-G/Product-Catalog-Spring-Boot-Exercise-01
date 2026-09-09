package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.exception.DuplicateSkuException;
import com.codewalnut.productcatalog.repository.ProductRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceSupport {

    private static final String SKU_UNIQUE_CONSTRAINT = "products_sku_unique_lower";

    private final ProductRepository productRepository;

    public ProductPersistenceSupport(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductEntity saveAndFlush(ProductEntity entity, String skuForDuplicateMapping) {
        try {
            return productRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException exception) {
            if (skuForDuplicateMapping != null && isSkuConstraintViolation(exception)) {
                throw new DuplicateSkuException(skuForDuplicateMapping);
            }
            throw exception;
        }
    }

    public ProductEntity saveAndFlush(ProductEntity entity) {
        return saveAndFlush(entity, null);
    }

    private boolean isSkuConstraintViolation(DataIntegrityViolationException exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation) {
                return SKU_UNIQUE_CONSTRAINT.equals(constraintViolation.getConstraintName());
            }
            current = current.getCause();
        }
        return false;
    }
}
