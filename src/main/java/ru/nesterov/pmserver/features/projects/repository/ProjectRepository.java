package ru.nesterov.pmserver.features.projects.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nesterov.pmserver.features.projects.entity.ProjectEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    List<ProjectEntity> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    Optional<ProjectEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("""
            select p from ProjectEntity p
            where p.ownerId = :userId
               or p.id in (select m.projectId from ProjectMemberEntity m where m.userId = :userId)
            order by p.createdAt desc
           """)
    List<ProjectEntity> findAccessibleByUser(@Param("userId") UUID userId);

    // профили показываем только если есть общий проект (или если это ты сам)
    @Query(value = """
        select exists(
          select 1
          from projects p
          left join project_members mv on mv.project_id = p.id and mv.user_id = :viewerId
          left join project_members mt on mt.project_id = p.id and mt.user_id = :targetId
          where (p.owner_id = :viewerId or mv.user_id is not null)
            and (p.owner_id = :targetId or mt.user_id is not null)
        )
        """, nativeQuery = true)
    boolean existsSharedProject(@Param("viewerId") UUID viewerId, @Param("targetId") UUID targetId);
}
