package com.zerobase.customboard.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
  USER("ROLE_USER", "일반 사용자"),
  ADMIN("ROLE_ADMIN", "관리자");
  private final String key;
  private final String description;
}
