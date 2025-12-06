package ru.nesterov.pmserver.features.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.comments.dto.CommentDto;
import ru.nesterov.pmserver.features.comments.dto.CreateCommentRequest;
import ru.nesterov.pmserver.features.comments.entity.TaskCommentEntity;
import ru.nesterov.pmserver.features.comments.repository.TaskCommentRepository;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;
import ru.nesterov.pmserver.features.tasks.repository.TaskRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final ProjectRepository projectRepository;

    private TaskEntity requireTask(UUID ownerId, UUID projectId, UUID taskId) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    public List<CommentDto> list(UUID userId, UUID projectId, UUID taskId) {
        requireTask(userId, projectId, taskId);
        return commentRepository.findAllByTask_IdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CommentDto create(UUID userId, UUID projectId, UUID taskId, CreateCommentRequest req) {
        TaskEntity task = requireTask(userId, projectId, taskId);

        TaskCommentEntity e = TaskCommentEntity.builder()
                .task(task)
                .authorId(userId)
                .text(req.getText().trim())
                .createdAt(Instant.now())
                .build();

        e = commentRepository.save(e);
        return toDto(e);
    }

    @Transactional
    public void delete(UUID userId, UUID projectId, UUID taskId, UUID commentId) {
        requireTask(userId, projectId, taskId);

        long deleted = commentRepository.deleteByIdAndTask_IdAndAuthorId(commentId, taskId, userId);
        if (deleted == 0) {
            // либо коммента нет, либо он не твой
            throw new IllegalArgumentException("Comment not found or not yours");
        }
    }

    private CommentDto toDto(TaskCommentEntity e) {
        return CommentDto.builder()
                .id(e.getId())
                .taskId(e.getTask().getId())
                .authorId(e.getAuthorId())
                .text(e.getText())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
