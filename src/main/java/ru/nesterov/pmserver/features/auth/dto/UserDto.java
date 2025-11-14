package ru.nesterov.pmserver.features.auth.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class UserDto {
    UUID id;
    String email;
    String displayName;
    String avatarUrl;
}
