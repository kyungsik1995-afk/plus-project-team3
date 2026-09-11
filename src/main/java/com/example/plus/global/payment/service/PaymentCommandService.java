package com.example.plus.global.payment.service;

import com.example.plus.global.payment.dto.PaymentConfirmResponse;
import com.example.plus.global.payment.entity.Payment;
import com.example.plus.orders.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;

    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.confirmPayment(payment);
        order.confirm();

        return new PaymentConfirmResponse(
                payment.getId(),
                order.getId(),
                payment.getFinalPrice(),
                payment.getStatus().name(),
                order.getStatus().name()
        );
    }
}
