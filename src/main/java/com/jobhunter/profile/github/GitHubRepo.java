package com.jobhunter.profile.github;

import java.util.List;

public class GitHubRepo {
  private String name;
  private String summary;
  private List<String> languages;

  public GitHubRepo() {}

  public GitHubRepo(String name, List<String> languages, String summary) {
    this.name = name;
    this.languages = languages;
    this.summary = summary;
  }

  public GitHubRepo(String name, List<String> languages) {
    this.name = name;
    this.languages = languages;
  }

  public String getName() {
    return name;
  }

  public String getSummary() {
    return summary;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public void setLanguages(List<String> languages) {
    this.languages = languages;
  }
}
