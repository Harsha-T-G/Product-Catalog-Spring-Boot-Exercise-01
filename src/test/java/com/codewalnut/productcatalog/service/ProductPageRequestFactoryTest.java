package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.config.CatalogProperties;
import com.codewalnut.productcatalog.exception.InvalidPaginationException;
import com.codewalnut.productcatalog.exception.InvalidSortDirectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductPageRequestFactoryTest {

    private ProductPageRequestFactory factory;

    @BeforeEach
    void setUp() {
        CatalogProperties properties = new CatalogProperties();
        properties.setDefaultPageSize(10);
        properties.setMaxPageSize(100);
        factory = new ProductPageRequestFactory(properties);
    }

    @Test
    void givenNoSort_whenCreatePageable_thenAppendsIdTieBreaker() {
        Pageable pageable = factory.createPageable(0, null, null);

        Sort.Order idOrder = pageable.getSort().getOrderFor("id");
        assertEquals(Sort.Direction.ASC, idOrder.getDirection());
    }

    @Test
    void givenNameSort_whenCreatePageable_thenIncludesIdTieBreaker() {
        Pageable pageable = factory.createPageable(0, 10, "name,asc");

        assertEquals(2, pageable.getSort().toList().size());
        assertEquals("name", pageable.getSort().toList().get(0).getProperty());
        assertEquals("id", pageable.getSort().toList().get(1).getProperty());
    }

    @Test
    void givenInvalidPageSize_whenCreatePageable_thenThrowsInvalidPaginationException() {
        assertThrows(InvalidPaginationException.class, () -> factory.createPageable(0, 0, null));
    }

    @Test
    void givenInvalidSortDirection_whenCreatePageable_thenThrowsInvalidSortDirectionException() {
        assertThrows(
                InvalidSortDirectionException.class,
                () -> factory.createPageable(0, 10, "name,sideways"));
    }
}
