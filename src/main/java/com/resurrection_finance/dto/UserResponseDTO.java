package com.resurrection_finance.dto;

import com.resurrection_finance.enums.Role;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponseDTO(
        UUID externalId,
        String name,
        String email,
        BigDecimal monthlyIncome,
        String phone,
        String address,
        Role role,
        AccountResponseDTO account
) {}
