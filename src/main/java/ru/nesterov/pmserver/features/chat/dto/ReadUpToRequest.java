package ru.nesterov.pmserver.features.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReadUpToRequest {
    @NotNull
    private UUID messageId;
}
