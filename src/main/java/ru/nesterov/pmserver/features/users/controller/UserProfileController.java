package ru.nesterov.pmserver.features.users.controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nesterov.pmserver.features.auth.security.JwtService;
import ru.nesterov.pmserver.features.auth.dto.UserDto;
import ru.nesterov.pmserver.features.auth.service.AuthService;
import ru.nesterov.pmserver.features.users.dto.UpdateMyProfileRequest;
import ru.nesterov.pmserver.features.users.entity.UserEntity;
import ru.nesterov.pmserver.features.users.repository.UserRepository;
import ru.nesterov.pmserver.features.users.service.UserProfileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserProfileController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserProfileService profileService;

    private File avatarsBaseDir() {
        return new File(System.getProperty("user.home"), "pm_uploads" + File.separator + "avatars");
    }

    private File userAvatarDir(UUID userId) {
        return new File(avatarsBaseDir(), userId.toString());
    }

    @GetMapping("/users/{userId}")
    public UserDto getUser(Authentication auth, @PathVariable UUID userId) {
        UUID viewerId = (UUID) auth.getPrincipal();
        return profileService.getProfile(viewerId, userId);
    }

    @PatchMapping("/me/profile")
    public UserDto updateMyProfile(Authentication auth, @Valid @RequestBody UpdateMyProfileRequest req) {
        UUID me = (UUID) auth.getPrincipal();

        UserEntity u = userRepository.findById(me)
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

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto uploadMyAvatar(Authentication auth,
                                  @RequestParam("file") MultipartFile file) throws IOException {

        UUID me = (UUID) auth.getPrincipal();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String ct = file.getContentType();
        if (!StringUtils.hasText(ct) || !ct.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        long max = 5L * 1024 * 1024;
        if (file.getSize() > max) {
            throw new IllegalArgumentException("File too large");
        }

        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) original = "avatar";
        String safeOriginal = original.replaceAll("[\\\\/]+", "_");

        String storedName = UUID.randomUUID() + "_" + safeOriginal;

        File dir = userAvatarDir(me);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create upload directory: " + dir.getAbsolutePath());
        }

        File target = new File(dir, storedName);
        try {
            file.transferTo(target);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot save file to disk: " + target.getAbsolutePath(), ex);
        }

        UserEntity u = userRepository.findById(me)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (u.getAvatarStoredName() != null) {
            try {
                Files.deleteIfExists(new File(dir, u.getAvatarStoredName()).toPath());
            } catch (Exception ignored) {}
        }

        u.setAvatarStoredName(storedName);
        u.setAvatarContentType(ct);
        u.setAvatarSize(file.getSize());
        u = userRepository.save(u);

        return AuthService.toDto(u);
    }

    @GetMapping("/users/{userId}/avatar")
    public ResponseEntity<FileSystemResource> avatar(@PathVariable UUID userId,
                                                     @RequestParam(required = false) String token,
                                                     Authentication auth) {

        UUID requester = extractUserId(auth, token);
        if (requester == null) {
            throw new IllegalArgumentException("Unauthorized");
        }

        if (!profileService.canView(requester, userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (u.getAvatarStoredName() == null || u.getAvatarStoredName().isBlank()) {
            throw new IllegalArgumentException("File not found");
        }

        File f = new File(userAvatarDir(userId), u.getAvatarStoredName());
        if (!f.exists()) {
            throw new IllegalArgumentException("File not found");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(u.getAvatarContentType())) {
            try {
                mediaType = MediaType.parseMediaType(u.getAvatarContentType());
            } catch (Exception ignored) {}
        }

        ContentDisposition cd = ContentDisposition.inline().filename("avatar").build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentLength(f.length())
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(f));
    }

    private UUID extractUserId(Authentication auth, String token) {
        if (auth != null && auth.getPrincipal() instanceof UUID) {
            return (UUID) auth.getPrincipal();
        }
        if (token != null && !token.isBlank()) {
            try {
                return jwtService.parseUserId(token.trim());
            } catch (JwtException ex) {
                throw new IllegalArgumentException("Unauthorized");
            }
        }
        return null;
    }
}
