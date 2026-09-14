package com.example.plus.domain.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.plus.domain.product.dto.ProductDetailResponse;
import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.dto.ProductUpdateRequest;
import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.entity.ProductCategory;
import com.example.plus.domain.product.repository.ProductRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
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
    void getProductDetailReturnsProductDetailResponse() {
        Long productId = 1L;
        Product product = mock(Product.class);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(productId);
        when(product.getName()).thenReturn("테스트 상품");
        when(product.getCategory()).thenReturn(ProductCategory.FOOD);
        when(product.getPrice()).thenReturn(10_000L);
        when(product.getStockQuantity()).thenReturn(5);
        when(product.getDescription()).thenReturn("테스트 상품 설명");

        ProductDetailResponse response = productService.getProductDetail(productId);

        assertEquals(productId, response.productId());
        assertEquals("테스트 상품", response.name());
        assertEquals(ProductCategory.FOOD, response.category());
        assertEquals(10_000L, response.price());
        assertEquals(5, response.stockQuantity());
        assertEquals("테스트 상품 설명", response.description());
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductDetailRejectsMissingProduct() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProductDetail(productId)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verify(productRepository).findById(productId);
    }

    @Test
    void updateProductUpdatesAllProvidedFields() {
        Long productId = 1L;
        Product product = createProduct(
                "기존 상품",
                ProductCategory.FOOD,
                10_000L,
                "기존 설명"
        );
        ProductUpdateRequest request = new ProductUpdateRequest(
                "수정 상품",
                ProductCategory.ELECTRONICS,
                20_000L,
                "수정 설명"
        );
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDetailResponse response = productService.updateProduct(productId, request);

        assertEquals("수정 상품", product.getName());
        assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
        assertEquals(20_000L, product.getPrice());
        assertEquals("수정 설명", product.getDescription());
        assertEquals("수정 상품", response.name());
        assertEquals(ProductCategory.ELECTRONICS, response.category());
        assertEquals(20_000L, response.price());
        assertEquals("수정 설명", response.description());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductUpdatesOnlyProvidedNameAndKeepsNullFields() {
        Long productId = 1L;
        Product product = createProduct(
                "기존 상품",
                ProductCategory.FOOD,
                10_000L,
                "기존 설명"
        );
        ProductUpdateRequest request = new ProductUpdateRequest("수정 상품", null, null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDetailResponse response = productService.updateProduct(productId, request);

        assertEquals("수정 상품", product.getName());
        assertEquals(ProductCategory.FOOD, product.getCategory());
        assertEquals(10_000L, product.getPrice());
        assertEquals("기존 설명", product.getDescription());
        assertEquals("수정 상품", response.name());
        assertEquals(ProductCategory.FOOD, response.category());
        assertEquals(10_000L, response.price());
        assertEquals("기존 설명", response.description());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductUpdatesOnlyProvidedDescriptionAndKeepsNullFields() {
        Long productId = 1L;
        Product product = createProduct(
                "기존 상품",
                ProductCategory.FOOD,
                10_000L,
                "기존 설명"
        );
        ProductUpdateRequest request = new ProductUpdateRequest(null, null, null, "수정 설명");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDetailResponse response = productService.updateProduct(productId, request);

        assertEquals("기존 상품", product.getName());
        assertEquals(ProductCategory.FOOD, product.getCategory());
        assertEquals(10_000L, product.getPrice());
        assertEquals("수정 설명", product.getDescription());
        assertEquals("기존 상품", response.name());
        assertEquals(ProductCategory.FOOD, response.category());
        assertEquals(10_000L, response.price());
        assertEquals("수정 설명", response.description());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductRejectsMissingProduct() {
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "수정 상품",
                null,
                null,
                null
        );
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.updateProduct(productId, request)
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
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

    @Test
    void productUpdateRequestAllowsNameWith200Characters() {
        ProductUpdateRequest request = new ProductUpdateRequest("가".repeat(200), null, null, null);

        assertTrue(validate(request).isEmpty());
    }

    @Test
    void productUpdateRequestRejectsNameOver200Characters() {
        ProductUpdateRequest request = new ProductUpdateRequest("가".repeat(201), null, null, null);

        Set<ConstraintViolation<ProductUpdateRequest>> violations = validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")));
    }

    @Test
    void productUpdateRequestRejectsBlankName() {
        ProductUpdateRequest request = new ProductUpdateRequest("   ", null, null, null);

        Set<ConstraintViolation<ProductUpdateRequest>> violations = validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")));
    }

    @Test
    void productUpdateRequestAllowsNullNameWhenAnotherFieldIsPresent() {
        ProductUpdateRequest request = new ProductUpdateRequest(
                null,
                ProductCategory.FOOD,
                null,
                null
        );

        assertTrue(validate(request).isEmpty());
    }

    @Test
    void productUpdateRequestRejectsEmptyPatch() {
        ProductUpdateRequest request = new ProductUpdateRequest(null, null, null, null);

        Set<ConstraintViolation<ProductUpdateRequest>> violations = validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation -> violation.getPropertyPath().toString()
                        .equals("anyFieldPresent")));
    }

    private <T> Set<ConstraintViolation<T>> validate(T request) {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            return validatorFactory.getValidator().validate(request);
        }
    }

    private Product createProduct(
            String name,
            ProductCategory category,
            Long price,
            String description
    ) {
        Product product = new TestProduct();
        product.update(name, category, price, description);
        return product;
    }

    private static class TestProduct extends Product {
    }
}
