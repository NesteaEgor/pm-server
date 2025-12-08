package ru.nesterov.pmserver.features.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "text", nullable = false, length = 5000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
