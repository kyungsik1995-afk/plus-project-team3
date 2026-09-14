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
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final OrderService orderService;
    private final MemberService memberService;
    private final CartService cartService;
    private final PaymentService paymentService;

    @Transactional
    public OrderCheckoutResponse createOrder(
            Long memberId,
            OrderCheckoutRequest request
    ) {
        // 1. 회원 존재 여부 확인
        Member member = memberService.findById(memberId);

        // 2. 현재 회원의 장바구니 상품 조회
        List<CartItem> cartItems = cartService.getCartItems(memberId);

        // 3. 주문할 장바구니 상품 결정
        List<CartItem> orderCartItems;

        if (request.cartItemIds().isEmpty()) {
            // cartItemIds가 비어 있으면 장바구니 전체를 주문한다.
            orderCartItems = cartItems;
        } else {
            // cartItemIds가 있으면 해당 상품만 주문한다.
            Set<Long> selectedCartItemIds = Set.copyOf(request.cartItemIds());

            orderCartItems = cartItems.stream()
                    .filter(cartItem ->
                            selectedCartItemIds.contains(cartItem.getId())
                    )
                    .toList();

            // 요청한 상품 중 현재 회원의 장바구니에 없는 상품이 있는지 확인한다.
            if (orderCartItems.size() != selectedCartItemIds.size()) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
        }

        // 4. 주문할 상품이 하나도 없는 경우
        if (orderCartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 5. 장바구니 상품을 OrderItem으로 변환하면서 재고 차감
        List<OrderItem> orderItems = orderCartItems.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();

                    // 재고가 부족하면 BusinessException이 발생하고
                    // @Transactional에 의해 지금까지의 변경도 함께 롤백된다.
                    product.decreaseStock(cartItem.getQuantity());

                    // 주문 당시 상품명과 가격을 스냅샷으로 저장한다.
                    return new OrderItem(
                            product,
                            product.getName(),
                            product.getPrice(),
                            cartItem.getQuantity()
                    );
                })
                .toList();

        // 6. 주문 총 금액 계산
        Long totalAmount = orderItems.stream()
                .mapToLong(orderItem ->
                        orderItem.getOrderPrice() * orderItem.getQuantity()
                )
                .sum();

        // 7. Order 생성
        Order order = orderService.createOrder(
                member,
                orderItems,
                totalAmount
        );

        // 8. 결제 대기 상태의 Payment 생성
        paymentService.createPayment(order, totalAmount);

        // 9. 주문 생성 시에는 장바구니를 삭제하지 않는다.
        //    장바구니 삭제는 모의 결제 성공 시 처리한다.

        // 10. 주문 생성 결과 반환
        return new OrderCheckoutResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name()
        );
    }
}