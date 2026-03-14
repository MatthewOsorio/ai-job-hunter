package com.jobhunter.profile;

import com.jobhunter.profile.github.GitHubFetcher;
import com.jobhunter.profile.github.GitHubProfile;
import com.jobhunter.profile.resume.ResumeParser;


// TODO: implement caching and invalidation logic
public class ProfileBuilder {
  private final ResumeParser resumeParser = new ResumeParser();
  private final GitHubFetcher githubFetcher = new GitHubFetcher();
  private Profile profile;

  public Profile getProfile() {
    if (profile == null) {
      profile = buildProfile();
    }
    return profile;
  }

  public Profile buildProfile() {
    String resume = resumeParser.parse();
    GitHubProfile githubProfile = githubFetcher.fetch();
    return new Profile(resume, githubProfile);
  }
}
