package com.example.plus.global.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,

        @NotNull(message = "결제 금액은 필수입니다.")
        Long paymentPrice
) {
}
