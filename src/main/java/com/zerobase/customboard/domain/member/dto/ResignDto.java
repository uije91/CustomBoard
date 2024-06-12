package com.zerobase.customboard.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "회원탈퇴")
public class ResignDto {
  @Schema(example = "!234qwer")
  private String password;

}
