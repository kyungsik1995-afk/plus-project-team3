package com.example.plus.domain.cart.controller;

import com.example.plus.domain.cart.dto.CartAddRequest;
import com.example.plus.domain.cart.dto.CartItemResponse;
import com.example.plus.domain.cart.dto.CartItemUpdateRequest;
import com.example.plus.domain.cart.dto.CartResponse;
import com.example.plus.domain.cart.service.CartService;
import com.example.plus.global.common.response.ApiResponse;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    // TODO(authentication): Replace this with the member ID supplied by the authenticated Principal.
    private static final Long TEMP_MEMBER_ID = 1L;

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.success(cartService.getCart(TEMP_MEMBER_ID));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        cartService.clearCart(TEMP_MEMBER_ID);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable("cartItemId") Long cartItemId
    ) {
        cartService.deleteItem(TEMP_MEMBER_ID, cartItemId);
        return ApiResponse.ok();
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateItemQuantity(
            @PathVariable("cartItemId") Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        return ApiResponse.success(
                cartService.updateItemQuantity(TEMP_MEMBER_ID, cartItemId, request)
        );
    }

    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addItem(
            @Valid @RequestBody CartAddRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        return ApiResponse.success(cartService.addItem(TEMP_MEMBER_ID, request));
    }
}
