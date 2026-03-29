package com.jobhunter.cli;

public class Spinner {
  private static final String[] FRAMES = {"|", "/", "-", "\\"};

  private Thread thread;

  public void start(String message) {
    thread = new Thread(() -> {
      int i = 0;
      while (!Thread.currentThread().isInterrupted()) {
        System.out.print("\r  " + message + " " + FRAMES[i % FRAMES.length]);
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
