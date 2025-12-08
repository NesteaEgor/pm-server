package ru.nesterov.pmserver.features.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.dto.SendMessageRequest;
import ru.nesterov.pmserver.features.chat.service.ProjectChatService;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProjectChatWsController {

    private final ProjectChatService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/messages")
    public void send(@DestinationVariable UUID projectId,
                     Principal principal,
                     @Valid @Payload SendMessageRequest req) {

        if (!(principal instanceof Authentication auth) || auth.getPrincipal() == null) {
            throw new MessagingException("Unauthorized");
        }

        UUID userId = (UUID) auth.getPrincipal();

        ChatMessageDto msg = service.send(userId, projectId, req);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/messages",
                msg
        );
    }
}
