package ru.nesterov.pmserver.features.chat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessageEntity, UUID> {

    List<ProjectMessageEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    List<ProjectMessageEntity> findByProjectIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID projectId,
            Instant before,
            Pageable pageable
    );
}
