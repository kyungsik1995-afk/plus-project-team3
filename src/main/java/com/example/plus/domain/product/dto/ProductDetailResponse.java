package com.example.plus.domain.product.dto;

import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.entity.ProductCategory;

public record ProductDetailResponse(
        Long productId,
        String name,
        ProductCategory category,
        Long price,
        Integer stockQuantity,
        String description
) {

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getDescription()
        );
    }
}
