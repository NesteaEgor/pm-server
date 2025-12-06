package ru.nesterov.pmserver.features.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.comments.dto.CommentDto;
import ru.nesterov.pmserver.features.comments.dto.CreateCommentRequest;
import ru.nesterov.pmserver.features.comments.service.TaskCommentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentService service;

    @GetMapping
    public List<CommentDto> list(Authentication auth,
                                 @PathVariable UUID projectId,
                                 @PathVariable UUID taskId) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.list(userId, projectId, taskId);
    }

    @PostMapping
    public CommentDto create(Authentication auth,
                             @PathVariable UUID projectId,
                             @PathVariable UUID taskId,
                             @Valid @RequestBody CreateCommentRequest req) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.create(userId, projectId, taskId, req);
    }

    @DeleteMapping("/{commentId}")
    public void delete(Authentication auth,
                       @PathVariable UUID projectId,
                       @PathVariable UUID taskId,
                       @PathVariable UUID commentId) {
        UUID userId = (UUID) auth.getPrincipal();
        service.delete(userId, projectId, taskId, commentId);
    }
}
