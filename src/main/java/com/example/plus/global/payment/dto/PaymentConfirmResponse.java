package com.example.plus.global.payment.dto;

public record PaymentConfirmResponse(
        Long paymentId,
        Long orderId,
        Long amount,
        String paymentStatus,
        String orderStatus
) {
}
