package com.example.plus.domain.payment.facade;

import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.entity.OrderStatus;
import com.example.plus.domain.payment.dto.PaymentConfirmRequest;
import com.example.plus.domain.payment.dto.PaymentConfirmResponse;
import com.example.plus.domain.payment.dto.RefundRequest;
import com.example.plus.domain.payment.dto.RefundResponse;
import com.example.plus.domain.payment.entity.Payment;
import com.example.plus.domain.payment.entity.PaymentStatus;
import com.example.plus.domain.payment.service.PaymentCommandService;
import com.example.plus.domain.payment.service.PaymentRefundService;
import com.example.plus.domain.payment.service.PaymentService;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentRefundService paymentRefundService;

    public PaymentConfirmResponse paymentConfirm(
            Long memberId,
            PaymentConfirmRequest request
    ) {
        Payment payment = paymentService.findByOrderIdWithOrder(request.orderId());
        Order order = payment.getOrder();

        validateOwner(order, memberId);

        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        if (!isPendingPayment(order)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (!request.paymentPrice().equals(payment.getFinalPrice())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return paymentCommandService.approvePaymentAndOrder(order.getId());
    }

    public RefundResponse paymentRefund(
            Long memberId,
            Long paymentId,
            RefundRequest request
    ) {
        Payment payment = paymentService.findByIdWithOrder(paymentId);

        validateOwner(payment.getOrder(), memberId);

        return paymentRefundService.refund(
                paymentId,
                memberId,
                request
        );
    }

    private void validateOwner(Order order, Long memberId) {
        if (!order.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isPendingPayment(Order order) {
        return order.getStatus() == OrderStatus.PAYMENT_PENDING;
    }
}