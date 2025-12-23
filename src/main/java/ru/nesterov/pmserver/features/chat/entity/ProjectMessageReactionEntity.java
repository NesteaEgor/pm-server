package ru.nesterov.pmserver.features.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_message_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id", "emoji"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMessageReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "emoji", nullable = false, length = 16)
    private String emoji;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
