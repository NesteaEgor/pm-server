package ru.nesterov.pmserver.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank
    @Size(max = 5000)
    private String text;
}
