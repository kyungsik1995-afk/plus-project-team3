package com.example.plus.domain.cart.service;

import com.example.plus.domain.cart.dto.CartAddRequest;
import com.example.plus.domain.cart.dto.CartItemResponse;
import com.example.plus.domain.cart.dto.CartItemUpdateRequest;
import com.example.plus.domain.cart.dto.CartResponse;
import com.example.plus.domain.cart.entity.Cart;
import com.example.plus.domain.cart.entity.CartItem;
import com.example.plus.domain.cart.repository.CartItemRepository;
import com.example.plus.domain.cart.repository.CartRepository;
import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.repository.ProductRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(this::createCartResponse)
                .orElseGet(() -> new CartResponse(List.of(), 0L));
    }

    @Transactional
    public void clearCart(Long memberId) {
        cartRepository.findByMemberId(memberId)
                .ifPresent(cart -> cartItemRepository.deleteAll(
                        cartItemRepository.findAllByCart(cart)
                ));
    }

    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        validateOwnership(memberId, cartItem);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public CartItemResponse updateItemQuantity(
            Long memberId,
            Long cartItemId,
            CartItemUpdateRequest request
    ) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        validateOwnership(memberId, cartItem);
        validateStock(cartItem.getProduct(), request.quantity());
        cartItem.changeQuantity(request.quantity());

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse addItem(Long memberId, CartAddRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.create(memberId)));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .map(existingItem -> addToExistingItem(existingItem, request.quantity(), product))
                .orElseGet(() -> createNewItem(cart, product, request.quantity()));

        return CartItemResponse.from(cartItem);
    }

    private CartResponse createCartResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findAllByCartWithProduct(cart).stream()
                .map(CartItemResponse::from)
                .toList();

        long totalPrice = items.stream()
                .mapToLong(CartItemResponse::itemTotalPrice)
                .sum();

        return new CartResponse(items, totalPrice);
    }

    private void validateOwnership(Long memberId, CartItem cartItem) {
        if (!Objects.equals(memberId, cartItem.getCart().getMemberId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private CartItem addToExistingItem(CartItem cartItem, Integer quantity, Product product) {
        int finalQuantity = cartItem.getQuantity() + quantity;
        validateStock(product, finalQuantity);
        cartItem.addQuantity(quantity);
        return cartItem;
    }

    private CartItem createNewItem(Cart cart, Product product, Integer quantity) {
        validateStock(product, quantity);
        return cartItemRepository.save(CartItem.create(cart, product, quantity));
    }

    private void validateStock(Product product, Integer quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }
}
