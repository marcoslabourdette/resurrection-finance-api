package com.resurrection_finance.service;

import com.resurrection_finance.dto.TransactionRequestDTO;
import com.resurrection_finance.dto.TransactionResponseDTO;
import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.Transaction;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.enums.TransactionType;
import com.resurrection_finance.exception.InsufficientFundsException;
import com.resurrection_finance.repository.AccountRepository;
import com.resurrection_finance.repository.TransactionRepository;
import com.resurrection_finance.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponseDTO transfer(UUID senderExternalId, TransactionRequestDTO transactionRequestDTO) {
        Account sender = accountRepository.findByUserExternalId(senderExternalId).orElseThrow(() -> new RuntimeException("Sender not found."));
        Account receiver = accountRepository.findByCvu(transactionRequestDTO.destinationCvu()).orElseThrow(() -> new RuntimeException("Receiver not found."));
        if (sender.getBalance().compareTo(transactionRequestDTO.amount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds to transfer.");
        }
        sender.setBalance(sender.getBalance().subtract(transactionRequestDTO.amount()));
        receiver.setBalance(receiver.getBalance().add(transactionRequestDTO.amount()));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequestDTO.amount());
        transaction.setOriginAccount(sender);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDestinationAccount(receiver);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToResponseDTO(savedTransaction);
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction savedTransaction) {
        return new TransactionResponseDTO(
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getTimestamp(),
                savedTransaction.getTransactionId(),
                savedTransaction.getDestinationAccount().getCvu()
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




