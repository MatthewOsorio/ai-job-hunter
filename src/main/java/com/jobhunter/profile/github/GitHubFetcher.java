package com.jobhunter.profile.github;

import java.io.InputStream;
import java.util.List;

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
    List<GitHubRepo> repos = new java.util.ArrayList<>();

    try {
      for (GHRepository repo : user.listRepositories()) {
        if (repo.isFork())
          continue;

        String name = repo.getName();

        if (!topRepos.contains(name))
          continue;

        List<String> languages = repo.listLanguages().keySet().stream().toList();
        InputStream fullReadme = repo.getReadme().read();
        String readmeContent =
            new String(fullReadme.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        fullReadme.close();

        String summary = claude.summarizeReadMe(readmeContent);
        repos.add(new GitHubRepo(name, languages, summary));
      }
    } catch (Exception e) {
      System.err.println("Error fetching repos: " + e.getMessage());
    }

    return new GitHubProfile(username, repos);
  }

}
