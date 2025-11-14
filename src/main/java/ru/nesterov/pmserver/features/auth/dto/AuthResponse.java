package ru.nesterov.pmserver.features.auth.dto;

import lombok.Value;

@Value
public class AuthResponse {
    String accessToken;
    UserDto user;
}
