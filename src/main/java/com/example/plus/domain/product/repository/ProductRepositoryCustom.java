package com.example.plus.domain.product.repository;

import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<ProductListResponse> findAllByConditions(
            ProductCategory category,
            Long minPrice,
            Long maxPrice,
            Pageable pageable
    );
}
