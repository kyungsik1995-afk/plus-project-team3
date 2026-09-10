package com.example.plus.domain.cart.dto;

import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        Long totalPrice
) {
}
