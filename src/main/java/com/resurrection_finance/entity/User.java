package com.resurrection_finance.entity;

import com.resurrection_finance.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 2, max = 20)
    private String name;
    private String phone;
    private String address;
    @Email
    @NotBlank
    private String email;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyIncome;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(unique = true, nullable = false, updatable = false)
    private UUID externalId = UUID.randomUUID();
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Account account;

    @PrePersist
    protected void onCreate() {
        this.externalId = UUID.randomUUID();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}


