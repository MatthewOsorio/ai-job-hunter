package com.jobhunter.cli.options;

import java.util.List;

import com.jobhunter.cli.Spinner;

public class MenuItemFactory {
  private List<String> itemNames = List.of("hunt", "view-profile");
  private final Spinner spinner = new Spinner();

  public MenuItem createAndReturnMenuItem(String itemName) {
    if (!itemNames.contains(itemName)) {
      throw new IllegalArgumentException("Unknown menu item: " + itemName);
    }

    switch (itemName) {
      case "hunt":
        return new HuntCommand("Run Job Hunter",
            "Scrape jobs from sources, filter them, tailor resume, and notify you of matches via email",
            spinner);
      case "view-profile":
        return new ViewProfileCommand("View Profile", "View your profile and optionally rebuild it",
            spinner);

      default:
        throw new IllegalArgumentException("Unknown menu item: " + itemName);
    }
  }
}
