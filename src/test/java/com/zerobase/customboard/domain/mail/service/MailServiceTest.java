package com.zerobase.customboard.domain.mail.service;

import static com.zerobase.customboard.domain.member.type.Status.INACTIVE;
import static com.zerobase.customboard.global.exception.ErrorCode.CODE_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.zerobase.customboard.domain.mail.dto.MailCheckDto;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.domain.member.type.Status;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.infra.service.RedisService;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

  @InjectMocks
  private MailService mailService;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private MimeMessage mimeMessage;

  @Mock
  private RedisService redisService;


  @Test
  @DisplayName("이메일 인증 확인")
  void testCertifyCheck_success() throws Exception {
    // given
    MailCheckDto check = new MailCheckDto("test@test.com", "123456");
    given(redisService.getData("[AUTH_CODE]test@test.com")).willReturn("123456");

    // when & then
    assertTrue(mailService.certifyCheck(check));
  }

  @Test
  @DisplayName("이메일 인증 실패 - 다른 코드 입력")
  void testCertifyCheck_fail_codeNotFound() throws Exception {
    // given
    MailCheckDto check = new MailCheckDto("test@test.com", "1234");
    given(redisService.getData("[AUTH_CODE]test@test.com")).willReturn("123456");

    // when & then
    assertFalse(mailService.certifyCheck(check));
  }

  @Test
  @DisplayName("이메일 전송 확인")
  void testSendMail() throws Exception {
    // given
    String email = "test@example.com";
    String subject = "Test Subject";
    String text = "Test Text";

    given(mailSender.createMimeMessage()).willReturn(mimeMessage);

    // when
    mailService.sendMail(email, subject, text);

    //then
    verify(mailSender, times(1)).createMimeMessage();
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("인증번호 생성 확인")
  void testGetVerificationCode() throws Exception {
    SecureRandom secureRandom = new SecureRandom();
    String code = String.valueOf(secureRandom.nextInt(900000) + 100000);

    assertNotNull(code);
    assertTrue(code.matches("\\d{6}")); // 6자리 숫자인지 확인
    assertTrue(Integer.parseInt(code) >= 100000); // 최소값 확인
    assertTrue(Integer.parseInt(code) <= 999999); // 최대값 확인
  }
}