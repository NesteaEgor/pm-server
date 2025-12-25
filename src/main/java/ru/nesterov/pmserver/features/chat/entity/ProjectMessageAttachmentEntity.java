package ru.nesterov.pmserver.features.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_message_attachments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "file_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMessageAttachmentEntity {

    @Id
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
