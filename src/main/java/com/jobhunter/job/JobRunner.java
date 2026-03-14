package com.jobhunter.job;

import java.util.List;

import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;
import com.jobhunter.profile.github.GitHubFetcher;

public class JobRunner {
  private final ProfileBuilder profileBuilder = new ProfileBuilder();

  public void runAll() {
    Profile profile = profileBuilder.getProfile();
    System.out.println(profile);
  }
}
