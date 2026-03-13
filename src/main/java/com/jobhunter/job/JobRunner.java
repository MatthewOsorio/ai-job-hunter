package com.jobhunter.job;

import java.util.List;

import com.jobhunter.profile.ProfileBuilder;

public class JobRunner {
    private final JobScraper scraper = new JobScraper();
    // private final JobFilter filter = new JobFilter();
    // private final JobTailor tailor = new JobTailor();

    public void runAll() {
        ProfileBuilder profile = new ProfileBuilder();
        profile.build();
        // JobScraperResult jobs = scraper.scrape();
        // List<Job> candidates = filter.filter(jobs.getValidJobs());
        // tailor.tailor(candidates);
    }

    /** Runs the full pipeline for a single job. Used by TailorCommand. */
    // public void runOne(Job job) {
    //     scraper.processJob(job);

    //     if (job.getNeedsManualReview()) {
    //         System.err.println("Could not fetch job description for: " + job.getUrl());
    //         return;
    //     }

    //     filter.filterOne(job);

    //     if (!job.isShouldApply()) {
    //         System.out.println("Skipping (score " + job.getMatchScore() + "): " + job.getFilterReason());
    //         return;
    //     }

    //     tailor.tailorOne(job);
    // }
}
