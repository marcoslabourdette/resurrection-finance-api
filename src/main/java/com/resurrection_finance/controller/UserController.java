package com.resurrection_finance.controller;


import com.resurrection_finance.dto.UserRequestDTO;
import com.resurrection_finance.dto.UserResponseDTO;
import com.resurrection_finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO response = userService.createUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<UserResponseDTO> getUserByExternalID(@PathVariable UUID externalId) {
        UserResponseDTO response = userService.getUserByExternalID(externalId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deleteUserByExternalId(@PathVariable UUID externalId) {
        userService.deleteUserByExternalID(externalId);
        return ResponseEntity.noContent().build();
    }
}
