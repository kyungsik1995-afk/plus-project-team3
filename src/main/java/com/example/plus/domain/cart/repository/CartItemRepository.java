package com.example.plus.domain.cart.repository;

import com.example.plus.domain.cart.entity.Cart;
import com.example.plus.domain.cart.entity.CartItem;
import com.example.plus.domain.product.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
