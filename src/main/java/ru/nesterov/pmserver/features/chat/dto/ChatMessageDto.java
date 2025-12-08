package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ChatMessageDto {
    private UUID id;
    private UUID projectId;
    private UUID authorId;
    private String authorName; // удобно для UI
    private String text;
    private Instant createdAt;
}
