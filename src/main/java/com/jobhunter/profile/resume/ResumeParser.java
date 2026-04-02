package com.jobhunter.profile.resume;

import com.jobhunter.ai.ClaudeService;
import com.jobhunter.cli.Main;
import com.jobhunter.exception.ProfileBuildException;
import com.jobhunter.exception.ResumeNotFoundException;
import com.jobhunter.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

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
    Utils.getFileType(resumePath);
    return parseResume(resumePath);
  }

  private String parseResume(String path) {
    try {
      String resumeText;
      if ("tex".equals(Utils.getFileType(path))) {
        resumeText = Utils.extractLatexPair(Files.readString(Paths.get(path))).getBody();
      } else {
        try (InputStream is = Files.newInputStream(Paths.get(path));
            XWPFDocument doc = new XWPFDocument(is);
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
          resumeText = extractor.getText();
        }
      }
      return claude.parseResumeTexOrDocx(resumeText);
    } catch (IOException e) {
      throw new ProfileBuildException("Failed to read resume file at: " + path, e);
    }
  }
}
