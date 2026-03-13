package com.jobhunter.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobhunter.cli.Main;
import com.typesafe.config.Config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClaudeService {
    private final AnthropicClient client = AnthropicOkHttpClient.builder()
            .apiKey(Main.dotenv.get("ANTHROPIC_API_KEY"))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Config prompts = Main.config.getConfig("jobhunter.prompts");

    private final String tailorSystemPrompt = """
            You are an expert resume optimization specialist with deep knowledge of ATS (Applicant Tracking Systems), technical recruiting, and the software engineering job market.

            Never fabricate, exaggerate, or invent any experience, skill, project, metric, or credential. Every claim in the output must be directly traceable to the base resume.
            Never add technologies, tools, or frameworks not listed or clearly demonstrated in the base resume.
            Return only what is asked for — no preamble, no sign-off.
            """;

    private final String tailorUserTemplate = """
            Tailor my LaTeX resume for the job posting below.

            ## Optimization Strategy

            ### 1. Keyword Alignment
            - Identify key technologies, tools, frameworks, methodologies, and soft skills in the job posting
            - Mirror the exact terminology from the job posting where I have matching experience
            - Naturally weave high-priority keywords into bullet points, skills section, and project descriptions

            ### 2. Bullet Point Rewriting
            - Rewrite bullet points using strong action verbs (Engineered, Architected, Optimized, Spearheaded, Automated, etc.)
            - Follow the X-Y-Z formula where possible: "Accomplished [X] as measured by [Y], by doing [Z]"
            - Keep all quantifiable results from the original
            - Reorder bullet points so the strongest matches to the job posting appear first under each role

            ### 3. Skills Section
            - Reorder so skills mentioned in the job posting appear first
            - Group to mirror the job posting's categories if applicable
            - Deprioritize skills irrelevant to this role

            ### 4. Experience & Project Prioritization
            - Keep all experiences and projects in their original chronological order
            - Condense less relevant entries to free space for what matters most
            - Mark any inferred additions with %% SUGGESTION: explaining the rationale

            ## Rules
            - Preserve all LaTeX structure
            - Keep all personal information, job titles, companies, and dates exactly the same
            - Never remove or merge \\item bullets
            - Do not alter \\section{} headers or their order
            - Do not wrap the LaTeX output in markdown code fences — return raw LaTeX only, starting with \\documentclass

            ## Output Format
            Return the complete LaTeX file first, then after a line containing only ---CHANGES--- list each meaningful change and why.

            ## My Resume (LaTeX):
            {{RESUME}}

            ## Job Posting:
            {{JOB_POSTING}}
            """;

    public Optional<String> extractJobDescription(String pageContent) {
        String raw = callClaude(
                Model.CLAUDE_HAIKU_4_5_20251001,
                prompts.getString("extraction.system"),
                prompts.getString("extraction.user").replace("{{PAGE_CONTENT}}", pageContent));
        try {
            ExtractionResult result = objectMapper.readValue(raw, ExtractionResult.class);
            return result.found() ? Optional.of(result.description()) : Optional.empty();
        } catch (Exception e) {
            System.err.println("Failed to parse extraction response: " + raw);
            return Optional.empty();
        }
    }

    public String parseResumeLatex(String latex) {
        return callClaude(
                Model.CLAUDE_HAIKU_4_5_20251001,
                prompts.getString("resume.system"),
                latex);
    }

    public String parseResumePdf(String encodedResume) {
        DocumentBlockParam documentBlock = DocumentBlockParam.builder()
                .source(Base64PdfSource.builder()
                        .data(encodedResume)
                        .build())
                .build();

        return callClaude(Model.CLAUDE_HAIKU_4_5_20251001, prompts.getString("resume.system"), documentBlock);
    }

    public FilterResult filterJob(String profile, String jobDescription) {
        String json = callClaude(
                Model.CLAUDE_HAIKU_4_5_20251001,
                prompts.getString("filter.system"),
                prompts.getString("filter.user")
                        .replace("{{PROFILE}}", profile)
                        .replace("{{JOB_DESCRIPTION}}", jobDescription));
        try {
            return objectMapper.readValue(json, FilterResult.class);
        } catch (Exception e) {
            System.err.println("Failed to parse filter response: " + json);
            return new FilterResult(true, 50, "Parse error — defaulting to apply");
        }
    }

    public String tailorResume(String resume, String jobDescription) {
        return callClaude(
                Model.CLAUDE_HAIKU_4_5_20251001,
                tailorSystemPrompt,
                tailorUserTemplate
                        .replace("{{RESUME}}", resume)
                        .replace("{{JOB_POSTING}}", jobDescription));
    }

    private String callClaude(Model model, String systemPrompt, DocumentBlockParam documentBlock) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(4096L)
                .system(systemPrompt)
                .addUserMessageOfBlockParams(List.of(
                        ContentBlockParam.ofDocument(documentBlock)))
                .build();

        return getClaudeResponse(client.messages().create(params));
    }

    private String callClaude(Model model, String systemPrompt, String userPrompt) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(8096L)
                .system(systemPrompt)
                .addUserMessage(userPrompt)
                .build();

        return getClaudeResponse(client.messages().create(params));
    }

    private String getClaudeResponse(Message response) {
        return stripFences(response.content().stream()
                .filter(ContentBlock::isText)
                .map(block -> block.asText().text())
                .collect(Collectors.joining("\n")));
    }

    private String stripFences(String raw) {
        return raw.replaceAll("(?s)^```\\w*\\s*", "").replaceAll("(?s)```\\s*$", "").trim();
    }
}