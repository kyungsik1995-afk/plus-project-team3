package com.example.plus.domain.order.controller;

import com.example.plus.domain.order.dto.OrderResponse;
import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.service.OrderService;
import com.example.plus.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 내 주문 목록을 최신순으로 조회한다.
     */
    @GetMapping
    public ApiResponse<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Long memberId
    ) {
        List<OrderResponse> responses = orderService.findOrderEntities(memberId)
                .stream()
                .map(orderService::toResponse)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 특정 주문의 상세 정보를 조회한다.
     *
     * 주문 ID뿐만 아니라 현재 로그인한 회원의 ID를 함께 전달하여
     * 본인의 주문인지 OrderService에서 확인한다.
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        Order order = orderService.findOrderEntity(memberId, orderId);

        return ApiResponse.success(orderService.toResponse(order));
    }
}