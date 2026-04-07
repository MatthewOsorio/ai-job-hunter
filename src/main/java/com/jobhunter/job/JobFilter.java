package com.jobhunter.job;

import com.jobhunter.cli.Main;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.ai.FilterResult;
import com.jobhunter.exception.AiServiceException;
import com.jobhunter.exception.FilteringException;
import com.jobhunter.exception.SpecialInterruption;
import com.jobhunter.profile.ProfileBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JobFilter {
  private final ClaudeService claudeService;
  private final ProfileBuilder profileBuilder;

  public JobFilter(ClaudeService claudeService) {
    this.claudeService = claudeService;
    this.profileBuilder = new ProfileBuilder(claudeService);
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
          throw new SpecialInterruption("Interrupted while filtering jobs");
        } catch (ExecutionException e) {
          throw new FilteringException(e.getMessage(), e.getCause());
        }
      }
    }

    return jobs.stream().filter(Job::isShouldApply).collect(Collectors.toList());
  }

  public void filterOne(Job job) {
    try {
      FilterResult result =
          claudeService.filterJob(profileBuilder.getProfile().toString(), job.getDescription());
      job.setShouldApply(result.shouldApply());
      job.setMatchScore(result.matchScore());
    } catch (AiServiceException e) {
      job.setShouldApply(false);
      job.setMatchScore(0);
    }
  }
}
