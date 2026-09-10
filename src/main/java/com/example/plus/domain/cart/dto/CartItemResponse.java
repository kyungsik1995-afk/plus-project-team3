package com.example.plus.domain.cart.dto;

import com.example.plus.domain.cart.entity.CartItem;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        Long productPrice,
        Integer quantity,
        Long itemTotalPrice
) {

    public static CartItemResponse from(CartItem cartItem) {
        Long productPrice = cartItem.getProduct().getPrice();
        Integer quantity = cartItem.getQuantity();

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                productPrice,
                quantity,
                productPrice * quantity
        );
    }
}
