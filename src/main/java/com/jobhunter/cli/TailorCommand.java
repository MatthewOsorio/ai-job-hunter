package com.jobhunter.cli;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.jobhunter.claude.ClaudeService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "tailor", description = "Tailor your resume to a job posting.")
public class TailorCommand implements Runnable {
    private static final ClaudeService claudeService = new ClaudeService();
    private String resumePath = Main.config.getString("jobhunter.resume.path");

    @Option(names = { "-j", "--job" }, description = "The job posting raw text", required = true)
    private String job;

    @Override
    public void run() {
        try {
            if (!Files.exists(Paths.get(job))) {
                System.err.println("Error: File not found: " + job);
                return;
            }

            System.out.println("Reading job posting from file...");
            String jobText = Files.readString(Paths.get(job));

            System.out.println("Tailoring resume...");
            String resume = Files.readString(Paths.get(resumePath));
            // claudeService.tailorResume(resume, jobText);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
