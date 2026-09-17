package com.example.plus.domain.order.controller;

import com.example.plus.domain.order.dto.OrderCheckoutRequest;
import com.example.plus.domain.order.dto.OrderCheckoutResponse;
import com.example.plus.domain.order.dto.OrderItemResponse;
import com.example.plus.domain.order.dto.OrderResponse;
import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.facade.OrderFacade;
import com.example.plus.domain.order.service.OrderService;
import com.example.plus.global.common.response.ApiResponse;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderCheckoutResponse> createOrder(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody OrderCheckoutRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        return ApiResponse.success(
                orderFacade.createOrder(memberId, request)
        );
    }

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

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        Order order = orderService.findOrderEntity(memberId, orderId);

        return ApiResponse.success(orderService.toResponse(order));
    }

    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        Order order = orderFacade.cancelOrder(memberId, orderId);

        return ApiResponse.success(
                orderService.toResponse(order)
        );
    }
}
