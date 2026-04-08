package com.resurrection_finance.dto;

import com.resurrection_finance.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID externalId,
        String name,
        String email,
        BigDecimal monthlyIncome,
        String phone,
        String address,
        boolean active,
        LocalDateTime createdAt,
        Role role,
        AccountResponseDTO account
) {}
