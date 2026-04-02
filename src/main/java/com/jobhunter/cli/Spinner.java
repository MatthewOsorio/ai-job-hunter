package com.jobhunter.cli;

public class Spinner {
  private static final String CYAN = "\033[96m";
  private static final String RESET = "\033[0m";
  private static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};

  private Thread thread;
  private volatile String currentMessage = "";

  public void start(String message) {
    if (thread != null) {
      stop();
    }
    currentMessage = message;
    thread = new Thread(() -> {
      int i = 0;
      while (!Thread.currentThread().isInterrupted()) {
        System.out.print("\r  " + CYAN + FRAMES[i % FRAMES.length] + RESET + " " + currentMessage);
        System.out.flush();
        i++;
        try {
          Thread.sleep(80);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  public void updateMessage(String message) {
    currentMessage = message;
  }

  public void stop() {
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    System.out.print("\r" + " ".repeat(60) + "\r");
    System.out.flush();
  }
}
