package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.job.JobRunner;
import com.jobhunter.profile.resume.ResumeNotFoundException;

public class HuntCommand extends MenuItem {
  public HuntCommand(String label, String description, Spinner spinner, ClaudeService claude) {
    super(label, description, spinner, claude);
  }

  @Override
  public void run(LineReader reader) {
    try {
      JobRunner jobRunner = new JobRunner(claude, spinner);
      jobRunner.runAll();
    } catch (ResumeNotFoundException e) {
      Console.error(e.getMessage());
    }
  }
}
