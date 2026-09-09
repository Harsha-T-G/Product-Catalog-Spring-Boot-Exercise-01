package com.codewalnut.productcatalog.exception;

public class ProductLimitReachedException extends RuntimeException {

    public ProductLimitReachedException(int maximumProducts) {
        super("Maximum number of products reached: " + maximumProducts);
    }
}
