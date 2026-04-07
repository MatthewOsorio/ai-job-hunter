package com.jobhunter.cli.options;


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

  public MenuItem(ClaudeService claude) {
    this("", "", claude);
  }

  public MenuItem(String label, String description) {
    this(label, description, null);
  }

  public MenuItem(String label) {
    this(label, "", null);
  }

  public String label() {
    return label;
  }

  public String description() {
    return description;
  }

  public abstract void run();
}
