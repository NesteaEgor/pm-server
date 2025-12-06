package ru.nesterov.pmserver.features.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.comments.entity.TaskCommentEntity;

import java.util.List;
import java.util.UUID;

public interface TaskCommentRepository extends JpaRepository<TaskCommentEntity, UUID> {

    List<TaskCommentEntity> findAllByTask_IdOrderByCreatedAtAsc(UUID taskId);

    @Modifying
    @Transactional
    long deleteByIdAndTask_IdAndAuthorId(UUID id, UUID taskId, UUID authorId);
}
