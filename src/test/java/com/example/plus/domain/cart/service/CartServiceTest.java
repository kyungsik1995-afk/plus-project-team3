package com.example.plus.domain.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartReturnsEmptyResponseWhenCartDoesNotExist() {
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart(MEMBER_ID);

        assertEquals(List.of(), response.items());
        assertEquals(0L, response.totalPrice());
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void getCartReturnsEmptyResponseWhenCartHasNoItems() {
        Cart cart = Cart.create(MEMBER_ID);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of());

        CartResponse response = cartService.getCart(MEMBER_ID);

        assertEquals(List.of(), response.items());
        assertEquals(0L, response.totalPrice());
    }

    @Test
    void getCartCalculatesSingleItemTotalPrice() {
        Cart cart = Cart.create(MEMBER_ID);
        CartItem cartItem = cartItemForResponse(100L, PRODUCT_ID, "상품 A", 10_000L, 3);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));

        CartResponse response = cartService.getCart(MEMBER_ID);

        assertEquals(30_000L, response.items().get(0).itemTotalPrice());
        assertEquals(30_000L, response.totalPrice());
    }

    @Test
    void getCartSumsMultipleItemTotalPrices() {
        Cart cart = Cart.create(MEMBER_ID);
        CartItem firstItem = cartItemForResponse(100L, 10L, "상품 A", 10_000L, 2);
        CartItem secondItem = cartItemForResponse(200L, 20L, "상품 B", 30_000L, 3);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(firstItem, secondItem));

        CartResponse response = cartService.getCart(MEMBER_ID);

        assertEquals(2, response.items().size());
        assertEquals(110_000L, response.totalPrice());
    }

    @Test
    void getCartIncludesCartItemAndProductInformation() {
        Cart cart = Cart.create(MEMBER_ID);
        CartItem cartItem = cartItemForResponse(
                100L,
                PRODUCT_ID,
                "테스트 상품",
                10_000L,
                3
        );

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCart(cart)).thenReturn(List.of(cartItem));

        CartItemResponse itemResponse = cartService.getCart(MEMBER_ID).items().get(0);

        assertEquals(100L, itemResponse.cartItemId());
        assertEquals(PRODUCT_ID, itemResponse.productId());
        assertEquals("테스트 상품", itemResponse.productName());
        assertEquals(10_000L, itemResponse.productPrice());
        assertEquals(3, itemResponse.quantity());
    }

    @Test
    void updateItemQuantityChangesToRequestedFinalQuantity() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        CartItem cartItem = CartItem.create(cart, product, 3);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        CartItemResponse response = cartService.updateItemQuantity(
                MEMBER_ID,
                100L,
                new CartItemUpdateRequest(5)
        );

        assertEquals(5, cartItem.getQuantity());
        assertEquals(5, response.quantity());
        assertEquals(5_000L, response.itemTotalPrice());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateItemQuantityDoesNotAddRequestedQuantity() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        CartItem cartItem = CartItem.create(cart, product, 3);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        cartService.updateItemQuantity(
                MEMBER_ID,
                100L,
                new CartItemUpdateRequest(5)
        );

        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void updateItemQuantityAllowsQuantityEqualToStock() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        CartItem cartItem = CartItem.create(cart, product, 3);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        CartItemResponse response = cartService.updateItemQuantity(
                MEMBER_ID,
                100L,
                new CartItemUpdateRequest(10)
        );

        assertEquals(10, cartItem.getQuantity());
        assertEquals(10, response.quantity());
    }

    @Test
    void updateItemQuantityRejectsQuantityOverStockWithoutChangingItem() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productWithStock(10);
        CartItem cartItem = CartItem.create(cart, product, 3);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(
                        MEMBER_ID,
                        100L,
                        new CartItemUpdateRequest(11)
                )
        );

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void updateItemQuantityRejectsMissingCartItem() {
        when(cartItemRepository.findById(100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(
                        MEMBER_ID,
                        100L,
                        new CartItemUpdateRequest(5)
                )
        );

        assertEquals(ErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateItemQuantityRejectsAnotherMembersItemWithoutChangingQuantity() {
        Cart anotherMembersCart = Cart.create(2L);
        Product product = org.mockito.Mockito.mock(Product.class);
        CartItem cartItem = CartItem.create(anotherMembersCart, product, 3);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.updateItemQuantity(
                        MEMBER_ID,
                        100L,
                        new CartItemUpdateRequest(5)
                )
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void updateItemQuantityDoesNotChangeProductStock() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        CartItem cartItem = CartItem.create(cart, product, 3);
        int stockBefore = product.getStockQuantity();

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        cartService.updateItemQuantity(
                MEMBER_ID,
                100L,
                new CartItemUpdateRequest(5)
        );

        assertEquals(stockBefore, product.getStockQuantity());
        verifyNoInteractions(productRepository);
    }

    @Test
    void addItemCreatesCartAndNewCartItem() {
        Product product = productForSuccess(10);
        CartAddRequest request = new CartAddRequest(PRODUCT_ID, 3);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(any(Cart.class), any(Product.class)))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartItemResponse response = cartService.addItem(MEMBER_ID, request);

        assertEquals(PRODUCT_ID, response.productId());
        assertEquals(3, response.quantity());
        assertEquals(3_000L, response.itemTotalPrice());
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemAddsQuantityWithoutCreatingAnotherRow() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        CartItem existingItem = CartItem.create(cart, product, 3);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(existingItem));

        CartItemResponse response = cartService.addItem(
                MEMBER_ID,
                new CartAddRequest(PRODUCT_ID, 2)
        );

        assertEquals(5, existingItem.getQuantity());
        assertEquals(5, response.quantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsFirstQuantityOverStock() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productWithStock(10);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(
                        MEMBER_ID,
                        new CartAddRequest(PRODUCT_ID, 11)
                )
        );

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsCombinedQuantityOverStockWithoutChangingExistingItem() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productWithStock(10);
        CartItem existingItem = CartItem.create(cart, product, 7);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product))
                .thenReturn(Optional.of(existingItem));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(
                        MEMBER_ID,
                        new CartAddRequest(PRODUCT_ID, 4)
                )
        );

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
        assertEquals(7, existingItem.getQuantity());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsMissingProduct() {
        Cart cart = Cart.create(MEMBER_ID);

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cartService.addItem(
                        MEMBER_ID,
                        new CartAddRequest(PRODUCT_ID, 1)
                )
        );

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    void addItemDoesNotDecreaseProductStock() {
        Cart cart = Cart.create(MEMBER_ID);
        Product product = productForSuccess(10);
        int stockBefore = product.getStockQuantity();

        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartAndProduct(cart, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cartService.addItem(MEMBER_ID, new CartAddRequest(PRODUCT_ID, 3));

        assertEquals(stockBefore, product.getStockQuantity());
        verify(productRepository, never()).save(any(Product.class));
    }

    private Product productForSuccess(Integer stockQuantity) {
        Product product = productWithStock(stockQuantity);
        when(product.getId()).thenReturn(PRODUCT_ID);
        when(product.getName()).thenReturn("테스트 상품");
        when(product.getPrice()).thenReturn(1_000L);
        return product;
    }

    private Product productWithStock(Integer stockQuantity) {
        Product product = org.mockito.Mockito.mock(Product.class);
        when(product.getStockQuantity()).thenReturn(stockQuantity);
        return product;
    }

    private CartItem cartItemForResponse(
            Long cartItemId,
            Long productId,
            String productName,
            Long productPrice,
            Integer quantity
    ) {
        Product product = org.mockito.Mockito.mock(Product.class);
        when(product.getId()).thenReturn(productId);
        when(product.getName()).thenReturn(productName);
        when(product.getPrice()).thenReturn(productPrice);

        CartItem cartItem = org.mockito.Mockito.mock(CartItem.class);
        when(cartItem.getId()).thenReturn(cartItemId);
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getQuantity()).thenReturn(quantity);
        return cartItem;
    }
}
