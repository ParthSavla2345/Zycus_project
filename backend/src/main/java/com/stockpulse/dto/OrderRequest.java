package com.stockpulse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull @Min(1) Integer quantity
) {}
