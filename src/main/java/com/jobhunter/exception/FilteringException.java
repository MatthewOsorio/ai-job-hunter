package com.jobhunter.exception;

public class FilteringException extends JobHunterException {
  public FilteringException(String message) {
    super(message);
  }

  public FilteringException(String message, Throwable cause) {
    super(message, cause);
  }

}
