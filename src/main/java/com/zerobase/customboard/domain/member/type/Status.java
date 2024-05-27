package com.zerobase.customboard.domain.member.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
  INACTIVE,
  ACTIVE,
  RESIGN
}
