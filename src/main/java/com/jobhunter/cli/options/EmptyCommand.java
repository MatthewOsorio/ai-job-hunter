package com.jobhunter.cli.options;

public class EmptyCommand extends MenuItem {
  public EmptyCommand(String label) {
    super(label);
  }

  @Override
  public void run() {
    // Do nothing
  }

}
