package com.jobhunter.profile;

import com.jobhunter.profile.github.GitHubFetcher;
import com.jobhunter.profile.github.GitHubProfile;
import com.jobhunter.profile.resume.ResumeParser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// TODO: implement caching and invalidation logic
public class ProfileBuilder {
  private final GitHubFetcher githubFetcher = new GitHubFetcher();
  private Profile profile;

  public synchronized Profile getProfile() {
    if (profile == null) {
      profile = buildProfile();
    }
    return profile;
  }

  public Profile buildProfile() {
    ResumeParser resumeParser = new ResumeParser();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> resumeFuture = executor.submit(resumeParser::parse);
      Future<GitHubProfile> githubFuture = executor.submit(githubFetcher::fetch);

      String resume = resumeFuture.get();
      GitHubProfile githubProfile = githubFuture.get();
      return new Profile(resume, githubProfile);
    } catch (Exception e) {
      throw new RuntimeException("Failed to build profile: " + e.getMessage(), e);
    }
  }
}
