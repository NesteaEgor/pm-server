package ru.nesterov.pmserver.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReactionToggleRequest {

    @NotBlank
    @Size(max = 16)
    private String emoji;
}
