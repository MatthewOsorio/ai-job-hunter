package com.jobhunter.cli;

public final class Console {
  private static final String INDENT = "  ";
  private static int lastHeaderWidth = 0;
  private static boolean verbose = false;

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
    lastHeaderWidth = title.length() + 12;
    System.out.println();
    System.out.println(INDENT + "===== " + title + " =====");
  }

  public static void footer() {
    clearLine();
    System.out.println(INDENT + "=".repeat(lastHeaderWidth));
  }

  public static void status(String message) {
    clearLine();
    System.out.println(INDENT + "> " + message);
  }

  public static void warn(String message) {
    clearLine();
    System.err.println(INDENT + "[WARN] " + message);
  }

  public static void progress(String tag, String detail) {
    clearLine();
    System.out.println(INDENT + "[" + tag + "] " + detail);
  }

  public static void error(String message) {
    clearLine();
    System.err.println(INDENT + "[ERROR] " + message);
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
    System.err.println(INDENT + "[ERROR] " + message + ": " + causeDescription);
    if (verbose) {
      cause.printStackTrace(System.err);
    }
  }

  public static void debug(String message) {
    if (verbose) {
      clearLine();
      System.out.println(INDENT + "[DEBUG] " + message);
    }
  }

  public static void item(String text) {
    clearLine();
    System.out.println(INDENT + "- " + text);
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
