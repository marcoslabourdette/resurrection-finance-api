package com.resurrection_finance.service;

import com.resurrection_finance.dto.AuthRequestDTO;
import com.resurrection_finance.dto.AuthResponseDTO;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.repository.UserRepository;
import com.resurrection_finance.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User not found."));
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token);
    }
}
