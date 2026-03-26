package com.jobhunter.job;

import java.util.List;

public class JobRunner {

  public void runAll() {
    JobScraper jobScraper = new JobScraper();
    JobScraperResult result = jobScraper.scrape();
    List<Job> validJobs = result.getValidJobs();
    JobFilter jobFilter = new JobFilter();
    List<Job> filteredJobs = jobFilter.filter(validJobs);

  }
}
