package com.jobhunter.profile.resume;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ResumeParser {
  private static final ClaudeService claude = new ClaudeService();
  private final String resumePath;

  public ResumeParser() {
    this.resumePath = Main.dotenv.get("RESUME_PATH");

    if (resumePath == null || resumePath.isEmpty()) {
      throw new IllegalArgumentException("RESUME_PATH environment variable is not set or empty.");
    }
  }

  public String parse() {
    if (resumePath.endsWith(".tex")) {
      return parseLatex(resumePath);
    } else if (resumePath.endsWith(".pdf")) {
      return parsePdf(resumePath);
    } else {
      throw new IllegalArgumentException("Unsupported resume format. Use .tex or .pdf");
    }
  }

  private String parseLatex(String path) {
    try {
      return claude.parseResumeLatex(Files.readString(Paths.get(path)));
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse LaTeX resume: " + e.getMessage(), e);
    }
  }

  private String parsePdf(String path) {
    try {
      byte[] pdfBytes = Files.readAllBytes(Paths.get(path));
      String base64 = Base64.getEncoder().encodeToString(pdfBytes);

      return claude.parseResumePdf(base64);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse PDF resume: " + e.getMessage(), e);
    }
  }
}
