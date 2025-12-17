package ru.nesterov.pmserver.features.projects.members.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProjectMemberDto {
    private UUID userId;
    private String email;
    private String displayName;
    private String role;   // OWNER / MEMBER
    private Instant addedAt;
}
