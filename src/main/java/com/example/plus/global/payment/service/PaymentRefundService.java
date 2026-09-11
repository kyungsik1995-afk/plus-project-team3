package com.example.plus.global.payment.service;

import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import com.example.plus.global.payment.dto.RefundItemRequest;
import com.example.plus.global.payment.dto.RefundRequest;
import com.example.plus.global.payment.dto.RefundResponse;
import com.example.plus.global.payment.entity.Payment;
import com.example.plus.global.payment.entity.PaymentStatus;
import com.example.plus.global.payment.entity.Refund;
import com.example.plus.global.payment.entity.RefundItem;
import com.example.plus.global.payment.repository.PaymentRepository;
import com.example.plus.global.payment.repository.RefundItemRepository;
import com.example.plus.global.payment.repository.RefundRepository;
import com.example.plus.orders.entity.Order;
import com.example.plus.orders.entity.OrderItem;
import com.example.plus.orders.repository.OrderItemRepository;
import com.example.plus.products.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;

    @Transactional
    public RefundResponse refund(Long paymentId, Long customerId, RefundRequest request) {
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        validatePaymentStatus(payment);

        Order order = payment.getOrder();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        if (request == null || request.items() == null || request.items().isEmpty()) {
            return refundAll(payment, order, orderItems, request == null ? null : request.reason());
        }

        return partialRefund(payment, order, orderItems, request);
    }

    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PAID
                && payment.getStatus() != PaymentStatus.PART_CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
    }

    private RefundResponse refundAll(
            Payment payment,
            Order order,
            List<OrderItem> orderItems,
            String reason
    ) {
        long alreadyRefunded = refundRepository.sumRefundedAmount(payment.getId());
        long refundAmount = payment.getFinalPrice() - alreadyRefunded;

        if (refundAmount <= 0) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REFUND);
        }

        Refund refund = new Refund(
                payment,
                refundAmount,
                reason,
                LocalDateTime.now()
        );
        refundRepository.save(refund);

        for (OrderItem orderItem : orderItems) {
            int refundedQuantity = refundItemRepository.sumRefundedQuantity(orderItem.getId());
            int remainingQuantity = orderItem.getQuantity() - refundedQuantity;

            if (remainingQuantity <= 0) {
                continue;
            }

            long itemRefundAmount = orderItem.getProductPrice() * remainingQuantity;
            refundItemRepository.save(new RefundItem(
                    refund,
                    orderItem,
                    remainingQuantity,
                    itemRefundAmount
            ));

            Product product = orderItem.getProduct();
            product.restoreStock(remainingQuantity);
        }

        payment.markAsCancelled();
        order.cancel();

        return new RefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                refundAmount,
                refund.getStatus().name(),
                "결제 취소 및 환불이 완료되었습니다."
        );
    }

    private RefundResponse partialRefund(
            Payment payment,
            Order order,
            List<OrderItem> orderItems,
            RefundRequest request
    ) {
        Map<Long, OrderItem> orderItemMap = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        Set<Long> requestedItemIds = new HashSet<>();
        long totalRefundAmount = 0L;

        for (RefundItemRequest itemRequest : request.items()) {
            if (itemRequest == null || itemRequest.orderItemId() == null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM);
            }
            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_QUANTITY);
            }
            if (!requestedItemIds.add(itemRequest.orderItemId())) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM);
            }

            OrderItem orderItem = orderItemMap.get(itemRequest.orderItemId());
            if (orderItem == null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM);
            }

            int alreadyRefunded = refundItemRepository.sumRefundedQuantity(orderItem.getId());
            int remainingQuantity = orderItem.getQuantity() - alreadyRefunded;

            if (itemRequest.quantity() > remainingQuantity) {
                throw new BusinessException(ErrorCode.REFUND_QUANTITY_MISMATCH);
            }

            totalRefundAmount += orderItem.getProductPrice() * itemRequest.quantity();
        }

        long alreadyRefundedAmount = refundRepository.sumRefundedAmount(payment.getId());
        if (alreadyRefundedAmount + totalRefundAmount > payment.getFinalPrice()) {
            throw new BusinessException(ErrorCode.REFUND_AMOUNT_MISMATCH);
        }

        Refund refund = new Refund(
                payment,
                totalRefundAmount,
                request.reason(),
                LocalDateTime.now()
        );
        refundRepository.save(refund);

        for (RefundItemRequest itemRequest : request.items()) {
            OrderItem orderItem = orderItemMap.get(itemRequest.orderItemId());
            long itemRefundAmount = orderItem.getProductPrice() * itemRequest.quantity();

            refundItemRepository.save(new RefundItem(
                    refund,
                    orderItem,
                    itemRequest.quantity(),
                    itemRefundAmount
            ));

            orderItem.getProduct().restoreStock(itemRequest.quantity());
        }

        long totalRefunded = alreadyRefundedAmount + totalRefundAmount;
        boolean fullyRefunded = totalRefunded >= payment.getFinalPrice();

        if (fullyRefunded) {
            payment.markAsCancelled();
            order.cancel();
        } else {
            payment.markAsPartCancelled();
        }

        return new RefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                totalRefundAmount,
                refund.getStatus().name(),
                fullyRefunded
                        ? "결제 취소 및 환불이 완료되었습니다."
                        : "부분 결제 취소 및 환불이 완료되었습니다."
        );
    }
}
