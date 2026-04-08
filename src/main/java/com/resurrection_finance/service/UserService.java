package com.resurrection_finance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resurrection_finance.dto.AccountResponseDTO;
import com.resurrection_finance.dto.TransactionEvent;
import com.resurrection_finance.dto.UserRequestDTO;
import com.resurrection_finance.dto.UserResponseDTO;
import com.resurrection_finance.entity.OutboxEvent;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.enums.Role;
import com.resurrection_finance.exception.EmailAlreadyExistsException;
import com.resurrection_finance.repository.OutboxRepository;
import com.resurrection_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("The email address is already registered.");
        }
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setRole(Role.USER);
        user.setMonthlyIncome(dto.monthlyIncome());
        User savedUser = userRepository.save(user);
        AccountResponseDTO accountDTO = accountService.createAccount(savedUser);
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID().toString(),
                "REGISTER",
                savedUser.getEmail(),
                savedUser.getEmail(),
                savedUser.getExternalId(),
                savedUser.getExternalId(),
                BigDecimal.ZERO,
                "Register successful.",
                LocalDateTime.now()
        );

        try {
            OutboxEvent outbox = new OutboxEvent();
            outbox.setTopic("resurrection-notifications");
            outbox.setEventType("REGISTER");
            outbox.setAggregateId(savedUser.getExternalId().toString());
            outbox.setPayload(objectMapper.writeValueAsString(event));

            outboxRepository.save(outbox);
            log.info("✅ OUTBOX RECORD: {} prepared for notification", savedUser.getEmail());
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to translate registration data: {}", e.getMessage());
            throw new RuntimeException("Fatal error in the data bunker", e);
        }
        return mapToResponseDTO(savedUser, accountDTO);
    }

    private UserResponseDTO mapToResponseDTO(User user, AccountResponseDTO accountDto) {
        return new UserResponseDTO(
                user.getExternalId(),
                user.getName(),
                user.getEmail(),
                user.getMonthlyIncome(),
                user.getPhone(),
                user.getAddress(),
                user.isActive(),
                user.getCreatedAt(),
                user.getRole(),
                accountDto
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    AccountResponseDTO accountDTO = accountService.getAccountByUser(user);
                    return mapToResponseDTO(user, accountDTO);
                })
                .toList();
    }

    public UserResponseDTO getUserByExternalID(UUID externalId) {
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        AccountResponseDTO accountDTO = accountService.getAccountByUser(user);
        return mapToResponseDTO(user, accountDTO);
    }
    @Transactional
    public void reactivateUser(UUID externalId) {
        User user = userRepository.findByExternalId(externalId).orElseThrow();
        String ghostEmail = user.getEmail();
        if (ghostEmail.startsWith("delete_")) {
            String originalEmail = ghostEmail.substring(ghostEmail.lastIndexOf("_") + 1);
            if (userRepository.existsByEmail(originalEmail)) {
                throw new RuntimeException("Email " + originalEmail + " is already registered to another active partner.");
            }

            user.setEmail(originalEmail);
            user.setName(originalEmail.substring(0, originalEmail.indexOf("@")).toUpperCase());
        }
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsersIncludingInactive() {
        return userRepository.findAll().stream().map(user -> {
            AccountResponseDTO accountDTO = accountService.getAccountByUser(user);
            return mapToResponseDTO(user, accountDTO);}).toList();
    }

    @Transactional
    public void deleteUserByExternalID(UUID externalId) {
        User user = userRepository.findByExternalId(externalId).orElseThrow(() -> new RuntimeException("Partner not found in the bunker platform."));
        String originalEmail = user.getEmail();
        user.setEmail("delete_" + System.currentTimeMillis() + "_" + originalEmail);
        user.setName("USER_DELETED");
        user.setPhone(null);
        user.setAddress(null);
        user.setActive(false);
        userRepository.save(user);
        log.info("👻 PARTNER ANONYMIZED: Original bunker for {} has been released.", originalEmail);
    }

}
