package com.codewalnut.productcatalog.dto;

import java.util.List;

public class ProductPageResponse {

    private final List<ProductResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public ProductPageResponse(
            List<ProductResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<ProductResponse> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
