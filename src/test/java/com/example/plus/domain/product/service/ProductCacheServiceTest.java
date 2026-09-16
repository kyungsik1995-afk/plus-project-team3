package com.example.plus.domain.product.service;

import static com.example.plus.global.config.cache.CacheConfig.PRODUCT_LIST_CACHE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.plus.domain.product.dto.ProductCacheListResponse;
import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.dto.ProductUpdateRequest;
import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.entity.ProductCategory;
import com.example.plus.domain.product.repository.ProductRepository;
import com.example.plus.global.config.cache.CacheConfig;
import com.example.plus.global.exception.business.BusinessException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {CacheConfig.class, ProductService.class})
class ProductCacheServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        reset(productRepository);
        cacheManager.getCache(PRODUCT_LIST_CACHE).clear();
    }

    @Test
    void getV2ProductsCachesStaticDataAndLoadsLatestStocksOnEveryRequest() {
        ProductListRequest request = new ProductListRequest(
                ProductCategory.FOOD,
                1_000L,
                10_000L,
                0,
                20
        );
        Page<ProductCacheListResponse> cachedPage = new PageImpl<>(List.of(
                new ProductCacheListResponse(
                        1L,
                        "첫 번째 상품",
                        ProductCategory.FOOD,
                        5_000L
                ),
                new ProductCacheListResponse(
                        2L,
                        "두 번째 상품",
                        ProductCategory.FOOD,
                        7_000L
                )
        ));
        when(productRepository.findAllCachedByConditions(
                eq(ProductCategory.FOOD),
                eq(1_000L),
                eq(10_000L),
                any(Pageable.class)
        )).thenReturn(cachedPage);
        when(productRepository.findStockQuantitiesByProductIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, 5, 2L, 7))
                .thenReturn(Map.of(1L, 4, 2L, 6));

        Page<ProductListResponse> first = getV2Products(request);
        Page<ProductListResponse> second = getV2Products(request);

        assertEquals(List.of(
                new ProductListResponse(
                        1L,
                        "첫 번째 상품",
                        ProductCategory.FOOD,
                        5_000L,
                        5
                ),
                new ProductListResponse(
                        2L,
                        "두 번째 상품",
                        ProductCategory.FOOD,
                        7_000L,
                        7
                )
        ), first.getContent());
        assertEquals(List.of(4, 6), second.getContent().stream()
                .map(ProductListResponse::stockQuantity)
                .toList());
        verify(productRepository, times(1)).findAllCachedByConditions(
                eq(ProductCategory.FOOD),
                eq(1_000L),
                eq(10_000L),
                any(Pageable.class)
        );
        verify(productRepository, times(2))
                .findStockQuantitiesByProductIds(List.of(1L, 2L));
        verify(productRepository, never()).findById(any());
    }

    @Test
    void getCachedProductsSeparatesEverySearchConditionInCacheKey() {
        when(productRepository.findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        )).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(3, Pageable.class)));

        List<ProductListRequest> requests = List.of(
                new ProductListRequest(null, null, null, 0, 20),
                new ProductListRequest(ProductCategory.FOOD, null, null, 0, 20),
                new ProductListRequest(null, 1_000L, null, 0, 20),
                new ProductListRequest(null, null, 10_000L, 0, 20),
                new ProductListRequest(null, null, null, 1, 20),
                new ProductListRequest(null, null, null, 0, 10)
        );

        requests.forEach(productService::getCachedProducts);
        requests.forEach(productService::getCachedProducts);

        verify(productRepository, times(requests.size())).findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        );
    }

    @Test
    void cacheResponseDoesNotContainStockQuantity() {
        boolean hasStockQuantity = Arrays.stream(ProductCacheListResponse.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("stockQuantity"));

        assertFalse(hasStockQuantity);
    }

    @Test
    void existingProductListResponseStillContainsStockQuantity() {
        boolean hasStockQuantity = Arrays.stream(ProductListResponse.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("stockQuantity"));

        assertTrue(hasStockQuantity);
    }

    @Test
    void updateProductEvictsAllProductListCacheEntriesAfterSuccess() {
        ProductListRequest request = new ProductListRequest(null, null, null, 0, 20);
        when(productRepository.findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        )).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(3, Pageable.class)));

        Product product = new TestProduct();
        product.update("기존 상품", ProductCategory.FOOD, 5_000L, "기존 설명");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        getV2Products(request);
        getV2Products(request);
        productService.updateProduct(
                1L,
                new ProductUpdateRequest("수정 상품", null, null, null)
        );
        getV2Products(request);

        verify(productRepository, times(2)).findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        );
    }

    @Test
    void updateProductDoesNotEvictProductListCacheWhenUpdateFails() {
        ProductListRequest request = new ProductListRequest(null, null, null, 0, 20);
        when(productRepository.findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        )).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(3, Pageable.class)));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        getV2Products(request);
        assertThrows(
                BusinessException.class,
                () -> productService.updateProduct(
                        1L,
                        new ProductUpdateRequest("수정 상품", null, null, null)
                )
        );
        getV2Products(request);

        verify(productRepository, times(1)).findAllCachedByConditions(
                nullable(ProductCategory.class),
                nullable(Long.class),
                nullable(Long.class),
                any(Pageable.class)
        );
    }

    private Page<ProductListResponse> getV2Products(ProductListRequest request) {
        Page<ProductCacheListResponse> cachedProducts =
                productService.getCachedProducts(request);
        return productService.getProductsWithLatestStock(cachedProducts);
    }

    private static class TestProduct extends Product {
    }
}
