package ru.nesterov.pmserver.features.tasks.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class TaskDto {
    UUID id;
    UUID projectId;
    UUID creatorId;
    UUID assigneeId;
    String title;
    String description;
    String status;
    Instant createdAt;
    Instant deadline;
}
