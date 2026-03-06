package com.resurrection_finance.service;

import com.resurrection_finance.dto.AccountResponseDTO;
import com.resurrection_finance.dto.UserRequestDTO;
import com.resurrection_finance.dto.UserResponseDTO;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.exception.EmailAlreadyExistsException;
import com.resurrection_finance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountService accountService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

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
        user.setRole(dto.role());
        user.setMonthlyIncome(dto.monthlyIncome());
        User savedUser = userRepository.save(user);
        AccountResponseDTO accountDTO = accountService.createAccount(savedUser);
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
    public void deleteUserByExternalID(UUID externalId) {
        User user = userRepository.findByExternalId(externalId).orElseThrow(() -> new RuntimeException("User not found."));
        userRepository.delete(user);
    }
}
