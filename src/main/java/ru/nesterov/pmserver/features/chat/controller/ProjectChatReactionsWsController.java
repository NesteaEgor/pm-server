package ru.nesterov.pmserver.features.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.nesterov.pmserver.features.chat.dto.ReactionEventDto;
import ru.nesterov.pmserver.features.chat.dto.ReactionToggleRequest;
import ru.nesterov.pmserver.features.chat.service.ProjectChatReactionService;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProjectChatReactionsWsController {

    private final ProjectChatReactionService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/projects/{projectId}/messages/{messageId}/reactions/toggle")
    public void toggle(@DestinationVariable UUID projectId,
                       @DestinationVariable UUID messageId,
                       Principal principal,
                       @Valid @Payload ReactionToggleRequest req) {

        UUID userId = requireUser(principal);
        ReactionEventDto dto = service.toggle(userId, projectId, messageId, req.getEmoji());

        messagingTemplate.convertAndSend(
                "/topic/projects/" + projectId + "/reactions",
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
