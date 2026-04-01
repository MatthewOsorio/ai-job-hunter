package com.jobhunter.utils;

public class Utils {
    public String extractBodyFromLatexFile(String latexContent) {
        int splitIdx = latexContent.indexOf("\\begin{document}");
        if (splitIdx == -1) {
            throw new IllegalArgumentException("Could not find \\begin{document} in LaTeX content");
        }
        return latexContent.substring(splitIdx + "\\begin{document}".length());
    }
}
