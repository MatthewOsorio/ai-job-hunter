package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ScrapingException;
import com.jobhunter.job.source.JobSource;
import com.jobhunter.job.source.JobSourceFactory;
import com.jobhunter.scraper.FetchResult;
import com.jobhunter.scraper.FetchStatus;
import com.jobhunter.scraper.PageFetcher;
import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JobScraper {
  private final Config config = Main.config;
  private final PageFetcher pageFetcher = new PageFetcher();
  private final ClaudeService claudeService;

  public JobScraper(ClaudeService claudeService) {
    this.claudeService = claudeService;
  }

  public JobScraperResult scrape() throws ScrapingException {
    List<Job> jobs = new ArrayList<>();

    if (!config.hasPath("jobhunter.sources") || config.getConfigList("jobhunter.sources").isEmpty()) {
      throw new ScrapingException("No job sources configured under 'jobhunter.sources'");
    }

    for (JobSource source : JobSourceFactory
        .fromConfig(config.getConfigList("jobhunter.sources"))) {
      jobs.addAll(source.scrape());
    }

    JobScraperResult results = new JobScraperResult();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> futures = new ArrayList<>();

      for (Job job : jobs) {
        futures.add(executor.submit(() -> processJob(job, results)));
      }

      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Console.error("Scrape interrupted", e);
        } catch (ExecutionException e) {
          Console.error("Scrape thread failed", e.getCause());
        }
      }
    }

    pageFetcher.close();

    return results;
  }

  public void processJob(Job job, JobScraperResult result) {
    FetchResult fetchResult = pageFetcher.fetch(job.getUrl());

    if (fetchResult.getStatus() != FetchStatus.SUCCESS) {
      Console.warn("Fetch failed for " + job.getUrl() + ": " + fetchResult.getReason());
      result.addFailedJob(job);
      return;
    }

    if (fetchResult.needsExtraction()) {
      Optional<String> desc = claudeService.extractJobDescription(fetchResult.getContent());
      if (desc.isPresent()) {
        job.setDescription(desc.get());
        result.addValidJob(job);
      } else {
        result.addFailedJob(job);
      }
    } else {
      job.setDescription(fetchResult.getContent());
      result.addValidJob(job);
    }
  }
}
