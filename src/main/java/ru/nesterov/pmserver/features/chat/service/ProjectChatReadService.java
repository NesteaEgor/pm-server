package ru.nesterov.pmserver.features.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.chat.dto.ReadReceiptDto;
import ru.nesterov.pmserver.features.chat.entity.ProjectChatReadEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectChatReadRepository;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageRepository;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectChatReadService {

    private final ProjectChatReadRepository readRepository;
    private final ProjectMessageRepository messageRepository;
    private final ProjectAccessService accessService;

    private void requireProject(UUID userId, UUID projectId) {
        // ✅ доступ: owner ИЛИ участник
        accessService.requireAccess(userId, projectId);
    }

    private ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity requireMessageInProject(UUID projectId, UUID messageId) {
        return messageRepository.findByIdAndProjectId(messageId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }

    @Transactional
    public ReadReceiptDto markRead(UUID userId, UUID projectId, UUID messageId) {
        requireProject(userId, projectId);

        var msg = requireMessageInProject(projectId, messageId);
        var now = Instant.now();

        var entity = readRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseGet(() -> ProjectChatReadEntity.builder()
                        .projectId(projectId)
                        .userId(userId)
                        .lastReadMessageId(messageId)
                        .lastReadAt(now)
                        .updatedAt(now)
                        .build()
                );

        entity.setLastReadMessageId(messageId);
        entity.setLastReadAt(now);
        entity.setUpdatedAt(now);

        entity = readRepository.save(entity);

        return ReadReceiptDto.builder()
                .projectId(entity.getProjectId())
                .userId(entity.getUserId())
                .lastReadMessageId(entity.getLastReadMessageId())
                .lastReadMessageAt(msg.getCreatedAt())
                .lastReadAt(entity.getLastReadAt())
                .build();
    }

    public List<ReadReceiptDto> list(UUID userId, UUID projectId) {
        requireProject(userId, projectId);

        var receipts = readRepository.findAllByProjectId(projectId);

        Set<UUID> messageIds = receipts.stream()
                .map(ProjectChatReadEntity::getLastReadMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Instant> createdAtById = messageIds.isEmpty()
                ? Map.of()
                : messageRepository.findAllById(messageIds).stream()
                .filter(m -> m.getProjectId().equals(projectId))
                .collect(Collectors.toMap(
                        ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity::getId,
                        ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity::getCreatedAt,
                        (a, b) -> a
                ));

        return receipts.stream()
                .map(e -> ReadReceiptDto.builder()
                        .projectId(e.getProjectId())
                        .userId(e.getUserId())
                        .lastReadMessageId(e.getLastReadMessageId())
                        .lastReadMessageAt(
                                e.getLastReadMessageId() == null ? null : createdAtById.get(e.getLastReadMessageId())
                        )
                        .lastReadAt(e.getLastReadAt())
                        .build())
                .toList();
    }
}
