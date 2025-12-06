package ru.nesterov.pmserver.features.comments.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CommentDto {
    private UUID id;
    private UUID taskId;
    private UUID authorId;
    private String text;
    private Instant createdAt;
}
