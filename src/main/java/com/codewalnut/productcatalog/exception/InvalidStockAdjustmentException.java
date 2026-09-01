package com.codewalnut.productcatalog.exception;

public class InvalidStockAdjustmentException extends RuntimeException {

    public InvalidStockAdjustmentException() {
        super("Stock adjustment must not be zero");
    }
}
