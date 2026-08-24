package com.stockpulse.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Low-level LLM client.
 * Calls the OpenAI-compatible /v1/chat/completions endpoint.
 * Credentials are read from environment variables only — never hardcoded.
 */
@Service
@Slf4j
public class LlmClient {

    private final WebClient webClient;
    private final String model;
    private final long timeoutSeconds;
    private final ObjectMapper objectMapper;

    public LlmClient(
            @Value("${llm.base-url}") String baseUrl,
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.model}") String model,
            @Value("${llm.timeout-seconds:30}") long timeoutSeconds,
            ObjectMapper objectMapper) {
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Sends a chat completion request and returns the raw response text.
     * Throws LlmException on any error (timeout, HTTP error, parsing failure).
     */
    public String complete(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 800
        );

        try {
            String response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorMap(ex -> new LlmException("LLM request failed: " + ex.getMessage(), ex))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            log.debug("LLM raw response: {}", content);
            return content;
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }
}
