package com.jobhunter.model;

public class Job {
    private String id;
    private String title;
    private String company;
    private String description;
    private String url;

    public Job(String title, String company, String description, String url) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
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

    @Override
    public String toString() {
        return String.format("%s at %s (%s)", title, company, url);
    }
}
