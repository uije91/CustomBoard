package com.zerobase.customboard.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class LoginDto {

  @Getter
  @Builder
  @Schema(name = "로그인")
  public static class request {

    @Schema(example = "test@test.com")
    private String email;

    @Schema(example = "!234qwer")
    private String password;
  }
}
