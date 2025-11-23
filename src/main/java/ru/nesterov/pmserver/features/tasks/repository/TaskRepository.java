package ru.nesterov.pmserver.features.tasks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nesterov.pmserver.features.tasks.entity.TaskEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<TaskEntity> findByIdAndProjectId(UUID id, UUID projectId);

    List<TaskEntity> findByProjectIdAndStatusOrderByCreatedAtDesc(UUID projectId, String status);

    // Сортировка по дедлайну: сначала у кого дедлайн задан, потом без дедлайна
    @Query("""
      select t from TaskEntity t
      where t.projectId = :projectId
      order by case when t.deadline is null then 1 else 0 end, t.deadline asc, t.createdAt desc
  """)
    List<TaskEntity> findByProjectIdOrderByDeadlineAscNullsLast(@Param("projectId") UUID projectId);

    @Query("""
      select t from TaskEntity t
      where t.projectId = :projectId and t.status = :status
      order by case when t.deadline is null then 1 else 0 end, t.deadline asc, t.createdAt desc
  """)
    List<TaskEntity> findByProjectIdAndStatusOrderByDeadlineAscNullsLast(
            @Param("projectId") UUID projectId,
            @Param("status") String status
    );
}
