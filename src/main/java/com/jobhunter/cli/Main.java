package com.jobhunter.cli;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "jobhunter", mixinStandardHelpOptions = true, version = "1.0",
    description = "Tailors your resume with AI and emails you when there are new job postings.",
    subcommands = {StartCommand.class})
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
    System.out.println("AI Job Hunter started! Checking configurations...");

    boolean apiKeyLoaded = dotenv.get("ANTHROPIC_API_KEY") != null;
    boolean configLoaded = false;

    try {
      config.getConfig("jobhunter");
      configLoaded = true;
    } catch (com.typesafe.config.ConfigException.Missing e) {
    }

    if (!apiKeyLoaded || !configLoaded) {
      System.err.println("Configuration error: API key or config file missing.");
      System.exit(1);
    }

    String resumePath = dotenv.get("RESUME_PATH");
    if (resumePath == null || resumePath.isEmpty() || !Files.exists(Paths.get(resumePath))) {
      System.err.println(
          "Resume not found. Set RESUME_PATH in your .env file to the path of your .pdf or .tex resume.");
      System.exit(1);
    }
  }

  @Override
  public void run() {
    ensureResumePath();
    List<MenuItem> menuItems = List.of(new MenuItem("Run Job Hunter",
        "Scrap jobs from sources, filter them, tailors resume, and notify you of matches",
        () -> new StartCommand().run()));
    new InteractiveMenu(menuItems).show();
  }

  private void ensureResumePath() {
    String resumePath = dotenv.get("RESUME_PATH");
    if (resumePath != null && !resumePath.isEmpty() && Files.exists(Paths.get(resumePath))) {
      return;
    }
    System.err.println(
        "Resume not configured. Please set RESUME_PATH in your .env file to a .pdf or .tex resume.");
    System.exit(1);
  }
}
