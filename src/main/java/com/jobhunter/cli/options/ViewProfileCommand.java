package com.jobhunter.cli.options;

import com.jobhunter.cli.Main;

import java.util.List;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.exception.JobHunterException;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

public class ViewProfileCommand extends MenuItem {
  private final ProfileBuilder profileBuilder;

  public ViewProfileCommand(String label, String description, ClaudeService claude) {
    super(label, description, claude);
    this.profileBuilder = new ProfileBuilder(claude);
  }

  @Override
  public void run() {
    String header = "Please view your profile here: " + profileBuilder.getCachePath().toString();
    while (true) {
      List<MenuItem> options = List.of(new EmptyCommand("Yes, rebuild my profile!"),
          new EmptyCommand("No, my profile is good"));

      int choice = Main.console.menu(options, header);
      if (choice == 0) {
        Main.console.spinnerStart("Rebuilding profile ");
        Profile rebuilt;
        try {
          rebuilt = profileBuilder.rebuildProfile();
        } catch (JobHunterException e) {
          Main.console.error(e.getMessage());
          return;
        } finally {
          Main.console.spinnerStop();
        }

        Main.console.generalCat("Here's your rebuilt profile!");
        Main.console.println(rebuilt.toString());
        break;
      } else if (choice == 1) {
        break;
      }
    }
  }
}
