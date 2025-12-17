package ru.nesterov.pmserver.features.projects.members.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_project_members_project_user",
                columnNames = {"project_id", "user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false, length = 32)
    private String role; // MEMBER (owner не храним тут)

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
