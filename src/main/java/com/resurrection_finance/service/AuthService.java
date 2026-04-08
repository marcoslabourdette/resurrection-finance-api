package com.resurrection_finance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resurrection_finance.dto.AuthRequestDTO;
import com.resurrection_finance.dto.AuthResponseDTO;
import com.resurrection_finance.dto.TransactionEvent;
import com.resurrection_finance.entity.OutboxEvent;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.repository.OutboxRepository;
import com.resurrection_finance.repository.UserRepository;
import com.resurrection_finance.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User not found in the platform."));
        String token = jwtService.generateToken(user);
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID().toString(),
                "LOGIN",
                request.email(),
                request.email(),
                user.getExternalId(),
                user.getExternalId(),
                BigDecimal.ZERO,
                "Successful authentication in the bunker.",
                LocalDateTime.now()
        );
        try {
            OutboxEvent outbox = new OutboxEvent();
            outbox.setTopic("resurrection-notifications");
            outbox.setEventType("LOGIN");
            outbox.setAggregateId(user.getExternalId().toString());
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outboxRepository.save(outbox);
            log.info("✅ LOGIN recorded in OUTBOX for user: {}", request.email());

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize LOGIN event: {}", e.getMessage());
        }
        return new AuthResponseDTO(token, user.getExternalId(), user.getRole().name(), user.getName());
    }
}
