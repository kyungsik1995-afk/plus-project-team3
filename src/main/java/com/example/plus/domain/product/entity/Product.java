package com.example.plus.domain.product.entity;

import com.example.plus.global.common.entity.BaseEntity;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ProductCategory category;

    @Column(nullable = false)
    private Long price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    public void update(
            String name,
            ProductCategory category,
            Long price,
            String description
    ) {
        if (name != null) {
            this.name = name;
        }
        if (category != null) {
            this.category = category;
        }
        if (price != null) {
            this.price = price;
        }
        if (description != null) {
            this.description = description;
        }
    }

    public void decreaseStock(Integer quantity) {

        if (stockQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        stockQuantity -= quantity;
    }

    // 환불된 상품의 수량만큼 재고를 다시 증가시킨다.
    public void restoreStock(Integer quantity) {

        // 재고 복구 수량이 올바른 값인지 확인한다.
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 환불된 수량만큼 상품 재고를 복구한다.
        stockQuantity += quantity;
    }
}
