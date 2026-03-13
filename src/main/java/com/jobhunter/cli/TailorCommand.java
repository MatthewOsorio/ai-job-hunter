package com.jobhunter.cli;

import com.jobhunter.job.Job;
import com.jobhunter.job.JobRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tailor", description = "Fetch a job posting by URL and tailor your resume to it.")
public class TailorCommand implements Runnable {

    @Option(names = { "-u", "--url" }, description = "URL of the job posting", required = true)
    private String url;

    @Option(names = { "-t", "--title" }, description = "Job title (optional)", defaultValue = "Unknown")
    private String title;

    @Option(names = { "-c", "--company" }, description = "Company name (optional)", defaultValue = "Unknown")
    private String company;

    @Override
    public void run() {
        System.out.println("Tailoring resume for: " + url);
        Job job = new Job(title, company, url);
        try {
            new JobRunner().runOne(job);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
