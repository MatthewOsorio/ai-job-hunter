package com.jobhunter.exception;

public class AiServiceException extends JobHunterException {
  public AiServiceException(String message) {
    super(message);
  }

  public AiServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
