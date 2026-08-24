package com.stockpulse.dto;

import jakarta.validation.constraints.NotNull;

public record SuggestionActionRequest(
        @NotNull Action action
) {
    public enum Action { ACCEPT, REJECT }
}
