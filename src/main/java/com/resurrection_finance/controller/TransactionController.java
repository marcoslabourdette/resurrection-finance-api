package com.resurrection_finance.controller;

import com.resurrection_finance.dto.TransactionRequestDTO;
import com.resurrection_finance.dto.TransactionResponseDTO;
import com.resurrection_finance.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/{senderExternalId}")
    public ResponseEntity<TransactionResponseDTO> transfer(@PathVariable UUID senderExternalId, @Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.ok(transactionService.transfer(senderExternalId, dto));
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<Page<TransactionResponseDTO>> getHistory(@PathVariable UUID externalId, Pageable pageable) {
        return ResponseEntity.ok(transactionService.getHistory(externalId, pageable));
    }
}
