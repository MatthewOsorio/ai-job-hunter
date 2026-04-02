package com.jobhunter.cli;

public final class Console {
  private static final String INDENT = "  ";
  private static int lastHeaderWidth = 0;
  private static boolean verbose = false;

  // ── Tactical Hunter theme ─────────────────────────────────────────
  private static final String CYAN = "\033[96m";
  private static final String GREEN = "\033[92m";
  private static final String YELLOW = "\033[93m";
  private static final String RED = "\033[91m";
  private static final String BLUE = "\033[94m";
  private static final String GRAY = "\033[90m";
  private static final String BOLD = "\033[1m";
  private static final String RESET = "\033[0m";
  // ─────────────────────────────────────────────────────────────────

  private Console() {}

  private static void clearLine() {
    System.out.print("\r\033[2K");
    System.out.flush();
  }

  public static void setVerbose(boolean v) {
    verbose = v;
  }

  public static boolean isVerbose() {
    return verbose;
  }

  public static void header(String title) {
    clearLine();
    int bar = title.length() + 4;
    lastHeaderWidth = bar;
    String rule = "═".repeat(bar);
    System.out.println();
    System.out.println(INDENT + CYAN + BOLD + "╔" + rule + "╗" + RESET);
    System.out.println(INDENT + CYAN + BOLD + "║" + RESET + "  " + BOLD + title + RESET + "  "
        + CYAN + BOLD + "║" + RESET);
    System.out.println(INDENT + CYAN + BOLD + "╠" + rule + "╣" + RESET);
  }

  public static void footer() {
    clearLine();
    System.out.println(INDENT + CYAN + BOLD + "╚" + "═".repeat(lastHeaderWidth) + "╝" + RESET);
  }

  public static void status(String message) {
    clearLine();
    System.out.println(INDENT + CYAN + "▸" + RESET + " " + message);
  }

  public static void warn(String message) {
    clearLine();
    System.err.println(INDENT + YELLOW + "⚠ " + RESET + " " + message);
  }

  public static void progress(String tag, String detail) {
    clearLine();
    System.out.println(INDENT + BLUE + "[" + tag + "]" + RESET + " " + detail);
  }

  public static void error(String message) {
    clearLine();
    System.err.println(INDENT + RED + "✖ " + RESET + " " + message);
  }

  public static void error(String message, Throwable cause) {
    if (cause == null) {
      error(message);
      return;
    }
    clearLine();
    String causeMessage = cause.getMessage();
    String causeDescription;
    if (causeMessage == null || causeMessage.isBlank()) {
      causeDescription = cause.toString();
    } else {
      causeDescription = cause.getClass().getName() + ": " + causeMessage;
    }
    System.err.println(INDENT + RED + "✖ " + RESET + " " + message + ": " + causeDescription);
    if (verbose) {
      cause.printStackTrace(System.err);
    }
  }

  public static void debug(String message) {
    if (verbose) {
      clearLine();
      System.out.println(INDENT + GRAY + "[debug]" + RESET + " " + message);
    }
  }

  public static void item(String text) {
    clearLine();
    System.out.println(INDENT + CYAN + "◆ " + RESET + " " + text);
  }

  public static void blank() {
    clearLine();
    System.out.println();
  }

  public static void println(String text) {
    clearLine();
    for (String line : text.split("\n", -1)) {
      System.out.println(INDENT + line);
    }
  }
}
