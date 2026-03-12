package com.jobhunter.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.jobhunter.cli.Main;

import java.io.IOException;
import java.util.stream.Collectors;

public class ClaudeService {
        private final AnthropicClient client = AnthropicOkHttpClient.builder()
                        .apiKey(Main.dotenv.get("ANTHROPIC_API_KEY"))
                        .build();

        private final String systemPrompt = """
                        You are an expert resume optimization specialist with deep knowledge of ATS (Applicant Tracking Systems), technical recruiting, and the software engineering job market.

                        Never fabricate, exaggerate, or invent any experience, skill, project, metric, or credential. Every claim in the output must be directly traceable to the base resume.
                        Never add technologies, tools, or frameworks not listed or clearly demonstrated in the base resume.
                        Return only what is asked for — no preamble, no sign-off.
                        """;

        private final String userPromptTemplate = """
                        Tailor my LaTeX resume for the job posting below.

                        ## Optimization Strategy

                        ### 1. Keyword Alignment
                        - Identify key technologies, tools, frameworks, methodologies, and soft skills in the job posting
                        - Mirror the exact terminology from the job posting where I have matching experience
                        - Naturally weave high-priority keywords into bullet points, skills section, and project descriptions

                        ### 2. Bullet Point Rewriting
                        - Rewrite bullet points using strong action verbs (Engineered, Architected, Optimized, Spearheaded, Automated, etc.)
                        - Follow the X-Y-Z formula where possible: "Accomplished [X] as measured by [Y], by doing [Z]"
                        - Keep all quantifiable results from the original. If a metric can be reasonably inferred without fabrication, add it as a LaTeX comment (%% SUGGESTION: ...) but never in the actual output
                        - Reorder bullet points so the strongest matches to the job posting appear first under each role

                        ### 3. Skills Section
                        - Reorder so skills mentioned in the job posting appear first
                        - Group to mirror the job posting's categories if applicable
                        - Deprioritize skills irrelevant to this role

                        ### 4. Experience & Project Prioritization
                        - Keep all experiences and projects in their original chronological order — do not reorder them
                        - Condense less relevant entries to free space for what matters most for this specific job
                        - If space allows, add a new bullet to an existing experience that directly addresses a key requirement from the job posting, but only if it can be reasonably inferred from the existing resume content without fabrication. Mark any such additions with %% SUGGESTION: explaining the rationale and what in the original resume supports it

                        ## Rules
                        - Preserve all LaTeX structure — document class, packages, preamble, formatting commands
                        - Keep all personal information, job titles, companies, and dates exactly the same
                        - Never remove or merge \\item bullets
                        - Claude may add new \\item bullets only if marked with %% SUGGESTION: explaining the rationale and what in the original resume supports it
                        - Do not alter \\section{} headers or their order
                        - Do not add any LaTeX comments except %% SUGGESTION: entries
                        - Do not wrap the LaTeX output in markdown code fences — return raw LaTeX only, starting with \\documentclass

                        ## Output Format
                        Return the complete LaTeX file first, then after a line containing only ---CHANGES--- list each meaningful change and why.

                        ## My Resume (LaTeX):
                        {{RESUME}}

                        ## Job Posting:
                        {{JOB_POSTING}}
                        """;

        public void tailorResume(String resume, String jobPosting) throws IOException {

                String userPrompt = userPromptTemplate.replace("{{JOB_POSTING}}", jobPosting)
                                .replace("{{RESUME}}", resume);

                String response = callClaude(userPrompt);

                String[] parts = response.split("---CHANGES---");
                String tailoredLatex = parts[0].trim()
                                .replaceAll("(?s)^```latex\\s*", "")
                                .replaceAll("(?s)^```\\s*", "")
                                .replaceAll("```$", "")
                                .trim();

                System.out.println(tailoredLatex);

                // Path original = Paths.get(RESUME_PATH);
                // Path output = original.resolveSibling(
                // original.getFileName().toString().replace(".tex", "_tailored.tex"));
                // Files.writeString(output, tailoredLatex);

                // System.out.println("Tailored resume saved to: " + output);
                // System.out.println("\n--- CHANGES ---\n" + changes);
        }

        private String callClaude(String userPrompt) {
                MessageCreateParams params = MessageCreateParams.builder()
                                .model(Model.CLAUDE_SONNET_4_5)
                                .maxTokens(8096L)
                                .addUserMessage(userPrompt)
                                .system(systemPrompt)
                                .build();

                Message response = client.messages().create(params);

                String text = response.content().stream()
                                .filter(block -> block.isText())
                                .map(block -> block.asText().text())
                                .collect(Collectors.joining("\n"));

                return text;
        }
}