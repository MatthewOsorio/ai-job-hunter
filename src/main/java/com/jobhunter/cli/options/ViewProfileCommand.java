package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

public class ViewProfileCommand extends MenuItem {
  private final ProfileBuilder profileBuilder;

  public ViewProfileCommand(String label, String description, Spinner spinner,
      ClaudeService claude) {
    super(label, description, spinner, claude);
    this.profileBuilder = new ProfileBuilder(claude);
  }

  @Override
  public void run(LineReader reader) {
    if (!profileBuilder.isCached()) {
      spinner.start("No profile found. Building one now ");
    } else {
      spinner.start("Loading profile from cache ");
    }

    Profile profile;
    try {
      profile = profileBuilder.getProfile();
    } finally {
      spinner.stop();
    }

    Console.header("Your Profile");
    Console.println(profile.toString());
    Console.footer();

    while (true) {
      String answer =
          reader.readLine("  Would you like to rebuild your profile? (y/n): ").trim().toLowerCase();
      if (answer.equals("y")) {
        spinner.start("Rebuilding profile ");
        Profile rebuilt;
        try {
          rebuilt = profileBuilder.rebuildProfile();
        } finally {
          spinner.stop();
        }

        Console.header("New Profile");
        Console.println(rebuilt.toString());
        Console.footer();
        break;
      } else if (answer.equals("n")) {
        break;
      }
      Console.println("Invalid input. Please enter y or n");
    }
  }
}
