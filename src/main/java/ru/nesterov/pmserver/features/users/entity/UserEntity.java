package ru.nesterov.pmserver.features.users.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    // старое поле оставляем (не ломаем), но теперь будем отдавать avatarUrl как endpoint
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "status", length = 160)
    private String status;

    @Column(name = "avatar_stored_name", length = 255)
    private String avatarStoredName;

    @Column(name = "avatar_content_type", length = 128)
    private String avatarContentType;

    @Column(name = "avatar_size")
    private Long avatarSize;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
