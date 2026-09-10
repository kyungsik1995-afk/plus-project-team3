package com.example.plus.domain.cart.dto;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long productPrice,
        Integer quantity,
        Long itemTotalPrice
) {
}
