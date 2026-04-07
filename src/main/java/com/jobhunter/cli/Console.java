package com.jobhunter.cli;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

import com.jobhunter.cli.options.MenuItem;

public final class Console {
  private Terminal terminal;
  private Attributes originalTerminalAttributes;
  private PrintWriter out;
  private LineReader reader;
  private NonBlockingReader nonBlockingReader;
  private Thread spinnerThread;
  private volatile String spinnerMessage = "";
  private static final String[] SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
  private final String YELLOW = "\033[93m";
  private final String RESET = "\033[0m";
  private final String GREEN = "\033[92m";
  private final String BLUE = "\033[94m";

  public void setReaders(LineReader reader, NonBlockingReader nonBlockingReader) {
    this.reader = reader;
    this.nonBlockingReader = nonBlockingReader;
  }

  public String readLine(String prompt) {
    return reader.readLine(prompt);
  }

  public void setTerminal(Terminal terminal) {
    this.terminal = terminal;
    this.out = terminal.writer();
  }

  public int menu(List<MenuItem> items) {
    return menu(items, false, null);
  }

  public int menu(List<MenuItem> items, boolean showWelcome) {
    return menu(items, showWelcome, null);
  }

  public int menu(List<MenuItem> items, String header) {
    return menu(items, false, header);
  }

  private int menu(List<MenuItem> items, boolean showWelcome, String header) {
    List<MenuItem> menuOptions = items;

    KeyMap<String> keyMap = new KeyMap<>();
    keyMap.bind("up", KeyMap.key(terminal, InfoCmp.Capability.key_up));
    keyMap.bind("down", KeyMap.key(terminal, InfoCmp.Capability.key_down));
    keyMap.bind("enter", "\r", "\n");
    BindingReader bindingReader = new BindingReader(terminal.reader());

    try {
      enableTerminalRawMode();
      out.print("\033[?25l");
      out.flush();
      int selectedIndex = 0;

      while (true) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        if (showWelcome) {
          printStartMessage();
        }
        if (header != null) {
          out.println(header);
        }

        out.println();

        for (int i = 0; i < menuOptions.size(); i++) {
          MenuItem item = menuOptions.get(i);
          if (i == selectedIndex) {
            out.print("\r" + GREEN + "> " + item.label() + "   " + item.description() + RESET);
          } else {
            out.print("\r" + "  " + item.label() + "   " + item.description());
          }
          out.println();
        }

        out.flush();

        String binding = bindingReader.readBinding(keyMap);
        if (binding == null) {
          continue;
        } else if ("enter".equals(binding)) {
          disableTerminalRawMode();
          out.println();
          return selectedIndex;
        } else if ("up".equals(binding)) {
          selectedIndex = (selectedIndex - 1 + menuOptions.size()) % menuOptions.size();
        } else if ("down".equals(binding)) {
          selectedIndex = (selectedIndex + 1) % menuOptions.size();
        }
      }
    } finally {
      out.print("\033[?25h");
      out.flush();
      disableTerminalRawMode();
    }
  }

  public void generalCat(String message) {
    out.println(coolCat(message));
  }

  public void println(String message) {
    out.println(message + "\n");
  }

  public void warn(String message) {
    out.println(YELLOW + "[WARNING] " + RESET + message);
  }

  public void info(String message) {
    out.println(BLUE + "[INFO] " + RESET + message);
  }

  public void error(String message) {
    out.println(madCat(message));
  }


  public void blank() {
    out.println();
  }

  public void spinnerStart(String message) {
    if (spinnerThread != null) {
      spinnerStop();
    }

    spinnerMessage = message;

    spinnerThread = new Thread(() -> {
      int i = 0;
      while (!Thread.currentThread().isInterrupted()) {
        out.print("\r" + spinnerMessage + SPINNER_FRAMES[i++ % SPINNER_FRAMES.length]);
        out.flush();
        try {
          Thread.sleep(80);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
    spinnerThread.setDaemon(true);
    spinnerThread.start();
  }

  public void spinnerUpdateMessage(String message) {
    spinnerMessage = message;
  }

  public void spinnerStop() {
    if (spinnerThread != null) {
      spinnerThread.interrupt();
      try {
        spinnerThread.join(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      spinnerThread = null;
    }
    out.print("\r" + " ".repeat(60) + "\r");
    out.flush();
  }

  public void spinnerSuccess(String message) {
    if (spinnerThread != null) {
      spinnerThread.interrupt();
      try {
        spinnerThread.join(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      spinnerThread = null;
    }
    out.println("\r\033[92m✓\033[0m " + message + " ".repeat(10));
    out.flush();
  }

  public void printStartMessage() {
    String message =
        "Hi I'm Hank, your AI-powered assistant for job hunting. Let's find your next opportunity!\n"
            + "Remember to always check results, AI isn't perfect (yet)!";

    out.println(coolCat(message));
  }

  private String coolCat(String text) {
    return createBubble(text) + """

                 /
                /
         /\\_/\\
        ( ⌐■_■ )
         /     \\
        (       )
        """;
  }

  private String madCat(String text) {
    return createBubble(text) + """

                 /
                /
         /\\_/\\
        ( >_< )
         /     \\
        (       )
        """;
  }


  private String createBubble(String text) {
    String[] lines = text.split("\n");
    int maxLen = Arrays.stream(lines).mapToInt(String::length).max().orElse(0);

    String border = "_".repeat(maxLen + 2);
    StringBuilder bubble = new StringBuilder();
    bubble.append(" ").append(border).append("\n");
    bubble.append("| ").append(" ".repeat(maxLen)).append(" |\n");

    for (String line : lines) {
      bubble.append("| ").append(String.format("%-" + maxLen + "s", line)).append(" |\n");
    }
    bubble.append("|").append(border).append("|");

    return bubble.toString();
  }

  private void enableTerminalRawMode() {
    originalTerminalAttributes = terminal.enterRawMode();
  }

  private void disableTerminalRawMode() {
    terminal.setAttributes(originalTerminalAttributes);
  }
}
