package com.mahiberawi.service;

import com.mahiberawi.config.FileUploadConfig;
import com.mahiberawi.dto.file.FileResponse;
import com.mahiberawi.entity.FileType;
import com.mahiberawi.entity.User;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.FileRepository;
import com.mahiberawi.repository.UserRepository;
import com.mahiberawi.util.ImageProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileUploadConfig fileConfig;
    private final ImageProcessor imageProcessor;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String getUploadDir() {
        return uploadDir;
    }

    @Transactional
    public FileResponse uploadFile(MultipartFile file, FileType type, String relatedEntityId, String uploaderId) {
        // Validate file
        validateFile(file, type);

        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploaderId));

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(fileConfig.getDirectory());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String storedFilename = UUID.randomUUID().toString() + fileExtension;

            // Process file based on type
            byte[] fileContent;
            if (isImageFile(file.getContentType()) && type == FileType.PROFILE_PICTURE) {
                fileContent = imageProcessor.processImage(file);
                // Also create and save thumbnail
                byte[] thumbnailContent = imageProcessor.createThumbnail(file);
                String thumbnailFilename = "thumb_" + storedFilename;
                Path thumbnailPath = uploadPath.resolve(thumbnailFilename);
                Files.write(thumbnailPath, thumbnailContent);
            } else {
                fileContent = file.getBytes();
            }

            // Save file
            Path filePath = uploadPath.resolve(storedFilename);
            Files.write(filePath, fileContent);

            // Create file entity
            com.mahiberawi.entity.File fileEntity = new com.mahiberawi.entity.File();
            fileEntity.setOriginalName(originalFilename);
            fileEntity.setStoredName(storedFilename);
            fileEntity.setContentType(file.getContentType());
            fileEntity.setSize((long) fileContent.length);
            fileEntity.setType(type);
            fileEntity.setUrl("/api/files/" + storedFilename);
            fileEntity.setUploader(uploader);
            fileEntity.setRelatedEntityId(relatedEntityId);
            fileEntity.setRelatedEntityType(type.name().split("_")[0]);
            fileEntity = fileRepository.save(fileEntity);

            return mapToFileResponse(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private void validateFile(MultipartFile file, FileType type) {
        // Check file size
        if (file.getSize() > fileConfig.getMaxSize()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }

        // Check file type
        String contentType = file.getContentType();
        List<String> allowedTypes = fileConfig.getAllowedTypes().get(type.name());
        if (allowedTypes != null && !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed for " + type);
        }
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    public FileResponse getFile(String fileId) {
        com.mahiberawi.entity.File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));
        return mapToFileResponse(file);
    }

    @Transactional
    public void deleteFile(String fileId) {
        com.mahiberawi.entity.File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        try {
            // Delete file from storage
            Path filePath = Paths.get(uploadDir).resolve(file.getStoredName());
            Files.deleteIfExists(filePath);

            // Delete file entity
            fileRepository.delete(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public List<FileResponse> getFilesByType(FileType type) {
        return fileRepository.findByType(type).stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    public List<FileResponse> getFilesByEntity(String entityId, String entityType) {
        return fileRepository.findByRelatedEntityIdAndRelatedEntityType(entityId, entityType).stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    public List<FileResponse> getUserFiles(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return fileRepository.findByUploader(user).stream()
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());
    }

    public List<FileResponse> getFilesByUser(User user) {
        return getUserFiles(user.getId());
    }

    public FileResponse uploadFile(MultipartFile file, User user) {
        throw new UnsupportedOperationException("Use uploadFile(MultipartFile, FileType, String, String) instead.");
    }

    private FileResponse mapToFileResponse(com.mahiberawi.entity.File file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .type(file.getType())
                .url(file.getUrl())
                .uploaderId(file.getUploader().getId())
                .uploaderName(file.getUploader().getFullName())
                .relatedEntityId(file.getRelatedEntityId())
                .relatedEntityType(file.getRelatedEntityType())
                .uploadedAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }
} 