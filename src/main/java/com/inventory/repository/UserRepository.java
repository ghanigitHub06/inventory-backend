package com.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.entity.User;

// JpaRepository<Entity, PrimaryKeyType>
// Gives you: save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data parses this method name → SELECT * FROM users WHERE email = ?
    // Returns Optional so caller must handle the "not found" case explicitly
    Optional<User> findByEmail(String email);

    // Used to check duplicates during registration
    boolean existsByEmail(String email);
}