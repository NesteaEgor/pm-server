package ru.nesterov.pmserver.features.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.nesterov.pmserver.features.users.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
