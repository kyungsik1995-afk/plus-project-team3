package com.example.plus.domain.order.service;

import com.example.plus.domain.member.entity.Member;
import com.example.plus.domain.order.dto.OrderItemResponse;
import com.example.plus.domain.order.dto.OrderResponse;
import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.entity.OrderItem;
import com.example.plus.domain.order.entity.OrderStatus;
import com.example.plus.domain.order.repository.OrderRepository;
import com.example.plus.domain.payment.entity.Payment;
import com.example.plus.domain.payment.service.PaymentService;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    /**
     * 주문을 생성하고 저장한다.
     *
     * 전달받은 주문 상품을 Order에 연결한 뒤 하나의 주문으로 저장한다.
     * Order의 Cascade 설정에 의해 OrderItem도 함께 저장된다.
     */
    @Transactional
    public Order createOrder(
            Member member,
            List<OrderItem> orderItems,
            Long totalAmount
    ) {
        String orderNumber = "ORD-" + UUID.randomUUID();

        Order order = new Order(
                member,
                orderNumber,
                totalAmount,
                OrderStatus.PAYMENT_PENDING
        );

        orderItems.forEach(order::addOrderItem);

        return orderRepository.save(order);
    }

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

// 주문에 연결된 결제 정보를 조회한다.
        // Payment → Order 단방향 연관관계는 그대로 유지한다.
        Payment payment = paymentService.findByOrderIdWithOrder(order.getId());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                payment.getStatus().name(),
                order.getCreatedAt(),
                orderItems
        );
    }
}