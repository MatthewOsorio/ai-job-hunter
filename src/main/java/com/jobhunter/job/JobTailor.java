package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Main;
import com.jobhunter.cli.Spinner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JobTailor {
  private static final int MAX_TASK_ATTEMPTS = 3;

  private final ClaudeService claudeService;
  private final Spinner spinner;
  private final String resumePath = Main.dotenv.get("RESUME_PATH");
  private final String outputDir = Main.dotenv.get("TARGET_DIR");

  public JobTailor(ClaudeService claudeService, Spinner spinner) {
    this.claudeService = claudeService;
    this.spinner = spinner;
  }

  public List<Path> tailor(List<Job> jobs) {
    String resumeContent;
    try {
      resumeContent = Files.readString(Paths.get(resumePath));
    } catch (IOException e) {
      Console.error("Failed to read resume from " + resumePath, e);
      return List.of();
    }

    int splitIdx = resumeContent.indexOf("\\begin{document}");
    if (splitIdx == -1) {
      Console.error("Could not find \\begin{document} in resume — is RESUME_PATH a .tex file?");
      return List.of();
    }
    String preamble = resumeContent.substring(0, splitIdx + "\\begin{document}".length());
    String body = resumeContent.substring(splitIdx + "\\begin{document}".length());

    int total = jobs.size();
    AtomicInteger completed = new AtomicInteger(0);
    List<Path> outputs = new ArrayList<>();

    List<Job> pending = new ArrayList<>(jobs);
    for (int attempt = 1; attempt <= MAX_TASK_ATTEMPTS && !pending.isEmpty(); attempt++) {
      if (attempt > 1) {
        Console.warn("Retrying " + pending.size() + " failed tailor task(s) (attempt " + attempt
            + "/" + MAX_TASK_ATTEMPTS + ")");
      }
      pending = runPass(pending, preamble, body, total, completed, outputs);
    }

    if (!pending.isEmpty()) {
      Console.error("Failed to tailor resume for " + pending.size() + " job(s) after "
          + MAX_TASK_ATTEMPTS + " attempts");
    }

    return outputs;
  }

  /** Runs one parallel pass over {@code jobs}. Returns any jobs that failed or timed out. */
  private List<Job> runPass(List<Job> jobs, String preamble, String body, int total,
      AtomicInteger completed, List<Path> outputs) {
    ConcurrentLinkedQueue<Job> failed = new ConcurrentLinkedQueue<>();
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    try {
      List<CompletableFuture<Void>> futures = jobs.stream()
          .map(job -> CompletableFuture.supplyAsync(() -> tailorOne(job, preamble, body), executor)
              .orTimeout(15, TimeUnit.MINUTES).handle((path, ex) -> {
                if (ex != null) {
                  Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                  if (cause instanceof TimeoutException) {
                    Console.error("Tailor timed out for '" + job.getTitle() + "' at '"
                        + job.getCompany() + "' — will retry");
                  } else if (!(cause instanceof InterruptedException)) {
                    Console.error(
                        "Tailor failed for '" + job.getTitle() + "' at '" + job.getCompany() + "'",
                        cause);
                  }
                  failed.add(job);
                } else {
                  path.ifPresent(p -> {
                    synchronized (outputs) {
                      outputs.add(p);
                    }
                  });
                }
                int done = completed.incrementAndGet();
                if (spinner != null) {
                  spinner.updateMessage("Tailoring resumes [" + done + "/" + total + "] ");
                }
                return (Void) null;
              }))
          .collect(Collectors.toList());
      try {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
      } catch (CompletionException ignored) {
      }
    } finally {
      executor.shutdownNow();
    }
    return new ArrayList<>(failed);
  }

  private Optional<Path> tailorOne(Job job, String preamble, String body) {
    try {
      String tailoredBody = claudeService.tailorResumeTex(body, job.getDescription());
      String fullTex = preamble + "\n" + tailoredBody;
      return Optional.of(writeOutput(job, fullTex));
    } catch (IOException e) {
      Console.error("Error writing tailored resume for " + job.getTitle() + ": " + e.getMessage(),
          e);
      return Optional.empty();
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
