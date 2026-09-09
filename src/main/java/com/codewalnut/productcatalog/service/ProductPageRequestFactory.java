package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.exception.InvalidPaginationException;
import com.codewalnut.productcatalog.exception.InvalidSortDirectionException;
import com.codewalnut.productcatalog.exception.InvalidSortFieldException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ProductPageRequestFactory {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("name", "price", "category", "createdAt", "stockQuantity");

    private final CatalogProperties catalogProperties;

    public ProductPageRequestFactory(CatalogProperties catalogProperties) {
        this.catalogProperties = catalogProperties;
    }

    public Pageable createPageable(int page, Integer size, String sort) {
        if (page < 0) {
            throw new InvalidPaginationException("Page number must not be negative");
        }

        int resolvedSize = size != null ? size : catalogProperties.getDefaultPageSize();
        if (resolvedSize <= 0 || resolvedSize > catalogProperties.getMaxPageSize()) {
            throw new InvalidPaginationException(
                    "Page size must be between 1 and " + catalogProperties.getMaxPageSize());
        }

        Sort sortOrder = parseSort(sort).and(Sort.by(Sort.Direction.ASC, "id"));
        return PageRequest.of(page, resolvedSize, sortOrder);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }

        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new InvalidSortFieldException(field);
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String directionValue = parts[1].trim().toLowerCase(Locale.ROOT);
            if ("desc".equals(directionValue)) {
                direction = Sort.Direction.DESC;
            } else if (!"asc".equals(directionValue)) {
                throw new InvalidSortDirectionException(parts[1].trim());
            }
        }

        return Sort.by(direction, field);
    }
}
