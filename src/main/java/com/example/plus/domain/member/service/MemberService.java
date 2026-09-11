package com.example.plus.domain.member.service;

import com.example.plus.domain.member.dto.MemberLoginRequest;
import com.example.plus.domain.member.dto.MemberLoginResponse;
import com.example.plus.domain.member.dto.MemberMyInfoResponse;
import com.example.plus.domain.member.dto.MemberSignupRequest;
import com.example.plus.domain.member.entity.Member;
import com.example.plus.domain.member.repository.MemberRepository;
import com.example.plus.global.exception.ErrorCode;
import com.example.plus.global.exception.business.BusinessException;
import com.example.plus.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(MemberSignupRequest request) {

        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = new Member(
                request.email(),
                encodedPassword,
                request.name(),
                request.phoneNumber()
        );

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberLoginResponse login(MemberLoginRequest request) {

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.LOGIN_FAILED)
                );

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtil.createToken(
                member.getId(),
                member.getEmail()
        );

        return new MemberLoginResponse(accessToken);
    }

    @Transactional(readOnly = true)
    public MemberMyInfoResponse getMyInfo(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.MEMBER_NOT_FOUND)
                );

        return new MemberMyInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber()
        );
    }
}