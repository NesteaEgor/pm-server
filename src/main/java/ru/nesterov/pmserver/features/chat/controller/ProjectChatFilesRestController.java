package ru.nesterov.pmserver.features.chat.controller;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.nesterov.pmserver.features.auth.security.JwtService;
import ru.nesterov.pmserver.features.chat.dto.ChatAttachmentDto;
import ru.nesterov.pmserver.features.chat.entity.ProjectFileEntity;
import ru.nesterov.pmserver.features.chat.repository.ProjectFileRepository;
import ru.nesterov.pmserver.features.projects.service.ProjectAccessService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
public class ProjectChatFilesRestController {

    private final ProjectAccessService accessService;
    private final ProjectFileRepository fileRepository;
    private final JwtService jwtService;

    private File baseDir() {
        File dir = new File(System.getProperty("user.home"), "pm_uploads");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create uploads dir: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private File projectDir(UUID projectId) {
        File dir = new File(baseDir(), "projects" + File.separator + projectId);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create project dir: " + dir.getAbsolutePath());
        }
        return dir;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatAttachmentDto upload(Authentication auth,
                                    @PathVariable UUID projectId,
                                    @RequestParam("file") MultipartFile file) throws IOException {

        UUID userId = (UUID) auth.getPrincipal();
        accessService.requireAccess(userId, projectId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) original = "file";
        String safeOriginal = original.replaceAll("[\\\\/]+", "_");

        UUID id = UUID.randomUUID();
        String storedName = id + "_" + safeOriginal;

        File dir = projectDir(projectId);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create upload directory: " + dir.getAbsolutePath());
        }

        File target = new File(dir, storedName);

        try {
            file.transferTo(target);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot save file to disk: " + target.getAbsolutePath(), ex);
        }

        ProjectFileEntity e = ProjectFileEntity.builder()
                .id(id)
                .projectId(projectId)
                .uploaderId(userId)
                .originalName(safeOriginal)
                .storedName(storedName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .createdAt(Instant.now())
                .build();

        try {
            fileRepository.save(e);
        } catch (Exception ex) {
            try {
                Files.deleteIfExists(target.toPath());
            } catch (Exception ignored) {
            }
            throw ex;
        }

        return ChatAttachmentDto.builder()
                .id(e.getId())
                .fileName(e.getOriginalName())
                .url("/api/projects/" + projectId + "/files/" + e.getId())
                .size(e.getSize())
                .contentType(e.getContentType())
                .build();
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileSystemResource> download(@PathVariable UUID projectId,
                                                       @PathVariable UUID fileId,
                                                       @RequestParam(required = false) String token,
                                                       Authentication auth) {

        UUID userId = extractUserId(auth, token);
        accessService.requireAccess(userId, projectId);

        ProjectFileEntity e = fileRepository.findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        File f = new File(projectDir(projectId), e.getStoredName());
        if (!f.exists()) {
            throw new IllegalArgumentException("File not found on disk");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(e.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(e.getContentType());
            } catch (Exception ignored) {
            }
        }

        ContentDisposition cd = ContentDisposition.attachment()
                .filename(e.getOriginalName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentLength(e.getSize())
                .body(new FileSystemResource(f));
    }

    @GetMapping("/{fileId}/raw")
    public ResponseEntity<FileSystemResource> raw(@PathVariable UUID projectId,
                                                  @PathVariable UUID fileId,
                                                  @RequestParam(required = false) String token,
                                                  Authentication auth) {

        UUID userId = extractUserId(auth, token);
        accessService.requireAccess(userId, projectId);

        ProjectFileEntity e = fileRepository.findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        File f = new File(projectDir(projectId), e.getStoredName());
        if (!f.exists()) {
            throw new IllegalArgumentException("File not found on disk");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(e.getContentType())) {
            try {
                mediaType = MediaType.parseMediaType(e.getContentType());
            } catch (Exception ignored) {
            }
        }

        ContentDisposition cd = ContentDisposition.inline()
                .filename(e.getOriginalName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .contentLength(e.getSize())
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

        throw new IllegalArgumentException("Unauthorized");
    }
}
