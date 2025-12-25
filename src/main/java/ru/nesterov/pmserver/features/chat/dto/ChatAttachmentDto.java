package ru.nesterov.pmserver.features.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatAttachmentDto {
    private UUID id;
    private String fileName;
    private String url;
    private Long size;
    private String contentType;
}
