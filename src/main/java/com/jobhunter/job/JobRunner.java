package com.jobhunter.job;

import java.util.List;

public class JobRunner {
    private final JobScraper scraper = new JobScraper();
    private final JobFilter filter = new JobFilter();
    private final JobTailor tailor = new JobTailor();

    /** Runs the full pipeline: scrape → filter → tailor. Used by StartCommand. */
    public void runAll() {
        List<Job> jobs = scraper.scrape();

        System.out.println("Scraped " + jobs.size() + " new jobs.");

        List<Job> candidates = filter.filter(jobs);

        System.out.println(candidates.size() + " jobs passed the filter.");

        tailor.tailor(candidates);
    }

    /** Runs the full pipeline for a single job. Used by TailorCommand. */
    public void runOne(Job job) {
        scraper.processJob(job);

        if (job.getNeedsManualReview()) {
            System.err.println("Could not fetch job description for: " + job.getUrl());
            return;
        }

        filter.filterOne(job);

        if (!job.isShouldApply()) {
            System.out.println("Skipping (score " + job.getMatchScore() + "): " + job.getFilterReason());
            return;
        }

        tailor.tailorOne(job);
    }
}
