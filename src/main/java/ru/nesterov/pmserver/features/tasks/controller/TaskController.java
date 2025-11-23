package ru.nesterov.pmserver.features.tasks.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.tasks.dto.CreateTaskRequest;
import ru.nesterov.pmserver.features.tasks.dto.TaskDto;
import ru.nesterov.pmserver.features.tasks.service.TaskService;
import ru.nesterov.pmserver.features.tasks.dto.UpdateTaskRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskDto create(Authentication auth, @PathVariable UUID projectId, @Valid @RequestBody CreateTaskRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return taskService.create(userId, projectId, req);
    }

    @GetMapping
    public List<TaskDto> list(Authentication auth,
                              @PathVariable UUID projectId,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String sort) {
        UUID userId = (UUID) auth.getPrincipal();
        return taskService.list(userId, projectId, status, sort);
    }

    @PatchMapping("/{taskId}")
    public TaskDto update(Authentication auth,
                          @PathVariable UUID projectId,
                          @PathVariable UUID taskId,
                          @Valid @RequestBody UpdateTaskRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return taskService.update(userId, projectId, taskId, req);
    }
}
