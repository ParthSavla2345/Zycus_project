package com.stockpulse.llm;

/**
 * Thrown when the LLM call fails for any reason:
 * timeout, connection error, HTTP error, malformed JSON, invalid values.
 * The caller (AiCommerceAdvisor) catches this and falls back to rules.
 */
public class LlmException extends RuntimeException {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
