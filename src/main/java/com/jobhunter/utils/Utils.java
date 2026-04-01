package com.jobhunter.utils;

public final class Utils {
  private Utils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static LatexPair extractLatexPair(String latexContent) {
    int splitIdx = latexContent.indexOf("\\begin{document}");
    if (splitIdx == -1) {
      throw new IllegalArgumentException("Could not find \\begin{document} in LaTeX content");
    }
    String preamble = latexContent.substring(0, splitIdx);
    String body = latexContent.substring(splitIdx + "\\begin{document}".length());
    return new LatexPair(preamble, body);
  }

  public static String getFileType(String path) {
    if (path.toLowerCase().endsWith(".tex"))
      return "tex";
    if (path.toLowerCase().endsWith(".docx"))
      return "docx";
    throw new IllegalArgumentException("Unsupported file type for resume: " + path);
  }
}
