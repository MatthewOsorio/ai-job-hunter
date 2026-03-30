package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.ai.FilterResult;
import com.jobhunter.cli.Console;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

import java.util.List;
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

  public List<Job> filter(List<Job> jobs) {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> futures = jobs.stream().map(job -> executor.submit(() -> filterOne(job)))
          .collect(Collectors.toList());

      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (Exception e) {
          Console.error("Filter failed", e);
        }
      }
    }

    return jobs.stream().filter(Job::isShouldApply).collect(Collectors.toList());
  }

  public void filterOne(Job job) {
    FilterResult result = claudeService.filterJob(profile.toString(), job.getDescription());
    job.setShouldApply(result.shouldApply());
    job.setMatchScore(result.matchScore());;
  }
}
