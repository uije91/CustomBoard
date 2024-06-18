package com.zerobase.customboard.domain.admin.dto;

import com.zerobase.customboard.global.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminDto {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class changeRoleDto{
    private Role role;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class addBoardDto{
    private String boardName;
  }

}
