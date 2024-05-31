package com.zerobase.customboard.domain.mail.controller;

import com.zerobase.customboard.domain.mail.dto.MailCheckDto;
import com.zerobase.customboard.domain.mail.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
@Tag(name = "E-Mail", description = "이메일 인증 API")
public class MailController {

  private final MailService mailService;

  @Operation(summary = "이메일 인증 API",description = "인증코드 발송 API 입니다.")
  @PostMapping("/certify/{email}")
  public ResponseEntity<?> certifySignup(
      @Parameter(name = "email", example = "test@test.com") @PathVariable String email) {
    return ResponseEntity.ok(mailService.sendCertificationMail(email));
  }

  @Operation(summary = "이메일 인증확인 API",description = "인증코드를 확인합니다.")
  @PostMapping("/certify/check")
  public ResponseEntity<?> certifyCheck(@RequestBody @Valid MailCheckDto check) {
    mailService.certifyCheck(check);
    return ResponseEntity.ok().build();
  }


}
