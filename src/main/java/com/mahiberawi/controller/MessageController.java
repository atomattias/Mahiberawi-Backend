package com.mahiberawi.controller;

import com.mahiberawi.dto.message.MessageRequest;
import com.mahiberawi.dto.message.MessageResponse;
import com.mahiberawi.dto.message.ReactionRequest;
import com.mahiberawi.dto.message.ReactionResponse;
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
    public ResponseEntity<MessageResponse> getMessage(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        MessageResponse message = messageService.getMessage(id, user);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/direct/{userId}")
    public ResponseEntity<List<MessageResponse>> getDirectMessages(
            @PathVariable String userId,
            @AuthenticationPrincipal User user) {
        List<MessageResponse> messages = messageService.getDirectMessages(userId, user);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MessageResponse>> getGroupMessages(
            @PathVariable String groupId,
            @AuthenticationPrincipal User user) {
        List<MessageResponse> messages = messageService.getGroupMessages(groupId, user);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<MessageResponse>> getEventMessages(
            @PathVariable String eventId,
            @AuthenticationPrincipal User user) {
        List<MessageResponse> messages = messageService.getEventMessages(eventId, user);
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

    // ========== REACTION ENDPOINTS ==========

    @PostMapping("/{postId}/reactions")
    public ResponseEntity<ReactionResponse> addReaction(
            @PathVariable String postId,
            @Valid @RequestBody ReactionRequest request,
            @AuthenticationPrincipal User user) {
        ReactionResponse response = messageService.addReaction(postId, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/reactions/{reactionType}")
    public ResponseEntity<ReactionResponse> removeReaction(
            @PathVariable String postId,
            @PathVariable String reactionType,
            @AuthenticationPrincipal User user) {
        ReactionResponse response = messageService.removeReaction(postId, reactionType, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/reactions")
    public ResponseEntity<ReactionResponse> getReactions(
            @PathVariable String postId,
            @AuthenticationPrincipal User user) {
        ReactionResponse response = messageService.getReactions(postId, user);
        return ResponseEntity.ok(response);
    }
} 