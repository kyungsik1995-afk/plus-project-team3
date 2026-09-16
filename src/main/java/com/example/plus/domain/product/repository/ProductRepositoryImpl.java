package com.example.plus.domain.product.repository;

import static com.example.plus.domain.product.entity.QProduct.product;

import com.example.plus.domain.product.dto.ProductCacheListResponse;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.entity.ProductCategory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductListResponse> findAllByConditions(
            ProductCategory category,
            Long minPrice,
            Long maxPrice,
            Pageable pageable
    ) {
        List<ProductListResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductListResponse.class,
                        product.id,
                        product.name,
                        product.category,
                        product.price,
                        product.stockQuantity
                ))
                .from(product)
                .where(
                        categoryEq(category),
                        priceGoe(minPrice),
                        priceLoe(maxPrice)
                )
                .orderBy(product.createdAt.desc(), product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        categoryEq(category),
                        priceGoe(minPrice),
                        priceLoe(maxPrice)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<ProductCacheListResponse> findAllCachedByConditions(
            ProductCategory category,
            Long minPrice,
            Long maxPrice,
            Pageable pageable
    ) {
        List<ProductCacheListResponse> content = queryFactory
                .select(Projections.constructor(
                        ProductCacheListResponse.class,
                        product.id,
                        product.name,
                        product.category,
                        product.price
                ))
                .from(product)
                .where(
                        categoryEq(category),
                        priceGoe(minPrice),
                        priceLoe(maxPrice)
                )
                .orderBy(product.createdAt.desc(), product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        categoryEq(category),
                        priceGoe(minPrice),
                        priceLoe(maxPrice)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Map<Long, Integer> findStockQuantitiesByProductIds(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> stocks = queryFactory
                .select(product.id, product.stockQuantity)
                .from(product)
                .where(product.id.in(productIds))
                .fetch();

        return stocks.stream()
                .collect(Collectors.toMap(
                        stock -> stock.get(product.id),
                        stock -> stock.get(product.stockQuantity)
                ));
    }

    private BooleanExpression categoryEq(ProductCategory category) {
        return category == null ? null : product.category.eq(category);
    }

    private BooleanExpression priceGoe(Long minPrice) {
        return minPrice == null ? null : product.price.goe(minPrice);
    }

    private BooleanExpression priceLoe(Long maxPrice) {
        return maxPrice == null ? null : product.price.loe(maxPrice);
    }
}
