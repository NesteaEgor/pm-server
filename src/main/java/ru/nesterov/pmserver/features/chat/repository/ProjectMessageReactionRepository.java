package ru.nesterov.pmserver.features.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.nesterov.pmserver.features.chat.entity.ProjectMessageReactionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMessageReactionRepository extends JpaRepository<ProjectMessageReactionEntity, UUID> {

    Optional<ProjectMessageReactionEntity> findByMessageIdAndUserIdAndEmoji(UUID messageId, UUID userId, String emoji);

    List<ProjectMessageReactionEntity> findAllByMessageIdInAndUserId(List<UUID> messageIds, UUID userId);

    interface EmojiCountRow {
        String getEmoji();
        long getCnt();
    }

    interface MessageEmojiCountRow {
        UUID getMessageId();
        String getEmoji();
        long getCnt();
    }

    @Query("""
            select r.emoji as emoji, count(r) as cnt
            from ProjectMessageReactionEntity r
            where r.messageId = :messageId
            group by r.emoji
            """)
    List<EmojiCountRow> countByMessageId(@Param("messageId") UUID messageId);

    @Query("""
            select r.messageId as messageId, r.emoji as emoji, count(r) as cnt
            from ProjectMessageReactionEntity r
            where r.messageId in :messageIds
            group by r.messageId, r.emoji
            """)
    List<MessageEmojiCountRow> countByMessageIdIn(@Param("messageIds") List<UUID> messageIds);
}
