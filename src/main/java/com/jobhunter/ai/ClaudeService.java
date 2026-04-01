package com.jobhunter.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jobhunter.cli.Console;
import com.jobhunter.exception.AiServiceException;
import com.typesafe.config.Config;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ClaudeService {
  private final Semaphore apiSemaphore;
  private final AnthropicClient client;
  private final ObjectMapper objectMapper =
      JsonMapper.builder().enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS).build();
  private final Config prompts;
  private final Config ai;

  public ClaudeService(String apiKey, Config jobhunterConfig) {
    this.client =
        AnthropicOkHttpClient.builder().apiKey(apiKey).timeout(Duration.ofMinutes(10)).build();
    this.prompts = jobhunterConfig.getConfig("prompts");
    this.ai = jobhunterConfig.getConfig("ai");
    int concurrency = ai.getInt("concurrency");
    if (concurrency < 1) {
      throw new IllegalArgumentException(
          "Invalid AI concurrency setting: " + concurrency + ". Expected a value >= 1.");
    }
    this.apiSemaphore = new Semaphore(concurrency);
  }

  public Optional<String> extractJobDescription(String pageContent) {
    List<ContentBlockParam> content = List.of(ContentBlockParam.ofText(TextBlockParam.builder()
        .text(prompts.getString("extraction.user").replace("{{PAGE_CONTENT}}", pageContent))
        .build()));
    String raw = callClaude(Model.of(ai.getString("models.fast")),
        prompts.getString("extraction.system"), ai.getLong("maxTokens.extraction"), content);
    try {
      ExtractionResult result = objectMapper.readValue(raw, ExtractionResult.class);
      return result.found() ? Optional.of(result.description()) : Optional.empty();
    } catch (JsonProcessingException e) {
      Console.error("Failed to parse extraction response", e);
      return Optional.empty();
    }
  }

  public String parseResumeTexOrDocx(String resume) {
    List<ContentBlockParam> content = List.of(ContentBlockParam.ofText(TextBlockParam.builder()
        .text(prompts.getString("resume.user").replace("{{RESUME_CONTENT}}", resume)).build()));
    return callClaude(Model.of(ai.getString("models.fast")), prompts.getString("resume.system"),
        ai.getLong("maxTokens.resume"), content);
  }

  public FilterResult filterJob(String profile, String jobDescription) {
    List<ContentBlockParam> content = List
        .of(ContentBlockParam.ofText(TextBlockParam.builder().text(prompts.getString("filter.user")
            .replace("{{PROFILE}}", profile).replace("{{JOB_DESCRIPTION}}", jobDescription))
            .build()));
    String json = callClaude(Model.of(ai.getString("models.fast")),
        prompts.getString("filter.system"), ai.getLong("maxTokens.filter"), content);
    try {
      FilterResult result = objectMapper.readValue(json, FilterResult.class);
      int clamped = Math.max(0, Math.min(100, result.matchScore()));
      return new FilterResult(result.shouldApply(), clamped);
    } catch (JsonProcessingException e) {
      throw new AiServiceException("Failed to parse filter response: " + e.getMessage(), e);
    }
  }

  public String summarizeReadMe(String readMeContent) {
    List<ContentBlockParam> content = List.of(ContentBlockParam.ofText(TextBlockParam.builder()
        .text(prompts.getString("readme.user").replace("{{README_CONTENT}}", readMeContent))
        .build()));
    return callClaude(Model.of(ai.getString("models.fast")), prompts.getString("readme.system"),
        ai.getLong("maxTokens.readme"), content);
  }

  public String tailorResumeTex(String texContent, String jobDescription,
      String pageCountFeedback) {
    String userPrompt = prompts.getString("tailor.user-tex").replace("{{TEX_CONTENT}}", texContent)
        .replace("{{JOB_POSTING}}", jobDescription);
    if (pageCountFeedback != null) {
      userPrompt += "\n\n## CRITICAL FEEDBACK FROM PREVIOUS ATTEMPT\n" + pageCountFeedback;
    }
    List<ContentBlockParam> content =
        List.of(ContentBlockParam.ofText(TextBlockParam.builder().text(userPrompt).build()));
    return callClaude(Model.of(ai.getString("models.strong")),
        prompts.getString("tailor.system-tex"), ai.getLong("maxTokens.tailor"), content);
  }

  private String callClaude(Model model, String systemPrompt, long maxTokens,
      List<ContentBlockParam> contentBlocks) {
    MessageCreateParams params = MessageCreateParams.builder().model(model).maxTokens(maxTokens)
        .system(systemPrompt).addUserMessageOfBlockParams(contentBlocks).build();
    return callClaudeWithRetry(params);
  }

  private String callClaudeWithRetry(MessageCreateParams params) {
    int maxRetries = 5;
    long delayMs = 2000;
    AnthropicServiceException lastException = null;
    for (int attempt = 0; attempt <= maxRetries; attempt++) {
      if (attempt > 0) {
        try {
          Console.warn(String.format("API error (status %d), retrying in %ds (attempt %d/%d)",
              lastException.statusCode(), delayMs / 1000, attempt, maxRetries));
          Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new AiServiceException("Interrupted during retry backoff", ie);
        }
        delayMs *= 2;
      }
      apiSemaphore.acquireUninterruptibly();
      try {
        return getClaudeResponse(client.messages().create(params));
      } catch (AnthropicServiceException e) {
        int status = e.statusCode();
        if (status == 429 || status == 500 || status == 529) {
          lastException = e;
        } else {
          throw e;
        }
      } finally {
        apiSemaphore.release();
      }
    }
    throw new AiServiceException("Claude API failed after " + maxRetries + " retries",
        lastException);
  }

  private String getClaudeResponse(Message response) {
    return stripFences(response.content().stream().filter(ContentBlock::isText)
        .map(block -> block.asText().text()).collect(Collectors.joining("\n")));
  }

  private String stripFences(String raw) {
    return raw.replaceAll("(?s)^```\\w*\\s*", "").replaceAll("(?s)```\\s*$", "").trim();
  }
}
