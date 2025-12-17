package ru.nesterov.pmserver.features.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.dto.EditMessageRequest;
import ru.nesterov.pmserver.features.chat.dto.SendMessageRequest;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageRepository;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectChatService {

    private final ProjectMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService accessService;

    private void requireProject(UUID userId, UUID projectId) {
        // ✅ ВАЖНО: теперь доступ по участию/владению, а не только ownerId
        accessService.requireAccess(userId, projectId);
    }

    private ProjectMessageEntity requireMessage(UUID userId, UUID projectId, UUID messageId) {
        requireProject(userId, projectId);

        ProjectMessageEntity msg = messageRepository.findByIdAndProjectId(messageId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // редактировать/удалять может только автор
        if (!msg.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return msg;
    }

    @Transactional
    public ChatMessageDto send(UUID userId, UUID projectId, SendMessageRequest req) {
        requireProject(userId, projectId);

        final String cid = req.getClientMessageId().trim();

        // ✅ идемпотентность: если клиент повторно отправил тот же clientMessageId
        var existing = messageRepository.findByProjectIdAndAuthorIdAndClientMessageId(projectId, userId, cid);
        if (existing.isPresent()) {
            return toDto(existing.get(), "CREATED");
        }

        ProjectMessageEntity e = ProjectMessageEntity.builder()
                .projectId(projectId)
                .authorId(userId)
                .text(req.getText().trim())
                .createdAt(Instant.now())
                .clientMessageId(cid)
                .build();

        e = messageRepository.save(e);
        return toDto(e, "CREATED");
    }

    @Transactional
    public ChatMessageDto edit(UUID userId, UUID projectId, UUID messageId, EditMessageRequest req) {
        ProjectMessageEntity msg = requireMessage(userId, projectId, messageId);

        if (msg.getDeletedAt() != null) {
            throw new IllegalArgumentException("Message deleted");
        }

        msg.setText(req.getText().trim());
        msg.setEditedAt(Instant.now());

        msg = messageRepository.save(msg);
        return toDto(msg, "UPDATED");
    }

    @Transactional
    public ChatMessageDto delete(UUID userId, UUID projectId, UUID messageId) {
        ProjectMessageEntity msg = requireMessage(userId, projectId, messageId);

        if (msg.getDeletedAt() == null) {
            msg.setDeletedAt(Instant.now());
        }

        msg = messageRepository.save(msg);
        return toDto(msg, "DELETED");
    }

    public List<ChatMessageDto> history(UUID userId, UUID projectId, Instant before, int limit) {
        requireProject(userId, projectId);

        int safeLimit = Math.min(Math.max(limit, 1), 100);
        var pageable = PageRequest.of(0, safeLimit);

        List<ProjectMessageEntity> list;
        if (before == null) {
            list = messageRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable);
        } else {
            list = messageRepository.findByProjectIdAndCreatedAtLessThanOrderByCreatedAtDesc(projectId, before, pageable);
        }

        // из базы DESC, для UI удобнее ASC
        Collections.reverse(list);

        // подтягиваем имена пачкой
        Set<UUID> authorIds = list.stream().map(ProjectMessageEntity::getAuthorId).collect(Collectors.toSet());
        Map<UUID, String> names = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getDisplayName()));

        return list.stream()
                .map(e -> toDto(e, names.get(e.getAuthorId()), null))
                .toList();
    }

    private ChatMessageDto toDto(ProjectMessageEntity e, String eventType) {
        String name = userRepository.findById(e.getAuthorId())
                .map(u -> u.getDisplayName())
                .orElse("Unknown");
        return toDto(e, name, eventType);
    }

    private ChatMessageDto toDto(ProjectMessageEntity e, String authorName, String eventType) {
        boolean deleted = e.getDeletedAt() != null;

        return ChatMessageDto.builder()
                .id(e.getId())
                .projectId(e.getProjectId())
                .authorId(e.getAuthorId())
                .authorName(authorName)
                .text(deleted ? "Сообщение удалено" : e.getText())
                .createdAt(e.getCreatedAt())
                .clientMessageId(e.getClientMessageId())
                .editedAt(e.getEditedAt())
                .deletedAt(e.getDeletedAt())
                .eventType(eventType)
                .build();
    }
}
