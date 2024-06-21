package com.zerobase.customboard.domain.member.dto;

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
@Schema(name = "비밀번호 변경")
public class PasswordDto {
  @Schema(example = "!234qwer")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
      message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
  private String password;

  @Schema(example = "!234qwer")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
      message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
  private String passwordConfirm;
}
