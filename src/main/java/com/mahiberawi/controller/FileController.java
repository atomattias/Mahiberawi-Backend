package com.mahiberawi.controller;

import com.mahiberawi.dto.file.FileResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.FileType;
import com.mahiberawi.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileType type,
            @RequestParam(value = "relatedId", required = false) String relatedId,
            @AuthenticationPrincipal User user) throws IOException {
        FileResponse uploadedFile = fileService.uploadFile(file, type, relatedId, user.getId());
        return ResponseEntity.ok(uploadedFile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getFile(@PathVariable String id) {
        FileResponse file = fileService.getFile(id);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/user")
    public ResponseEntity<List<FileResponse>> getUserFiles(@AuthenticationPrincipal User user) {
        List<FileResponse> files = fileService.getFilesByUser(user);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
} 