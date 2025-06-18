package com.mahiberawi.controller;

import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.service.GroupService;
import com.mahiberawi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(groupService.createGroup(request, user));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable String groupId,
            @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable String groupId,
            @PathVariable String userId) {
        return ResponseEntity.ok(groupService.addMember(groupId, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable String userId) {
        return ResponseEntity.ok(groupService.getUserGroups(userId));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> inviteMember(
            @PathVariable String groupId,
            @Valid @RequestBody GroupMemberRequest request,
            @AuthenticationPrincipal User inviter) {
        GroupResponse group = groupService.inviteMember(groupId, request, inviter);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/members/accept")
    public ResponseEntity<GroupResponse> acceptInvitation(
            @PathVariable String groupId,
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.acceptInvitation(groupId, user);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{groupId}/members/reject")
    public ResponseEntity<GroupResponse> rejectInvitation(
            @PathVariable String groupId,
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.rejectInvitation(groupId, user);
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{groupId}/members/{userId}/role")
    public ResponseEntity<GroupResponse> updateMemberRole(
            @PathVariable String groupId,
            @PathVariable String userId,
            @RequestParam GroupMemberRole newRole,
            @AuthenticationPrincipal User updater) {
        GroupResponse group = groupService.updateMemberRole(groupId, userId, newRole, updater);
        return ResponseEntity.ok(group);
    }
} 