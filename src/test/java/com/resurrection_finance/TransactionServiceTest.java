package com.resurrection_finance;

import com.resurrection_finance.dto.TransactionRequestDTO;
import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.Transaction;
import com.resurrection_finance.exception.InsufficientFundsException;
import com.resurrection_finance.repository.AccountRepository;
import com.resurrection_finance.repository.TransactionRepository;
import com.resurrection_finance.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testTransferSuccess() {
        Account origin = new Account();
        origin.setBalance(new BigDecimal("100.00"));

        Account destination = new Account();
        destination.setBalance(new BigDecimal("50.00"));
        destination.setCvu("1234567890123456789012");

        TransactionRequestDTO request = new TransactionRequestDTO(
                destination.getCvu(),
                new BigDecimal("30.00")
        );

        when(accountRepository.findByUserExternalId(any())).thenReturn(Optional.of(origin));
        when(accountRepository.findByCvu(any())).thenReturn(Optional.of(destination));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        transactionService.transfer(UUID.randomUUID(), request);

        assertEquals(new BigDecimal("70.00"), origin.getBalance());
        assertEquals(new BigDecimal("80.00"), destination.getBalance());
    }
    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        Account origin = new Account();
        origin.setBalance(new BigDecimal("10.00"));

        Account destination = new Account();
        destination.setCvu("1234567890123456789012");

        TransactionRequestDTO request = new TransactionRequestDTO(
                destination.getCvu(),
                new BigDecimal("30.00")
        );

        when(accountRepository.findByUserExternalId(any())).thenReturn(Optional.of(origin));
        when(accountRepository.findByCvu(any())).thenReturn(Optional.of(destination));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.transfer(UUID.randomUUID(), request)
        );
    }
}


