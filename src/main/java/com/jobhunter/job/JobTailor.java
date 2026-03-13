package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JobTailor {
    private final ClaudeService claudeService = new ClaudeService();
    private final String resumePath = Main.config.getString("jobhunter.resume.path");

    /** Tailors the resume for each job and writes the output to disk. */
    public void tailor(List<Job> jobs) {
        for (Job job : jobs) {
            tailorOne(job);
        }
    }

    /** Tailors the resume for a single job and writes the output to disk. */
    public void tailorOne(Job job) {
        try {
            String resume = Files.readString(Paths.get(resumePath));
            String tailored = claudeService.tailorResume(resume, job.getDescription());
            Path output = writeOutput(job, tailored);
            System.out.println("Tailored resume written to: " + output);
        } catch (IOException e) {
            System.err.println("Error tailoring resume for " + job.getTitle() + ": " + e.getMessage());
        }
    }

    private Path writeOutput(Job job, String tailored) throws IOException {
        String outputDir = Main.config.getString("jobhunter.resume.output-dir");
        Files.createDirectories(Paths.get(outputDir));

        String safeTitle = job.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
        String safeCompany = job.getCompany().replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = safeCompany + "_" + safeTitle + ".tex";

        Path output = Paths.get(outputDir, filename);
        Files.writeString(output, tailored);
        return output;
    }
}
