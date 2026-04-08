package com.resurrection_finance.service;

import com.resurrection_finance.dto.AccountResponseDTO;
import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponseDTO createAccount(User user) {
        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("100.00"));
        account.setCvu(generateRandomCvu());
        Account savedAccount = accountRepository.save(account);
        return new AccountResponseDTO(savedAccount.getCvu(), savedAccount.getBalance());
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountByUser(User user) {
        return accountRepository.findByUser(user)
                .map(acc -> new AccountResponseDTO(acc.getCvu(), acc.getBalance()))
                .orElseThrow(() -> new RuntimeException("Account not found for user: " + user.getName()));
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountDetailsByEmail(String email) {
        Account account = accountRepository.findByUserEmail(email).orElseThrow(() -> new EntityNotFoundException("Account not found for email: " + email));
        return new AccountResponseDTO(account.getCvu(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public String getOwnerNameByCvu(String cvu) {
        return accountRepository.findByCvu(cvu).map(account -> account.getUser().getName()).orElseThrow(() -> new EntityNotFoundException("CVU not found on the track."));
    }

    private String generateRandomCvu() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 22; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
