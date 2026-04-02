package com.jobhunter.cli.options;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.cli.Spinner;

public class MenuItemFactory {
  private final Spinner spinner;
  private final ClaudeService claude;

  public MenuItemFactory() {
    this.spinner = new Spinner();
    this.claude =
        new ClaudeService(Main.dotenv.get("ANTHROPIC_API_KEY"), Main.config.getConfig("jobhunter"));
  }

  public MenuItem createMenuItem(String itemName) {
    return switch (itemName) {
      case "hunt" -> new HuntCommand("Run Job Hunter",
          "Scrape jobs from sources, filter them, tailor resume, and notify you of matches via email",
          spinner, claude);
      case "view-profile" -> new ViewProfileCommand("View Profile",
          "View your profile and optionally rebuild it", spinner, claude);
      case "hunt-one" -> new HuntOneCommand("Hunt Single Job",
          "Scrape a single job URL, filter it, and optionally tailor your resume", spinner, claude);
      default -> throw new IllegalArgumentException("Unknown menu item: " + itemName);
    };
  }
}
