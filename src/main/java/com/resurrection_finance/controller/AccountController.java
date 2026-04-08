package com.resurrection_finance.controller;

import com.resurrection_finance.dto.AccountResponseDTO;
import com.resurrection_finance.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/my-balance")
    public ResponseEntity<AccountResponseDTO> getMyBalance(Principal principal) {
        AccountResponseDTO response = accountService.getAccountDetailsByEmail(principal.getName());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/owner/{cvu}")
    public ResponseEntity<String> getOwnerName(@PathVariable String cvu) {
        String ownerName = accountService.getOwnerNameByCvu(cvu);
        return ResponseEntity.ok(ownerName);
    }
}