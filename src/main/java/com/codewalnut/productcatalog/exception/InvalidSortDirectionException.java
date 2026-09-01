package com.codewalnut.productcatalog.exception;

public class InvalidSortDirectionException extends RuntimeException {

    public InvalidSortDirectionException(String direction) {
        super("Invalid sort direction '" + direction + "'. Use asc or desc.");
    }
}
