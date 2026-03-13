package com.jobhunter.profile;

import com.jobhunter.ai.ClaudeService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ResumeParser {
    private static final ClaudeService claude = new ClaudeService();

    public String parse(String resumePath) {
        if (resumePath.endsWith(".tex")) {
            System.out.println("Parsing LaTeX resume...");
            return parseLatex(resumePath);
        } else if (resumePath.endsWith(".pdf")) {
            System.out.println("Parsing PDF resume...");
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