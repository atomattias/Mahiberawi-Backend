package com.mahiberawi.service;

import com.mahiberawi.dto.event.EventRequest;
import com.mahiberawi.dto.event.EventResponse;
import com.mahiberawi.dto.event.EventParticipantResponse;
import com.mahiberawi.entity.*;
import com.mahiberawi.entity.AttendanceStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.EventRepository;
import com.mahiberawi.repository.EventAttendanceRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventResponse createEvent(EventRequest request, User creator) {
        Group group = null;
        if (request.getGroupId() != null) {
            group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setGroup(group);
        event.setCreator(creator);

        Event savedEvent = eventRepository.save(event);
        return mapToEventResponse(savedEvent);
    }

    public EventResponse getEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return mapToEventResponse(event);
    }

    public List<EventResponse> getEventsByGroup(String groupId) {
        List<Event> events = eventRepository.findByGroupId(groupId);
        return events.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> getUpcomingEvents() {
        List<Event> events = eventRepository.findByStartTimeAfter(LocalDateTime.now());
        return events.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(String id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setMaxParticipants(request.getMaxParticipants());

        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));
            event.setGroup(group);
        }

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        eventRepository.delete(event);
    }

    @Transactional
    public EventResponse registerParticipant(String eventId, User user) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getMaxParticipants() > 0 && 
             eventAttendanceRepository.countByEventAndStatus(event, AttendanceStatus.REGISTERED) >= event.getMaxParticipants()) {
            throw new IllegalStateException("Event is full");
        }

        EventAttendance participant = new EventAttendance();
        participant.setEvent(event);
        participant.setUser(user);
        participant.setStatus(AttendanceStatus.REGISTERED);

        eventAttendanceRepository.save(participant);
        return mapToEventResponse(event);
    }

    @Transactional
    public EventResponse updateParticipantStatus(String eventId, String userId, AttendanceStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        EventAttendance participant = eventAttendanceRepository.findByEventAndUserId(event, userId)
                .orElseThrow(() -> new ResourceNotFoundException("EventAttendance", "userId", userId));

        participant.setStatus(status);
        eventAttendanceRepository.save(participant);
        return mapToEventResponse(event);
    }

    private EventResponse mapToEventResponse(Event event) {
        List<EventAttendance> participants = eventAttendanceRepository.findByEvent(event);
        
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .maxParticipants(event.getMaxParticipants())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .participants(participants.stream()
                        .map(this::mapToParticipantResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private EventParticipantResponse mapToParticipantResponse(EventAttendance participant) {
        return EventParticipantResponse.builder()
                .id(participant.getId())
                .userId(participant.getUser() != null ? participant.getUser().getId() : null)
                .status(participant.getStatus())
                .build();
    }

    public List<Event> getUpcomingEventsForReminders(LocalDateTime now) {
        LocalDateTime weekFromNow = now.plusWeeks(1);
        return eventRepository.findByStartTimeBetween(now, weekFromNow);
    }

    public List<Event> getActiveEvents(LocalDateTime now) {
        return eventRepository.findByStartTimeAfter(now);
    }

    @Transactional
    public EventResponse updateEventTime(String id, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        event.setStartTime(newStartTime);
        event.setEndTime(newEndTime);

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponse(updatedEvent);
    }

    @Transactional
    public EventResponse updateEventCapacity(String id, Integer newMaxParticipants) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        event.setMaxParticipants(newMaxParticipants);

        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponse(updatedEvent);
    }

    // ========== ADMIN METHODS ==========
    
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }
    
    public List<EventResponse> getRecentEvents(int limit) {
        List<Event> events = eventRepository.findTop10ByOrderByCreatedAtDesc();
        return events.stream()
                .limit(limit)
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }
} 