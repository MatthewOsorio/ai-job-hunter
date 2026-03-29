package com.jobhunter.job;

import java.util.List;

import com.jobhunter.cli.Console;

public class JobRunner {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;

  public JobRunner() {
    this.jobScraper = new JobScraper();
    this.jobFilter = new JobFilter();
  }

  public void runAll() {
    JobScraperResult result = jobScraper.scrape();
    List<Job> validJobs = result.getValidJobs();
    List<Job> filteredJobs = jobFilter.filter(validJobs);


    Console.header("Filtered Jobs");
    for (Job job : filteredJobs) {
      Console.item(job.toString());
    }
    Console.footer();
  }
}
