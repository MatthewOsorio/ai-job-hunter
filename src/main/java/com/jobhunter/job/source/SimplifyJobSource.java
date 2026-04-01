package com.jobhunter.job.source;

import com.jobhunter.cli.Console;
import com.jobhunter.exception.ScrapingException;
import com.jobhunter.job.Job;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimplifyJobSource extends JobSource {

  public SimplifyJobSource(String name, String url) {
    super(name, url);
  }

  @Override
  public List<Job> scrape() {
    List<Job> jobs = new ArrayList<>();
    try {
      Document doc = Jsoup.connect(getUrl())
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
              + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .timeout(10_000).get();

      Element heading = doc.select("h2:contains(software engineer)").first();
      if (heading == null)
        throw new ScrapingException("Could not find job listings heading for: " + getName());

      Element table =
          heading.parent().nextElementSibling().nextElementSibling().select("table").first();
      if (table == null)
        throw new ScrapingException("Could not find job listings table for: " + getName());

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

        if (!age.equals("0d") && !age.equals("1d") && !age.equals("2d"))
          continue;

        Element link = linkCol.select("a").first();
        if (link == null)
          continue;

        jobs.add(new Job(title, firstCol, link.attr("href")));
      }
    } catch (ScrapingException | IOException e) {
      Console.error("Scraping failed for " + getName(), e);
    }
    return jobs;
  }
}
