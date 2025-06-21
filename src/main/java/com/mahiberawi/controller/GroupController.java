package com.mahiberawi.controller;

import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupMemberResponse;
import com.mahiberawi.dto.group.JoinGroupRequest;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.service.GroupService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(
    name = "Groups",
    description = "Group management APIs for creating, joining, and managing groups. " +
                 "Supports public and private groups with multiple join methods including " +
                 "join codes, invitation links, and email invitations."
)
@SecurityRequirement(name = "Bearer Authentication")
public class GroupController {
    private final GroupService groupService;

    @Operation(
        summary = "Create a new group",
        description = "Creates a new group with the current user as the admin. " +
                     "The group can be set as public or private. " +
                     "A join code and invitation link will be automatically generated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group created successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid group details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Parameter(
                description = "Group creation details including name, description, " +
                            "profile picture, and privacy settings",
                required = true
            )
            @Valid @RequestBody GroupRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.createGroup(request, user);
        return ResponseEntity.ok(group);
    }

    @Operation(
        summary = "Get group details",
        description = "Retrieves detailed information about a specific group, " +
                     "including member count, creator details, and join information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group found",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(
            @Parameter(
                description = "ID of the group to retrieve",
                example = "550e8400-e29b-41d4-a716-446655440000",
                required = true
            )
            @PathVariable String id) {
        GroupResponse group = groupService.getGroup(id);
        return ResponseEntity.ok(group);
    }

    @Operation(
        summary = "Update group details",
        description = "Updates the information of an existing group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group updated successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update this group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @Parameter(description = "ID of the group to update", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Updated group details", required = true)
            @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, request));
    }

    @Operation(
        summary = "Delete a group",
        description = "Permanently deletes a group and all its associated data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "ID of the group to delete", required = true)
            @PathVariable String groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Add member to group",
        description = "Adds a new member to an existing group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member added successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to add members"),
        @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> addMember(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "ID of the user to add", required = true)
            @PathVariable String userId) {
        return ResponseEntity.ok(groupService.addMember(groupId, userId));
    }

    @Operation(
        summary = "Get user's groups",
        description = "Retrieves all groups the current user is a member of, " +
                     "including groups they created and groups they joined."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user")
    public ResponseEntity<List<GroupResponse>> getUserGroups(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupResponse> groups = groupService.getGroupsByUser(user);
        return ResponseEntity.ok(groups);
    }

    @Operation(
        summary = "Invite member to group",
        description = "Sends an invitation to a user to join the group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitation sent successfully",
            content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to invite members"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/members/invite")
    public ResponseEntity<GroupMemberResponse> inviteMember(
            @Parameter(
                description = "ID of the group",
                example = "550e8400-e29b-41d4-a716-446655440000",
                required = true
            )
            @PathVariable String groupId,
            @Parameter(
                description = "Invitation details including email and role",
                required = true
            )
            @Valid @RequestBody GroupMemberRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupMemberResponse member = groupService.inviteMember(groupId, request, user);
        return ResponseEntity.ok(member);
    }

    @Operation(
        summary = "Accept group invitation",
        description = "Accepts a pending invitation to join a group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitation accepted successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group or invitation not found")
    })
    @PostMapping("/{groupId}/members/accept")
    public ResponseEntity<GroupResponse> acceptInvitation(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.acceptInvitation(groupId, user);
        return ResponseEntity.ok(group);
    }

    @Operation(
        summary = "Reject group invitation",
        description = "Rejects a pending invitation to join a group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitation rejected successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group or invitation not found")
    })
    @PostMapping("/{groupId}/members/reject")
    public ResponseEntity<GroupResponse> rejectInvitation(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.rejectInvitation(groupId, user);
        return ResponseEntity.ok(group);
    }

    @Operation(
        summary = "Get group members",
        description = "Retrieves a list of all members in a group, including their roles and join dates. " +
                     "Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Members retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @Parameter(
                description = "ID of the group",
                example = "550e8400-e29b-41d4-a716-446655440000",
                required = true
            )
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId, user);
        return ResponseEntity.ok(members);
    }

    @Operation(
        summary = "Leave group",
        description = "Removes the current user from the group. " +
                     "If the user is the last admin, the group will be deleted."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully left the group",
            content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<GroupMemberResponse> leaveGroup(
            @Parameter(
                description = "ID of the group to leave",
                example = "550e8400-e29b-41d4-a716-446655440000",
                required = true
            )
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupMemberResponse member = groupService.leaveGroup(groupId, user);
        return ResponseEntity.ok(member);
    }

    @Operation(
        summary = "Join a group",
        description = "Joins a group using the group code or invitation link"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully joined the group",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid group code"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "Already a member of the group")
    })
    @PostMapping("/{groupId}/join")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> joinGroup(
            @Parameter(description = "ID of the group to join", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Join request with group code", required = true)
            @Valid @RequestBody JoinGroupRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.joinGroup(groupId, request, user));
    }
} 