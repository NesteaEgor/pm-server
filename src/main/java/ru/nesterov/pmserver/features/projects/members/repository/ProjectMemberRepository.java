package ru.nesterov.pmserver.features.projects.members.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.projects.members.entity.ProjectMemberEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, UUID> {

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Optional<ProjectMemberEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMemberEntity> findAllByProjectIdOrderByCreatedAtAsc(UUID projectId);

    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}
