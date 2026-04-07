package com.jobhunter.email;

import com.jobhunter.cli.Main;
import com.jobhunter.job.Job;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class EmailService {
  private final String senderEmail;
  private final String appPassword;
  private final String recipientEmail;

  public EmailService() {
    this.senderEmail = Main.dotenv.get("EMAIL_SENDER");
    this.appPassword = Main.dotenv.get("APP_PASSWORD");
    this.recipientEmail = Main.dotenv.get("RECIPIENT_EMAIL");
  }

  public void sendJobReport(List<Job> matchedJobs, List<Job> failedJobs) {
    if (isBlank(senderEmail) || isBlank(appPassword) || isBlank(recipientEmail)) {
      Main.console.warn(
          "Email not configured - skipping notification. Set EMAIL_SENDER, APP_PASSWORD, and RECIPIENT_EMAIL in .env");
      return;
    }

    boolean hasMatches = matchedJobs != null && !matchedJobs.isEmpty();
    boolean hasFailed = failedJobs != null && !failedJobs.isEmpty();

    if (!hasMatches && !hasFailed) {
      Main.console.warn("No matched or failed jobs - skipping email.");
      return;
    }

    try {
      Session session = createSession();
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(senderEmail));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
      message.setSubject("Job Hunter Report - "
          + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
      message.setContent(buildHtmlBody(matchedJobs, failedJobs), "text/html; charset=utf-8");
      Transport.send(message);
      Main.console.generalCat("Email report sent to " + recipientEmail);
    } catch (MessagingException e) {
      Main.console.error("Failed to send email notification: " + e.getMessage());
    }
  }

  private Session createSession() {
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    return Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(senderEmail, appPassword);
      }
    });
  }

  private String resumeFilename(Job job) {
    String safeCompany = job.getCompany().replaceAll("[^a-zA-Z0-9_-]", "_");
    String safeTitle = job.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
    return safeCompany + "_" + safeTitle + ".tex";
  }

  private String buildHtmlBody(List<Job> matchedJobs, List<Job> failedJobs) {
    List<Job> sorted = (matchedJobs == null ? List.<Job>of() : matchedJobs).stream()
        .sorted(Comparator.comparingInt(Job::getMatchScore).reversed()).toList();
    List<Job> failed = failedJobs == null ? List.of() : failedJobs;

    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'></head>");
    sb.append(
        "<body style='margin:0;padding:0;background-color:#f0f2f5;font-family:Arial,Helvetica,sans-serif;'>");
    sb.append(
        "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td align='center' style='padding:32px 16px;'>");

    // Card container
    sb.append(
        "<table width='600' cellpadding='0' cellspacing='0' border='0' style='max-width:600px;width:100%;'>");

    // Header
    sb.append(
        "<tr><td style='background:linear-gradient(135deg,#1e3a5f 0%,#2563eb 100%);border-radius:12px 12px 0 0;padding:36px 40px;'>");
    sb.append(
        "<p style='margin:0 0 4px 0;font-size:12px;font-weight:600;letter-spacing:2px;text-transform:uppercase;color:#93c5fd;'>AI Job Hunter</p>");
    sb.append(
        "<h1 style='margin:0 0 8px 0;font-size:28px;font-weight:700;color:#ffffff;'>Your Job Report</h1>");
    sb.append("<p style='margin:0;font-size:15px;color:#bfdbfe;'>").append(date)
        .append(" &nbsp;·&nbsp; ");
    sb.append("<strong style='color:#ffffff;'>").append(sorted.size()).append(" match")
        .append(sorted.size() == 1 ? "" : "es").append(" found</strong>");
    if (!failed.isEmpty()) {
      sb.append(" &nbsp;·&nbsp; <strong style='color:#fca5a5;'>").append(failed.size())
          .append(" need review</strong>");
    }
    sb.append("</p>");
    sb.append("</td></tr>");

    // Matched jobs section
    if (!sorted.isEmpty()) {
      sb.append("<tr><td style='background:#ffffff;padding:8px 0;'>");

      for (int i = 0; i < sorted.size(); i++) {
        Job job = sorted.get(i);
        int score = job.getMatchScore();
        String scoreColor = score >= 80 ? "#16a34a" : "#d97706";
        String scoreBg = score >= 80 ? "#f0fdf4" : "#fffbeb";
        String scoreBorder = score >= 80 ? "#bbf7d0" : "#fde68a";
        String filename = resumeFilename(job);
        boolean isLast = i == sorted.size() - 1;

        sb.append(
            "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td style='padding:24px 40px;");
        if (!isLast)
          sb.append("border-bottom:1px solid #e5e7eb;");
        sb.append("'>");

        // Rank + score row
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr>");
        sb.append("<td style='vertical-align:middle;'>");
        sb.append(
            "<span style='font-size:12px;font-weight:600;color:#9ca3af;letter-spacing:1px;text-transform:uppercase;'>Match #")
            .append(i + 1).append("</span>");
        sb.append("</td>");
        sb.append("<td align='right' style='vertical-align:middle;'>");
        sb.append(
            "<span style='display:inline-block;padding:4px 12px;border-radius:20px;background:")
            .append(scoreBg).append(";border:1px solid ").append(scoreBorder).append(";");
        sb.append("font-size:13px;font-weight:700;color:").append(scoreColor).append(";'>&#9679; ")
            .append(score).append("/100</span>");
        sb.append("</td></tr></table>");

        // Title + company
        sb.append("<h2 style='margin:10px 0 2px 0;font-size:20px;font-weight:700;color:#111827;'>")
            .append(escapeHtml(job.getTitle())).append("</h2>");
        sb.append("<p style='margin:0 0 16px 0;font-size:15px;color:#6b7280;'>")
            .append(escapeHtml(job.getCompany())).append("</p>");

        // Resume file
        sb.append(
            "<table cellpadding='0' cellspacing='0' border='0' style='margin-bottom:16px;'><tr>");
        sb.append(
            "<td style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;padding:8px 14px;'>");
        sb.append(
            "<span style='font-size:11px;font-weight:600;letter-spacing:1px;text-transform:uppercase;color:#94a3b8;'>Tailored Resume&nbsp;</span>");
        sb.append(
            "<span style='font-family:\"Courier New\",Courier,monospace;font-size:13px;color:#1e40af;font-weight:600;'>")
            .append(escapeHtml(filename)).append("</span>");
        sb.append("</td></tr></table>");

        // CTA button
        sb.append("<a href='").append(escapeHtml(job.getUrl())).append(
            "' style='display:inline-block;padding:10px 24px;background:#2563eb;color:#ffffff;");
        sb.append(
            "text-decoration:none;border-radius:6px;font-size:14px;font-weight:600;letter-spacing:0.3px;'>View Posting &#8594;</a>");

        sb.append("</td></tr></table>");
      }

      sb.append("</td></tr>");
    }

    // Failed jobs section
    if (!failed.isEmpty()) {
      // Section header
      sb.append(
          "<tr><td style='background:#fef2f2;border-top:2px solid #fecaca;padding:16px 40px;'>");
      sb.append(
          "<p style='margin:0;font-size:12px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:#dc2626;'>&#9888; Needs Manual Review</p>");
      sb.append(
          "<p style='margin:4px 0 0 0;font-size:13px;color:#9f1239;'>These jobs failed during scraping and could not be processed automatically.</p>");
      sb.append("</td></tr>");

      // Failed job rows
      sb.append("<tr><td style='background:#fff7f7;padding:4px 0 8px;'>");

      for (int i = 0; i < failed.size(); i++) {
        Job job = failed.get(i);
        boolean isLast = i == failed.size() - 1;

        sb.append(
            "<table width='100%' cellpadding='0' cellspacing='0' border='0'><tr><td style='padding:16px 40px;");
        if (!isLast)
          sb.append("border-bottom:1px solid #fee2e2;");
        sb.append("'>");

        // Title + company
        sb.append("<p style='margin:0 0 2px 0;font-size:15px;font-weight:700;color:#111827;'>")
            .append(escapeHtml(job.getTitle())).append("</p>");
        sb.append("<p style='margin:0 0 8px 0;font-size:13px;color:#6b7280;'>")
            .append(escapeHtml(job.getCompany())).append("</p>");

        // URL as plain link
        sb.append("<a href='").append(escapeHtml(job.getUrl())).append(
            "' style='font-size:13px;color:#dc2626;text-decoration:underline;word-break:break-all;'>")
            .append(escapeHtml(job.getUrl())).append("</a>");

        sb.append("</td></tr></table>");
      }

      sb.append("</td></tr>");
    }

    // Footer
    sb.append("<tr><td style='background:#1f2937;border-radius:0 0 12px 12px;padding:24px 40px;'>");
    sb.append(
        "<p style='margin:0;font-size:13px;color:#9ca3af;text-align:center;'>Generated by <strong style='color:#e5e7eb;'>AI Job Hunter</strong></p>");
    sb.append("</td></tr>");

    sb.append("</table>"); // card
    sb.append("</td></tr></table>"); // outer
    sb.append("</body></html>");

    return sb.toString();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  private String escapeHtml(String text) {
    if (text == null)
      return "";
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;").replace("'", "&#39;");
  }
}
