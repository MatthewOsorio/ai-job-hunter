package com.jobhunter.exception;

public class ScrapingException extends JobHunterException {
  public ScrapingException(String message) {
    super(message);
  }

  public ScrapingException(String message, Throwable cause) {
    super(message, cause);
  }
}
