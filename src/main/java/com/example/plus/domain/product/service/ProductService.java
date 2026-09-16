package com.example.plus.domain.product.service;

import static com.example.plus.global.config.cache.CacheConfig.PRODUCT_LIST_CACHE;

import com.example.plus.domain.product.dto.ProductCacheListResponse;
import com.example.plus.domain.product.dto.ProductDetailResponse;
import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.dto.ProductUpdateRequest;
import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.repository.ProductRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductListResponse> getProducts(ProductListRequest request) {
        validatePriceRange(request.minPrice(), request.maxPrice());

        PageRequest pageRequest = createPageRequest(request);

        return productRepository.findAllByConditions(
                request.category(),
                request.minPrice(),
                request.maxPrice(),
                pageRequest
        );
    }

    @Cacheable(
            cacheNames = PRODUCT_LIST_CACHE,
            key = "#p0"
    )
    public Page<ProductCacheListResponse> getCachedProducts(ProductListRequest request) {
        validatePriceRange(request.minPrice(), request.maxPrice());

        PageRequest pageRequest = createPageRequest(request);

        return productRepository.findAllCachedByConditions(
                request.category(),
                request.minPrice(),
                request.maxPrice(),
                pageRequest
        );
    }

    public Page<ProductListResponse> getProductsWithLatestStock(
            Page<ProductCacheListResponse> cachedProducts
    ) {
        List<Long> productIds = cachedProducts.getContent().stream()
                .map(ProductCacheListResponse::productId)
                .toList();
        Map<Long, Integer> stockQuantities =
                productRepository.findStockQuantitiesByProductIds(productIds);

        return cachedProducts.map(product -> new ProductListResponse(
                product.productId(),
                product.name(),
                product.category(),
                product.price(),
                stockQuantities.get(product.productId())
        ));
    }

    public ProductDetailResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return ProductDetailResponse.from(product);
    }

    @Transactional
    @CacheEvict(cacheNames = PRODUCT_LIST_CACHE, allEntries = true)
    public ProductDetailResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.update(
                request.name(),
                request.category(),
                request.price(),
                request.description()
        );

        return ProductDetailResponse.from(product);
    }

    private PageRequest createPageRequest(ProductListRequest request) {
        return PageRequest.of(
                request.page(),
                request.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    private void validatePriceRange(Long minPrice, Long maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "minPrice는 maxPrice보다 클 수 없습니다."
            );
        }
    }
}
