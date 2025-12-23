package ru.nesterov.pmserver.features.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.chat.dto.ReactionEventDto;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageReactionEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageReactionRepository;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageRepository;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectChatReactionService {

    private final ProjectAccessService accessService;
    private final ProjectMessageRepository messageRepository;
    private final ProjectMessageReactionRepository reactionRepository;

    private void requireMessageInProject(UUID projectId, UUID messageId) {
        messageRepository.findByIdAndProjectId(messageId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }

    @Transactional
    public ReactionEventDto toggle(UUID userId, UUID projectId, UUID messageId, String emoji) {
        accessService.requireAccess(userId, projectId);
        requireMessageInProject(projectId, messageId);

        String e = emoji.trim();
        if (e.isEmpty() || e.length() > 16) {
            throw new IllegalArgumentException("Invalid emoji");
        }

        boolean added;
        var existing = reactionRepository.findByMessageIdAndUserIdAndEmoji(messageId, userId, e);
        if (existing.isPresent()) {
            reactionRepository.delete(existing.get());
            added = false;
        } else {
            ProjectMessageReactionEntity r = ProjectMessageReactionEntity.builder()
                    .messageId(messageId)
                    .userId(userId)
                    .emoji(e)
                    .createdAt(Instant.now())
                    .build();
            reactionRepository.save(r);
            added = true;
        }

        Map<String, Integer> counts = new HashMap<>();
        for (var row : reactionRepository.countByMessageId(messageId)) {
            counts.put(row.getEmoji(), (int) row.getCnt());
        }

        return ReactionEventDto.builder()
                .projectId(projectId)
                .messageId(messageId)
                .userId(userId)
                .emoji(e)
                .added(added)
                .reactions(counts)
                .build();
    }
}
