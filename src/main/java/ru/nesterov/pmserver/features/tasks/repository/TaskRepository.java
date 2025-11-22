package ru.nesterov.pmserver.features.tasks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<TaskEntity> findByIdAndProjectId(UUID id, UUID projectId);
}
