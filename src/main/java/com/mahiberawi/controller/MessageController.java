package com.mahiberawi.controller;

import com.mahiberawi.dto.message.MessageRequest;
import com.mahiberawi.dto.message.MessageResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal User user) {
        MessageResponse message = messageService.sendMessage(request, user);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable String id) {
        MessageResponse message = messageService.getMessage(id);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/direct/{userId}")
    public ResponseEntity<List<MessageResponse>> getDirectMessages(@PathVariable String userId) {
        List<MessageResponse> messages = messageService.getDirectMessages(userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MessageResponse>> getGroupMessages(@PathVariable String groupId) {
        List<MessageResponse> messages = messageService.getGroupMessages(groupId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<MessageResponse>> getEventMessages(@PathVariable String eventId) {
        List<MessageResponse> messages = messageService.getEventMessages(eventId);
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        MessageResponse message = messageService.markAsRead(id, user);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        messageService.deleteMessage(id, user);
        return ResponseEntity.noContent().build();
    }
} 