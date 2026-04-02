package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.ai.FilterResult;
import com.jobhunter.cli.Console;
import com.jobhunter.exception.AiServiceException;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JobFilter {
  private final ClaudeService claudeService;
  private final Profile profile;

  public JobFilter(ClaudeService claudeService) {
    this.claudeService = claudeService;
    this.profile = new ProfileBuilder(claudeService).getProfile();
  }

  public JobFilter(ClaudeService claudeService, Profile profile) {
    this.claudeService = claudeService;
    this.profile = profile;
  }

  public List<Job> filter(List<Job> jobs) {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> futures = jobs.stream().map(job -> executor.submit(() -> filterOne(job)))
          .collect(Collectors.toList());

      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Console.error("Filter interrupted", e);
        } catch (ExecutionException e) {
          Console.error("Filter failed", e.getCause());
        }
      }
    }

    return jobs.stream().filter(Job::isShouldApply).collect(Collectors.toList());
  }

  public void filterOne(Job job) {
    try {
      FilterResult result = claudeService.filterJob(profile.toString(), job.getDescription());
      job.setShouldApply(result.shouldApply());
      job.setMatchScore(result.matchScore());
    } catch (AiServiceException e) {
      Console.warn("Could not filter job '" + job.getTitle() + "': " + e.getMessage());
      job.setShouldApply(false);
    }
  }
}
