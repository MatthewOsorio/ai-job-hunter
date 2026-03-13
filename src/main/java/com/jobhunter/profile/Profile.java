package com.jobhunter.profile;

import java.util.List;

public class Profile {
    private final String resume;
    private final String username;
    private final String bio;
    private final List<GitHubRepo> topRepos;
    
    public Profile(String resume, String username, String bio, List<GitHubRepo> topRepos) {
        this.resume = resume;
        this.username = username;
        this.bio = bio;
        this.topRepos = topRepos;
    }

    public String getResume() {
        return resume;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public List<GitHubRepo> getTopRepos() {
        return topRepos;
    }

    // @Override
    // public String toString() { 

    // }


}
