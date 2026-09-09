package com.codewalnut.productcatalog.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Product with SKU already exists: " + sku);
    }
}
