package ru.nesterov.pmserver.features.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.nesterov.pmserver.features.chat.dto.ReadReceiptDto;
import ru.nesterov.pmserver.features.chat.service.ProjectChatReadService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/reads")
public class ProjectChatReadsRestController {

    private final ProjectChatReadService service;

    @GetMapping
    public List<ReadReceiptDto> list(Authentication auth, @PathVariable UUID projectId) {
        UUID userId = (UUID) auth.getPrincipal();
        return service.list(userId, projectId);
    }
}
