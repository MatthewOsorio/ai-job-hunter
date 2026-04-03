package com.jobhunter.scraper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class BrowserPool {
  private final BlockingQueue<BrowserInstance> pool;
  public static final int DEFAULT_POOL_SIZE = 5;

  public BrowserPool() {
    this(DEFAULT_POOL_SIZE);
  }

  public BrowserPool(int poolSize) {
    this.pool = new LinkedBlockingQueue<>(poolSize);

    for (int i = 0; i < poolSize; i++) {
      Playwright pw = Playwright.create();
      Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      pool.add(new BrowserInstance(pw, browser));
    }

    Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
  }

  public BrowserInstance borrow() throws InterruptedException {
    return pool.take();
  }

  public void returnInstance(BrowserInstance instance) {
    pool.add(instance);
  }

  public void shutdown() {
    for (BrowserInstance instance : pool) {
      instance.browser().close();
      instance.playwright().close();
    }
  }

  public record BrowserInstance(Playwright playwright, Browser browser) {}
}
