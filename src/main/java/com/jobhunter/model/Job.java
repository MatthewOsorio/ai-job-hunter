package com.jobhunter.model;

import java.util.UUID;

public class Job {
    private String id;
    private String title;
    private String company;
    private String url;
    private String description;
    private boolean needsManualReview;

    public Job(String title, String company, String url) {
        this.id = generatedId();
        this.title = title;
        this.company = company;
        this.url = url;
        this.needsManualReview = false;
    }

    public Job(String title, String company, String url, String description) {
        this.id = generatedId();
        this.title = title;
        this.company = company;
        this.url = url;
        this.description = description;
        this.needsManualReview = false;
    }

    private String generatedId() {
        return UUID.randomUUID().toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNeedsManualReview(boolean needsManualReview) {
        this.needsManualReview = needsManualReview;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public boolean getNeedsManualReview(){
        return needsManualReview;
    }

    @Override
    public String toString() {
        return String.format("%s at %s (%s) - %s", title, company, url, description);
    }
}
