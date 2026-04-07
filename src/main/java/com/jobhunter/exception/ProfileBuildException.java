package com.jobhunter.exception;

public class ProfileBuildException extends JobHunterException {
  public ProfileBuildException(String message) {
    super(message, null);
  }

  public ProfileBuildException(String message, Throwable cause) {
    super(message, cause);
  }
}
