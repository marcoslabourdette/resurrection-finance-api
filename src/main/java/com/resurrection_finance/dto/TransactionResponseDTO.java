package com.resurrection_finance.dto;

import com.resurrection_finance.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
        BigDecimal amount,
        TransactionType type,
        LocalDateTime timestamp,
        UUID externalId,
        String destinationCvu
) {
}


