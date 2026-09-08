package com.example.plus.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_003", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_004", "서버 오류가 발생했습니다."),

    // 회원
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_001", "이미 가입된 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "회원을 찾을 수 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "MEMBER_003", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_002", "상품의 재고가 부족합니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "유효하지 않은 가격 범위입니다."),
    INVALID_PRODUCT(HttpStatus.BAD_REQUEST, "PRODUCT_004", "유효하지 않은 상품 정보입니다."),

    // 장바구니
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_002", "장바구니 상품을 찾을 수 없습니다."),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "상품 수량은 1개 이상이어야 합니다."),
    CART_ITEM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CART_004", "본인의 장바구니 상품이 아닙니다."),
    CART_ITEM_STOCK_EXCEEDED(HttpStatus.CONFLICT, "CART_005", "요청 수량이 현재 상품 재고를 초과합니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_002", "본인의 주문이 아닙니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "ORDER_003", "현재 주문 상태에서는 처리할 수 없습니다."),

    // 결제
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.CONFLICT, "PAYMENT_002", "현재 결제 상태에서는 처리할 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_003", "결제 금액이 일치하지 않습니다."),
    ALREADY_PROCESSED_PAYMENT(HttpStatus.CONFLICT, "PAYMENT_004", "이미 처리된 결제입니다."),
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT_005", "본인의 결제가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}