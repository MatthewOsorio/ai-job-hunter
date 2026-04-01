package com.jobhunter.profile;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobhunter.profile.github.GitHubProfile;

public class Profile {
  private final String resume;
  private final Optional<GitHubProfile> githubProfile;

  @JsonCreator
  public Profile(@JsonProperty("resume") String resume,
      @JsonProperty("githubProfile") GitHubProfile githubProfile) {
    this.resume = resume;
    this.githubProfile = Optional.ofNullable(githubProfile);
  }

  public String getResume() {
    return resume;
  }

  @JsonIgnore
  public Optional<GitHubProfile> getGithubProfile() {
    return githubProfile;
  }

  @JsonGetter("githubProfile")
  public GitHubProfile getGithubProfileForJson() {
    return githubProfile.orElse(null);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Resume:\n").append(resume).append("\n\n");
    githubProfile.ifPresent(gh -> sb.append(gh).append("\n"));
    return sb.toString();
  }
}
