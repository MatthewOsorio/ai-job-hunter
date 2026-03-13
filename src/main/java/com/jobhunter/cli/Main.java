package com.jobhunter.cli;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.cdimascio.dotenv.Dotenv;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "jobhunter",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Tailors your resume with AI and emails you when there are new job postings.",
    subcommands = { StartCommand.class, TailorCommand.class }
)
public class Main implements Runnable {
    public static final Dotenv dotenv = Dotenv.load();
    public static final Config config = ConfigFactory.load("application.conf");

    public static void main(String[] args) {
        new CommandLine(new Main())
                .setExecutionStrategy(Main::executionStrategy)
                .execute(args);
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
    }

    @Override
    public void run() {
        System.out.println("Use --help to see available commands.");
    }
}
