package com.jobhunter.cli;

import com.jobhunter.job.JobRunner;
import com.jobhunter.profile.resume.ResumeNotFoundException;
import picocli.CommandLine.Command;

@Command(name = "start", description = "Start the job hunter, running every hour until stopped.")
public class StartCommand implements Runnable {

  @Override
  public void run() {
    try {
      JobRunner jobRunner = new JobRunner();
      jobRunner.runAll();
    } catch (ResumeNotFoundException e) {
      System.err.println(e.getMessage());
    }
  }
}
