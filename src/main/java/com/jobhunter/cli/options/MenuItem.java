package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.cli.Spinner;

public abstract class MenuItem {
  protected final Spinner spinner;
  private final String label;
  private final String description;

  public MenuItem(String label, String description, Spinner spinner) {
    this.label = label;
    this.description = description;
    this.spinner = spinner;
  }

  public String label() {
    return label;
  }

  public String description() {
    return description;
  }

  public abstract void run(LineReader reader);
}
