package ru.nesterov.pmserver.features.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.chat.entity.ProjectChatReadEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectChatReadRepository extends JpaRepository<ProjectChatReadEntity, UUID> {
    Optional<ProjectChatReadEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);
    List<ProjectChatReadEntity> findAllByProjectId(UUID projectId);
}
