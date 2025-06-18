package com.mahiberawi.dto.event;

import com.mahiberawi.entity.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventAttendanceResponse {
    private String id;
    private String eventId;
    private String eventName;
    private String userId;
    private String userFullName;
    private String userEmail;
    private AttendanceStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 