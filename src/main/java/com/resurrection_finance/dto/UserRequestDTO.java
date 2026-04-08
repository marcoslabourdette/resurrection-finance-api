package com.resurrection_finance.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UserRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 20, message = "Name must be between 2 and 20 characters")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 50, message = "Email cannot exceed 50 characters")
        String email,
        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        @Pattern(regexp = "^[+]?[0-9]*$", message = "Invalid phone format")
        String phone,
        @Size(max = 100, message = "Address cannot exceed 100 characters")
        String address,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password,
        @NotNull(message = "Monthly income is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Income must be greater than 0")
        @DecimalMax(value = "99999999.99", message = "Income exceeds the bunker's security limit")
        BigDecimal monthlyIncome
) {}
