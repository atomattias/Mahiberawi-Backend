package com.mahiberawi.dto.file;

import com.mahiberawi.entity.FileType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileResponse {
    private String id;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long size;
    private FileType type;
    private String url;
    private String uploaderId;
    private String uploaderName;
    private String relatedEntityId; // ID of the related entity (user, event, group, etc.)
    private String relatedEntityType; // Type of the related entity
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
} 