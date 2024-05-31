package com.zerobase.customboard.global.exception;

import static com.zerobase.customboard.global.exception.ErrorCode.BAD_REQUEST_VALID_ERROR;
import static com.zerobase.customboard.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ErrorResponse handleCustomException(CustomException e) {
    log.error("Exception is occurred : \"{}({})\"", e.getErrorCode(), e.getErrorCode().getMessage());

    return new ErrorResponse(e.getErrorCode(), e.getErrorCode().getStatus(),
        e.getErrorCode().getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    List<String> errors = e.getBindingResult().getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .toList();
    log.error("Exception is occurred : \"{}({})\"", BAD_REQUEST_VALID_ERROR, errors.get(0));

    return new ErrorResponse(BAD_REQUEST_VALID_ERROR, 400, errors.get(0));
  }

  @ExceptionHandler(Exception.class)
  public ErrorResponse handleException(Exception e) {
    log.error("Exception is occurred : {} ", e.getMessage());

    return new ErrorResponse(INTERNAL_SERVER_ERROR, 500, INTERNAL_SERVER_ERROR.getMessage());
  }
}
