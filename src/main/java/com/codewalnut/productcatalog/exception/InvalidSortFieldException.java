package com.codewalnut.productcatalog.exception;

public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String field) {
        super("Sorting is not allowed by field '" + field + "'");
    }
}
