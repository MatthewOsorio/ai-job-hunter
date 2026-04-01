package com.jobhunter.profile.resume;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ResumeNotFoundException;
import com.jobhunter.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResumeParser {
  private final ClaudeService claude;
  private final String resumePath;

  public ResumeParser(ClaudeService claude) {
    this.claude = claude;
    this.resumePath = Main.dotenv.get("RESUME_PATH");

    if (resumePath == null || resumePath.isEmpty()) {
      throw new ResumeNotFoundException(
          "RESUME_PATH is not set in .env. Please add the path to your resume.");
    }
  }

  public String parse() {
    if (!Files.exists(Paths.get(resumePath))) {
      throw new ResumeNotFoundException("Resume file not found at: " + resumePath);
    }
    String fileType = Utils.getFileType(resumePath);
    if ("tex".equals(fileType) || "docx".equals(fileType)) {
      return parseResume(resumePath);
    } else {
      throw new IllegalArgumentException("Unsupported resume format. Use .tex or .docx");
    }
  }

  private String parseResume(String path) {
    try {
      String resumeText = Files.readString(Paths.get(path));

      if ("tex".equals(Utils.getFileType(path))) {
        resumeText = Utils.extractLatexPair(resumeText).getBody();
      }

      return claude.parseResumeTexOrDocx(resumeText);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resume file at: " + path, e);
    }
  }
}
