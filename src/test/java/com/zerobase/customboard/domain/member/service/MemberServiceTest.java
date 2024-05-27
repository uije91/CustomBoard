package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.customboard.domain.member.dto.Signup;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("회원가입 성공")
  void signup_success() {
    // given
    Signup request = Signup.builder().email("test@test.com").build();

    when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

    // when
    memberService.signup(request);

    //then
    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 회원")
  void signup_fail_alreadyResignedUser(){
    Signup request = Signup.builder().email("test@test.com").build();

    // given
    when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new Member()));

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.signup(request));

    //then
    assertEquals(ALREADY_REGISTERED_USER, exception.getErrorCode());
    verify(memberRepository, never()).save(any(Member.class));
  }
}