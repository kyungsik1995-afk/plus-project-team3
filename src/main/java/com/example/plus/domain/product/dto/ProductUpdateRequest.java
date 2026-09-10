package com.example.plus.domain.product.dto;

import com.example.plus.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ProductUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "상품명은 공백일 수 없습니다.")
        String name,

        ProductCategory category,

        @Positive(message = "가격은 양수여야 합니다.")
        Long price,

        String description
) {

    @AssertTrue(message = "수정할 필드를 하나 이상 입력해야 합니다.")
    public boolean isAnyFieldPresent() {
        return name != null || category != null || price != null || description != null;
    }
}
