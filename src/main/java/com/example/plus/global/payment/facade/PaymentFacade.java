//package com.example.plus.global.payment.facade;
//
//import com.example.plus.global.exception.ErrorCode;
//import com.example.plus.global.exception.business.BusinessException;
//import com.example.plus.global.payment.dto.PaymentCancelRequest;
//import com.example.plus.global.payment.dto.PaymentCancelResponse;
//import com.example.plus.global.payment.dto.PaymentConfirmResponse;
//import com.example.plus.global.payment.entity.Payment;
//import com.example.plus.global.payment.service.PaymentCommandService;
//import com.example.plus.global.payment.service.PaymentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentFacade {
//    private final PaymentService paymentService;
//    private final PaymentCommandService commandService;
//    private final PaymentGateway paymentGateway;
//    private final PointService pointService;
//
//    public PaymentConfirmResponse paymentConfirm(Long userId, PaymentConfirmRequest confirmRequest) {
//        Payment payment = paymentService.findByOrderIdWithOrder(confirmRequest.orderId());
//        Order order = payment.getOrder();
//        Long customerId = order.getCustomer().getId();
//
//        //주문자와 사용자 일치 확인
//        if (!customerId.equals(userId)) {
//            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
//        }
//
//        //결제 중복 확인
//        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
//            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
//        }
//
//        //주문 상태 전이 가능 여부 확인
//        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
//            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
//        }
//
//        //포인트 사용 가능 여부 확인
//        if (pointService.getBalance(customerId).Balance() >= payment.getPointUsed()) {
//            throw new RuntimeException("Invalid point");
//        }
//
//        //PG 사에서 실결제 정보 확인
//        PaymentGatewayResponse paymentInfo = paymentGateway.getPayment(payment.getPortoneId());
//
//        //결제 금액과 주문 금액 검증
//        if (!confirmRequest.paymentPrice().equals(paymentInfo.totalAmount())) {
//            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
//        }
//
//        //결제 실패 시 payment 상태를 FAILED, order 상태를 CANCELLED로
//        //상품 재고 전량 복구
//        if(confirmRequest.result().equals("FAIL")) {
//            commandService.failPaymentAndOrder(order.getId());
//
//            throw new BusinessException(ErrorCode.PG_FAILURE);
//        }
//
//        return commandService.approvePaymentAndOrder(order.getId());
//    }
//
//    public PaymentCancelResponse paymentCancel(Long customerId, Long paymentId, PaymentCancelRequest request) {
//        String cancelReason = (request != null && request.reason() != null)
//                ? request.reason() : "사용자 요청 취소";
//
//        log.info("PaymentId: {}", paymentId);
//
//        Payment payment = paymentService.findByIdWithOrder(paymentId);
//        if (!payment.getOrder().getCustomer().getId().equals(customerId)) {
//            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
//        }
//
//        if (payment.getStatus() != PaymentStatus.PAID) {
//            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
//        }
//
//        PaymentCancelResponse response = commandService.cancelPaymentAndOrder(paymentId);
//
//        try {
//            paymentGateway.cancelPayment(payment.getPortoneId(), cancelReason);
//        } catch (Exception e) {
//            log.error("PG 결제 취소 실패 : DB 커밋됨, 수동 처리 필요 paymentId={}", response.portoneId(), e);
//        }
//
//        return response;
//    }
//}


package com.example.plus.global.payment.facade;

import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import com.example.plus.global.payment.dto.PaymentConfirmRequest;
import com.example.plus.global.payment.dto.PaymentConfirmResponse;
import com.example.plus.global.payment.dto.RefundRequest;
import com.example.plus.global.payment.dto.RefundResponse;
import com.example.plus.global.payment.entity.Payment;
import com.example.plus.global.payment.entity.PaymentStatus;
import com.example.plus.global.payment.service.PaymentCommandService;
import com.example.plus.global.payment.service.PaymentRefundService;
import com.example.plus.global.payment.service.PaymentService;
import com.example.plus.orders.entity.Order;
import com.example.plus.orders.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentRefundService paymentRefundService;

    public PaymentConfirmResponse paymentConfirm(
            Long customerId,
            PaymentConfirmRequest request
    ) {
        Payment payment = paymentService.findByOrderIdWithOrder(request.orderId());
        Order order = payment.getOrder();

        validateOwner(order, customerId);

        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        if (!isPendingPayment(order)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (!request.paymentPrice().equals(payment.getFinalPrice())) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }

        return paymentCommandService.approvePaymentAndOrder(order.getId());
    }

    public RefundResponse paymentRefund(
            Long customerId,
            Long paymentId,
            RefundRequest request
    ) {
        Payment payment = paymentService.findByIdWithOrder(paymentId);
        validateOwner(payment.getOrder(), customerId);

        return paymentRefundService.refund(paymentId, customerId, request);
    }

    private void validateOwner(Order order, Long customerId) {
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isPendingPayment(Order order) {
        return order.getOrderStatus() == OrderStatus.PENDING_PAYMENT;
    }
}
