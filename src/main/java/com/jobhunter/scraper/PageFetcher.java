package com.jobhunter.scraper;

import java.io.IOException;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jobhunter.model.FetchResult;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

public class PageFetcher {
    private static final int[] BLOCKED_STATUS_CODES = { 403, 429, 503 };

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private final BrowserPool browserPool = new BrowserPool();

    public FetchResult fetch(String url) {
        url = url.replaceAll("/(application|apply)(\\?|$)", "$2");
        boolean isWorkday = url.contains("myworkdayjobs.com") || url.contains("myworkdaysite.com");

        if (!isWorkday) {
            FetchResult result = fetchWithJsoup(url);
            if (result.getStatus() == FetchStatus.SUCCESS) {
                return result;
            }
        }

        try {
            BrowserPool.BrowserInstance instance = browserPool.borrow();
            try {
                return fetchWithPlaywright(url, instance.browser());
            } finally {
                browserPool.returnInstance(instance);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return FetchResult.error("Interrupted waiting for browser");
        }
    }

    public void close() {
        browserPool.shutdown();
    }

    private FetchResult fetchWithJsoup(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .followRedirects(true)
                    .timeout(30_000)
                    .get();

            doc.select("script, style, noscript, iframe, svg").remove();
            String text = doc.body().text().trim();

            if (text.isEmpty()) {
                return FetchResult.empty();
            }
            return FetchResult.success(text, true);
        } catch (HttpStatusException e) {
            if (isBlockedStatus(e.getStatusCode())) {
                return FetchResult.blocked("Blocked by website with status code: " + e.getStatusCode());
            }
            return FetchResult.error("HTTP error: " + e.getStatusCode());
        } catch (IOException e) {
            return FetchResult.error("IO error: " + e.getMessage());
        }
    }

    private FetchResult fetchWithPlaywright(String url, Browser browser) {
        try {
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent(USER_AGENT)
                    .setViewportSize(1920, 1080));

            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(60_000));

            String text;

            boolean isWorkday = url.contains("myworkdayjobs.com") || url.contains("myworkdaysite.com");

            if (isWorkday) {
                try {
                    page.waitForSelector("[data-automation-id='jobPostingDescription']",
                            new Page.WaitForSelectorOptions().setTimeout(15_000));
                    text = page.locator("[data-automation-id='jobPostingDescription']").innerText().trim();
                } catch (Exception e) {
                    text = page.locator("body").innerText().trim();
                }
            } else {
                page.waitForTimeout(10_000);
                text = page.locator("body").innerText().trim();
            }

            context.close();

            if (text.isEmpty()) {
                return FetchResult.empty();
            }
            return FetchResult.success(text, !isWorkday);
        } catch (Exception e) {
            return FetchResult.error("Playwright: " + e.getMessage());
        }
    }

    private boolean isBlockedStatus(int statusCode) {
        for (int code : BLOCKED_STATUS_CODES) {
            if (statusCode == code) {
                return true;
            }
        }
        return false;
    }
}