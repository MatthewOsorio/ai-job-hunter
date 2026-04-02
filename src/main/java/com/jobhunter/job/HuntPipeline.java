package com.jobhunter.job;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.email.EmailService;
import com.jobhunter.profile.Profile;

public class HuntPipeline {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final JobTailor jobTailor;
  private final EmailService emailService;
  private final Spinner spinner;

  public HuntPipeline(JobScraper jobScraper, JobFilter jobFilter, JobTailor jobTailor,
      EmailService emailService, Spinner spinner) {
    this.jobScraper = jobScraper;
    this.jobFilter = jobFilter;
    this.jobTailor = jobTailor;
    this.emailService = emailService;
    this.spinner = spinner;
  }

  public HuntPipeline(ClaudeService claude, Spinner spinner) {
    this(new JobScraper(claude), new JobFilter(claude), new JobTailor(claude), new EmailService(),
        spinner);
  }

  public HuntPipeline(ClaudeService claude, Profile profile, Spinner spinner) {
    this(new JobScraper(claude), new JobFilter(claude, profile), new JobTailor(claude),
        new EmailService(), spinner);
  }

  public Optional<Job> scrapeOne(String url) {
    return jobScraper.scrapeOne(url);
  }

  public void filterOne(Job job) {
    jobFilter.filterOne(job);
  }

  public List<Path> tailor(List<Job> jobs) {
    return jobTailor.tailor(jobs);
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
