package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.zerobase.customboard.global.exception.ErrorCode.AUTHENTICATE_YOUR_ACCOUNT;
import static com.zerobase.customboard.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.customboard.domain.member.dto.LoginDto;
import com.zerobase.customboard.domain.member.dto.SignupDto;
import com.zerobase.customboard.domain.member.dto.SignupDto.request;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.member.type.Status;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.exception.ErrorCode;
import com.zerobase.customboard.global.jwt.JwtUtil;
import com.zerobase.customboard.global.jwt.dto.TokenDto;
import com.zerobase.customboard.infra.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RedisService redisService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private AuthenticationManagerBuilder authenticationManagerBuilder;

  @Mock
  private AuthenticationManager authenticationManager;


  @BeforeEach
  void setUp() {
    login = LoginDto.request.builder()
        .email("test@test.com")
        .password("password")
        .build();

    member = Member.builder()
        .email("test@test.com")
        .password("password")
        .status(Status.ACTIVE)
        .build();

    token = TokenDto.builder()
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();
  }

  @Test
  @DisplayName("회원가입 성공")
  void testSignup_success() {
    // given
    request request = SignupDto.request.builder().email("test@test.com").build();

    when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

    // when
    memberService.signup(request);

    //then
    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 회원")
  void testSignup_fail_alreadyResignedUser(){
    request request = SignupDto.request.builder().email("test@test.com").build();

    // given
    when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new Member()));

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.signup(request));

    //then
    assertEquals(ALREADY_REGISTERED_USER, exception.getErrorCode());
    verify(memberRepository, never()).save(any(Member.class));
  }


  private LoginDto.request login;
  private Member member;
  private TokenDto token;


  @Test
  @DisplayName("로그인 성공")
  void testLogin_success() throws Exception {
    // given
    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn("test@test.com");

    when(memberRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(member));
    when(passwordEncoder.matches(login.getPassword(), member.getPassword())).thenReturn(true);
    when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(jwtUtil.generateToken(authentication)).thenReturn(token);
    when(jwtUtil.getExpirationTime(token.getRefreshToken())).thenReturn(3600L);

    // when
    TokenDto result = memberService.login(login);

    //then
    verify(memberRepository).findByEmail(login.getEmail());
    verify(passwordEncoder).matches(login.getPassword(), member.getPassword());
    verify(authenticationManagerBuilder.getObject()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil).generateToken(authentication);
    verify(redisService).setDataExpire("[RT]test@test.com", token.getRefreshToken(), 3600L);
    assertEquals(token, result);
  }
  
  @Test
  @DisplayName("로그인 실패 - 유저 정보 없음")
  void testLogin_fail_userNotFound() throws Exception{
    // given
    when(memberRepository.findByEmail(login.getEmail())).thenReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));
    
    //then
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void testLogin_fail_passwordNotMatch() throws Exception{
    // given
    when(memberRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(member));
    when(passwordEncoder.matches(login.getPassword(), member.getPassword())).thenReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));

    //then
    assertEquals(PASSWORD_NOT_MATCH,exception.getErrorCode());
  }

  @Test
  @DisplayName("로그인 실패 - 계정 비활성화")
  void testLogin_fail_accountNotActive() throws Exception{
    // given
    member= Member.builder().status(Status.INACTIVE).build();
    when(memberRepository.findByEmail(login.getEmail())).thenReturn(Optional.of(member));
    when(passwordEncoder.matches(login.getPassword(), member.getPassword())).thenReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));

    //then
    assertEquals(AUTHENTICATE_YOUR_ACCOUNT, exception.getErrorCode());
  }
  
  @Test
  @DisplayName("로그아웃 성공")
  void testLogout_success() throws Exception{
    // given
    String accessToken = "AccessToken";
    String email = "test@test.com";
    String redisKey = "[RT]" + email;
    String refreshTokenInRedis = "mockRefreshToken";

    when(jwtUtil.resolveToken(request)).thenReturn(accessToken);
    when(jwtUtil.getAuthentication(accessToken)).thenReturn(new UsernamePasswordAuthenticationToken(email, null));
    when(redisService.getData(redisKey)).thenReturn(refreshTokenInRedis);

    // when
    memberService.logout(request);
    
    //then
    verify(redisService).deleteData(redisKey);
    verify(redisService).setDataExpire(eq(accessToken), eq("logout"), anyLong());
  }
}