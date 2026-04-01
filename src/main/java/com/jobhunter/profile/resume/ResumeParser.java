package com.jobhunter.profile.resume;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

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
    String fileType = getFileType(resumePath);
    if ("tex".equals(fileType) || "docx".equals(fileType)) {
      return parseResume(resumePath);
    } else {
      throw new IllegalArgumentException("Unsupported resume format. Use .tex or .docx");
    }
  }

  private String parseResume(String path) {
    try {
      byte[] fileBytes = Files.readAllBytes(Paths.get(path));

      return claude.parseResumePdf(base64);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse PDF resume: " + e.getMessage(), e);
    }
  }

  private String getFileType(String path) {
    if (path.toLowerCase().endsWith(".tex"))
      return "tex";
    if (path.toLowerCase().endsWith(".docx"))
      return "docx";
    throw new IllegalArgumentException("Unsupported file type for resume: " + path);
  }
}
