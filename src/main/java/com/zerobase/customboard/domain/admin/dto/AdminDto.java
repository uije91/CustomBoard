package com.zerobase.customboard.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(name = "게시판 추가")
  public static class addBoardDto{
    private String boardName;
  }

}
