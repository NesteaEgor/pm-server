package ru.nesterov.pmserver.features.tasks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;
import ru.nesterov.pmserver.features.tasks.dto.CreateTaskRequest;
import ru.nesterov.pmserver.features.tasks.dto.TaskDto;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;
import ru.nesterov.pmserver.features.tasks.repository.TaskRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskDto create(UUID ownerId, UUID projectId, CreateTaskRequest req) {
        // важно: проверяем, что проект принадлежит текущему юзеру
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        TaskEntity task = TaskEntity.builder()
                .projectId(projectId)
                .title(req.getTitle().trim())
                .description(req.getDescription() == null ? null : req.getDescription().trim())
                .status("TODO")
                .createdAt(Instant.now())
                .build();

        task = taskRepository.save(task);
        return toDto(task);
    }

    public List<TaskDto> list(UUID ownerId, UUID projectId) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(TaskService::toDto)
                .toList();
    }

    public static TaskDto toDto(TaskEntity t) {
        return new TaskDto(t.getId(), t.getProjectId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getCreatedAt());
    }
}
