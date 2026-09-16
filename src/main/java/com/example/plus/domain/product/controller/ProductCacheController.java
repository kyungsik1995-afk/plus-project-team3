package com.example.plus.domain.product.controller;

import com.example.plus.domain.product.dto.ProductCacheListResponse;
import com.example.plus.domain.product.dto.ProductListRequest;
import com.example.plus.domain.product.dto.ProductListResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
public class ProductCacheController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @Valid @ModelAttribute ProductListRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        Page<ProductCacheListResponse> cachedProducts = productService.getCachedProducts(request);
        return ApiResponse.success(productService.getProductsWithLatestStock(cachedProducts));
    }
}
