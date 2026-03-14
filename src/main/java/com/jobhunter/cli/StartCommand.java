package com.jobhunter.cli;

import com.jobhunter.job.JobRunner;
import picocli.CommandLine.Command;

@Command(name = "start", description = "Start the job hunter, running every hour until stopped.")
public class StartCommand implements Runnable {
  private final JobRunner jobRunner = new JobRunner();

  @Override
  public void run() {
    jobRunner.runAll();
  }
}
