package com.resurrection_finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionEvent(
        String id,
        String type,
        String sender,
        String receiver,
        UUID senderExternalId,
        UUID receiverExternalId,
        BigDecimal amount,
        String description,
        LocalDateTime timestamp) {
}

