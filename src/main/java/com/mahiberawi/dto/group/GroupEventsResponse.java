package com.mahiberawi.dto.group;

import com.mahiberawi.dto.event.EventResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupEventsResponse {
    private String groupId;
    private String groupName;
    private String userRole;
    private List<EventResponse> events;
    private Boolean canCreateEvents;
    private Integer totalEvents;
} 