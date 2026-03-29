package com.jobhunter.profile.github;

import java.util.List;

public class GitHubProfile {
  String username;
  List<GitHubRepo> topRepos;

  public GitHubProfile() {}

  GitHubProfile(String username, List<GitHubRepo> topRepos) {
    this.username = username;
    this.topRepos = topRepos;
  }

  public String getUsername() {
    return username;
  }

  public List<GitHubRepo> getTopRepos() {
    return topRepos;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setTopRepos(List<GitHubRepo> topRepos) {
    this.topRepos = topRepos;
  }

  public void addRepo(GitHubRepo repo) {
    this.topRepos.add(repo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GitHub Profile: ").append(username).append("\n");
    sb.append("Top Repositories:\n");
    for (GitHubRepo repo : topRepos) {
      sb.append("- ").append(repo.getName()).append(": ").append("\n");
      sb.append("  Languages: ").append(String.join(", ", repo.getLanguages())).append("\n");
      sb.append("  Summary:\n").append(repo.getSummary()).append("\n");
    }
    return sb.toString();
  }
}
