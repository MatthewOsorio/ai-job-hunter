package com.jobhunter.job;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
// import com.jobhunter.db.JobRepository;
import com.jobhunter.scraper.FetchResult;
import com.jobhunter.scraper.FetchStatus;
import com.jobhunter.scraper.PageFetcher;
import com.typesafe.config.Config;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JobScraper {
    private final Config config = Main.config;
    private final PageFetcher pageFetcher = new PageFetcher();
    private final ClaudeService claudeService = new ClaudeService();
    // private final JobRepository repository = new JobRepository();

    public JobScraperResult scrape() {
        List<Job> jobs = new ArrayList<>();

        for (Config source : config.getConfigList("jobhunter.sources")) {
            if (source.getBoolean("enabled")) {
                jobs.addAll(scrapeGitHubRepo(source.getString("name"), source.getString("url")));
            }
        }

        // // Filter out already-seen URLs
        // jobs.removeIf(job -> repository.hasBeenSeen(job.getUrl()));

        JobScraperResult results = new JobScraperResult();
        ExecutorService executor = Executors.newFixedThreadPool(7);
        List<Future<?>> futures = new ArrayList<>();

        for (Job job : jobs) {
            futures.add(executor.submit(() -> processJob(job, results)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("Thread error: " + e.getMessage());
            }
        }

        executor.shutdown();
        pageFetcher.close();

        // // Mark all as seen regardless of extraction success
        // jobs.forEach(job -> repository.markAsSeen(job.getUrl()));

        return results;
    }

    public void processJob(Job job, JobScraperResult result) {
        FetchResult fetchResult = pageFetcher.fetch(job.getUrl());

        if (fetchResult.getStatus() != FetchStatus.SUCCESS) {
            result.addFailedJob(job);
            return;
        }

        if (fetchResult.needsExtraction()) {
            Optional<String> desc = claudeService.extractJobDescription(fetchResult.getContent());
            if (desc.isPresent()) {
                job.setDescription(desc.get());
                result.addValidJob(job);
            } else {
                result.addFailedJob(job);
            }
            ;
        } else {
            job.setDescription(fetchResult.getContent());
            result.addValidJob(job);
        }
    }

    private List<Job> scrapeGitHubRepo(String name, String url) {
        List<Job> jobs = new ArrayList<>();
        try {
            System.out.println("Scraping source: " + name + " (" + url + ")");
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10_000)
                    .get();

            Element heading = doc.select("h2:contains(software engineer)").first();
            if (heading == null)
                throw new Exception("Could not find job listings heading for: " + name);

            Element table = heading.parent().nextElementSibling().nextElementSibling().select("table").first();
            if (table == null)
                throw new Exception("Could not find job listings table for: " + name);

            String currentCompany = null;
            for (Element row : table.select("tr")) {
                if (!row.select("th").isEmpty())
                    continue;

                Elements cols = row.select("td");
                String firstCol = cols.get(0).text().trim();

                if (firstCol.equals("↳")) {
                    firstCol = currentCompany;
                } else {
                    currentCompany = firstCol;
                }

                String title = cols.get(1).text().trim().replaceAll("[^\\x00-\\x7F]", "-");
                Element linkCol = cols.get(3);
                String age = cols.get(4).text().trim();

                if (!age.equals("0d") && !age.equals("1d"))
                    continue;

                Element link = linkCol.select("a").first();
                if (link == null)
                    continue;

                jobs.add(new Job(title, firstCol, link.attr("href")));
            }
        } catch (Exception e) {
            System.err.println("Error scraping source: " + name);
            e.printStackTrace();
        }
        return jobs;
    }
}
