package com.jobhunter.job;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JobScraperResult {
    private final Queue<Job> validJobs = new ConcurrentLinkedQueue<>();
    private final Queue<Job> failedJobs = new ConcurrentLinkedQueue<>();

    public List<Job> getValidJobs() {
        return new ArrayList<>(validJobs);
    }

    public List<Job> getFailedJobs() {
        return new ArrayList<>(failedJobs);
    }

    public void addValidJob(Job job) {
        validJobs.add(job);
    }

    public void addFailedJob(Job job) {
        failedJobs.add(job);
    }
}
