package com.mahiberawi.dto.event;

import com.mahiberawi.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventAttendanceRequest {
    @NotNull(message = "Event ID is required")
    private String eventId;
    
    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
    
    private String notes;
} 