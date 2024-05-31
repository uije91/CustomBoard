package com.zerobase.customboard.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

public class SignupDto {

  @Builder
  @Getter
  @Schema(name = "회원가입")
  public static class request{

    @Schema(example = "test@test.com")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
        message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @Schema(example = "!234qwer")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
        message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
    private String password;

    @Schema(example = "철수")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$",
        message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nickname;

    @Schema(example = "010-1234-5678")
    @Pattern(regexp = "^(01[016789]{1})-?[0-9]{3,4}-?[0-9]{4}$",
        message = "010-1234-5678 형식으로 입력해주세요.")
    private String mobile;
  }


}
