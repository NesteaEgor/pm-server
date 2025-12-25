package ru.nesterov.pmserver.features.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SendMessageRequest {

    @Size(max = 5000)
    private String text;

    @NotBlank
    private String clientMessageId;

    private List<UUID> attachmentIds;
}
