package com.example.plus.domain.product.dto;

import com.example.plus.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductListRequest(
        ProductCategory category,

        @PositiveOrZero(message = "minPrice는 0 이상이어야 합니다.")
        Long minPrice,

        @PositiveOrZero(message = "maxPrice는 0 이상이어야 합니다.")
        Long maxPrice,

        @Min(value = 0, message = "page는 0 이상이어야 합니다.")
        Integer page,

        @Min(value = 1, message = "size는 1 이상이어야 합니다.")
        @Max(value = 100, message = "size는 100 이하여야 합니다.")
        Integer size
) {

    public ProductListRequest {
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
    }
}
