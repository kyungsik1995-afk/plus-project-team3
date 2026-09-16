package com.example.plus.domain.product.dto;

import com.example.plus.domain.product.entity.ProductCategory;

public record ProductCacheListResponse(
        Long productId,
        String name,
        ProductCategory category,
        Long price
) {
}
