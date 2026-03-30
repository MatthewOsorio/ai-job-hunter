package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JobTailor {
  private final ClaudeService claudeService;
  private final String resumePath = Main.dotenv.get("RESUME_PATH");
  private final String outputDir = Main.dotenv.get("TARGET_DIR");

  public JobTailor(ClaudeService claudeService) {
    this.claudeService = claudeService;
  }

  public void tailor(List<Job> jobs) {
    String resumeContent;
    try {
      resumeContent = Files.readString(Paths.get(resumePath));
    } catch (IOException e) {
      Console.error("Failed to read resume from " + resumePath, e);
      return;
    }

    int splitIdx = resumeContent.indexOf("\\begin{document}");
    if (splitIdx == -1) {
      Console.error("Could not find \\begin{document} in resume — is RESUME_PATH a .tex file?");
      return;
    }
    String preamble = resumeContent.substring(0, splitIdx + "\\begin{document}".length());
    String body = resumeContent.substring(splitIdx + "\\begin{document}".length());

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<JobTask> tasks = jobs.stream()
          .map(job -> new JobTask(job, executor.submit(() -> tailorOne(job, preamble, body))))
          .collect(Collectors.toList());

      for (JobTask task : tasks) {
        try {
          task.future().get();
        } catch (Exception e) {
          Console.error("Tailor failed for job '" + task.job().getTitle() + "' at company '"
              + task.job().getCompany() + "'", e);
        }
      }
    }
  }

  private record JobTask(Job job, Future<?> future) {}

  private void tailorOne(Job job, String preamble, String body) {
    try {
      String tailoredBody = claudeService.tailorResumeTex(body, job.getDescription(), null);
      String fullTex = preamble + "\n" + tailoredBody;
      writeOutput(job, fullTex);
    } catch (IOException e) {
      Console.error("Error writing tailored resume for " + job.getTitle() + ": " + e.getMessage(),
          e);
    }
  }

  private Path writeOutput(Job job, String content) throws IOException {
    Files.createDirectories(Paths.get(outputDir));
    String safeCompany = job.getCompany().replaceAll("[^a-zA-Z0-9_-]", "_");
    String safeTitle = job.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
    String filename = safeCompany + "_" + safeTitle + ".tex";
    Path output = Paths.get(outputDir, filename);
    Files.writeString(output, content);
    return output;
  }
}
