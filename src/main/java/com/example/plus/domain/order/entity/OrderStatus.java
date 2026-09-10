package com.example.plus.domain.order.entity;

/**
 * 주문의 상태를 정의하는 Enum
 *
 * <p>주문은 아무 상태로나 변경할 수 있는 것이 아니라,
 * 미리 정해진 상태 전이 규칙에 따라서만 변경할 수 있다.</p>
 *
 * <p>우리 프로젝트의 주문 상태 흐름</p>
 *
 * <pre>
 * PAYMENT_PENDING → COMPLETED
 * PAYMENT_PENDING → CANCELED
 * COMPLETED       → CANCELED
 * CANCELED        → 전이 불가
 * </pre>
 */
public enum OrderStatus {

    /**
     * 결제 대기 상태
     *
     * 주문이 생성되었지만 아직 결제가 완료되지 않은 상태이다.
     *
     * 결제가 성공하면 COMPLETED로 변경할 수 있고,
     * 주문을 취소하면 CANCELED로 변경할 수 있다.
     */
    PAYMENT_PENDING {

        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == COMPLETED || target == CANCELED;
        }
    },

    /**
     * 주문 완료 상태
     *
     * 결제가 정상적으로 완료되어 주문이 확정된 상태이다.
     *
     * 완료된 주문은 취소할 수 있지만,
     * 다시 PAYMENT_PENDING으로 되돌릴 수는 없다.
     */
    COMPLETED {

        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CANCELED;
        }
    },

    /**
     * 주문 취소 상태
     *
     * 취소된 주문은 최종 상태이므로
     * 다른 상태로 다시 변경할 수 없다.
     */
    CANCELED {

        @Override
        public boolean canTransitTo(OrderStatus target) {
            return false;
        }
    };

    /**
     * 현재 주문 상태에서 target 상태로 변경할 수 있는지 확인한다.
     *
     * <p>각 Enum 상수가 자신의 상태 전이 규칙을 직접 구현한다.
     * 따라서 서비스 계층에서 상태별 if문을 반복해서 작성하지 않아도 된다.</p>
     *
     * @param target 변경하려는 다음 주문 상태
     * @return 상태 변경이 가능하면 true, 불가능하면 false
     */
    public abstract boolean canTransitTo(OrderStatus target);
}