package com.mahiberawi.controller;

import com.mahiberawi.entity.Notification;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(
        summary = "Get user notifications",
        description = "Retrieves all notifications for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notifications retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getUserNotifications(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(notifications)
                .build());
    }

    @Operation(
        summary = "Get unread notifications",
        description = "Retrieves all unread notifications for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Unread notifications retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/unread")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getUnreadNotifications(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Unread notifications retrieved successfully")
                .data(notifications)
                .build());
    }

    @Operation(
        summary = "Mark notification as read",
        description = "Marks a specific notification as read"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notification marked as read successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> markAsRead(
            @Parameter(description = "ID of the notification", required = true)
            @PathVariable String notificationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        notificationService.markAsRead(notificationId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Notification marked as read successfully")
                .build());
    }

    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications for the current user as read"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All notifications marked as read successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/read-all")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> markAllAsRead(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("All notifications marked as read successfully")
                .build());
    }

    @Operation(
        summary = "Delete notification",
        description = "Deletes a specific notification"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notification deleted successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> deleteNotification(
            @Parameter(description = "ID of the notification", required = true)
            @PathVariable String notificationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        notificationService.deleteNotification(notificationId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Notification deleted successfully")
                .build());
    }

    @Operation(
        summary = "Get notification settings",
        description = "Retrieves notification settings for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notification settings retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/settings")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getNotificationSettings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        Map<String, Boolean> settings = notificationService.getNotificationSettings(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Notification settings retrieved successfully")
                .data(settings)
                .build());
    }

    @Operation(
        summary = "Update notification settings",
        description = "Updates notification settings for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notification settings updated successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid settings"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/settings")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> updateNotificationSettings(
            @Parameter(description = "Notification settings", required = true)
            @RequestBody Map<String, Boolean> settings,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        notificationService.updateNotificationSettings(user, settings);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Notification settings updated successfully")
                .build());
    }
}