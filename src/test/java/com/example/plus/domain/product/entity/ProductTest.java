package com.example.plus.domain.product.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTest {

    @Test
    void decreaseStockDecreasesRequestedQuantityWhenStockIsSufficient() {
        Product product = createProductWithStock(10);

        product.decreaseStock(3);

        assertEquals(7, product.getStockQuantity());
    }

    @Test
    void decreaseStockMakesStockZeroWhenQuantityEqualsStock() {
        Product product = createProductWithStock(10);

        product.decreaseStock(10);

        assertEquals(0, product.getStockQuantity());
    }

    @Test
    void decreaseStockRejectsQuantityGreaterThanStock() {
        Product product = createProductWithStock(10);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> product.decreaseStock(11)
        );

        assertEquals(ErrorCode.OUT_OF_STOCK, exception.getErrorCode());
    }

    @Test
    void decreaseStockKeepsStockWhenQuantityIsInsufficient() {
        Product product = createProductWithStock(10);

        assertThrows(BusinessException.class, () -> product.decreaseStock(11));

        assertEquals(10, product.getStockQuantity());
    }

    @Test
    void restoreStockIncreasesRequestedQuantity() {
        Product product = createProductWithStock(7);

        product.restoreStock(3);

        assertEquals(10, product.getStockQuantity());
    }

    private Product createProductWithStock(int stockQuantity) {
        Product product = new TestProduct();
        ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
        return product;
    }

    private static class TestProduct extends Product {
    }
}
