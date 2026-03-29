package com.inventory.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.inventory.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NOT NULL enforced at DB level too
    @Column(nullable = false, length = 100)
    private String name;

    // unique = true creates a unique index on email column
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Never store plain text — this holds the BCrypt hash
    @Column(nullable = false)
    private String password;

    // EnumType.STRING stores "ADMIN" / "CUSTOMER" as text
    // EnumType.ORDINAL stores 0 / 1 — avoid it, breaks if enum order changes
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Hibernate auto-sets this on INSERT, never touches it again
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ── UserDetails interface ────────────────────────────────────────────────
    // Spring Security calls getAuthorities() to check roles
    // "ROLE_" prefix is required — Spring's hasRole("ADMIN") looks for "ROLE_ADMIN"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Spring Security uses email as the username for this app
    @Override
    public String getUsername() { return email; }

    // Return false here to disable accounts — useful for bans/suspensions
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}