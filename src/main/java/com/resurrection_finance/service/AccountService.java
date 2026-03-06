package com.resurrection_finance.service;

import com.resurrection_finance.dto.AccountResponseDTO;
import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.User;
import com.resurrection_finance.repository.AccountRepository;
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
        // Con bono de 100$ :D
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

    private String generateRandomCvu() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 22; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
