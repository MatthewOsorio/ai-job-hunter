package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;

public abstract class MenuItem {
  protected final ClaudeService claude;
  private final String label;
  private final String description;

  public MenuItem(String label, String description, ClaudeService claude) {
    this.label = label;
    this.description = description;
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
