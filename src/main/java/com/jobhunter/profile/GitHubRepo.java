package com.jobhunter.profile;

import java.util.List;

public class GitHubRepo {
    private final String name;
    private final String description;
    private final List<String> languages;
    private final String readme;

    public GitHubRepo(String name, String description, List<String> languages, String readme) {
        this.name = name;
        this.description = description;
        this.languages = languages;
        this.readme = readme;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public String getReadme() {
        return readme;
    }

}
