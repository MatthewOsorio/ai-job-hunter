package com.jobhunter.exception;

public class ResumeNotFoundException extends JobHunterException {
  public ResumeNotFoundException(String message) {
    super(message);
  }

  public ResumeNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
