package com.zerobase.customboard.domain.member.controller;

import com.zerobase.customboard.domain.member.dto.Signup;
import com.zerobase.customboard.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

  @Operation(summary = "회원가입 API", description = "유저가 회원가입시 사용하는 API입니다.", responses = {
      @ApiResponse(responseCode = "200", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "회원가입 실패")
  })
  @Parameters({
      @Parameter(name = "email", example = "test@test.com"),
      @Parameter(name = "password", description = "8~20자 이내 영문,숫자,특수문자 조합"),
      @Parameter(name = "nickname", description = "2~10자 이내 한글,영문,숫자 조합"),
      @Parameter(name = "mobile", description = "휴대폰 번호", example = "010-1234-5678")
  })
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@RequestBody @Valid Signup request) {
    memberService.signup(request);
    return ResponseEntity.ok().build();
  }

}
