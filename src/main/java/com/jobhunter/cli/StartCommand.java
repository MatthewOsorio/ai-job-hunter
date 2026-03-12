package com.jobhunter.cli;

import com.jobhunter.scraper.JobScraper;

import picocli.CommandLine.Command;

@Command(name = "start", description = "Start the job hunter application.")
public class StartCommand implements Runnable {
    private static final JobScraper jobScraper = new JobScraper();

    @Override
    public void run() {
        System.out.println("Starting the job hunter application...");
        jobScraper.scrap();
    }
}
