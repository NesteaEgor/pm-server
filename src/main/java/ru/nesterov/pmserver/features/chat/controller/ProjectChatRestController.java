package ru.nesterov.pmserver.features.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.service.ProjectChatService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/messages")
public class ProjectChatRestController {

    private final ProjectChatService service;

    @GetMapping
    public List<ChatMessageDto> history(Authentication auth,
                                        @PathVariable UUID projectId,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        Instant before,
                                        @RequestParam(defaultValue = "30") int limit) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.history(userId, projectId, before, limit);
    }
}
