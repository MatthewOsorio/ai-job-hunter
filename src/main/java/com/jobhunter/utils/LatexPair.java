package com.jobhunter.utils;

public class LatexPair {
  private final String preamble;
  private final String body;

  public LatexPair(String preamble, String body) {
    this.preamble = preamble;
    this.body = body;
  }

  public String getPreamble() {
    return preamble;
  }

  public String getBody() {
    return body;
  }
}
