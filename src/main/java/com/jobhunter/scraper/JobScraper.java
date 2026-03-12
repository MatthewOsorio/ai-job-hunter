package com.jobhunter.scraper;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jobhunter.cli.Main;
import com.jobhunter.model.Job;
import com.typesafe.config.Config;

public class JobScraper {
    private Config config = Main.config;

    public void scrap() {
        List<Job> jobs = new ArrayList<>();

        for (Config source : config.getConfigList("jobhunter.sources")) {
            if (source.getBoolean("enabled")) {
                scrapSource(source.getString("name"), source.getString("url"));
            }
        }
    }

    private String getJobDescription(Element jobLink){
        
    }

    private void scrapSource(String name, String url) {
        try {
            Document doc = Jsoup.connect(url).get();
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

                String secondCol = cols.get(1).text().trim();
                Element forthCol = cols.get(3);
                String fifthCol = cols.get(4).text().trim();

                if (!fifthCol.equals("0d")) {
                    continue;
                }

                System.out.println(forthCol);
            }
        } catch (Exception e) {
            System.out.println("Error scraping source: " + name + " (" + url + ")");
            e.printStackTrace();
        }
    }
}
