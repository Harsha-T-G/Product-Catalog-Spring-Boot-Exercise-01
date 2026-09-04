package com.codewalnut.productcatalog.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Stock adjustment would reduce quantity below zero");
    }
}
