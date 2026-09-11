package com.example.plus.domain.order.facade;

import com.example.plus.domain.member.entity.Member;
import com.example.plus.domain.member.service.MemberService;
import com.example.plus.domain.order.dto.OrderCheckoutRequest;
import com.example.plus.domain.order.dto.OrderCheckoutResponse;
import com.example.plus.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFacade {

    private final OrderService orderService;
    private final MemberService memberService;

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

        return null;
    }
}