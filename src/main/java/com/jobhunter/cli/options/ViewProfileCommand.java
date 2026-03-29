package com.jobhunter.cli.options;

import org.jline.reader.LineReader;

import com.jobhunter.cli.Console;
import com.jobhunter.cli.Spinner;
import com.jobhunter.profile.Profile;
import com.jobhunter.profile.ProfileBuilder;

public class ViewProfileCommand extends MenuItem {
  private final ProfileBuilder profileBuilder;

  public ViewProfileCommand(String label, String description, Spinner spinner) {
    super(label, description, spinner);
    this.profileBuilder = new ProfileBuilder();
  }

  @Override
  public void run(LineReader reader) {
    if (!profileBuilder.isCached()) {
      spinner.start("No profile found. Building one now ");
    } else {
      spinner.start("Loading profile from cache ");
    }

    Profile profile = profileBuilder.getProfile();
    spinner.stop();

    Console.header("Your Profile");
    Console.println(profile.toString());
    Console.footer();

    String answer =
        reader.readLine("  Would you like to rebuild your profile? (y/n): ").trim().toLowerCase();
    if (answer.equals("y") || answer.equals("yes")) {
      spinner.start("Rebuilding profile ");
      Profile rebuilt = profileBuilder.rebuildProfile();
      spinner.stop();

      Console.header("New Profile");
      Console.println(rebuilt.toString());
      Console.footer();
    }
  }
}
