package com.zerobase.customboard.global.jwt.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {
  private String accessToken;
  private String refreshToken;

  @Getter
  @Builder
  public static class requestRefresh{
    private String refreshToken;
  }
}
