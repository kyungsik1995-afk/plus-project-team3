package com.example.plus.domain.payment.service;

import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import com.example.plus.domain.payment.dto.PaymentResponse;
import com.example.plus.domain.payment.entity.Payment;
import com.example.plus.domain.payment.entity.PaymentStatus;
import com.example.plus.domain.payment.repository.PaymentRepository;
import com.example.plus.orders.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse getPayment(Long customerId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getFinalPrice(),
                payment.getStatus().name(),
                payment.getPaidAt()
        );
    }

    public Payment findByOrderIdWithOrder(Long orderId) {
        return paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findByIdWithOrder(Long paymentId) {
        return paymentRepository.findByIdWithOrder(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional
    public void createPayment(Order order, Long totalPrice) {
        Payment payment = Payment.create(totalPrice, PaymentStatus.IN_PROGRESS, order);
        paymentRepository.save(payment);
    }

    @Transactional
    public void confirmPayment(Payment payment) {
        payment.markAsPaid();
    }

    @Transactional
    public void failPayment(Payment payment) {
        payment.markAsFailed();
    }
}
