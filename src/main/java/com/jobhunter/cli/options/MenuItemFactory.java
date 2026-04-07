package com.jobhunter.cli.options;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ConfigurationException;

public class MenuItemFactory {
  private final ClaudeService claude;

  public MenuItemFactory() {
    this.claude =
        new ClaudeService(Main.dotenv.get("ANTHROPIC_API_KEY"), Main.config.getConfig("jobhunter"));
  }

  public MenuItem createMenuItem(String itemName) {
    return switch (itemName) {
      case "hunt" -> new HuntCommand("Run Job Hunter",
          "Scrape jobs from sources, filter them, tailor resume, and notify you of matches via email",
          claude);
      case "hunt-one" -> new HuntOneCommand("Hunt One Job",
          "Scrape a single job from sources, filter it, and tailor resume", claude);
      case "manage-profile" ->
        new ViewProfileCommand("Manage Profile", "Manage your profile here", claude);
      case "exit" -> new ExitCommand();
      default -> throw new ConfigurationException("Unknown menu item: " + itemName);
    };
  }
}
