package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class ChatMessageDto {
    private UUID id;
    private UUID projectId;
    private UUID authorId;
    private String authorName;
    private String text;
    private Instant createdAt;

    private String clientMessageId;

    private Instant editedAt;
    private Instant deletedAt;

    private String eventType;

    private Map<String, Integer> reactions;
    private Set<String> myReactions;

    private List<ChatAttachmentDto> attachments;
}
