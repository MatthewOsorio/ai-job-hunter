package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.job.HuntPipeline;

public class HuntCommand extends MenuItem {
  private final HuntPipeline huntPipeline;

  public HuntCommand(String label, String description, Spinner spinner, ClaudeService claude) {
    super(label, description, spinner, claude);
    this.huntPipeline = new HuntPipeline(claude, spinner);
  }

  @Override
  public void run(LineReader reader) {
    try {
      huntPipeline.runAll();
    } catch (JobHunterException e) {
      Console.error(e.getMessage(), e);
    }
  }
}
