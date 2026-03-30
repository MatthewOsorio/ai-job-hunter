package com.jobhunter.exception;

public abstract class JobHunterException extends RuntimeException {
  public JobHunterException(String message) {
    super(message);
  }

  public JobHunterException(String message, Throwable cause) {
    super(message, cause);
  }
}
