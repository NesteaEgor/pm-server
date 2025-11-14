package ru.nesterov.pmserver.features.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nesterov.pmserver.features.auth.dto.*;
import ru.nesterov.pmserver.features.auth.security.JwtService;
import ru.nesterov.pmserver.features.users.entity.UserEntity;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .displayName(req.getDisplayName().trim())
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toDto(user));
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toDto(user));
    }

    public static UserDto toDto(UserEntity user) {
        return new UserDto(user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl());
    }
}
