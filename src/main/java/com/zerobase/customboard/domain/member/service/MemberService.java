package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.zerobase.customboard.global.exception.ErrorCode.AUTHENTICATE_YOUR_ACCOUNT;
import static com.zerobase.customboard.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;
import static com.zerobase.customboard.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.customboard.domain.member.dto.LoginDto;
import com.zerobase.customboard.domain.member.dto.SignupDto;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.member.type.Status;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.jwt.JwtUtil;
import com.zerobase.customboard.global.jwt.dto.TokenDto;
import com.zerobase.customboard.infra.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RedisService redisService;
  private final JwtUtil jwtUtil;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  public void signup(SignupDto.request request) {
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

  public TokenDto login(LoginDto.request request) {
    Member member = memberRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    if (Status.ACTIVE != member.getStatus()) {
      throw new CustomException(AUTHENTICATE_YOUR_ACCOUNT);
    }
    // 스프링 시큐리티 로그인 로직
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
    Authentication authentication =
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);

    // 토큰 생성
    return generateToken(authentication);
  }

  // 토큰 발급
  private TokenDto generateToken(Authentication authentication) {
    String email = authentication.getName();

    if (redisService.getData("[RT]" + email) != null) {
      redisService.deleteData("[RT]" + email);
    }

    TokenDto tokenDto = jwtUtil.generateToken(authentication);
    long getExpirationTime = jwtUtil.getExpirationTime(tokenDto.getRefreshToken());
    redisService.setDataExpire("[RT]" + email, tokenDto.getRefreshToken(), getExpirationTime);
    return tokenDto;
  }

  public void logout(HttpServletRequest request) {
    String accessToken = jwtUtil.resolveToken(request);
    String email = jwtUtil.getAuthentication(accessToken).getName();

    String redisKey = "[RT]" + email;
    String refreshTokenInRedis = redisService.getData(redisKey);
    if (refreshTokenInRedis != null) {
      redisService.deleteData(redisKey);
    }
    redisService.setDataExpire(accessToken, "logout", jwtUtil.getExpirationTime(accessToken));
  }

  public TokenDto reissue(HttpServletRequest request,TokenDto.requestRefresh refresh) {
    String accessToken = jwtUtil.resolveToken(request);
    String refreshToken = refresh.getRefreshToken();

    if (!jwtUtil.validateToken(refreshToken)) {
      log.error("[MemberService][reissue] : RefreshToken 검증 실패");
      throw new CustomException(INVALID_REFRESH_TOKEN);
    }

    Authentication authentication = jwtUtil.getAuthentication(accessToken);
    String email = authentication.getName();

    String refreshTokenInRedis = redisService.getData("[RT]" + email);
    if (!refreshToken.equals(refreshTokenInRedis)) {
      log.error("[MemberService][reissue] : Redis에 저장된 RT와 가져온 RT가 불일치");
      throw new CustomException(INVALID_REFRESH_TOKEN);
    }

    return generateToken(authentication);
  }
}
