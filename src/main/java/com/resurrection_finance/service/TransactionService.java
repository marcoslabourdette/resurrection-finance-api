package com.resurrection_finance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resurrection_finance.dto.TransactionEvent;
import com.resurrection_finance.dto.TransactionRequestDTO;
import com.resurrection_finance.dto.TransactionResponseDTO;
import com.resurrection_finance.dto.TransferRequestDTO;
import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.OutboxEvent;
import com.resurrection_finance.entity.Transaction;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.enums.TransactionType;
import com.resurrection_finance.exception.InsufficientFundsException;
import com.resurrection_finance.exception.SelfTransferException;
import com.resurrection_finance.repository.AccountRepository;
import com.resurrection_finance.repository.OutboxRepository;
import com.resurrection_finance.repository.TransactionRepository;
import com.resurrection_finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    @Transactional
    public TransactionResponseDTO transfer(UUID senderExternalId, TransactionRequestDTO transactionRequestDTO) {
        Account sender = accountRepository.findByUserExternalId(senderExternalId).orElseThrow(() -> new RuntimeException("Sender not found."));
        Account receiver = accountRepository.findByCvu(transactionRequestDTO.destinationCvu()).orElseThrow(() -> new RuntimeException("Receiver not found."));
        if (sender.getId().equals(receiver.getId())) {
            throw new SelfTransferException("🚨 Self-transfer operations are not permitted in the bunker.");
        }

        if (sender.getBalance().compareTo(transactionRequestDTO.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds to transfer.");
        }
        sender.setBalance(sender.getBalance().subtract(transactionRequestDTO.amount()));
        receiver.setBalance(receiver.getBalance().add(transactionRequestDTO.amount()));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequestDTO.amount());
        transaction.setDescription(transactionRequestDTO.description());
        transaction.setOriginAccount(sender);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDestinationAccount(receiver);
        Transaction savedTransaction = transactionRepository.save(transaction);
        TransactionEvent event = new TransactionEvent(
                savedTransaction.getTransactionId().toString(),
                "TRANSFER",
                sender.getUser().getEmail(),
                receiver.getUser().getEmail(),
                sender.getUser().getExternalId(),
                receiver.getUser().getExternalId(),
                savedTransaction.getAmount(),
                savedTransaction.getDescription(),
                savedTransaction.getTimestamp()
        );
        try {
            OutboxEvent outbox = new OutboxEvent();
            outbox.setTopic("resurrection-notifications");
            outbox.setEventType("TRANSFER");
            outbox.setAggregateId(savedTransaction.getTransactionId().toString());
            outbox.setPayload(objectMapper.writeValueAsString(event));
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("🚨 Failed to serialize the bunker event data", e);
        }
        return mapToResponseDTO(savedTransaction);
    }

    @Transactional
    public TransactionResponseDTO transferByEmail(TransferRequestDTO dto) {
        if (dto.senderEmail().equalsIgnoreCase(dto.receiverEmail())) {
            throw new RuntimeException("🚨 Self-transfers are not permitted in the bunker.");
        }
        User sender = userRepository.findByEmail(dto.senderEmail())
                .orElseThrow(() -> new RuntimeException("🚨 Sender not found in the bunker platform."));
        User receiver = userRepository.findByEmail(dto.receiverEmail())
                .orElseThrow(() -> new RuntimeException("🚨 Recipient not found in the bunker platform."));

        TransactionRequestDTO legacyDto = new TransactionRequestDTO(
                receiver.getAccount().getCvu(),
                dto.amount(),
                dto.description()
        );
        return this.transfer(sender.getExternalId(), legacyDto);
    }
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getRecentByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Partner not found in the bunker."));
        Pageable topFive = PageRequest.of(0, 5, Sort.by("timestamp").descending());
        Page<Transaction> transactions = transactionRepository.findByOriginAccountUserExternalIdOrDestinationAccountUserExternalId(user.getExternalId(), user.getExternalId(), topFive);
        return transactions.getContent().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    private TransactionResponseDTO mapToResponseDTO(Transaction savedTransaction) {
        return new TransactionResponseDTO(
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getTimestamp(),
                savedTransaction.getTransactionId(),
                savedTransaction.getDestinationAccount().getCvu(),
                savedTransaction.getDescription(),
                savedTransaction.getOriginAccount().getUser().getEmail(),
                savedTransaction.getDestinationAccount().getUser().getEmail()
        );
    }

    public Page<TransactionResponseDTO> getHistory(UUID externalId, Pageable pageable) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!user.getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You do not have permission to view this history.");
        }
        return transactionRepository.findByOriginAccountUserExternalIdOrDestinationAccountUserExternalId(
                        externalId, externalId, pageable)
                .map(this::mapToResponseDTO);
    }
}




