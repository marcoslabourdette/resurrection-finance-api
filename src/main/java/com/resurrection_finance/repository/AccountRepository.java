package com.resurrection_finance.repository;

import com.resurrection_finance.entity.Account;
import com.resurrection_finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCvu(String cvu);

    Optional<Account> findByUser(User user);

    Optional<Account> findByUserExternalId(UUID externalId);
}

