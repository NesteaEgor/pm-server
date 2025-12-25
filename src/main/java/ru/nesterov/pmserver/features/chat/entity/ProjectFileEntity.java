package ru.nesterov.pmserver.features.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectFileEntity {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
