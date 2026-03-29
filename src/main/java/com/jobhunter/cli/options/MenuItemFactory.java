package com.jobhunter.cli.options;

import com.jobhunter.cli.Spinner;

public class MenuItemFactory {
  private final Spinner spinner = new Spinner();

  public MenuItem createMenuItem(String itemName) {
    return switch (itemName) {
      case "hunt" -> new HuntCommand("Run Job Hunter",
          "Scrape jobs from sources, filter them, tailor resume, and notify you of matches via email",
          spinner);
      case "view-profile" -> new ViewProfileCommand("View Profile",
          "View your profile and optionally rebuild it", spinner);
      default -> throw new IllegalArgumentException("Unknown menu item: " + itemName);
    };
  }
}
