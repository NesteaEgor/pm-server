package ru.nesterov.pmserver.features.projects.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.projects.entity.ProjectEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    Optional<ProjectEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
