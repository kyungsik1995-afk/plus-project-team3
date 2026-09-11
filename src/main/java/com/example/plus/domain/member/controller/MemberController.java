package com.example.plus.domain.member.controller;

import com.example.plus.domain.member.dto.MemberLoginRequest;
import com.example.plus.domain.member.dto.MemberLoginResponse;
import com.example.plus.domain.member.dto.MemberMyInfoResponse;
import com.example.plus.domain.member.dto.MemberSignupRequest;
import com.example.plus.domain.member.service.MemberService;
import com.example.plus.global.common.response.ApiResponse;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<Void> signup(
            @Valid @RequestBody MemberSignupRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        memberService.signup(request);

        return ApiResponse.success(null);
    }

    @PostMapping("/login")
    public ApiResponse<MemberLoginResponse> login(
            @Valid @RequestBody MemberLoginRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
            throw new BusinessException(ErrorCode.INVALID_REQUEST, message);
        }

        return ApiResponse.success(memberService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<MemberMyInfoResponse> getMyInfo(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(memberService.getMyInfo(memberId));
    }
}