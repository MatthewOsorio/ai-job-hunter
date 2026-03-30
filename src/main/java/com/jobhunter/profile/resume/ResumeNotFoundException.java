package com.jobhunter.profile.resume;

import com.jobhunter.exception.JobHunterException;

public class ResumeNotFoundException extends JobHunterException {
  public ResumeNotFoundException(String message) {
    super(message);
  }
}
