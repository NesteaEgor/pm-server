package ru.nesterov.pmserver.features.comments.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "task_comments")
public class TaskCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "text", nullable = false, length = 5000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
