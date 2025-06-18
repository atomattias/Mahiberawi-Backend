package com.mahiberawi.dto.event;

import com.mahiberawi.entity.EventStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String groupId;
    private String groupName;
    private String creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private EventStatus status;
    private List<EventParticipantResponse> participants;
} 