package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Spinner;

public abstract class MenuItem {
  protected final Spinner spinner;
  protected final ClaudeService claude;
  private final String label;
  private final String description;

  public MenuItem(String label, String description, Spinner spinner, ClaudeService claude) {
    this.label = label;
    this.description = description;
    this.spinner = spinner;
    this.claude = claude;
  }

  public String label() {
    return label;
  }

  public String description() {
    return description;
  }

  public abstract void run(LineReader reader);
}
