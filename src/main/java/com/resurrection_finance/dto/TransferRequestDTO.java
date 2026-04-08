package com.resurrection_finance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransferRequestDTO(
        String senderEmail,
        @NotBlank(message = "Recipient email is required")
        @Email(message = "Invalid email format")
        @Size(max = 50, message = "Recipient email cannot exceed 50 characters")
        String receiverEmail,
        @NotNull(message = "Transfer amount is required")
        @DecimalMin(value = "0.01", message = "Minimum transfer amount is 0.01")
        @DecimalMax(value = "99999999.99", message = "Transfer exceeds the bunker's security limit")
        BigDecimal amount,
        @Size(max = 30, message = "Description cannot exceed 30 characters")
        String description
) {
}