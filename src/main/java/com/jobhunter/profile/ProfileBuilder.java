package com.jobhunter.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ProfileBuildException;
import com.jobhunter.exception.SpecialInterruption;
import com.jobhunter.profile.github.GitHubFetcher;
import com.jobhunter.profile.github.GitHubProfile;
import com.jobhunter.profile.resume.ResumeParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProfileBuilder {
  private final Path cachePath = Paths.get("profile-cache.json").toAbsolutePath();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ResumeParser resumeParser;
  private final ClaudeService claude;
  private Profile profile;

  public ProfileBuilder(ClaudeService claude) {
    this.claude = claude;
    this.resumeParser = new ResumeParser(claude);
  }

  public Path getCachePath() {
    return cachePath;
  }

  public Profile getProfile() {
    if (profile != null)
      return profile;

    if (!isCached()) {
      profile = buildAndCache();
    } else {
      profile = loadFromCache();
    }

    return profile;
  }

  public Profile rebuildProfile() {
    deleteCache();
    profile = buildAndCache();
    return profile;
  }

  private boolean isCached() {
    return Files.exists(cachePath);
  }

  private Profile loadFromCache() {
    try {
      Profile cachedProfile = objectMapper.readValue(cachePath.toFile(), Profile.class);
      return cachedProfile;
    } catch (IOException e) {
      throw new ProfileBuildException("Failed to read profile cache", e);
    }
  }

  private Profile buildAndCache() {
    Profile builtProfile = buildProfile();
    try {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), builtProfile);
    } catch (IOException e) {
      throw new ProfileBuildException("Failed to write profile cache", e);
    }
    return builtProfile;
  }

  private void deleteCache() {
    try {
      Files.deleteIfExists(cachePath);
    } catch (IOException e) {
      throw new ProfileBuildException("Failed to delete profile cache", e);
    }
  }

  private Profile buildProfile() {
    GitHubFetcher githubFetcher = null;

    if (isGithubAvailable()) {
      githubFetcher = new GitHubFetcher(claude);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> resumeFuture = executor.submit(resumeParser::parse);
      Future<GitHubProfile> githubFuture =
          githubFetcher != null ? executor.submit(githubFetcher::fetch) : null;

      String resume = resumeFuture.get();

      GitHubProfile githubProfile = null;
      if (githubFuture != null) {
        try {
          githubProfile = githubFuture.get();
        } catch (ExecutionException e) {
          Main.console.warn(
              "GitHub profile fetch failed, continuing without it: " + e.getCause().getMessage());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Main.console.warn("GitHub profile fetch interrupted, continuing without it.");
        }
      }

      return new Profile(resume, githubProfile);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SpecialInterruption("Profile build interrupted");
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      throw new ProfileBuildException("Profile build failed: " + cause.getMessage(), cause);
    }
  }

  private boolean isGithubAvailable() {
    boolean hasUsername = Main.config.hasPath("jobhunter.github.username");
    boolean hasRepos = Main.config.hasPath("jobhunter.github.repos");
    if (Main.config.hasPath("jobhunter.github") && !(hasUsername && hasRepos)) {
      Main.console.warn(
          "GitHub config is incomplete (missing username or repos) - skipping GitHub profile");
      return false;
    }
    return hasUsername && hasRepos;
  }
}
