package com.jobhunter.cli;

public final class Console {
  private static final String INDENT = "  ";
  private static int lastHeaderWidth = 0;
  private static boolean verbose = false;

  private Console() {}

  public static void setVerbose(boolean v) {
    verbose = v;
  }

  public static boolean isVerbose() {
    return verbose;
  }

  public static void header(String title) {
    lastHeaderWidth = title.length() + 12;
    System.out.println();
    System.out.println(INDENT + "===== " + title + " =====");
  }

  public static void footer() {
    System.out.println(INDENT + "=".repeat(lastHeaderWidth));
  }

  public static void status(String message) {
    System.out.println(INDENT + "> " + message);
  }

  public static void warn(String message) {
    System.err.println(INDENT + "[WARN] " + message);
  }

  public static void progress(String tag, String detail) {
    System.out.println(INDENT + "[" + tag + "] " + detail);
  }

  public static void error(String message) {
    System.err.println(INDENT + "[ERROR] " + message);
  }

  public static void error(String message, Throwable cause) {
    if (cause == null) {
      error(message);
      return;
    }
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
      System.out.println(INDENT + "[DEBUG] " + message);
    }
  }

  public static void item(String text) {
    System.out.println(INDENT + "- " + text);
  }

  public static void blank() {
    System.out.println();
  }

  public static void println(String text) {
    for (String line : text.split("\n", -1)) {
      System.out.println(INDENT + line);
    }
  }
}
