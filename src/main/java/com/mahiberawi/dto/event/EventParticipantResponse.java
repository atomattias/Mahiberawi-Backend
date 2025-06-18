package com.mahiberawi.dto.event;

import com.mahiberawi.entity.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventParticipantResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String userFullName;
    private String userProfilePicture;
    private AttendanceStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
} 