package com.jobhunter.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.jobhunter.cli.Main;

import kotlin.io.path.PathsKt;

public class ProfileBuilder {
    private final String resumePath = Main.dotenv.get("RESUME_PATH");
    private final ResumeParser resumeParser = new ResumeParser();

    // private static final Profile profile = null;

    // public Profile getProfile() {
    // if (profile != null) {
    // return profile;
    // }

    // return build();
    // }

    public void build() {
        String resume = resumeParser.parse(resumePath);
        System.out.println(resume);
    }
}
