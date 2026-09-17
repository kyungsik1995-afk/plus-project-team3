package com.example.plus.domain.order.dto;

import java.util.List;

/**
 * 주문 생성 요청 DTO
 *
 * <p>주문할 장바구니 상품 ID 목록을 전달받는다.</p>
 *
 * <p>목록이 비어 있으면 장바구니 전체 상품을 주문 대상으로 한다.</p>
 */
public record OrderCheckoutRequest(
        List<Long> cartItemIds
) {
    public OrderCheckoutRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }
    }
}