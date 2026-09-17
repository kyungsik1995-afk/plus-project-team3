package com.example.plus.domain.product.controller;

import com.example.plus.domain.product.dto.ProductCacheListResponse;
import com.example.plus.domain.product.dto.ProductDetailResponse;
import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
import com.example.plus.domain.product.dto.ProductUpdateRequest;
import com.example.plus.domain.product.service.ProductService;
import com.example.plus.global.common.response.ApiResponse;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @Valid @ModelAttribute ProductListRequest request,
            BindingResult bindingResult
    ) {
        validateRequest(bindingResult);

        return ApiResponse.success(productService.getProducts(request));
    }

    @GetMapping("/v2/products")
    public ApiResponse<Page<ProductListResponse>> getCachedProducts(
            @Valid @ModelAttribute ProductListRequest request,
            BindingResult bindingResult
    ) {
        validateRequest(bindingResult);

        Page<ProductCacheListResponse> cachedProducts = productService.getCachedProducts(request);
        return ApiResponse.success(productService.getProductsWithLatestStock(cachedProducts));
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ProductDetailResponse> getProductDetail(
            @PathVariable("productId") Long productId
    ) {
        return ApiResponse.success(productService.getProductDetail(productId));
    }

    @PatchMapping("/products/{productId}")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateRequest request,
            BindingResult bindingResult
    ) {
        validateRequest(bindingResult);

        return ApiResponse.success(productService.updateProduct(productId, request));
    }

    private void validateRequest(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }
    }
}
