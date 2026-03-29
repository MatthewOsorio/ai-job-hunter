package com.jobhunter.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobhunter.profile.github.GitHubProfile;

public class Profile {
  private final String resume;
  private final GitHubProfile githubProfile;

  @JsonCreator
  public Profile(@JsonProperty("resume") String resume,
      @JsonProperty("githubProfile") GitHubProfile githubProfile) {
    this.resume = resume;
    this.githubProfile = githubProfile;
  }

  public String getResume() {
    return resume;
  }

  public GitHubProfile getGithubProfile() {
    return githubProfile;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Resume:\n").append(resume).append("\n\n");
    sb.append(githubProfile.toString()).append("\n");
    return sb.toString();
  }
}
