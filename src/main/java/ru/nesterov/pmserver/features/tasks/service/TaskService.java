package ru.nesterov.pmserver.features.tasks.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;
import ru.nesterov.pmserver.features.tasks.dto.CreateTaskRequest;
import ru.nesterov.pmserver.features.tasks.dto.TaskDto;
import ru.nesterov.pmserver.features.tasks.dto.UpdateTaskRequest;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;
import ru.nesterov.pmserver.features.tasks.repository.TaskRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectAccessService accessService;

    public TaskDto create(UUID userId, UUID projectId, CreateTaskRequest req) {
        accessService.requireAccess(userId, projectId);

        UUID assigneeId = req.getAssigneeId();
        if (assigneeId != null && !accessService.isUserInProject(assigneeId, projectId)) {
            throw new IllegalArgumentException("Assignee not in project");
        }

        TaskEntity task = TaskEntity.builder()
                .projectId(projectId)
                .creatorId(userId)
                .assigneeId(assigneeId)
                .title(req.getTitle().trim())
                .description(req.getDescription() == null ? null : req.getDescription().trim())
                .status("TODO")
                .createdAt(Instant.now())
                .deadline(req.getDeadline())
                .build();

        task = taskRepository.save(task);
        return toDto(task);
    }

    public List<TaskDto> list(UUID userId, UUID projectId, String status, String sort) {
        accessService.requireAccess(userId, projectId);

        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            normalizedStatus = status.trim().toUpperCase();
            if (!normalizedStatus.equals("TODO") && !normalizedStatus.equals("IN_PROGRESS") && !normalizedStatus.equals("DONE")) {
                throw new IllegalArgumentException("Invalid status");
            }
        }

        String normalizedSort = (sort == null || sort.isBlank())
                ? "createdat"
                : sort.trim().toLowerCase();

        if (!normalizedSort.equals("createdat") && !normalizedSort.equals("deadline")) {
            throw new IllegalArgumentException("Invalid sort");
        }

        List<TaskEntity> tasks;

        if (normalizedSort.equals("deadline")) {
            if (normalizedStatus == null) {
                tasks = taskRepository.findByProjectIdOrderByDeadlineAscNullsLast(projectId);
            } else {
                tasks = taskRepository.findByProjectIdAndStatusOrderByDeadlineAscNullsLast(projectId, normalizedStatus);
            }
        } else {
            if (normalizedStatus == null) {
                tasks = taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
            } else {
                tasks = taskRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, normalizedStatus);
            }
        }

        return tasks.stream().map(TaskService::toDto).toList();
    }

    public TaskDto update(UUID userId, UUID projectId, UUID taskId, UpdateTaskRequest req) {
        accessService.requireAccess(userId, projectId);

        TaskEntity task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        boolean isOwner = accessService.isOwner(userId, projectId);
        boolean isCreator = userId.equals(task.getCreatorId());
        boolean isAssignee = task.getAssigneeId() != null && userId.equals(task.getAssigneeId());

        if (!isOwner && !isCreator && !isAssignee) {
            throw new IllegalArgumentException("Access denied");
        }

        // исполнитель (если он не owner и не creator) может менять ТОЛЬКО статус
        boolean assigneeLimited = isAssignee && !isOwner && !isCreator;

        if (assigneeLimited) {
            boolean triesToChangeOther =
                    req.getTitle() != null
                            || req.getDescription() != null
                            || req.getDeadline() != null
                            || req.getAssigneeId() != null;

            if (triesToChangeOther) {
                throw new IllegalArgumentException("Access denied");
            }

            if (req.getStatus() == null) {
                throw new IllegalArgumentException("Nothing to update");
            }

            task.setStatus(normalizeStatus(req.getStatus()));
            task = taskRepository.save(task);
            return toDto(task);
        }

        // owner или creator: могут менять всё
        if (req.getTitle() != null) task.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) task.setDescription(req.getDescription().trim());
        if (req.getDeadline() != null) task.setDeadline(req.getDeadline());

        if (req.getAssigneeId() != null) {
            UUID a = req.getAssigneeId();
            if (!accessService.isUserInProject(a, projectId)) {
                throw new IllegalArgumentException("Assignee not in project");
            }
            task.setAssigneeId(a);
        }

        if (req.getStatus() != null) {
            task.setStatus(normalizeStatus(req.getStatus()));
        }

        task = taskRepository.save(task);
        return toDto(task);
    }

    @Transactional
    public void delete(UUID userId, UUID projectId, UUID taskId) {
        accessService.requireAccess(userId, projectId);

        TaskEntity task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        boolean isOwner = accessService.isOwner(userId, projectId);
        boolean isCreator = userId.equals(task.getCreatorId());

        if (!isOwner && !isCreator) {
            throw new IllegalArgumentException("Access denied");
        }

        taskRepository.delete(task);
    }

    private String normalizeStatus(String raw) {
        String s = raw.trim().toUpperCase();
        if (!s.equals("TODO") && !s.equals("IN_PROGRESS") && !s.equals("DONE")) {
            throw new IllegalArgumentException("Invalid status");
        }
        return s;
    }

    public static TaskDto toDto(TaskEntity t) {
        return new TaskDto(
                t.getId(),
                t.getProjectId(),
                t.getCreatorId(),
                t.getAssigneeId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getDeadline()
        );
    }
}
