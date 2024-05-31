package com.zerobase.customboard.domain.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "이메일 인증")
public class MailCheckDto {

  @Schema(example = "test@test.com")
  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
      message = "이메일 형식에 맞지 않습니다.")
  private String email;

  @Schema(example = "123456")
  @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리의 숫자로 입력해주세요")
  private String code;
}
