package com.codewalnut.productcatalog.dto;

import lombok.Value;

import java.util.List;

@Value
public class ProductPageResponse {

    List<ProductResponse> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
}
