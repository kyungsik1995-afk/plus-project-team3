package com.example.plus.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundItemRequest(
        @NotNull(message = "주문 상품 ID는 필수입니다.")
        Long orderItemId,

        @NotNull(message = "환불 수량은 필수입니다.")
        @Positive(message = "환불 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
