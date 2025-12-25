package ru.nesterov.pmserver.features.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.chat.entity.ProjectFileEntity;

import java.util.Optional;
import java.util.UUID;

public interface ProjectFileRepository extends JpaRepository<ProjectFileEntity, UUID> {
    Optional<ProjectFileEntity> findByIdAndProjectId(UUID id, UUID projectId);
}
