package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ReactionEventDto {
    private UUID projectId;
    private UUID messageId;
    private UUID userId;
    private String emoji;
    private boolean added;
    private Map<String, Integer> reactions;
}
