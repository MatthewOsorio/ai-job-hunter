package com.jobhunter.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.jobhunter.cli.options.MenuItem;

public class InteractiveMenu {

  // @formatter:off
  private static final String BANNER =
      "   _       _       _                 _            \n" +
      "  (_) ___ | |__   | |__  _   _ _ __ | |_ ___ _ __\n" +
      "  | |/ _ \\| '_ \\  | '_ \\| | | | '_ \\| __/ _ \\ '__|\n" +
      "  | | (_) | |_) | | | | | |_| | | | | ||  __/ |  \n" +
      " _/ |\\___/|_.__/  |_| |_|\\__,_|_| |_|\\__\\___|_|  \n" +
      "|__/                                               \n";
  // @formatter:on

  private final List<MenuItem> items;

  public InteractiveMenu(List<MenuItem> items) {
    this.items = new ArrayList<>(items);
  }

  public void show() {
    try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
      LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
      loop(terminal, reader);
    } catch (IOException e) {
      Console.error("Terminal error: " + e.getMessage());
    }
  }

  private void loop(Terminal terminal, LineReader reader) {
    while (true) {
      printMenu(terminal);

      String input;
      try {
        input = reader.readLine("\033[96m  ▶\033[0m  Select: ").trim();
      } catch (UserInterruptException e) {
        break;
      }

      int choice;
      try {
        choice = Integer.parseInt(input);
      } catch (NumberFormatException e) {
        terminal.writer().println("\n  \033[93m⚠ \033[0m  Invalid input. Please enter a number.\n");
        terminal.writer().flush();
        continue;
      }

      int exitChoice = items.size() + 1;
      if (choice == exitChoice) {
        terminal.writer().println("\n  \033[96m▸\033[0m  Goodbye!\n");
        terminal.writer().flush();
        break;
      }

      if (choice < 1 || choice > items.size()) {
        terminal.writer().println(
            "\n  \033[93m⚠ \033[0m  Invalid option. Choose between 1 and " + exitChoice + ".\n");
        terminal.writer().flush();
        continue;
      }

      terminal.writer().println();
      terminal.writer().flush();
      try {
        items.get(choice - 1).run(reader);
      } catch (Exception e) {
        Console.error("Command failed unexpectedly: " + e.getMessage(), e);
      }
      terminal.writer().println();
      terminal.writer().flush();
    }
  }

  private void printMenu(Terminal terminal) {
    terminal.writer().println("\033[96m\033[1m" + BANNER + "\033[0m");
    terminal.writer().println("  \033[90mAI-Powered Job Application Assistant\033[0m");
    terminal.writer().println();
    terminal.writer().println(
        "  \033[93m⚠  AI outputs may be imperfect. Always review results before applying.\033[0m");
    terminal.writer().println();
    for (int i = 0; i < items.size(); i++) {
      MenuItem item = items.get(i);
      terminal.writer().printf(
          "  \033[96m\033[1m[%d]\033[0m \033[1m%-20s\033[0m \033[90m%s\033[0m%n", i + 1,
          item.label(), item.description());
    }
    terminal.writer().printf("  \033[96m\033[1m[%d]\033[0m \033[1m%-20s\033[0m%n%n",
        items.size() + 1, "Exit");
    terminal.writer().flush();
  }
}
