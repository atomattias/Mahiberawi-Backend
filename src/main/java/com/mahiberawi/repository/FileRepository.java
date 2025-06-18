package com.mahiberawi.repository;

import com.mahiberawi.entity.File;
import com.mahiberawi.entity.FileType;
import com.mahiberawi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, String> {
    List<File> findByType(FileType type);
    List<File> findByUploader(User uploader);
    List<File> findByRelatedEntityIdAndRelatedEntityType(String entityId, String entityType);
} 