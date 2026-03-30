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
    if (resumePath.toLowerCase().endsWith(".pdf")) {
      return parsePdf(resumePath);
    } else {
      throw new IllegalArgumentException("Unsupported resume format. Use .pdf");
    }
  }

  private String parsePdf(String path) {
    try {
      byte[] pdfBytes = Files.readAllBytes(Paths.get(path));
      String base64 = Base64.getEncoder().encodeToString(pdfBytes);

      return claude.parseResumePdf(base64);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse PDF resume: " + e.getMessage(), e);
    }
  }
}
