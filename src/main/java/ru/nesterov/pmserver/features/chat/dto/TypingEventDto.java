package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TypingEventDto {
    private UUID projectId;
    private UUID userId;
    private String userName;
    private boolean typing;
    private Instant sentAt;
}
