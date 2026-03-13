package com.jobhunter.job;

import java.util.UUID;

public class Job {
    private final String id;
    private String title;
    private String company;
    private String url;
    private String description;
    private boolean needsManualReview;
    private boolean shouldApply;
    private int matchScore;
    private String filterReason;

    public Job(String title, String company, String url) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.company = company;
        this.url = url;
        this.needsManualReview = false;
    }

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public String getCompany()      { return company; }
    public String getUrl()          { return url; }
    public String getDescription()  { return description; }
    public boolean getNeedsManualReview() { return needsManualReview; }
    public boolean isShouldApply()  { return shouldApply; }
    public int getMatchScore()      { return matchScore; }
    public String getFilterReason() { return filterReason; }

    public void setTitle(String title)              { this.title = title; }
    public void setCompany(String company)          { this.company = company; }
    public void setUrl(String url)                  { this.url = url; }
    public void setDescription(String description)  { this.description = description; }
    public void setNeedsManualReview(boolean v)     { this.needsManualReview = v; }
    public void setShouldApply(boolean v)           { this.shouldApply = v; }
    public void setMatchScore(int v)                { this.matchScore = v; }
    public void setFilterReason(String v)           { this.filterReason = v; }

    @Override
    public String toString() {
        return String.format("%s at %s (%s)", title, company, url);
    }
}
