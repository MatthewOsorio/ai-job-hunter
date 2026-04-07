package com.jobhunter.exception;

public class ConfigurationException extends JobHunterException {
  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
