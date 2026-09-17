package com.example.plus.domain.cart.repository;

import com.example.plus.domain.cart.entity.Cart;
import com.example.plus.domain.cart.entity.CartItem;
import com.example.plus.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findAllByCart(Cart cart);

    @Query("""
            SELECT ci
            FROM CartItem ci
            JOIN FETCH ci.product
            WHERE ci.cart = :cart
            """)
    List<CartItem> findAllByCartWithProduct(@Param("cart") Cart cart);
}
