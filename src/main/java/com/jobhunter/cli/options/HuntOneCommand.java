package com.jobhunter.cli.options;

import com.jobhunter.cli.Main;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.exception.TailoringException;
import com.jobhunter.job.Job;
import com.jobhunter.job.JobFilter;
import com.jobhunter.job.JobScraper;
import com.jobhunter.job.JobTailor;

public class HuntOneCommand extends MenuItem {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final int browserPoolSize = 1;

  public HuntOneCommand(String label, String description, ClaudeService claude) {
    super(label, description, claude);
    this.jobScraper = new JobScraper(claude, browserPoolSize);
    this.jobFilter = new JobFilter(claude);
    this.jobTailor = new JobTailor(claude);
  }

  private String scoreLabel(int score) {
    if (score >= 80)
      return "strong match";
    if (score >= 60)
      return "moderate match";
    if (score >= 40)
      return "weak match";
    return "poor match";
  }

  @Override
  public void run() {
    Main.console.info("Paste a full job URL (e.g., https://example.com/job/123)");
    String jobUrl;
    while (true) {
      jobUrl = Main.console.readLine("  Enter the job URL to hunt: ").trim();
      if (jobUrl.isEmpty()) {
        Main.console.warn("Invalid input. Please enter a job URL.");
        continue;
      }
      try {
        URI uri = new URI(jobUrl);
        if (uri.getScheme() == null || uri.getHost() == null) {
          Main.console
              .warn("Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
          continue;
        }
      } catch (URISyntaxException e) {
        Main.console
            .warn("Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
        continue;
      }
      break;
    }

    Main.console.spinnerStart("Fetching job posting ");
    Optional<Job> maybeJob;
    try {
      maybeJob = jobScraper.scrapeOne(jobUrl);
    } catch (JobHunterException e) {
      Main.console.error(e.getMessage());
      return;
    } finally {
      Main.console.spinnerStop();
    }

    if (maybeJob.isEmpty()) {
      Main.console.error("Could not fetch or extract job description");
      return;
    }

    Job job = maybeJob.get();
    Main.console.spinnerStart("Filtering job ");
    try {
      jobFilter.filterOne(job);
    } catch (JobHunterException e) {
      Main.console.error(e.getMessage());
      return;
    } finally {
      Main.console.spinnerStop();
    }

    Main.console.blank();

    int score = job.getMatchScore();
    String scoreStr = "Match score: " + score + "/100 (" + scoreLabel(score) + ")";
    if (job.isShouldApply()) {
      Main.console.generalCat("You should apply!\n" + scoreStr);
    } else {
      Main.console.generalCat("I wouldn't recommend applying.\n" + scoreStr);
      return;
    }
    Main.console.blank();

    while (true) {
      List<MenuItem> options = List.of(new MenuItem("Yes, tailor my resume for this job") {
        @Override
        public void run() {}
      }, new MenuItem("No, don't tailor my resume for this job") {
        @Override
        public void run() {}
      });

      int choice = Main.console.menu(options);
      if (choice == 0) {
        Main.console.spinnerStart("Tailoring resume ");
        List<Path> outputs;
        try {
          outputs = jobTailor.tailor(List.of(job));
        } catch (JobHunterException e) {
          Main.console.error(e.getMessage());
          return;
        } finally {
          Main.console.spinnerStop();
        }

        if (outputs.isEmpty()) {
          throw new TailoringException("Resume could not be tailored.");
        } else {
          Main.console.generalCat("Resume saved to: " + outputs.get(0).toAbsolutePath());
          break;
        }
      } else {
        Main.console.info("Job URL: " + job.getUrl());
        break;
      }
    }
  }

}
