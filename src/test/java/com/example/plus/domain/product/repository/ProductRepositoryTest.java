package com.example.plus.domain.product.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.plus.domain.product.entity.Product;
import com.example.plus.domain.product.entity.ProductCategory;
import com.example.plus.global.config.jpa.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class ProductRepositoryTest {

    private static final Sort LATEST_SORT = Sort.by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.DESC, "id"));
    private static final int LARGE_PAGE_SIZE = 10_000;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Product foodLow;
    private Product foodMiddle;
    private Product foodHigh;
    private Product fashionMiddle;
    private Product electronicsHigh;

    @BeforeEach
    void setUp() {
        foodLow = saveProduct("repository-test-food-low", ProductCategory.FOOD, 1_000L);
        foodMiddle = saveProduct("repository-test-food-middle", ProductCategory.FOOD, 3_000L);
        foodHigh = saveProduct("repository-test-food-high", ProductCategory.FOOD, 5_000L);
        fashionMiddle = saveProduct(
                "repository-test-fashion-middle",
                ProductCategory.FASHION,
                3_000L
        );
        electronicsHigh = saveProduct(
                "repository-test-electronics-high",
                ProductCategory.ELECTRONICS,
                8_000L
        );

        updateCreatedAt(foodLow, LocalDateTime.of(2099, 1, 1, 9, 0));
        updateCreatedAt(foodMiddle, LocalDateTime.of(2099, 1, 2, 9, 0));
        updateCreatedAt(foodHigh, LocalDateTime.of(2099, 1, 3, 9, 0));
        updateCreatedAt(fashionMiddle, LocalDateTime.of(2099, 1, 5, 9, 0));
        updateCreatedAt(electronicsHigh, LocalDateTime.of(2099, 1, 5, 9, 0));

        entityManager.clear();
    }

    @Test
    void findAllByConditionsReturnsAllProductsWithoutFilters() {
        Page<Product> result = productRepository.findAllByConditions(
                null,
                null,
                null,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertEquals(productRepository.count(), result.getTotalElements());
        assertEquals(result.getTotalElements(), result.getNumberOfElements());
    }

    @Test
    void findAllByConditionsFiltersByCategory() {
        List<Product> expected = productRepository.findAll().stream()
                .filter(product -> product.getCategory() == ProductCategory.FOOD)
                .toList();

        Page<Product> result = productRepository.findAllByConditions(
                ProductCategory.FOOD,
                null,
                null,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertEquals(idsOf(expected), idsOf(result.getContent()));
        assertTrue(result.getContent().stream()
                .allMatch(product -> product.getCategory() == ProductCategory.FOOD));
    }

    @Test
    void findAllByConditionsFiltersByMinPrice() {
        List<Product> expected = productRepository.findAll().stream()
                .filter(product -> product.getPrice() >= 5_000L)
                .toList();

        Page<Product> result = productRepository.findAllByConditions(
                null,
                5_000L,
                null,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertEquals(idsOf(expected), idsOf(result.getContent()));
        assertTrue(result.getContent().stream()
                .allMatch(product -> product.getPrice() >= 5_000L));
    }

    @Test
    void findAllByConditionsFiltersByMaxPrice() {
        List<Product> expected = productRepository.findAll().stream()
                .filter(product -> product.getPrice() <= 3_000L)
                .toList();

        Page<Product> result = productRepository.findAllByConditions(
                null,
                null,
                3_000L,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertEquals(idsOf(expected), idsOf(result.getContent()));
        assertTrue(result.getContent().stream()
                .allMatch(product -> product.getPrice() <= 3_000L));
    }

    @Test
    void findAllByConditionsFiltersByCategoryAndPriceRange() {
        List<Product> expected = productRepository.findAll().stream()
                .filter(product -> product.getCategory() == ProductCategory.FOOD)
                .filter(product -> product.getPrice() >= 2_000L)
                .filter(product -> product.getPrice() <= 5_000L)
                .toList();

        Page<Product> result = productRepository.findAllByConditions(
                ProductCategory.FOOD,
                2_000L,
                5_000L,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertEquals(idsOf(expected), idsOf(result.getContent()));
        assertTrue(result.getContent().stream()
                .allMatch(product -> product.getCategory() == ProductCategory.FOOD
                        && product.getPrice() >= 2_000L
                        && product.getPrice() <= 5_000L));
    }

    @Test
    void findAllByConditionsReturnsEmptyPageWhenNoProductMatches() {
        Page<Product> result = productRepository.findAllByConditions(
                null,
                Long.MAX_VALUE,
                null,
                PageRequest.of(0, LARGE_PAGE_SIZE)
        );

        assertTrue(result.isEmpty());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void findAllByConditionsAppliesCreatedAtAndIdDescendingSort() {
        Page<Product> result = productRepository.findAllByConditions(
                null,
                null,
                null,
                PageRequest.of(0, 5, LATEST_SORT)
        );

        assertEquals(
                List.of(
                        electronicsHigh.getId(),
                        fashionMiddle.getId(),
                        foodHigh.getId(),
                        foodMiddle.getId(),
                        foodLow.getId()
                ),
                result.getContent().stream().map(Product::getId).toList()
        );
    }

    @Test
    void findAllByConditionsAppliesPaginationAndTotalElements() {
        List<Long> allProductIds = productRepository.findAll(LATEST_SORT).stream()
                .map(Product::getId)
                .toList();

        Page<Product> result = productRepository.findAllByConditions(
                null,
                null,
                null,
                PageRequest.of(1, 2, LATEST_SORT)
        );

        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertEquals(allProductIds.size(), result.getTotalElements());
        assertEquals(allProductIds.subList(2, 4),
                result.getContent().stream().map(Product::getId).toList());
    }

    private Product saveProduct(String name, ProductCategory category, Long price) {
        Product product = BeanUtils.instantiateClass(Product.class);
        product.update(name, category, price, name + " description");
        ReflectionTestUtils.setField(product, "stockQuantity", 10);
        return productRepository.saveAndFlush(product);
    }

    private void updateCreatedAt(Product product, LocalDateTime createdAt) {
        entityManager.createQuery("""
                        UPDATE Product p
                        SET p.createdAt = :createdAt
                        WHERE p.id = :productId
                        """)
                .setParameter("createdAt", createdAt)
                .setParameter("productId", product.getId())
                .executeUpdate();
    }

    private Set<Long> idsOf(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
    }
}
