package com.example.plus.domain.payment.dto;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        Long totalPrice,
        String status,
        LocalDateTime paidAt
) {
}
