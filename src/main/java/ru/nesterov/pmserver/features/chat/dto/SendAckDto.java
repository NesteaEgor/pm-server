package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SendAckDto {
    private String clientMessageId;
    private UUID serverMessageId;
    private Instant createdAt;
}
