package com.jobhunter.cli.options;

import com.jobhunter.cli.Main;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.job.HuntPipeline;

public class HuntCommand extends MenuItem {
  private final HuntPipeline huntPipeline;

  public HuntCommand(String label, String description, ClaudeService claude) {
    super(label, description, claude);
    this.huntPipeline = new HuntPipeline(claude, Main.console);
  }

  @Override
  public void run(LineReader reader) {
    try {
      huntPipeline.runAll();
    } catch (JobHunterException e) {
      Main.console.error(e.getMessage(), e);
    }
  }
}
