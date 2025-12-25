package ru.nesterov.pmserver.features.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.chat.dto.ChatAttachmentDto;
import ru.nesterov.pmserver.features.chat.dto.ChatMessageDto;
import ru.nesterov.pmserver.features.chat.dto.EditMessageRequest;
import ru.nesterov.pmserver.features.chat.dto.SendMessageRequest;
import ru.nesterov.pmserver.features.chat.entity.ProjectFileEntity;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageAttachmentEntity;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectFileRepository;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageAttachmentRepository;
import ru.nesterov.pmserver.features.chat.repository.ProjectMessageReactionRepository;
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
    private final ProjectMessageReactionRepository reactionRepository;
    private final ProjectFileRepository fileRepository;
    private final ProjectMessageAttachmentRepository attachmentRepository;

    private final UserRepository userRepository;
    private final ProjectAccessService accessService;

    private void requireProject(UUID userId, UUID projectId) {
        accessService.requireAccess(userId, projectId);
    }

    private ProjectMessageEntity requireMessage(UUID userId, UUID projectId, UUID messageId) {
        requireProject(userId, projectId);

        ProjectMessageEntity msg = messageRepository.findByIdAndProjectId(messageId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!msg.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return msg;
    }

    @Transactional
    public ChatMessageDto send(UUID userId, UUID projectId, SendMessageRequest req) {
        requireProject(userId, projectId);

        final String cid = req.getClientMessageId().trim();

        var existing = messageRepository.findByProjectIdAndAuthorIdAndClientMessageId(projectId, userId, cid);
        if (existing.isPresent()) {
            return toDtoWithReactionsAndAttachments(existing.get(), "CREATED", userId);
        }

        String text = req.getText() == null ? "" : req.getText().trim();
        List<UUID> attachmentIds = req.getAttachmentIds() == null ? List.of() : req.getAttachmentIds();

        if (text.isEmpty() && attachmentIds.isEmpty()) {
            throw new IllegalArgumentException("Empty message");
        }

        ProjectMessageEntity e = ProjectMessageEntity.builder()
                .projectId(projectId)
                .authorId(userId)
                .text(text)
                .createdAt(Instant.now())
                .clientMessageId(cid)
                .build();

        e = messageRepository.save(e);

        if (!attachmentIds.isEmpty()) {
            saveAttachmentsLinks(projectId, e.getId(), attachmentIds);
        }

        return toDtoWithReactionsAndAttachments(e, "CREATED", userId);
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
        return toDtoWithReactionsAndAttachments(msg, "UPDATED", userId);
    }

    @Transactional
    public ChatMessageDto delete(UUID userId, UUID projectId, UUID messageId) {
        ProjectMessageEntity msg = requireMessage(userId, projectId, messageId);

        if (msg.getDeletedAt() == null) {
            msg.setDeletedAt(Instant.now());
        }

        msg = messageRepository.save(msg);
        return toDtoWithReactionsAndAttachments(msg, "DELETED", userId);
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

        Collections.reverse(list);

        Set<UUID> authorIds = list.stream().map(ProjectMessageEntity::getAuthorId).collect(Collectors.toSet());
        Map<UUID, String> names = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getDisplayName()));

        List<UUID> messageIds = list.stream().map(ProjectMessageEntity::getId).toList();

        Map<UUID, Map<String, Integer>> reactionsByMessage = loadReactionsByMessage(messageIds);
        Map<UUID, Set<String>> myReactionsByMessage = loadMyReactionsByMessage(messageIds, userId);

        Map<UUID, List<ChatAttachmentDto>> attachmentsByMessage = loadAttachmentsByMessage(projectId, messageIds);

        return list.stream()
                .map(e -> toDto(
                        e,
                        names.getOrDefault(e.getAuthorId(), "Unknown"),
                        null,
                        reactionsByMessage.getOrDefault(e.getId(), Map.of()),
                        myReactionsByMessage.getOrDefault(e.getId(), Set.of()),
                        attachmentsByMessage.getOrDefault(e.getId(), List.of())
                ))
                .toList();
    }

    private void saveAttachmentsLinks(UUID projectId, UUID messageId, List<UUID> attachmentIds) {
        for (UUID fileId : attachmentIds) {
            if (fileId == null) continue;

            fileRepository.findByIdAndProjectId(fileId, projectId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found"));

            ProjectMessageAttachmentEntity link = ProjectMessageAttachmentEntity.builder()
                    .id(UUID.randomUUID())
                    .messageId(messageId)
                    .fileId(fileId)
                    .createdAt(Instant.now())
                    .build();

            attachmentRepository.save(link);
        }
    }

    private Map<UUID, List<ChatAttachmentDto>> loadAttachmentsByMessage(UUID projectId, List<UUID> messageIds) {
        if (messageIds.isEmpty()) return Map.of();

        List<ProjectMessageAttachmentEntity> links = attachmentRepository.findAllByMessageIdIn(messageIds);
        if (links.isEmpty()) return Map.of();

        Set<UUID> fileIds = links.stream().map(ProjectMessageAttachmentEntity::getFileId).collect(Collectors.toSet());

        Map<UUID, ProjectFileEntity> filesById = fileRepository.findAllById(fileIds).stream()
                .filter(f -> f.getProjectId().equals(projectId))
                .collect(Collectors.toMap(ProjectFileEntity::getId, x -> x));

        Map<UUID, List<ChatAttachmentDto>> out = new HashMap<>();

        for (ProjectMessageAttachmentEntity link : links) {
            ProjectFileEntity f = filesById.get(link.getFileId());
            if (f == null) continue;

            out.computeIfAbsent(link.getMessageId(), k -> new ArrayList<>())
                    .add(toAttachmentDto(projectId, f));
        }

        return out;
    }

    private ChatAttachmentDto toAttachmentDto(UUID projectId, ProjectFileEntity f) {
        return ChatAttachmentDto.builder()
                .id(f.getId())
                .fileName(f.getOriginalName())
                .url("/api/projects/" + projectId + "/files/" + f.getId())
                .size(f.getSize())
                .contentType(f.getContentType())
                .build();
    }

    private Map<UUID, Map<String, Integer>> loadReactionsByMessage(List<UUID> messageIds) {
        if (messageIds.isEmpty()) return Map.of();

        Map<UUID, Map<String, Integer>> out = new HashMap<>();
        for (var row : reactionRepository.countByMessageIdIn(messageIds)) {
            out.computeIfAbsent(row.getMessageId(), k -> new HashMap<>())
                    .put(row.getEmoji(), (int) row.getCnt());
        }
        return out;
    }

    private Map<UUID, Set<String>> loadMyReactionsByMessage(List<UUID> messageIds, UUID userId) {
        if (messageIds.isEmpty()) return Map.of();

        Map<UUID, Set<String>> out = new HashMap<>();
        for (var r : reactionRepository.findAllByMessageIdInAndUserId(messageIds, userId)) {
            out.computeIfAbsent(r.getMessageId(), k -> new HashSet<>()).add(r.getEmoji());
        }
        return out;
    }

    private ChatMessageDto toDtoWithReactionsAndAttachments(ProjectMessageEntity e, String eventType, UUID currentUserId) {
        String name = userRepository.findById(e.getAuthorId())
                .map(u -> u.getDisplayName())
                .orElse("Unknown");

        Map<String, Integer> rx = new HashMap<>();
        for (var row : reactionRepository.countByMessageId(e.getId())) {
            rx.put(row.getEmoji(), (int) row.getCnt());
        }

        Set<String> my = reactionRepository.findAllByMessageIdInAndUserId(List.of(e.getId()), currentUserId).stream()
                .map(x -> x.getEmoji())
                .collect(Collectors.toSet());

        List<ChatAttachmentDto> atts = loadAttachmentsByMessage(e.getProjectId(), List.of(e.getId()))
                .getOrDefault(e.getId(), List.of());

        return toDto(e, name, eventType, rx, my, atts);
    }

    private ChatMessageDto toDto(ProjectMessageEntity e,
                                 String authorName,
                                 String eventType,
                                 Map<String, Integer> reactions,
                                 Set<String> myReactions,
                                 List<ChatAttachmentDto> attachments) {

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
                .reactions(reactions == null ? Map.of() : reactions)
                .myReactions(myReactions == null ? Set.of() : myReactions)
                .attachments(attachments == null ? List.of() : attachments)
                .build();
    }
}
