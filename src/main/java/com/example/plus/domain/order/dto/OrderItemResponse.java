package com.example.plus.domain.order.dto;

public record OrderItemResponse(
        String productName,
        Long orderPrice,
        Integer quantity
) {
}