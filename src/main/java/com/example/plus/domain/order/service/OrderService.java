package com.example.plus.domain.order.service;

import com.example.plus.domain.order.dto.OrderItemResponse;
import com.example.plus.domain.order.dto.OrderResponse;
import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.repository.OrderRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    // 내 주문 목록 조회
    public List<Order> findOrderEntities(Long memberId) {
        return orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    // 주문 상세 조회
    public Order findOrderEntity(Long memberId, Long orderId) {

        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ORDER_NOT_FOUND)
                );

        // 본인의 주문인지 확인
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return order;
    }

    // 주문 정보를 응답 DTO로 변환
    public OrderResponse toResponse(Order order) {

        List<OrderItemResponse> orderItems = order.getOrderItems()
                .stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getProductName(),
                        orderItem.getOrderPrice(),
                        orderItem.getQuantity()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                orderItems
        );
    }
}