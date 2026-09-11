package com.example.plus.domain.member.dto;

public record MemberMyInfoResponse(
        Long id,
        String email,
        String name,
        String phoneNumber
) {
}