package com.zerobase.customboard.domain.member.service;

import static com.zerobase.customboard.global.exception.ErrorCode.ALREADY_REGISTERED_USER;
import static com.zerobase.customboard.global.exception.ErrorCode.AUTHENTICATE_YOUR_ACCOUNT;
import static com.zerobase.customboard.global.exception.ErrorCode.CODE_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.INVALID_CODE;
import static com.zerobase.customboard.global.exception.ErrorCode.INVALID_REFRESH_TOKEN;
import static com.zerobase.customboard.global.exception.ErrorCode.NICKNAME_ALREADY_EXISTS;
import static com.zerobase.customboard.global.exception.ErrorCode.PASSWORD_NOT_MATCH;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.customboard.domain.member.dto.LoginDto.loginRequest;
import com.zerobase.customboard.domain.member.dto.PasswordDto;
import com.zerobase.customboard.domain.member.dto.ProfileDto.profileRequest;
import com.zerobase.customboard.domain.member.dto.ProfileDto.profileResponse;
import com.zerobase.customboard.domain.member.dto.ResignDto;
import com.zerobase.customboard.domain.member.dto.SignupDto.signupRequest;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.type.Status;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
import com.zerobase.customboard.global.jwt.JwtUtil;
import com.zerobase.customboard.global.jwt.dto.TokenDto;
import com.zerobase.customboard.infra.service.RedisService;
import com.zerobase.customboard.infra.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;
  private final RedisService redisService;
  private final S3Service s3Service;
  private final JwtUtil jwtUtil;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final String BASE_IMAGE_URL = "https://custom-board.s3.ap-northeast-2.amazonaws.com/baseImage.png";

  // 회원가입
  public void signup(signupRequest request) {
    memberRepository.findByEmail(request.getEmail()).ifPresent(m -> {
      throw new CustomException(ALREADY_REGISTERED_USER);
    });

    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new CustomException(NICKNAME_ALREADY_EXISTS);
    }

    String imageUrl = BASE_IMAGE_URL;
    if (request.getProfileFile() != null && !request.getProfileFile().isEmpty()) {
      imageUrl = s3Service.uploadFile(request.getProfileFile(), request.getEmail() + "/profile");
    }

    Member member = Member.builder()
        .email(request.getEmail())
        .nickname(request.getNickname())
        .password(passwordEncoder.encode(request.getPassword()))
        .mobile(request.getMobile())
        .profileImage(imageUrl)
        .build();

    memberRepository.save(member);
  }

  public TokenDto login(loginRequest request) {
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

  // 로그아웃
  @Transactional
  public void logout(HttpServletRequest request) {
    String accessToken = jwtUtil.resolveToken(request);
    String email = jwtUtil.getAuthentication(accessToken).getName();

    setTokenBlackList(email, accessToken);
  }

  // 토큰 재발급
  @Transactional
  public TokenDto reissue(HttpServletRequest request, TokenDto.requestRefresh refresh) {
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

  // 회원정보 조회
  public profileResponse getProfile(CustomUserDetails principal) {
    Member member = memberRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    return profileResponse.builder()
        .email(member.getEmail())
        .nickname(member.getNickname())
        .mobile(member.getMobile())
        .profileImage(member.getProfileImage())
        .build();
  }

  // 회원정보 수정
  @Transactional
  public void updateProfile(CustomUserDetails principal, profileRequest request) {
    Member member = memberRepository.findByEmail(principal.getUsername())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if (request.getNickname() != null) {
      if (memberRepository.existsByNickname(request.getNickname())) {
        throw new CustomException(NICKNAME_ALREADY_EXISTS);
      }
      member.changeNickname(request.getNickname());
    }

    if (request.getPassword() != null) {
      String encodedPassword = passwordEncoder.encode(request.getPassword());
      member.changePassword(encodedPassword);
    }

    if (request.getMobile() != null) {
      member.changeMobile(request.getMobile());
    }

    if (request.getProfileFile() != null) { // 파일이 있을 경우
      if (member.getProfileImage() != null && !member.getProfileImage().equals(BASE_IMAGE_URL)) { // 기존 이미지가 있다면
        s3Service.deleteFile(member.getProfileImage());
      }
      member.changeProfileImage(s3Service.uploadFile(request.getProfileFile(),
          principal.getUsername() + "/profile"));
    } else if (member.getProfileImage() != null && !member.getProfileImage().equals(BASE_IMAGE_URL)) { // 새로운 파일이 없고 기존 이미지가 있다면
      s3Service.deleteFile(member.getProfileImage());
      member.changeProfileImage(BASE_IMAGE_URL);
    }

    memberRepository.save(member);
  }

  // 회원 탈퇴
  @Transactional
  public void resign(HttpServletRequest request, ResignDto resignDto) {
    String accessToken = jwtUtil.resolveToken(request);
    String email = jwtUtil.getAuthentication(accessToken).getName();

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if (!passwordEncoder.matches(resignDto.getPassword(), member.getPassword())) {
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    member.changeStatus(Status.RESIGN);
    memberRepository.save(member);
    setTokenBlackList(email, accessToken);
  }

  private void setTokenBlackList(String email, String accessToken) {
    String redisKey = "[RT]" + email;
    String refreshTokenInRedis = redisService.getData(redisKey);
    if (refreshTokenInRedis != null) {
      redisService.deleteData(redisKey);
    }
    redisService.setDataExpire(accessToken, "blackList", jwtUtil.getExpirationTime(accessToken));
  }

  // 비밀번호 변경
  public void changePassword(String email,String code, PasswordDto passwordDto) {
    String redisInCode = redisService.getData("[AUTH_CODE]" + email);

    if(redisInCode==null){
      throw new CustomException(CODE_NOT_FOUND);
    }
    if(!redisInCode.equals(code)){
      throw new CustomException(INVALID_CODE);
    }

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if(!passwordDto.getPassword().equals(passwordDto.getPasswordConfirm())){
      throw new CustomException(PASSWORD_NOT_MATCH);
    }

    String password = passwordEncoder.encode(passwordDto.getPassword());
    member.changePassword(password);
    memberRepository.save(member);

    redisService.deleteData("[AUTH_CODE]" + email);
  }





}
