package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;

import com.zerobase.customboard.domain.member.dto.Signup;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  public void signup(Signup request) {
    memberRepository.findByEmail(request.getEmail()).ifPresent(m -> {
      throw new CustomException(ALREADY_REGISTERED_USER);
    });

    Member member = Member.builder()
        .email(request.getEmail())
        .nickname(request.getNickname())
        .password(passwordEncoder.encode(request.getPassword()))
        .mobile(request.getMobile())
        .build();

    memberRepository.save(member);
  }
}
