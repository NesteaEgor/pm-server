package ru.nesterov.pmserver.features.projects.members.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.pmserver.features.projects.members.dto.ProjectMemberDto;
import ru.nesterov.pmserver.features.projects.members.entity.ProjectMemberEntity;
import ru.nesterov.pmserver.features.projects.members.repository.ProjectMemberRepository;
import ru.nesterov.pmserver.features.projects.repository.ProjectRepository;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    private void requireOwner(UUID ownerId, UUID projectId) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    @Transactional
    public ProjectMemberDto addMemberByEmail(UUID ownerId, UUID projectId, String email) {
        requireOwner(ownerId, projectId);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getId().equals(ownerId)) {
            throw new IllegalArgumentException("Owner already exists");
        }

        if (memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalArgumentException("Member already exists");
        }

        var e = ProjectMemberEntity.builder()
                .projectId(projectId)
                .userId(user.getId())
                .role("MEMBER")
                .createdAt(Instant.now())
                .build();

        memberRepository.save(e);

        return ProjectMemberDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role("MEMBER")
                .addedAt(e.getCreatedAt())
                .build();
    }

    @Transactional
    public void removeMember(UUID ownerId, UUID projectId, UUID memberUserId) {
        requireOwner(ownerId, projectId);

        if (memberUserId.equals(ownerId)) {
            throw new IllegalArgumentException("Cannot remove owner");
        }

        memberRepository.deleteByProjectIdAndUserId(projectId, memberUserId);
    }

    public List<ProjectMemberDto> list(UUID userId, UUID projectId) {
        // доступ: owner ИЛИ member
        boolean isOwner = projectRepository.existsByIdAndOwnerId(projectId, userId);
        boolean isMember = memberRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!isOwner && !isMember) {
            throw new IllegalArgumentException("Project not found");
        }

        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        var out = new ArrayList<ProjectMemberDto>();

        // OWNER как “виртуальный” участник
        var owner = userRepository.findById(project.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        out.add(ProjectMemberDto.builder()
                .userId(owner.getId())
                .email(owner.getEmail())
                .displayName(owner.getDisplayName())
                .role("OWNER")
                .addedAt(project.getCreatedAt())
                .build());

        // members
        var members = memberRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId);

        var users = userRepository.findAllById(
                members.stream().map(ProjectMemberEntity::getUserId).toList()
        );

        var map = users.stream().collect(java.util.stream.Collectors.toMap(u -> u.getId(), u -> u));

        for (var m : members) {
            var u = map.get(m.getUserId());
            if (u == null) continue;

            out.add(ProjectMemberDto.builder()
                    .userId(u.getId())
                    .email(u.getEmail())
                    .displayName(u.getDisplayName())
                    .role(m.getRole())
                    .addedAt(m.getCreatedAt())
                    .build());
        }

        return out;
    }
}
