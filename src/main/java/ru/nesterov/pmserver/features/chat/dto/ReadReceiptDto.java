package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReadReceiptDto {
    private UUID projectId;
    private UUID userId;

    private UUID lastReadMessageId;
    private Instant lastReadMessageAt;

    private Instant lastReadAt;
}
