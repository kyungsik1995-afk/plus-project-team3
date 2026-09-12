package com.example.plus.domain.order.facade;

import com.example.plus.domain.cart.entity.CartItem;
import com.example.plus.domain.cart.service.CartService;
import com.example.plus.domain.member.entity.Member;
import com.example.plus.domain.member.service.MemberService;
import com.example.plus.domain.order.dto.OrderCheckoutRequest;
import com.example.plus.domain.order.dto.OrderCheckoutResponse;
import com.example.plus.domain.order.entity.Order;
import com.example.plus.domain.order.entity.OrderItem;
import com.example.plus.domain.order.service.OrderService;
import com.example.plus.domain.payment.service.PaymentService;
import com.example.plus.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final OrderService orderService;
    private final MemberService memberService;
    private final CartService cartService;
    private final PaymentService paymentService;

    /**
     * 주문 생성
     *
     * 주문 생성에 필요한 여러 도메인의 작업을 하나의 트랜잭션으로 묶는다.
     * 회원 확인 → 장바구니 상품 확인 → 재고 차감 → 주문 생성 → 결제 생성
     * 과정을 하나의 흐름으로 조율한다.
     */
    @Transactional
    public OrderCheckoutResponse createOrder(
            Long memberId,
            OrderCheckoutRequest request
    ) {
        // 주문을 생성할 회원을 조회하고, 존재하지 않으면 예외를 발생시킨다.
        Member member = memberService.findById(memberId);

        // 회원의 장바구니 상품을 조회한다.
        List<CartItem> cartItems = cartService.getCartItems(memberId);

        // 장바구니 상품을 주문 상품으로 변환하고 재고를 차감한다.
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();

                    product.decreaseStock(cartItem.getQuantity());

                    return new OrderItem(
                            product,
                            product.getName(),
                            product.getPrice(),
                            cartItem.getQuantity()
                    );
                })
                .toList();

        // 총 주문 금액을 계산한다.
        Long totalAmount = orderItems.stream()
                .mapToLong(orderItem ->
                        orderItem.getOrderPrice() * orderItem.getQuantity()
                )
                .sum();

        // 주문을 생성한다.
        Order order = orderService.createOrder(
                member,
                orderItems,
                totalAmount
        );

        // 주문에 대한 결제 정보를 생성한다.
        paymentService.createPayment(order, totalAmount);

        // 주문이 생성되었으므로 장바구니를 비운다.
        cartService.clearCart(memberId);

        // 주문 생성 결과를 반환한다.
        return new OrderCheckoutResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name()
        );
    }
}