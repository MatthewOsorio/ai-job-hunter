package com.jobhunter.job;

import com.jobhunter.cli.Main;

import java.nio.file.Path;
import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.email.EmailService;
import com.jobhunter.exception.FilteringException;

public class HuntPipeline {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final EmailService emailService;

  public HuntPipeline(ClaudeService claude) {
    this.jobScraper = new JobScraper(claude);
    this.jobFilter = new JobFilter(claude);
    this.jobTailor = new JobTailor(claude);
    this.emailService = new EmailService();
  }

  public void runAll() {
    JobScraperResult result = null;
    Main.console.spinnerStart("Scraping jobs ");

    try {
      result = jobScraper.scrape();
    } finally {
      if (result != null) {
        List<Job> failed = result.getFailedJobs();
        String failedSuffix = failed.isEmpty() ? "" : " (" + failed.size() + " failed)";
        Main.console.spinnerSuccess(result.getValidJobs().size() + " jobs scraped" + failedSuffix);
      } else {
        Main.console.spinnerStop();
      }
    }

    List<Job> validJobs = result.getValidJobs();

    List<Job> filteredJobs = null;
    Main.console.spinnerStart("Filtering jobs ");
    try {
      filteredJobs = jobFilter.filter(validJobs);
    } finally {
      if (filteredJobs != null) {
        Main.console.spinnerSuccess(
            filteredJobs.size() + " of " + validJobs.size() + " jobs matched your profile");
      } else {
        Main.console.spinnerStop();
      }
    }

    if (filteredJobs == null) {
      throw new FilteringException("Filtering did not complete successfully");
    }

    List<Path> tailored = null;
    Main.console.spinnerStart("Tailoring resumes ");
    try {
      tailored = jobTailor.tailor(filteredJobs);
    } finally {
      if (tailored != null) {
        Main.console.spinnerSuccess(tailored.size() + " resumes tailored");
      } else {
        Main.console.spinnerStop();
      }
    }

    emailService.sendJobReport(filteredJobs, result.getFailedJobs());

    Main.console.generalCat("Hunt complete!");
  }
}
