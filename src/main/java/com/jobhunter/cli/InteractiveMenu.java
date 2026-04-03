package com.jobhunter.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import com.jobhunter.cli.options.MenuItem;

public class InteractiveMenu {
  private final List<MenuItem> items;

  public InteractiveMenu(List<MenuItem> items) {
    this.items = new ArrayList<>(items);
  }

  public void show() {
    try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
      LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
      NonBlockingReader nonBlockingReader = terminal.reader();
      Main.console.setReaders(reader, nonBlockingReader);
      Main.console.setTerminal(terminal);
      loop(terminal, reader);
    } catch (IOException e) {
      Main.console.error("Terminal error: " + e.getMessage());
    }
  }

  private void loop(Terminal terminal, LineReader reader) {
    while (true) {
      int choice = Main.console.menu(items);

      if (choice == items.size() - 1) {
        break;
      }

      items.get(choice).run(reader);
    }
  }
}
