package com.resurrection_finance.dto;

import com.resurrection_finance.enums.Role;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 20, message = "El nombre debe tener entre 2 y 20 caracteres")
        String name,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,
        String phone,
        String address,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,
        Role role,
        @NotNull(message = "El ingreso mensual es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El ingreso debe ser mayor a 0")
        BigDecimal monthlyIncome
) {}
