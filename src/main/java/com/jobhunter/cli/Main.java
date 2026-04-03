package com.jobhunter.cli;

import com.jobhunter.cli.options.MenuItem;
import com.jobhunter.cli.options.MenuItemFactory;
import com.jobhunter.exception.ConfigurationException;
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
  public static final Console console = new Console();

  private static Main main;

  public static void main(String[] args) {
    new CommandLine(new Main()).setExecutionStrategy(Main::executionStrategy).execute(args);
  }

  private static int executionStrategy(CommandLine.ParseResult parseResult) {
    main = parseResult.commandSpec().userObject() instanceof Main m ? m : new Main();
    try {
      main.validate();
    } catch (ConfigurationException e) {
      console.error(e.getMessage());
      return 1;
    }
    return new CommandLine.RunLast().execute(parseResult);
  }

  private void validate() {
    boolean apiKeyLoaded = dotenv.get("ANTHROPIC_API_KEY") != null;
    boolean configLoaded = config.hasPath("jobhunter");

    if (!apiKeyLoaded || !configLoaded) {
      throw new ConfigurationException("API key or config file missing!");
    }

    String resumePath = dotenv.get("RESUME_PATH");
    if (resumePath == null || resumePath.isEmpty() || !Files.exists(Paths.get(resumePath))) {
      throw new ConfigurationException(
          "Set RESUME_PATH in your .env file to a .tex or .docx file!");
    }

    String targetDir = dotenv.get("TARGET_DIR");
    if (targetDir == null || targetDir.isEmpty()) {
      throw new ConfigurationException(
          "Set TARGET_DIR in your .env file to the output directory for tailored resumes!");
    }
  }

  @Override
  public void run() {
    MenuItemFactory factory = new MenuItemFactory();
    List<MenuItem> menuItems = List.of(factory.createMenuItem("hunt"),
        factory.createMenuItem("hunt-one"), factory.createMenuItem("view-profile"));

    new InteractiveMenu(menuItems).show();

    System.exit(0);
  }
}
