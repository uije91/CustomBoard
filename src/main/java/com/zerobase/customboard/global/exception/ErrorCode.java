package com.zerobase.customboard.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  USER_NOT_FOUND(404, "회원 정보를 찾을 수 없습니다"),
  ALREADY_REGISTERED_USER(400, "이미 가입한 회원입니다."),


  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다.");

  private final int status;
  private final String message;
}
