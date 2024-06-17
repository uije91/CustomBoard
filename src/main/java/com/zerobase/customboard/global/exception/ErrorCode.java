package com.zerobase.customboard.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Mail Error
  CODE_NOT_FOUND(400, "인증번호가 존재하지 않습니다."),

  // Member Error
  USER_NOT_FOUND(404, "회원 정보를 찾을 수 없습니다"),
  ALREADY_REGISTERED_USER(400, "이미 가입한 회원입니다."),
  PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다"),
  AUTHENTICATE_YOUR_ACCOUNT(400, "인증된 사용자만 로그인 할 수 있습니다"),
  INVALID_CODE(400,"잘못된 인증코드 입니다."),
  INVALID_REFRESH_TOKEN(400,"잘못된 리프레시 토큰입니다"),
  NICKNAME_ALREADY_EXISTS(400,"이미 존재하는 닉네임입니다" ),

  // System Error
  INTERNAL_SERVER_ERROR(500, "내부 서버 오류가 발생했습니다."),
  BAD_REQUEST_VALID_ERROR(400, "유효성 검사에 실패했습니다."),


  ;
  private final int status;
  private final String message;
}
