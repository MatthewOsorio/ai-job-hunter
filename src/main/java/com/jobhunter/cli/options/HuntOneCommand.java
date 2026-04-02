package com.jobhunter.cli.options;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.job.Job;
import com.jobhunter.job.JobFilter;
import com.jobhunter.job.JobScraper;
import com.jobhunter.job.JobTailor;

public class HuntOneCommand extends MenuItem {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;

  public HuntOneCommand(String label, String description, Spinner spinner, ClaudeService claude) {
    super(label, description, spinner, claude);
    this.jobScraper = new JobScraper(claude);
    this.jobFilter = new JobFilter(claude);
    this.jobTailor = new JobTailor(claude, spinner);
  }

  @Override
  public void run(LineReader reader) {
    String jobUrl;
    while (true) {
      jobUrl = reader.readLine("  Enter the job URL to hunt: ").trim();
      if (jobUrl.isEmpty()) {
        Console.warn("Invalid input. Please enter a job URL.");
        continue;
      }
      try {
        URI uri = new URI(jobUrl);
        if (uri.getScheme() == null || uri.getHost() == null) {
          Console.warn("Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
          continue;
        }
      } catch (URISyntaxException e) {
        Console.warn("Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
        continue;
      }
      break;
    }

    spinner.start("Fetching job posting ");
    Optional<Job> maybeJob;
    try {
      maybeJob = jobScraper.scrapeOne(jobUrl);
    } catch (JobHunterException e) {
      Console.error(e.getMessage(), e);
      return;
    } finally {
      spinner.stop();
    }

    if (maybeJob.isEmpty()) {
      Console.status("Could not fetch or extract job description.");
      return;
    }

    Job job = maybeJob.get();
    spinner.start("Filtering job ");
    try {
      jobFilter.filterOne(job);
    } finally {
      spinner.stop();
    }

    Console.blank();
    Console.header("Result");
    Console.status("Match score: " + job.getMatchScore() + "/100");
    if (job.isShouldApply()) {
      Console.status("Recommendation: Apply");
    } else {
      Console.status("Recommendation: Skip");
      Console.footer();
      return;
    }
    Console.footer();
    Console.blank();

    while (true) {
      String answer = reader.readLine("  Tailor your resume for this job? (y/n): ").trim();
      if (answer.equalsIgnoreCase("y")) {
        spinner.start("Tailoring resume ");
        List<Path> outputs;
        try {
          outputs = jobTailor.tailor(List.of(job));
        } finally {
          spinner.stop();
        }

        if (outputs.isEmpty()) {
          Console.error("Resume tailoring failed — see error details above.");
        } else {
          Console.status("Resume saved to: " + outputs.get(0).toAbsolutePath());
          break;
        }
      } else if (answer.equalsIgnoreCase("n")) {
        break;
      } else {
        Console.warn("Invalid input. Please enter y or n.");
      }
    }
  }
}
