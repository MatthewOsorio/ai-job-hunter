package com.jobhunter.cli.options;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.job.HuntPipeline;
import com.jobhunter.job.Job;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

public class HuntOneCommand extends MenuItem {
  public HuntOneCommand(String label, String description, Spinner spinner, ClaudeService claude) {
    super(label, description, spinner, claude);
  }

  @Override
  public void run(LineReader reader) {
    String jobUrl;

    while (true) {
      jobUrl = reader.readLine("  Enter the job URL to hunt: ").trim();
      if (jobUrl.isEmpty()) {
        Console.println("  Invalid input. Please enter a job URL.");
        continue;
      }
      try {
        URI uri = new URI(jobUrl);
        if (uri.getScheme() == null || uri.getHost() == null) {
          Console.println(
              "  Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
          continue;
        }
      } catch (URISyntaxException e) {
        Console
            .println("  Invalid URL. Please enter a full URL (e.g., https://example.com/job/123).");
        continue;
      }
      break;
    }

    ProfileBuilder profileBuilder = new ProfileBuilder(claude);
    Profile profile;
    if (!profileBuilder.isCached()) {
      Console.status("No profile found. Building profile from resume and GitHub...");
      spinner.start("Building profile ");
      try {
        profile = profileBuilder.getProfile();
      } catch (JobHunterException e) {
        Console.error("Failed to build profile: " + e.getMessage(), e);
        return;
      } finally {
        spinner.stop();
      }
      Console.status("Profile built and cached.");
    } else {
      profile = profileBuilder.getProfile();
    }

    HuntPipeline pipeline = new HuntPipeline(claude, profile, spinner);

    spinner.start("Fetching job posting ");
    Optional<Job> maybeJob;
    try {
      maybeJob = pipeline.scrapeOne(jobUrl);
    } finally {
      spinner.stop();
    }

    if (maybeJob.isEmpty()) {
      Console.println("  Could not fetch or extract job description.");
      return;
    }
    Job job = maybeJob.get();
    Console.status("Position: " + job.getTitle() + " at " + job.getCompany());

    spinner.start("Filtering job ");
    try {
      pipeline.filterOne(job);
    } catch (JobHunterException e) {
      Console.error(e.getMessage(), e);
      return;
    } finally {
      spinner.stop();
    }

    Console.blank();
    Console.header("Result");
    Console.status("Match score: " + job.getMatchScore() + "/100");
    if (job.isShouldApply()) {
      Console.status("Recommendation: Apply");
    } else {
      Console.status("Recommendation: Skip");
      Console.footer();
      return;
    }
    Console.footer();

    String answer = reader.readLine("  Tailor your resume for this job? (y/n): ").trim();
    if (!answer.equalsIgnoreCase("y")) {
      return;
    }

    spinner.start("Tailoring resume ");
    List<Path> outputs;
    try {
      outputs = pipeline.tailor(List.of(job));
    } catch (JobHunterException e) {
      Console.error(e.getMessage(), e);
      return;
    } finally {
      spinner.stop();
    }

    if (outputs.isEmpty()) {
      Console.println("  Tailoring failed — check errors above.");
    } else {
      Console.status("Resume saved to: " + outputs.get(0).toAbsolutePath());
    }
  }
}
