package ru.nesterov.pmserver.features.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_chat_reads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectChatReadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
