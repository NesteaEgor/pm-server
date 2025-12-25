package ru.nesterov.pmserver.features.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageAttachmentEntity;

import java.util.List;
import java.util.UUID;

public interface ProjectMessageAttachmentRepository extends JpaRepository<ProjectMessageAttachmentEntity, UUID> {
    List<ProjectMessageAttachmentEntity> findAllByMessageIdIn(List<UUID> messageIds);
    List<ProjectMessageAttachmentEntity> findAllByMessageId(UUID messageId);
}
