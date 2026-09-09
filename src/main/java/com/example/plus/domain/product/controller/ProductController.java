package com.example.plus.domain.product.controller;

import com.example.plus.domain.product.dto.ProductDetailResponse;
import com.example.plus.domain.product.service.ProductService;
import com.example.plus.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProductDetail(
            @PathVariable("productId") Long productId
    ) {
        return ApiResponse.success(productService.getProductDetail(productId));
    }
}
