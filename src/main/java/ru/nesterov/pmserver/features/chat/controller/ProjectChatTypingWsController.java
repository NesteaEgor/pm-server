package ru.nesterov.pmserver.features.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.nesterov.pmserver.features.chat.dto.TypingEventDto;
import ru.nesterov.pmserver.features.chat.dto.TypingRequest;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProjectChatTypingWsController {

    private final ProjectAccessService accessService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/typing")
    public void typing(@DestinationVariable UUID projectId,
                       Principal principal,
                       @Valid @Payload TypingRequest req) {

        UUID userId = requireUser(principal);
        accessService.requireAccess(userId, projectId);

        String name = userRepository.findById(userId)
                .map(u -> u.getDisplayName())
                .orElse("Unknown");

        TypingEventDto dto = TypingEventDto.builder()
                .projectId(projectId)
                .userId(userId)
                .userName(name)
                .typing(req.isTyping())
                .sentAt(Instant.now())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/typing",
                dto
        );
    }

    private UUID requireUser(Principal principal) {
        if (!(principal instanceof Authentication auth) || auth.getPrincipal() == null) {
            throw new MessagingException("Unauthorized");
        }
        return (UUID) auth.getPrincipal();
    }
}
