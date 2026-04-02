package com.jobhunter.job;

import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.email.EmailService;

public class HuntPipeline {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final EmailService emailService;
  private final Spinner spinner;

  public HuntPipeline(ClaudeService claude, Spinner spinner) {
    this.jobScraper = new JobScraper(claude);
    this.jobFilter = new JobFilter(claude);
    this.jobTailor = new JobTailor(claude, spinner);
    this.emailService = new EmailService();
    this.spinner = spinner;
  }

  public void runAll() {
    JobScraperResult result;
    spinner.start("Scraping jobs ");
    try {
      result = jobScraper.scrape();
    } finally {
      spinner.stop();
    }

    List<Job> validJobs = result.getValidJobs();

    List<Job> filteredJobs;
    spinner.start("Filtering jobs ");
    try {
      filteredJobs = jobFilter.filter(validJobs);
    } finally {
      spinner.stop();
    }

    spinner.start("Tailoring resumes ");
    try {
      jobTailor.tailor(filteredJobs);
    } finally {
      spinner.stop();
    }

    spinner.start("Sending email report ");
    try {
      emailService.sendJobReport(filteredJobs, result.getFailedJobs());
    } finally {
      spinner.stop();
    }

    Console.status("Hunt job complete!");
  }
}
