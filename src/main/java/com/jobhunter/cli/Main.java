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
import picocli.CommandLine.Option;

@Command(name = "jobhunter", mixinStandardHelpOptions = true, version = "1.0",
    description = "AI Job Hunter is a command-line tool that helps you find and apply to jobs that match your profile. It scrapes job listings from various sources, filters them based on your preferences, tailors your resume for each job, and notifies you of matches via email.")
public class Main implements Runnable {
  public static final Dotenv dotenv = Dotenv.load();
  public static final Config config = ConfigFactory.load("application.conf");

  @Option(names = {"-v", "--verbose"}, description = "Print full stack traces and debug output")
  private boolean verbose;

  public static void main(String[] args) {
    new CommandLine(new Main()).setExecutionStrategy(Main::executionStrategy).execute(args);
  }

  private static int executionStrategy(CommandLine.ParseResult parseResult) {
    Main main = parseResult.commandSpec().userObject() instanceof Main m ? m : new Main();
    if (main.verbose) {
      Console.setVerbose(true);
    }
    try {
      validate();
    } catch (ConfigurationException e) {
      Console.error(e.getMessage());
      return 1;
    }
    return new CommandLine.RunLast().execute(parseResult);
  }

  private static void validate() {
    Console.status("AI Job Hunter started! Checking configurations...");

    boolean apiKeyLoaded = dotenv.get("ANTHROPIC_API_KEY") != null;
    boolean configLoaded = config.hasPath("jobhunter");

    if (!apiKeyLoaded || !configLoaded) {
      throw new ConfigurationException("API key or config file missing");
    }

    String resumePath = dotenv.get("RESUME_PATH");
    if (resumePath == null || resumePath.isEmpty() || !Files.exists(Paths.get(resumePath))) {
      throw new ConfigurationException(
          "Resume not found. Set RESUME_PATH in your .env file to the path of your .tex or .docx file");
    }

    String targetDir = dotenv.get("TARGET_DIR");
    if (targetDir == null || targetDir.isEmpty()) {
      throw new ConfigurationException(
          "TARGET_DIR not set. Set TARGET_DIR in your .env file to the output directory for tailored resumes");
    }
  }

  @Override
  public void run() {
    MenuItemFactory factory = new MenuItemFactory();
    List<MenuItem> menuItems = List.of(factory.createMenuItem("hunt"),
        factory.createMenuItem("hunt-one"), factory.createMenuItem("view-profile"));

    new InteractiveMenu(menuItems).show();
  }
}
