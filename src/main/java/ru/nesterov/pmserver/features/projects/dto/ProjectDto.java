package ru.nesterov.pmserver.features.projects.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class ProjectDto {
    UUID id;
    String name;
    String description;
    Instant createdAt;
}
