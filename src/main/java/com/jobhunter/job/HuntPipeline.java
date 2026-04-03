package com.jobhunter.job;

import com.jobhunter.cli.Main;

import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.email.EmailService;

public class HuntPipeline {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final EmailService emailService;
  private final Console console;

  public HuntPipeline(ClaudeService claude, Console console) {
    this.jobScraper = new JobScraper(claude);
    this.jobFilter = new JobFilter(claude);
    this.jobTailor = new JobTailor(claude);
    this.emailService = new EmailService();
    this.console = console;
  }

  public void runAll() {
    JobScraperResult result;
    console.spinnerStart("Scraping jobs ");
    try {
      result = jobScraper.scrape();
    } finally {
      console.spinnerStop();
    }

    List<Job> validJobs = result.getValidJobs();

    List<Job> filteredJobs;
    console.spinnerStart("Filtering jobs ");
    try {
      filteredJobs = jobFilter.filter(validJobs);
    } finally {
      console.spinnerStop();
    }

    console.spinnerStart("Tailoring resumes ");
    try {
      jobTailor.tailor(filteredJobs);
    } finally {
      console.spinnerStop();
    }

    console.spinnerStart("Sending email report ");
    try {
      emailService.sendJobReport(filteredJobs, result.getFailedJobs());
    } finally {
      console.spinnerStop();
    }

    Main.console.status("Hunt job complete!");
  }
}
