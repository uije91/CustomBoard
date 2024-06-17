package com.zerobase.customboard.global.jwt.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
  private String accessToken;
  private String refreshToken;

  @Getter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class requestRefresh{
    private String refreshToken;
  }
}
