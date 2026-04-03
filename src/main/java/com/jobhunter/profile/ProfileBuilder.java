package com.jobhunter.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ProfileBuildException;
import com.jobhunter.profile.github.GitHubFetcher;
import com.jobhunter.profile.github.GitHubProfile;
import com.jobhunter.profile.resume.ResumeParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProfileBuilder {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ResumeParser resumeParser;
  private final ClaudeService claude;
  private final Path cachePath;
  private Profile profile;

  public ProfileBuilder(ClaudeService claude) {
    this.claude = claude;
    this.resumeParser = new ResumeParser(claude);
    String path = Main.config.hasPath("jobhunter.profile.cachePath")
        ? Main.config.getString("jobhunter.profile.cachePath")
        : "profile-cache.json";
    this.cachePath = Paths.get(path);
  }

  public boolean isCached() {
    return Files.exists(cachePath);
  }

  public Profile getProfile() {
    if (profile != null)
      return profile;
    profile = loadFromCache().orElseGet(this::buildAndCache);
    return profile;
  }

  public Profile rebuildProfile() {
    deleteCache();
    profile = buildAndCache();
    return profile;
  }

  private Optional<Profile> loadFromCache() {
    if (!Files.exists(cachePath))
      return Optional.empty();
    try {
      Profile cached = objectMapper.readValue(cachePath.toFile(), Profile.class);
      return Optional.of(cached);
    } catch (IOException e) {
      Main.console.warn("Profile cache unreadable, rebuilding: " + e.getMessage());
      return Optional.empty();
    }
  }

  private Profile buildAndCache() {
    Profile built = buildProfile();
    try {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), built);
    } catch (IOException e) {
      Main.console.error("Failed to write profile cache", e);
    }
    return built;
  }

  private void deleteCache() {
    try {
      Files.deleteIfExists(cachePath);
    } catch (IOException e) {
      Main.console.error("Failed to delete profile cache", e);
    }
  }

  private Profile buildProfile() {
    GitHubFetcher githubFetcher = null;

    if (isGithubAvailable()) {
      githubFetcher = new GitHubFetcher(claude);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> resumeFuture = executor.submit(resumeParser::parse);

      if (githubFetcher != null) {
        Future<GitHubProfile> githubFuture = executor.submit(githubFetcher::fetch);
        String resume = resumeFuture.get();
        GitHubProfile githubProfile = githubFuture.get();
        return new Profile(resume, githubProfile);
      } else {
        String resume = resumeFuture.get();
        return new Profile(resume, null);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ProfileBuildException("Profile build interrupted", e);
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
