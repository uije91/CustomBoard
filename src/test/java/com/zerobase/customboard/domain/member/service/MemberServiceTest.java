package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.zerobase.customboard.global.exception.ErrorCode.AUTHENTICATE_YOUR_ACCOUNT;
import static com.zerobase.customboard.global.exception.ErrorCode.CODE_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.INVALID_CODE;
import static com.zerobase.customboard.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;
import static com.zerobase.customboard.global.exception.ErrorCode.NICKNAME_ALREADY_EXISTS;
import static com.zerobase.customboard.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.customboard.domain.member.dto.LoginDto.loginRequest;
import com.zerobase.customboard.domain.member.dto.PasswordDto;
import com.zerobase.customboard.domain.member.dto.ProfileDto.profileRequest;
import com.zerobase.customboard.domain.member.dto.ResignDto;
import com.zerobase.customboard.domain.member.dto.SignupDto.signupRequest;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.member.type.Status;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
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

  private loginRequest login;
  private Member member;
  private TokenDto token;
  private signupRequest signup;

  @BeforeEach
  void setUp() {
    signup = signupRequest.builder()
        .email("test@test.com")
        .nickname("철수")
        .build();

    login = loginRequest.builder()
        .email("test@test.com")
        .password("password")
        .build();

    member = Member.builder()
        .email("test@test.com")
        .password("password")
        .nickname("철수")
        .mobile("010-1234-1234")
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
    given(memberRepository.findByEmail(signup.getEmail())).willReturn(Optional.empty());
    given(memberRepository.existsByNickname(signup.getNickname())).willReturn(false);
    given(passwordEncoder.encode(signup.getPassword())).willReturn("encodedPassword");

    // when
    memberService.signup(signup);

    //then
    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 회원")
  void testSignup_fail_alreadyResignedUser() {

    // given
    given(memberRepository.findByEmail(signup.getEmail())).willReturn(Optional.of(new Member()));


    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.signup(signup));

    //then
    assertEquals(ALREADY_REGISTERED_USER, exception.getErrorCode());
    verify(memberRepository, never()).save(any(Member.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이미 존재하는 닉네임")
  void testSignup_fail_nicknameAlreadyExists() {
    // given
    given(memberRepository.findByEmail(signup.getEmail())).willReturn(Optional.empty());
    given(memberRepository.existsByNickname(signup.getNickname())).willReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.signup(signup));

    //then
    assertEquals(NICKNAME_ALREADY_EXISTS, exception.getErrorCode());
    verify(memberRepository, never()).save(any(Member.class));
  }

  @Test
  @DisplayName("로그인 성공")
  void testLogin_success() throws Exception {
    // given
    Authentication authentication = mock(Authentication.class);
    given(authentication.getName()).willReturn("test@test.com");

    given(memberRepository.findByEmail(login.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(login.getPassword(), member.getPassword())).willReturn(true);
    given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(authentication);
    given(jwtUtil.generateToken(authentication)).willReturn(token);
    given(jwtUtil.getExpirationTime(token.getRefreshToken())).willReturn(3600L);

    // when
    TokenDto result = memberService.login(login);

    //then
    verify(memberRepository).findByEmail(login.getEmail());
    verify(passwordEncoder).matches(login.getPassword(), member.getPassword());
    verify(authenticationManagerBuilder.getObject()).authenticate(
        any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtil).generateToken(authentication);
    verify(redisService).setDataExpire("[RT]test@test.com", token.getRefreshToken(), 3600L);
    assertEquals(token, result);
  }

  @Test
  @DisplayName("로그인 실패 - 유저 정보 없음")
  void testLogin_fail_userNotFound() throws Exception {
    // given
    given(memberRepository.findByEmail(login.getEmail())).willReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));

    //then
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void testLogin_fail_passwordNotMatch() throws Exception {
    // given
    given(memberRepository.findByEmail(login.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(login.getPassword(), member.getPassword())).willReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));

    //then
    assertEquals(PASSWORD_NOT_MATCH, exception.getErrorCode());
  }

  @Test
  @DisplayName("로그인 실패 - 계정 비활성화")
  void testLogin_fail_accountNotActive() throws Exception {
    // given
    member = Member.builder().status(Status.INACTIVE).build();
    given(memberRepository.findByEmail(login.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(login.getPassword(), member.getPassword())).willReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.login(login));

    //then
    assertEquals(AUTHENTICATE_YOUR_ACCOUNT, exception.getErrorCode());
  }

  @Test
  @DisplayName("로그아웃 성공")
  void testLogout_success() throws Exception {
    // given
    String accessToken = "AccessToken";
    String email = "test@test.com";
    String redisKey = "[RT]" + email;
    String refreshTokenInRedis = "mockRefreshToken";

    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(
        new UsernamePasswordAuthenticationToken(email, null));
    given(redisService.getData(redisKey)).willReturn(refreshTokenInRedis);

    // when
    memberService.logout(request);

    //then
    verify(redisService).deleteData(redisKey);
    verify(redisService).setDataExpire(eq(accessToken), eq("blackList"), anyLong());
  }

  @Test
  @DisplayName("토큰 재발급 성공")
  void testReissue_success() throws Exception {
    // given
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";
    String email = "test@test.com";

    TokenDto.requestRefresh refresh = TokenDto.requestRefresh.builder()
        .refreshToken(refreshToken).build();

    Authentication authentication = mock(Authentication.class);
    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.validateToken(refreshToken)).willReturn(true);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(authentication);
    given(authentication.getName()).willReturn(email);
    given(redisService.getData("[RT]" + email)).willReturn(refreshToken);
    given(jwtUtil.generateToken(authentication)).willReturn(
        new TokenDto("newAccessToken", "newRefreshToken"));
    given(jwtUtil.getExpirationTime("newRefreshToken")).willReturn(1000L);

    // when
    TokenDto result = memberService.reissue(request, refresh);

    //then
    assertNotNull(result);
    assertEquals("newAccessToken", result.getAccessToken());
    assertEquals("newRefreshToken", result.getRefreshToken());
    verify(redisService).deleteData("[RT]" + email);
    verify(redisService).setDataExpire("[RT]" + email, "newRefreshToken", 1000L);
  }

  @Test
  @DisplayName("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
  void testReissue_fail_invalidRefreshToken() throws Exception {
    // given
    String accessToken = "accessToken";
    String refreshToken = "invalidRefreshToken";

    TokenDto.requestRefresh refresh = TokenDto.requestRefresh.builder()
        .refreshToken(refreshToken).build();

    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.validateToken(refreshToken)).willReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.reissue(request, refresh));

    //then
    assertEquals(INVALID_REFRESH_TOKEN, exception.getErrorCode());
  }

  @Test
  @DisplayName("토큰 재발급 실패 - Reids의 RT와 불일치")
  void testReissue_fail_refreshTokenMismatch() throws Exception {
    // given
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";
    String email = "test@test.com";

    TokenDto.requestRefresh refresh = TokenDto.requestRefresh.builder()
        .refreshToken(refreshToken).build();

    Authentication authentication = mock(Authentication.class);
    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.validateToken(refreshToken)).willReturn(true);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(authentication);
    given(authentication.getName()).willReturn(email);
    given(redisService.getData("[RT]" + email)).willReturn("differentRefreshToken");

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.reissue(request, refresh));

    //then
    assertEquals(INVALID_REFRESH_TOKEN, exception.getErrorCode());
  }

  @Test
  @DisplayName("프로필 조회 성공")
  void testGetProfile_success() throws Exception {
    // given
    CustomUserDetails principal = new CustomUserDetails(member);
    given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));

    // when
    memberService.getProfile(principal);

    //then
    assertEquals("test@test.com", member.getEmail());
    assertEquals("철수", member.getNickname());
    assertEquals("010-1234-1234", member.getMobile());
  }

  @Test
  @DisplayName("프로필 정보 수정 성공")
  void testUpdateProfile_fail_nicknameAlreadyExists() throws Exception {
    // given
    profileRequest request = profileRequest.builder()
        .nickname("영희")
        .password("newPassword")
        .mobile("010-1212-3434")
        .build();

    CustomUserDetails principal = new CustomUserDetails(member);
    given(memberRepository.findByEmail(principal.getUsername())).willReturn(Optional.of(member));
    given(memberRepository.existsByNickname(request.getNickname())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");

    // when
    memberService.updateProfile(principal, request);

    //then
    assertEquals("영희", member.getNickname());
    assertEquals("encodedPassword", member.getPassword());
    assertEquals("010-1212-3434", member.getMobile());
    verify(memberRepository, times(1)).save(member);
  }

  @Test
  @DisplayName("프로필 정보 수정 실패 - 이미 존재하는 닉네임")
  void testUpdateProfile_success() throws Exception {
    // given
    profileRequest request = profileRequest.builder()
        .nickname("영희")
        .password("newPassword")
        .mobile("010-1212-3434")
        .build();

    CustomUserDetails principal = new CustomUserDetails(member);
    given(memberRepository.findByEmail(principal.getUsername())).willReturn(Optional.of(member));
    given(memberRepository.existsByNickname(request.getNickname())).willReturn(true);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.updateProfile(principal, request));

    //then
    assertEquals(NICKNAME_ALREADY_EXISTS, exception.getErrorCode());
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  void testResign_success() throws Exception {
    // given
    String accessToken = "AccessToken";
    String email = member.getEmail();
    String redisKey = "[RT]" + email;
    String refreshTokenInRedis = "mockRefreshToken";

    ResignDto resign = ResignDto.builder()
        .password("password")
        .build();

    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(
        new UsernamePasswordAuthenticationToken(email, null));
    given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
    given(passwordEncoder.matches(member.getPassword(), resign.getPassword())).willReturn(true);
    given(redisService.getData(redisKey)).willReturn(refreshTokenInRedis);

    // when
    memberService.resign(request, resign);

    //then
    verify(redisService).deleteData(redisKey);
    verify(redisService).setDataExpire(eq(accessToken), eq("blackList"), anyLong());
    verify(memberRepository).save(member);
  }

  @Test
  @DisplayName("회원 탈퇴 실패 - 유저 정보 없음")
  void testResign_fail_userNotFound() throws Exception {
    // given
    String accessToken = "AccessToken";

    ResignDto resign = ResignDto.builder()
        .password("password")
        .build();

    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(
        new UsernamePasswordAuthenticationToken(member.getEmail(), null));
    given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.empty());

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.resign(request, resign));

    //then
    assertEquals(USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
  void testResign_fail_passwordNotMatch() throws Exception {
    // given
    String accessToken = "AccessToken";

    ResignDto resign = ResignDto.builder()
        .password("password")
        .build();

    given(jwtUtil.resolveToken(request)).willReturn(accessToken);
    given(jwtUtil.getAuthentication(accessToken)).willReturn(
        new UsernamePasswordAuthenticationToken(member.getEmail(), null));
    given(memberRepository.findByEmail(member.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(member.getPassword(), resign.getPassword())).willReturn(false);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.resign(request, resign));

    //then
    assertEquals(PASSWORD_NOT_MATCH, exception.getErrorCode());
  }

  @Test
  @DisplayName("비밀번호 재설정 성공")
  void testChangePassword_success() throws Exception {
    // given
    String email = "test@test.com";
    String code = "123456";

    PasswordDto password = PasswordDto.builder()
        .password("newPassword")
        .passwordConfirm("newPassword")
        .build();


    given(redisService.getData("[AUTH_CODE]"+email)).willReturn(code);
    given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

    // when
    memberService.changePassword(email,code,password);

    //then
    verify(memberRepository).save(member);
    verify(redisService).deleteData("[AUTH_CODE]" + email);
  }

  @Test
  @DisplayName("비밀번호 재설정 실패 - 비밀번호 불일치")
  void testChangePassword_fail_passwordNotMatch() throws Exception {
    // given
    String email = "test@test.com";
    String code = "123456";

    PasswordDto password = PasswordDto.builder()
        .password("newPassword1")
        .passwordConfirm("newPassword2")
        .build();


    given(redisService.getData("[AUTH_CODE]"+email)).willReturn(code);
    given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.changePassword(email,code,password));

    //then
    assertEquals(PASSWORD_NOT_MATCH, exception.getErrorCode());
  }

  @Test
  @DisplayName("비밀번호 재설정 실패 - 코드 불일치")
  void testChangePassword_fail_invalidCode() throws Exception {
    // given
    String email = "test@test.com";
    String code = "123456";

    PasswordDto password = PasswordDto.builder()
        .password("newPassword")
        .passwordConfirm("newPassword")
        .build();


    given(redisService.getData("[AUTH_CODE]"+email)).willReturn("654321");

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.changePassword(email,code,password));

    //then
    assertEquals(INVALID_CODE, exception.getErrorCode());
  }

  @Test
  @DisplayName("비밀번호 재설정 실패 - 레디스에 존재하는 코드가 없음")
  void testChangePassword_fail_notFoundCode() throws Exception {
    // given
    String email = "test@test.com";
    String code = "123456";

    PasswordDto password = PasswordDto.builder()
        .password("newPassword")
        .passwordConfirm("newPassword")
        .build();


    given(redisService.getData("[AUTH_CODE]"+email)).willReturn(null);

    // when
    CustomException exception = assertThrows(CustomException.class,
        () -> memberService.changePassword(email,code,password));

    //then
    assertEquals(CODE_NOT_FOUND, exception.getErrorCode());
  }

}