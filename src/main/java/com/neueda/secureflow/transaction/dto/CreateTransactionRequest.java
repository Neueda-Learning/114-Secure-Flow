package com.neueda.secureflow.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "\\s*[A-Za-z0-9-]+\\s*",
                message = "must use letters, numbers, or hyphens")
        String accountId,
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "\\s*[A-Za-z0-9-]+\\s*",
                message = "must use letters, numbers, or hyphens")
        String payeeId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @Size(max = 255) String description
) {}
