package com.example.plus.domain.payment.service;

import com.example.plus.domain.cart.service.CartService;
import com.example.plus.domain.payment.dto.PaymentConfirmResponse;
import com.example.plus.domain.payment.entity.Payment;
import com.example.plus.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final CartService cartService;

    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.confirmPayment(payment);
        order.confirm();

        // 결제 성공 시 주문한 상품의 장바구니 항목만 삭제한다.
        List<Long> productIds = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .toList();

        cartService.deleteItemsByProductIds(
                order.getMember().getId(),
                productIds
        );

        return new PaymentConfirmResponse(
                payment.getId(),
                order.getId(),
                payment.getFinalPrice(),
                payment.getStatus().name(),
                order.getStatus().name()
        );
    }
}
