package ru.nesterov.pmserver.features.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 6, max = 72)
    private String password;

    @NotBlank @Size(min = 2, max = 64)
    private String displayName;
}
