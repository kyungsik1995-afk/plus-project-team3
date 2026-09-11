package com.example.plus.domain.order.dto;

public record OrderCheckoutResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        String status
) {
}