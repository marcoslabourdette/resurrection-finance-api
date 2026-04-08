package com.resurrection_finance.dto;

import java.util.UUID;

public record AuthResponseDTO(String token, UUID externalId, String role, String name) {
}
