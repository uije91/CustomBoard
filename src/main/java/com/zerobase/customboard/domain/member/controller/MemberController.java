package com.zerobase.customboard.domain.member.controller;

import com.zerobase.customboard.domain.member.dto.LoginDto.loginRequest;
import com.zerobase.customboard.domain.member.dto.PasswordDto;
import com.zerobase.customboard.domain.member.dto.ProfileDto.profileRequest;
import com.zerobase.customboard.domain.member.dto.ResignDto;
import com.zerobase.customboard.domain.member.dto.SignupDto.signupRequest;
import com.zerobase.customboard.domain.member.service.MemberService;
import com.zerobase.customboard.global.jwt.CustomUserDetails;
import com.zerobase.customboard.global.jwt.dto.TokenDto.requestRefresh;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "Member", description = "회원 API")
public class MemberController {

  private final MemberService memberService;

  @Operation(summary = "회원가입 API")
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@ModelAttribute @Valid signupRequest request) {
    memberService.signup(request);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "로그인 API")
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody loginRequest request) {
    return ResponseEntity.ok(memberService.login(request));
  }

  @Operation(summary = "로그아웃 API")
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    memberService.logout(request);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "토큰재발급 API")
  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(HttpServletRequest request,
      @RequestBody requestRefresh refreshToken) {
    return ResponseEntity.ok(memberService.reissue(request, refreshToken));
  }

  @Operation(summary = "회원정보 조회 API")
  @GetMapping("/profile")
  public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails principal) {
    return ResponseEntity.ok(memberService.getProfile(principal));
  }

  @Operation(summary = "회원정보 수정 API")
  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails principal,
      @ModelAttribute @Valid profileRequest request) {
    memberService.updateProfile(principal, request);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "회원탈퇴 API")
  @PutMapping("/resign")
  public ResponseEntity<?> resign(HttpServletRequest request, @RequestBody ResignDto password) {
    memberService.resign(request, password);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "비밀번호 재설정 API")
  @PutMapping("/password/")
  public ResponseEntity<?> updatePassword(
      @Parameter(name = "email",example = "test@test.com") @RequestParam String email,
      @Parameter(name = "code",example = "123456")@RequestParam String code,
      @RequestBody @Valid PasswordDto passwordDto){
    memberService.changePassword(email,code,passwordDto);
    return ResponseEntity.ok().build();
  }
}
