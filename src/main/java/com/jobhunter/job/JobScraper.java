package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.ai.ExtractionResult;
import com.jobhunter.ai.JobMetaResult;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.AiServiceException;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.exception.ScrapingException;
import com.jobhunter.exception.SpecialInterruption;
import com.jobhunter.job.source.JobSource;
import com.jobhunter.job.source.JobSourceFactory;
import com.jobhunter.scraper.BrowserPool;
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
  private final PageFetcher pageFetcher;
  private final ClaudeService claudeService;

  public JobScraper(ClaudeService claudeService) {
    this(claudeService, BrowserPool.DEFAULT_POOL_SIZE);
  }

  public JobScraper(ClaudeService claudeService, int browserPoolSize) {
    this.claudeService = claudeService;
    this.pageFetcher = new PageFetcher(browserPoolSize);
  }

  public JobScraperResult scrape() throws ScrapingException {
    List<Job> jobs = new ArrayList<>();

    if (!config.hasPath("jobhunter.sources")
        || config.getConfigList("jobhunter.sources").isEmpty()) {
      throw new ScrapingException("No job sources configured under 'jobhunter.sources'");
    }

    List<JobSource> sources =
        JobSourceFactory.fromConfig(config.getConfigList("jobhunter.sources"));

    if (sources.isEmpty()) {
      throw new ScrapingException("No valid job sources found under 'jobhunter.sources'");
    }

    for (JobSource source : sources) {
      jobs.addAll(source.scrape());
    }

    if (jobs.isEmpty()) {
      Main.console.warn("No jobs found from any source");
      throw new ScrapingException("No jobs found from any source");
    }

    JobScraperResult results = new JobScraperResult();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> futures = new ArrayList<>();

      for (Job job : jobs) {
        futures.add(executor.submit(() -> {
          try {
            processJob(job, results);
          } catch (SpecialInterruption e) {
            throw e;
          } catch (JobHunterException e) {
            Main.console.warn("Skipping job at " + job.getUrl() + ": " + e.getMessage());
            results.addFailedJob(job);
          }
        }));
      }

      for (Future<?> future : futures) {
        try {
          future.get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SpecialInterruption("Job scraping interrupted!");
        } catch (ExecutionException e) {
          if (e.getCause() instanceof SpecialInterruption si) {
            throw si;
          }
          throw new ScrapingException("Scrape task failed: " + e.getCause().getMessage(),
              e.getCause());
        }
      }
    }

    pageFetcher.close();

    if (results.getValidJobs().isEmpty()) {
      throw new ScrapingException("All " + jobs.size() + " job(s) failed to fetch or extract");
    }

    return results;
  }

  public Optional<Job> scrapeOne(String url) {
    Job job = new Job(null, null, url);
    JobScraperResult result = new JobScraperResult();
    processJob(job, result);
    pageFetcher.close();
    return result.getValidJobs().isEmpty() ? Optional.empty()
        : Optional.of(result.getValidJobs().get(0));
  }

  public void processJob(Job job, JobScraperResult result) {
    FetchResult fetchResult = pageFetcher.fetch(job.getUrl());

    if (fetchResult.getStatus() != FetchStatus.SUCCESS) {
      Main.console.warn("Fetch failed for " + job.getUrl() + ": " + fetchResult.getReason());
      result.addFailedJob(job);
      return;
    }

    if (fetchResult.needsExtraction()) {
      Optional<ExtractionResult> desc =
          claudeService.extractJobDescription(fetchResult.getContent());
      if (desc.isPresent()) {
        job.setDescription(desc.get().description());
        try {
          Optional<JobMetaResult> meta = claudeService.extractJobMeta(fetchResult.getContent());
          meta.ifPresent(m -> {
            if (m.title() != null)
              job.setTitle(m.title());
            if (m.company() != null)
              job.setCompany(m.company());
          });
        } catch (AiServiceException e) {
          // meta is best-effort; title/company stay null
        }
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
