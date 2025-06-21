package com.mahiberawi.controller;

import com.mahiberawi.dto.event.*;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.AttendanceStatus;
import com.mahiberawi.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {
    private final EventService eventService;

    @Operation(
        summary = "Create a new event",
        description = "Creates a new event with the current user as the creator"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event created successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Parameter(description = "Event creation details", required = true)
            @Valid @RequestBody EventRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        EventResponse event = eventService.createEvent(request, user);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Get event by ID",
        description = "Retrieves detailed information about a specific event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event found",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(
            @Parameter(description = "ID of the event to retrieve", required = true)
            @PathVariable String id) {
        EventResponse event = eventService.getEvent(id);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Get events by group",
        description = "Retrieves all events associated with a specific group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<EventResponse>> getEventsByGroup(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId) {
        List<EventResponse> events = eventService.getEventsByGroup(groupId);
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Get upcoming events",
        description = "Retrieves all upcoming events"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        )
    })
    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents() {
        List<EventResponse> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Update event details",
        description = "Updates the information of an existing event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event updated successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update this event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @Parameter(description = "ID of the event to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated event details", required = true)
            @Valid @RequestBody EventRequest request) {
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Delete an event",
        description = "Permanently deletes an event and all its associated data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID of the event to delete", required = true)
            @PathVariable String id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Register for an event",
        description = "Registers the current user as a participant in the event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registration successful",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Event is full or registration is closed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PostMapping("/{id}/register")
    public ResponseEntity<EventResponse> registerParticipant(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        EventResponse event = eventService.registerParticipant(id, user);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Update participant status",
        description = "Updates the attendance status of a participant in the event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status updated successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update status"),
        @ApiResponse(responseCode = "404", description = "Event or participant not found")
    })
    @PutMapping("/{id}/participants/{userId}/status")
    public ResponseEntity<EventResponse> updateParticipantStatus(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable String id,
            @Parameter(description = "ID of the participant", required = true)
            @PathVariable String userId,
            @Parameter(description = "New attendance status", required = true)
            @RequestParam AttendanceStatus status) {
        EventResponse event = eventService.updateParticipantStatus(id, userId, status);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Update event time",
        description = "Updates the start and end time of an event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Time updated successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid time range"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update time"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/{id}/time")
    public ResponseEntity<EventResponse> updateEventTime(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable String id,
            @Parameter(description = "New start time", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newStartTime,
            @Parameter(description = "New end time", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newEndTime) {
        EventResponse event = eventService.updateEventTime(id, newStartTime, newEndTime);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Update event capacity",
        description = "Updates the maximum number of participants allowed in the event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Capacity updated successfully",
            content = @Content(schema = @Schema(implementation = EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid capacity"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update capacity"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @PutMapping("/{id}/capacity")
    public ResponseEntity<EventResponse> updateEventCapacity(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable String id,
            @Parameter(description = "New maximum number of participants", required = true)
            @RequestParam Integer newMaxParticipants) {
        EventResponse event = eventService.updateEventCapacity(id, newMaxParticipants);
        return ResponseEntity.ok(event);
    }
} 