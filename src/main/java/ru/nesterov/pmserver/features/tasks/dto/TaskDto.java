package ru.nesterov.pmserver.features.tasks.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class TaskDto {
    UUID id;
    UUID projectId;
    String title;
    String description;
    String status;
    Instant createdAt;
}
