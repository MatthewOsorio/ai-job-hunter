package com.jobhunter.job.source;

import com.jobhunter.job.Job;

import java.util.List;

public abstract class JobSource {
  private final String name;
  private final String url;

  protected JobSource(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public abstract List<Job> scrape();

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }
}
