package ru.nesterov.pmserver.features.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.dto.EditMessageRequest;
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

        UUID userId = requireUser(principal);
        ChatMessageDto msg = service.send(userId, projectId, req);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/messages",
                msg
        );
    }

    @MessageMapping("/projects/{projectId}/messages/{messageId}/edit")
    public void edit(@DestinationVariable UUID projectId,
                     @DestinationVariable UUID messageId,
                     Principal principal,
                     @Valid @Payload EditMessageRequest req) {

        UUID userId = requireUser(principal);
        ChatMessageDto msg = service.edit(userId, projectId, messageId, req);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/messages",
                msg
        );
    }

    @MessageMapping("/projects/{projectId}/messages/{messageId}/delete")
    public void delete(@DestinationVariable UUID projectId,
                       @DestinationVariable UUID messageId,
                       Principal principal) {

        UUID userId = requireUser(principal);
        ChatMessageDto msg = service.delete(userId, projectId, messageId);

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/messages",
                msg
        );
    }

    private UUID requireUser(Principal principal) {
        if (!(principal instanceof Authentication auth) || auth.getPrincipal() == null) {
            throw new MessagingException("Unauthorized");
        }
        return (UUID) auth.getPrincipal();
    }
}
