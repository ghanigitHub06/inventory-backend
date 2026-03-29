package com.inventory.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.dto.request.LoginRequest;
import com.inventory.dto.request.RegisterRequest;
import com.inventory.dto.response.AuthResponse;
import com.inventory.entity.User;
import com.inventory.enums.Role;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final JwtUtil            jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException(
                "Email already registered: " + req.getEmail());
        }

        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(Role.CUSTOMER)
            .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .role(user.getRole().name())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }

    public AuthResponse login(LoginRequest req) {
        // Load user first — fail with clear message if not found
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No user found with email: " + req.getEmail()));

        // Manually verify the password using BCrypt
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new org.springframework.security.authentication
                .BadCredentialsException("Invalid email or password");
        }

        // Authenticate to set the security context
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                req.getEmail(), req.getPassword())
        );

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
            .token(token)
            .role(user.getRole().name())
            .name(user.getName())
            .email(user.getEmail())
            .build();
    }
}