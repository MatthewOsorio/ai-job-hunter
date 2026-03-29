package com.jobhunter.scraper;

public class FetchResult {
  private final FetchStatus status;
  private final String content;
  private final String reason;
  private final boolean needsExtraction;

  private FetchResult(FetchStatus status, String content, String reason, boolean needsExtraction) {
    this.status = status;
    this.content = content;
    this.reason = reason;
    this.needsExtraction = needsExtraction;
  }

  public static FetchResult success(String content, boolean needsExtraction) {
    return new FetchResult(FetchStatus.SUCCESS, content, null, needsExtraction);
  }

  public static FetchResult blocked(String reason) {
    return new FetchResult(FetchStatus.BLOCKED, null, reason, false);
  }

  public static FetchResult empty() {
    return new FetchResult(FetchStatus.EMPTY, null, "Page returned no content", false);
  }

  public static FetchResult error(String reason) {
    return new FetchResult(FetchStatus.ERROR, null, reason, false);
  }

  public FetchStatus getStatus() {
    return status;
  }

  public String getContent() {
    return content;
  }

  public String getReason() {
    return reason;
  }

  public boolean needsExtraction() {
    return needsExtraction;
  }

  @Override
  public String toString() {
    return "FetchResult{" + "status=" + status + ", contentLength="
        + (content != null ? content.length() : 0) + ", needsExtraction=" + needsExtraction
        + ", reason='" + reason + '\'' + '}';
  }
}
