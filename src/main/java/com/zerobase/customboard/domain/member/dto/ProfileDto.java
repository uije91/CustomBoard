package com.zerobase.customboard.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

public class ProfileDto {

  @Getter
  @Builder
  @Schema(name = "회원정보 수정")
  public static class profileRequest{

    @Schema(example = "영희")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$",
        message = "닉네임은 특수문자를 제외한 2~10자리여야 합니다.")
    private String nickname;

    @Schema(example = "qwer123$")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,20}$",
        message = "비밀번호는 8~20자 영문,숫자,특수문자를 사용하세요.")
    private String password;

    @Schema(example = "010-1234-5678")
    @Pattern(regexp = "^(01[016789]{1})-?[0-9]{3,4}-?[0-9]{4}$",
        message = "010-1234-5678 형식으로 입력해주세요.")
    private String mobile;
  }

  @Getter
  @Builder
  @Schema(name = "회원정보 조회")
  public static class profileResponse{
    private String email;
    private String nickname;
    private String mobile;
  }

}
