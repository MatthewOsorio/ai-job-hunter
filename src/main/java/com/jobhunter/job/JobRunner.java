package com.jobhunter.job;

import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Spinner;
import com.jobhunter.email.EmailService;

public class JobRunner {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final EmailService emailService;
  private final ClaudeService claude;
  private final Spinner spinner;

  public JobRunner(ClaudeService claude, Spinner spinner) {
    this.claude = claude;
    this.spinner = spinner;
    this.jobScraper = new JobScraper(this.claude);
    this.jobFilter = new JobFilter(this.claude);
    this.jobTailor = new JobTailor(this.claude);
    this.emailService = new EmailService();
  }

  public void runAll() {
    spinner.start("Scraping jobs ");
    JobScraperResult result = jobScraper.scrape();
    spinner.stop();

    List<Job> validJobs = result.getValidJobs();

    spinner.start("Filtering jobs ");
    List<Job> filteredJobs = jobFilter.filter(validJobs);
    spinner.stop();

    spinner.start("Tailoring resumes ");
    jobTailor.tailor(filteredJobs);
    spinner.stop();

    spinner.start("Sending email report ");
    emailService.sendJobReport(filteredJobs, result.getFailedJobs());
    spinner.stop();

    com.jobhunter.cli.Console.status("Hunt job complete!");
  }
}
