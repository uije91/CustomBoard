package com.zerobase.customboard.domain.member.controller;

import com.zerobase.customboard.domain.member.dto.LoginDto;
import com.zerobase.customboard.domain.member.dto.SignupDto;
import com.zerobase.customboard.domain.member.service.MemberService;
import com.zerobase.customboard.global.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<?> signup(@RequestBody @Valid SignupDto.request request) {
    memberService.signup(request);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "로그인 API")
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginDto.request request) {
    return ResponseEntity.ok(memberService.login(request));
  }

  private final JwtUtil jwtUtil;
  @Operation(summary = "로그아웃 API")
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    memberService.logout(request);
    return ResponseEntity.ok().build();
  }
}
