package com.jobhunter.cli;

import com.jobhunter.cli.options.MenuItem;
import com.jobhunter.cli.options.MenuItemFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jobhunter", mixinStandardHelpOptions = true, version = "1.0",
    description = "AI Job Hunter is a command-line tool that helps you find and apply to jobs that match your profile. It scrapes job listings from various sources, filters them based on your preferences, tailors your resume for each job, and notifies you of matches via email.")
public class Main implements Runnable {
  public static final Dotenv dotenv = Dotenv.load();
  public static final Config config = ConfigFactory.load("application.conf");

  public static void main(String[] args) {
    new CommandLine(new Main()).setExecutionStrategy(Main::executionStrategy).execute(args);
  }

  private static int executionStrategy(CommandLine.ParseResult parseResult) {
    validate();
    return new CommandLine.RunLast().execute(parseResult);
  }

  private static void validate() {
    Console.status("AI Job Hunter started! Checking configurations...");

    boolean apiKeyLoaded = dotenv.get("ANTHROPIC_API_KEY") != null;
    boolean configLoaded = false;

    try {
      config.getConfig("jobhunter");
      configLoaded = true;
    } catch (com.typesafe.config.ConfigException.Missing e) {
    }

    if (!apiKeyLoaded || !configLoaded) {
      Console.error("API key or config file missing");
      System.exit(1);
    }

    String resumePath = dotenv.get("RESUME_PATH");
    if (resumePath == null || resumePath.isEmpty() || !Files.exists(Paths.get(resumePath))) {
      Console.error(
          "Resume not found. Set RESUME_PATH in your .env file to the path of your .pdf or .tex resume");
      System.exit(1);
    }
  }

  @Override
  public void run() {
    MenuItemFactory factory = new MenuItemFactory();
    List<MenuItem> menuItems =
        List.of(factory.createMenuItem("hunt"), factory.createMenuItem("view-profile"));

    new InteractiveMenu(menuItems).show();
  }
}
