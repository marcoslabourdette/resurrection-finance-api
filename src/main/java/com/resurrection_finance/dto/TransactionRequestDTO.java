package com.resurrection_finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequestDTO(
        @NotBlank(message = "El CVU de destino es obligatorio")
        @Size(min = 22, max = 22, message = "El CVU debe tener exactamente 22 dígitos")
        String destinationCvu,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto mínimo a transferir es 0.01")
        BigDecimal amount
) {
}
