package com.jobhunter.cli;

import com.jobhunter.scraper.JobScraper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.cdimascio.dotenv.Dotenv;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jobhunter", mixinStandardHelpOptions = true, version = "1.0", description = "Scrapes job listings, tailors your resume with AI, and emails you the results.")
public class Main implements Runnable {

    public static final Dotenv dotenv = Dotenv.load();
    public static final Config config = ConfigFactory.load("application.conf");

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("AI Job Hunter started!");
        System.out.println("API Key loaded: " + (dotenv.get("ANTHROPIC_API_KEY") != null ? "✓" : "✗"));

        try {
            config.getConfig("jobhunter");
            System.out.println("Config loaded: ✓");
        } catch (com.typesafe.config.ConfigException.Missing e) {
            System.out.println("Config loaded: ✗ - application.conf not found");
            System.exit(1);
        }

        new JobScraper().scrap();
    }
}