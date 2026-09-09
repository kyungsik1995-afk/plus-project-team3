package com.example.plus.domain.product.dto;

import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.entity.ProductCategory;

public record ProductListResponse(
        Long productId,
        String name,
        ProductCategory category,
        Long price,
        Integer stockQuantity
) {

    public static ProductListResponse from(Product product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
}
