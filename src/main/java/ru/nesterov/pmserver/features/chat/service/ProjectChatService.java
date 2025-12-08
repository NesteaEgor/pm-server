package ru.nesterov.pmserver.features.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.dto.SendMessageRequest;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageRepository;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectChatService {

    private final ProjectMessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private void requireProject(UUID ownerId, UUID projectId) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @Transactional
    public ChatMessageDto send(UUID userId, UUID projectId, SendMessageRequest req) {
        requireProject(userId, projectId);

        ProjectMessageEntity e = ProjectMessageEntity.builder()
                .projectId(projectId)
                .authorId(userId)
                .text(req.getText().trim())
                .createdAt(Instant.now())
                .build();

        e = messageRepository.save(e);
        return toDto(e);
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

        // У нас из базы идёт DESC, а в UI удобнее ASC
        Collections.reverse(list);

        // Подтягиваем displayName пачкой
        Set<UUID> authorIds = list.stream().map(ProjectMessageEntity::getAuthorId).collect(Collectors.toSet());
        Map<UUID, String> names = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getDisplayName()));

        return list.stream().map(e -> toDto(e, names.get(e.getAuthorId()))).toList();
    }

    private ChatMessageDto toDto(ProjectMessageEntity e) {
        String name = userRepository.findById(e.getAuthorId())
                .map(u -> u.getDisplayName())
                .orElse("Unknown");
        return toDto(e, name);
    }

    private ChatMessageDto toDto(ProjectMessageEntity e, String authorName) {
        return ChatMessageDto.builder()
                .id(e.getId())
                .projectId(e.getProjectId())
                .authorId(e.getAuthorId())
                .authorName(authorName)
                .text(e.getText())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
