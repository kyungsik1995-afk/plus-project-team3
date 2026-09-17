package com.example.plus.domain.cart.entity;

import com.example.plus.domain.product.entity.Product;
import com.example.plus.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_items_cart_product",
                columnNames = {"cart_id", "product_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    private CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public static CartItem create(Cart cart, Product product, Integer quantity) {
        return new CartItem(cart, product, quantity);
    }

    public void addQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    public void changeQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
