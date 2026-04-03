package com.jobhunter.cli.options;

import com.jobhunter.cli.Main;

import org.jline.reader.LineReader;

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
  public void run(LineReader reader) {
    if (!profileBuilder.isCached()) {
      // spinner.start("No profile found. Building one now ");
    } else {
      // spinner.start("Loading profile from cache ");
    }

    Profile profile;
    try {
      profile = profileBuilder.getProfile();
    } catch (JobHunterException e) {
      Main.console.error("Failed to load profile: " + e.getMessage(), e);
      return;
    } finally {
      // spinner.stop();
    }

    // Main.console.header("Your Profile");
    Main.console.println(profile.toString());
    // Main.console.footer();

    while (true) {
      String answer =
          reader.readLine("  Would you like to rebuild your profile? (y/n): ").trim().toLowerCase();
      if (answer.equals("y")) {
        // spinner.start("Rebuilding profile ");
        Profile rebuilt;
        try {
          rebuilt = profileBuilder.rebuildProfile();
        } catch (JobHunterException e) {
          Main.console.error("Failed to rebuild profile: " + e.getMessage(), e);
          return;
        } finally {
          // spinner.stop();
        }

        // Main.console.header("New Profile");
        Main.console.println(rebuilt.toString());
        // Main.console.footer();
        break;
      } else if (answer.equals("n")) {
        break;
      }
      Main.console.println("Invalid input. Please enter y or n");
    }
  }
}
