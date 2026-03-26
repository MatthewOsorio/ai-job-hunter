package com.jobhunter.profile.github;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;

public class GitHubFetcher {
  private final GHUser user;
  private final List<String> topRepos = Main.config.getStringList("jobhunter.github.repos");
  private final String username = Main.config.getString("jobhunter.github.username");
  private final ClaudeService claude = new ClaudeService();

  public GitHubFetcher() {
    try {
      GitHub github = GitHub.connectAnonymously();
      this.user = github.getUser(username);
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect to GitHub for user: " + username, e);
    }
  }

  public GitHubProfile fetch() {
    List<Future<GitHubRepo>> futures = new ArrayList<>();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (GHRepository repo : user.listRepositories()) {
        if (repo.isFork())
          continue;

        String name = repo.getName();

        if (!topRepos.contains(name))
          continue;

        futures.add(executor.submit(() -> {
          List<String> languages = repo.listLanguages().keySet().stream().toList();
          try (InputStream fullReadme = repo.getReadme().read()) {
            String readmeContent =
                new String(fullReadme.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String summary = claude.summarizeReadMe(readmeContent);
            return new GitHubRepo(name, languages, summary);
          }
        }));
      }
    } catch (Exception e) {
      System.err.println("Error fetching repos: " + e.getMessage());
    }

    List<GitHubRepo> repos = new ArrayList<>();
    for (Future<GitHubRepo> future : futures) {
      try {
        repos.add(future.get());
      } catch (Exception e) {
        System.err.println("Error processing repo: " + e.getMessage());
      }
    }

    return new GitHubProfile(username, repos);
  }

}
