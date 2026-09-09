package com.example.plus.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.entity.ProductCategory;
import com.example.plus.domain.product.repository.ProductRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductsReturnsEmptyPageWithRequestedConditionsAndLatestSort() {
        ProductListRequest request = new ProductListRequest(
                ProductCategory.FOOD,
                10_000L,
                50_000L,
                1,
                10
        );

        when(productRepository.findAllByConditions(
                eq(ProductCategory.FOOD),
                eq(10_000L),
                eq(50_000L),
                any(Pageable.class)
        )).thenAnswer(invocation -> Page.empty(invocation.getArgument(3)));

        Page<ProductListResponse> result = productService.getProducts(request);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAllByConditions(
                eq(ProductCategory.FOOD),
                eq(10_000L),
                eq(50_000L),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertTrue(result.isEmpty());
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getProductsRejectsInvalidPriceRange() {
        ProductListRequest request = new ProductListRequest(
                null,
                50_000L,
                10_000L,
                0,
                20
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProducts(request)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void productListRequestUsesDefaultPageAndSize() {
        ProductListRequest request = new ProductListRequest(null, null, null, null, null);

        assertEquals(0, request.page());
        assertEquals(20, request.size());
    }

    @Test
    void productListRequestRejectsInvalidPageAndSize() {
        ProductListRequest request = new ProductListRequest(null, null, null, -1, 101);

        Set<ConstraintViolation<ProductListRequest>> violations = validate(request);

        assertEquals(2, violations.size());
    }

    @Test
    void productListRequestRejectsNegativeMinPrice() {
        ProductListRequest request = new ProductListRequest(null, -1L, null, 0, 20);

        Set<ConstraintViolation<ProductListRequest>> violations = validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("minPrice")));
    }

    @Test
    void productListRequestRejectsNegativeMaxPrice() {
        ProductListRequest request = new ProductListRequest(null, null, -1L, 0, 20);

        Set<ConstraintViolation<ProductListRequest>> violations = validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("maxPrice")));
    }

    @Test
    void productListRequestAllowsZeroPrices() {
        ProductListRequest request = new ProductListRequest(null, 0L, 0L, 0, 20);

        assertTrue(validate(request).isEmpty());
    }

    private Set<ConstraintViolation<ProductListRequest>> validate(ProductListRequest request) {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator().validate(request);
        }
    }
}
