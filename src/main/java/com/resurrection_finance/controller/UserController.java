package com.resurrection_finance.controller;


import com.resurrection_finance.dto.UserRequestDTO;
import com.resurrection_finance.dto.UserResponseDTO;
import com.resurrection_finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:8081", methods = {RequestMethod.DELETE, RequestMethod.OPTIONS})
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

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllForAdmin() {
        return ResponseEntity.ok(userService.getAllUsersIncludingInactive());
    }

    @PutMapping("/admin/reactivate/{externalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivate(@PathVariable UUID externalId) {
        userService.reactivateUser(externalId);
        return ResponseEntity.ok().build();
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
