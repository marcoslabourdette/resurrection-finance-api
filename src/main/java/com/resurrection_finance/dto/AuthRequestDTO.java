package com.resurrection_finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 50, message = "Email cannot exceed 50 characters")
        String email,
        @NotBlank(message = "Password is required")
        @Size(max = 100, message = "Password too long")
        String password
) {
}
