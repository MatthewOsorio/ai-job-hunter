package com.jobhunter.cli.options;

import com.jobhunter.cli.Main;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.exception.JobHunterException;
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

  @Override
  public void run(LineReader reader) {
    String jobUrl;
    while (true) {
      jobUrl = reader.readLine("  Enter the job URL to hunt: ").trim();
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

    // console.spinnerStart("Fetching job posting ");
    Optional<Job> maybeJob;
    try {
      maybeJob = jobScraper.scrapeOne(jobUrl);
    } catch (JobHunterException e) {
      Main.console.error(e.getMessage(), e);
      return;
    } finally {
      // console.spinnerStop();
    }

    if (maybeJob.isEmpty()) {
      Main.console.status("Could not fetch or extract job description.");
      return;
    }

    Job job = maybeJob.get();
    // console.spinnerStart("Filtering job ");
    try {
      jobFilter.filterOne(job);
    } finally {
      // console.spinnerStop();
    }

    Main.console.blank();
    // Main.console.header("Result");
    // Main.console.status("Match score: " + job.getMatchScore() + "/100");
    if (job.isShouldApply()) {
      Main.console.status("Recommendation: Apply");
    } else {
      Main.console.status("Recommendation: Skip");
      // Main.console.footer();
      return;
    }
    // Main.console.footer();
    Main.console.blank();

    while (true) {
      String answer = reader.readLine("  Tailor your resume for this job? (y/n): ").trim();
      if (answer.equalsIgnoreCase("y")) {
        // console.spinnerStart("Tailoring resume ");
        List<Path> outputs;
        try {
          outputs = jobTailor.tailor(List.of(job));
        } finally {
          // console.spinnerStop();
        }

        if (outputs.isEmpty()) {
          Main.console.error("Resume tailoring failed — see error details above.");
        } else {
          Main.console.status("Resume saved to: " + outputs.get(0).toAbsolutePath());
          break;
        }
      } else if (answer.equalsIgnoreCase("n")) {
        break;
      } else {
        Main.console.warn("Invalid input. Please enter y or n.");
      }
    }
  }
}
