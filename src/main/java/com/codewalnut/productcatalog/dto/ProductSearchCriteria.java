package com.codewalnut.productcatalog.dto;

public record ProductSearchCriteria(
        int page,
        Integer size,
        String sort,
        String category,
        Boolean active) {
}
