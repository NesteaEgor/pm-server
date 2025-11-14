package ru.nesterov.pmserver.features.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.auth.dto.UserDto;
import ru.nesterov.pmserver.features.auth.service.AuthService;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        var user = userRepository.findById(userId).orElseThrow();
        return AuthService.toDto(user);
    }
}
