package com.jobhunter.scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jobhunter.cli.Main;
import com.jobhunter.model.FetchResult;
import com.jobhunter.model.Job;
import com.typesafe.config.Config;

public class JobScraper {
    private Config config = Main.config;
    private PageFetcher pageFetcher = new PageFetcher();

    public void scrap() {
        List<Job> jobs = new ArrayList<>();

        for (Config source : config.getConfigList("jobhunter.sources")) {
            if (source.getBoolean("enabled")) {
                jobs.addAll(scrapGitHubRepo(source.getString("name"), source.getString("url")));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(7);
        List<Future<?>> futures = new ArrayList<>();

        for (Job job : jobs) {
            futures.add(executor.submit(() -> processJob(job)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Thread error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        executor.shutdown();
        pageFetcher.close();
    }

    private List<Job> scrapGitHubRepo(String name, String url) {
        List<Job> jobs = new ArrayList<>();
        try {
            System.out.println("Scraping source: " + name + " (" + url + ")");
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10_000)
                    .get();
            Element heading = doc.select("h2:contains(software engineer)").first();

            if (heading == null) {
                throw new Exception("Could not find job listings heading for source: " + name);
            }

            Element table = heading.parent().nextElementSibling().nextElementSibling().select("table").first();

            if (table == null) {
                throw new Exception("Could not find job listings table for source: " + name);
            }

            String currentCompany = null;
            for (Element row : table.select("tr")) {
                if (!row.select("th").isEmpty()) {
                    continue;
                }

                Elements cols = row.select("td");
                String firstCol = cols.get(0).text().trim();

                if (firstCol.equals("↳")) {
                    firstCol = currentCompany;
                } else {
                    currentCompany = firstCol;
                }

                String secondCol = cols.get(1).text().trim().replaceAll("[^\\x00-\\x7F]", "-");
                Element forthCol = cols.get(3);
                String fifthCol = cols.get(4).text().trim();

                if (!fifthCol.equals("0d") && !fifthCol.equals("1d")) {
                    continue;
                }

                String linkToJobPost = forthCol.select("a").first().attr("href");

                if (linkToJobPost == null) {
                    continue;
                }

                jobs.add(new Job(secondCol, firstCol, linkToJobPost));
            }
        } catch (Exception e) {
            System.out.println("Error scraping source: " + name + " (" + url + ")");
            e.printStackTrace();
        }
        return jobs;
    }

    private void processJob(Job job) {
        FetchResult result = pageFetcher.fetch(job.getUrl());

        if (result.getStatus() != FetchStatus.SUCCESS) {
            job.setNeedsManualReview(true);
            System.out.println("  ✗ Failed: " + job + " — " + result.getReason());
            return;
        }

        if (result.needsExtraction()) {
            // TODO: Send result.getContent() to LLM for extraction
            // System.out.println("  → Needs LLM extraction: " + job);
        } else {
            job.setDescription(result.getContent());
            // System.out.println("  ✓ Got description: " + job);
        }
    }
}
