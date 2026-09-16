package com.example.plus.domain.cart.service;

import com.example.plus.domain.cart.dto.CartAddRequest;
import com.example.plus.domain.cart.dto.CartItemResponse;
import com.example.plus.domain.cart.dto.CartItemUpdateRequest;
import com.example.plus.domain.cart.dto.CartResponse;
import com.example.plus.domain.cart.entity.Cart;
import com.example.plus.domain.cart.entity.CartItem;
import com.example.plus.domain.cart.repository.CartItemRepository;
import com.example.plus.domain.cart.repository.CartRepository;
import com.example.plus.domain.member.entity.Member;
import com.example.plus.domain.member.repository.MemberRepository;
import com.example.plus.domain.payment.service.PaymentService;
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
    private final MemberRepository memberRepository;

    public CartResponse getCart(Long memberId) {
        return cartRepository.findByMember_Id(memberId)
                .map(this::createCartResponse)
                .orElseGet(() -> new CartResponse(List.of(), 0L));
    }

    @Transactional
    public void clearCart(Long memberId) {
        cartRepository.findByMember_Id(memberId)
                .ifPresent(cart -> cartItemRepository.deleteAll(
                        cartItemRepository.findAllByCart(cart)
                ));
    }

    /**
     * 주문 생성에 필요한 장바구니 상품을 조회한다.
     *
     * 장바구니가 존재하지 않으면 예외를 발생시킨다.
     * 상품 정보도 함께 조회하여 주문 생성 시 사용할 수 있도록 한다.
     */
    public List<CartItem> getCartItems(Long memberId) {
        Cart cart = cartRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        return cartItemRepository.findAllByCartWithProduct(cart);
    }

    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {
        CartItem cartItem = findOwnedCartItem(memberId, cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public CartItemResponse updateItemQuantity(
            Long memberId,
            Long cartItemId,
            CartItemUpdateRequest request
    ) {
        CartItem cartItem = findOwnedCartItem(memberId, cartItemId);
        validateStock(cartItem.getProduct(), request.quantity());
        cartItem.changeQuantity(request.quantity());

        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse addItem(Long memberId, CartAddRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Cart cart = cartRepository.findByMember_Id(memberId)
                .orElseGet(() -> createCart(memberId));

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

    private Cart createCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return cartRepository.save(Cart.create(member));
    }

    private CartItem findOwnedCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        validateOwnership(memberId, cartItem);
        return cartItem;
    }

    private void validateOwnership(Long memberId, CartItem cartItem) {
        if (!Objects.equals(memberId, cartItem.getCart().getMember().getId())) {
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

    /**
     * 주문한 상품에 해당하는 장바구니 상품만 삭제한다.
     *
     * 결제 성공 시 주문에 포함된 상품만 장바구니에서 삭제하기 위해 사용한다.
     * 주문하지 않은 다른 장바구니 상품은 그대로 유지한다.
     */
    @Transactional
    public void deleteItemsByProductIds(Long memberId, List<Long> productIds) {
        Cart cart = cartRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findAllByCartWithProduct(cart);

        List<CartItem> itemsToDelete = cartItems.stream()
                .filter(cartItem ->
                        productIds.contains(cartItem.getProduct().getId())
                )
                .toList();

        cartItemRepository.deleteAll(itemsToDelete);
    }
}
