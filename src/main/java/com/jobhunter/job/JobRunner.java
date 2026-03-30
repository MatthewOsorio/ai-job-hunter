package com.jobhunter.job;

import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;

public class JobRunner {
  private final JobScraper jobScraper;
  private final JobFilter jobFilter;
  private final ClaudeService claude;

  public JobRunner(ClaudeService claude) {
    this.claude = claude;
    this.jobScraper = new JobScraper(this.claude);
    this.jobFilter = new JobFilter(this.claude);
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
