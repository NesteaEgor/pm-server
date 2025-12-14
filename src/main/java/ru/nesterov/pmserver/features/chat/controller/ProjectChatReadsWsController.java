package ru.nesterov.pmserver.features.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.nesterov.pmserver.features.chat.dto.ReadReceiptDto;
import ru.nesterov.pmserver.features.chat.dto.ReadUpToRequest;
import ru.nesterov.pmserver.features.chat.service.ProjectChatReadService;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProjectChatReadsWsController {

    private final ProjectChatReadService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/read")
    public void markRead(@DestinationVariable UUID projectId,
                         Principal principal,
                         @Valid @Payload ReadUpToRequest req) {

        if (!(principal instanceof Authentication auth) || auth.getPrincipal() == null) {
            throw new MessagingException("Unauthorized");
        }

        UUID userId = (UUID) auth.getPrincipal();

        ReadReceiptDto dto = service.markRead(userId, projectId, req.getMessageId());

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/reads",
                dto
        );
    }
}
