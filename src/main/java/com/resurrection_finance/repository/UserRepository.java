package com.resurrection_finance.repository;

import com.resurrection_finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByExternalId(UUID externalId);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
