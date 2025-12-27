package ru.nesterov.pmserver.features.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.auth.dto.UserDto;
import ru.nesterov.pmserver.features.auth.service.AuthService;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;
import ru.nesterov.pmserver.features.users.dto.UpdateMyProfileRequest;
import ru.nesterov.pmserver.features.users.entity.UserEntity;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public UserDto getProfile(UUID viewerId, UUID targetId) {
        if (!canView(viewerId, targetId)) {
            throw new IllegalArgumentException("Access denied");
        }

        UserEntity u = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return AuthService.toDto(u);
    }

    @Transactional
    public UserDto updateMe(UUID userId, UpdateMyProfileRequest req) {
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getDisplayName() != null) {
            u.setDisplayName(req.getDisplayName().trim());
        }

        if (req.getStatus() != null) {
            String s = req.getStatus().trim();
            u.setStatus(s.isBlank() ? null : s);
        }

        u = userRepository.save(u);
        return AuthService.toDto(u);
    }

    public boolean canView(UUID viewerId, UUID targetId) {
        if (viewerId.equals(targetId)) return true;
        return projectRepository.existsSharedProject(viewerId, targetId);
    }
}
