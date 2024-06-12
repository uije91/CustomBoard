package com.zerobase.customboard.domain.mail.service;

import static com.zerobase.customboard.domain.member.type.Status.ACTIVE;
import static com.zerobase.customboard.domain.member.type.Status.INACTIVE;
import static com.zerobase.customboard.global.exception.ErrorCode.CODE_NOT_FOUND;
import static com.zerobase.customboard.global.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.customboard.domain.mail.dto.MailCheckDto;
import com.zerobase.customboard.domain.member.entity.Member;
import com.zerobase.customboard.domain.member.repository.MemberRepository;
import com.zerobase.customboard.global.exception.CustomException;
import com.zerobase.customboard.infra.service.RedisService;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;
  private final RedisService redisService;
  private final static Long VERIFICATION_EXPIRED = 60 * 3 * 1000L; // 3분
  private final static String AUTH_CODE_PREFIX = "[AUTH_CODE]";
  private final MemberRepository memberRepository;

  // 인증 번호 생성
  private String createCode(String email) {
    SecureRandom secureRandom = new SecureRandom();
    String code = String.valueOf(secureRandom.nextInt(900000) + 100000);

    if (redisService.getData(AUTH_CODE_PREFIX + email) != null) {
      redisService.deleteData(AUTH_CODE_PREFIX + email);
    }

    redisService.setDataExpire(AUTH_CODE_PREFIX + email, code, VERIFICATION_EXPIRED);
    return code;
  }

  // 인증번호 확인
  public boolean certifyCheck(MailCheckDto check) {
    String authCode = redisService.getData(AUTH_CODE_PREFIX + check.getEmail());

    if(authCode==null){
      return false;
    }

    if (authCode.equals(check.getCode())) {
      redisService.deleteData(AUTH_CODE_PREFIX + check.getEmail());
      return true;
    }
    return false;
  }

  // 인증 메일 보내기
  public String sendCertificationMail(String email) {
    String code = createCode(email);
    String subject = "[Custom Board] 인증 메일";
    String text =
        "안녕하세요. [Custom Board] 입니다.<p>"
            + "인증 번호는 [" + code + "] 입니다.<p>"
            + "인증 번호를 입력하고 인증 완료 버튼을 눌러주세요.";

    sendMail(email, subject, text);
    return code;
  }

  // 이메일 전송 구현
  public void sendMail(String email, String subject, String text) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      messageHelper.setTo(email);
      messageHelper.setSubject(subject);
      messageHelper.setText(text, true);

      mailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error("[MailService][sendMail] : {}", e.getMessage());
    }
  }

  //

}
