package com.resurrection_finance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "Formato de email inválido")
        String email,
        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}
