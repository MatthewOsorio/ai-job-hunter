package com.jobhunter.cli;

public final class Console {
  private static final String INDENT = "  ";
  private static int lastHeaderWidth = 0;

  private Console() {}

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

  public static void progress(String tag, String detail) {
    System.out.println(INDENT + "[" + tag + "] " + detail);
  }

  public static void error(String message) {
    System.err.println(INDENT + "[ERROR] " + message);
  }

  public static void error(String message, Throwable cause) {
    System.err.println(INDENT + "[ERROR] " + message + ": " + cause.toString());
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
