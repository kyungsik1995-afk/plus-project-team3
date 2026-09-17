package com.example.plus.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        String status,
        String paymentStatus,
        LocalDateTime createdAt,
        List<OrderItemResponse> orderItems
) {
}