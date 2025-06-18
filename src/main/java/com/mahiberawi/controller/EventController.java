package com.mahiberawi.controller;

import com.mahiberawi.dto.event.*;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.AttendanceStatus;
import com.mahiberawi.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal User user) {
        EventResponse event = eventService.createEvent(request, user);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String id) {
        EventResponse event = eventService.getEvent(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EventResponse>> getEventsByGroup(@PathVariable String groupId) {
        List<EventResponse> events = eventService.getEventsByGroup(groupId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable String id,
            @Valid @RequestBody EventRequest request) {
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventResponse> registerParticipant(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        EventResponse event = eventService.registerParticipant(id, user);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}/participants/{userId}/status")
    public ResponseEntity<EventResponse> updateParticipantStatus(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestParam AttendanceStatus status) {
        EventResponse event = eventService.updateParticipantStatus(id, userId, status);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}/time")
    public ResponseEntity<EventResponse> updateEventTime(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndTime) {
        EventResponse event = eventService.updateEventTime(id, newStartTime, newEndTime);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}/capacity")
    public ResponseEntity<EventResponse> updateEventCapacity(
            @PathVariable String id,
            @RequestParam Integer newMaxParticipants) {
        EventResponse event = eventService.updateEventCapacity(id, newMaxParticipants);
        return ResponseEntity.ok(event);
    }
} 