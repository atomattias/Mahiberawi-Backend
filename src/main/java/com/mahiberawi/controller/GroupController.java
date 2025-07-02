package com.mahiberawi.controller;

import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupMemberResponse;
import com.mahiberawi.dto.group.JoinGroupRequest;
import com.mahiberawi.dto.group.JoinByEmailRequest;
import com.mahiberawi.dto.group.JoinByLinkRequest;
import com.mahiberawi.dto.group.JoinResponse;
import com.mahiberawi.dto.group.GroupInvitationRequest;
import com.mahiberawi.dto.group.GroupInvitationResponse;
import com.mahiberawi.dto.group.UpdateRoleRequest;
import com.mahiberawi.dto.group.GroupPermissionsResponse;
import com.mahiberawi.dto.group.GroupEventsResponse;
import com.mahiberawi.dto.group.GroupPostsResponse;
import com.mahiberawi.dto.group.GroupPaymentsResponse;
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
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.getGroup(id, user);
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
        summary = "Get user's groups (alternative endpoint)",
        description = "Alternative endpoint to retrieve all groups the current user is a member of. " +
                     "This endpoint provides the same functionality as /user for frontend compatibility."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupResponse> groups = groupService.getGroupsByUser(user);
        return ResponseEntity.ok(groups);
    }

    @Operation(
        summary = "Get user's group memberships",
        description = "Retrieves detailed membership information for all groups the current user belongs to, " +
                     "including role, join date, and status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Memberships retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/memberships")
    public ResponseEntity<List<GroupMemberResponse>> getMyMemberships(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupMemberResponse> memberships = groupService.getUserMemberships(user);
        return ResponseEntity.ok(memberships);
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

    @Operation(
        summary = "Join group by email invitation",
        description = "Sends an email invitation to join a group. If groupCode is provided, " +
                     "sends invitation to that specific group. If not, sends a general invitation " +
                     "that can be used to join any group."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email invitation sent successfully",
            content = @Content(schema = @Schema(implementation = JoinResponse.class))
        ),
        @ApiResponse(
            responseCode = "202",
            description = "Email invitation sent, verification required",
            content = @Content(schema = @Schema(implementation = JoinResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid email or group code"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "User already a member")
    })
    @PostMapping("/join-by-email")
    public ResponseEntity<JoinResponse> joinByEmail(
            @Parameter(description = "Email invitation details", required = true)
            @Valid @RequestBody JoinByEmailRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        JoinResponse response = groupService.joinByEmail(request, user);
        
        if (response.isSuccess() && !response.isRequiresVerification()) {
            return ResponseEntity.ok(response);
        } else if (response.isRequiresVerification()) {
            return ResponseEntity.accepted().body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Join group by invitation link",
        description = "Joins a group using an invitation link. The link should contain " +
                     "the group identifier and optional invitation token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully joined the group",
            content = @Content(schema = @Schema(implementation = JoinResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid invitation link"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "Already a member of the group"),
        @ApiResponse(responseCode = "410", description = "Invitation link expired")
    })
    @PostMapping("/join-by-link")
    public ResponseEntity<JoinResponse> joinByLink(
            @Parameter(description = "Invitation link details", required = true)
            @Valid @RequestBody JoinByLinkRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        JoinResponse response = groupService.joinByLink(request, user);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Verify email invitation",
        description = "Verifies an email invitation token and completes the group joining process"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified and group joined successfully",
            content = @Content(schema = @Schema(implementation = JoinResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @PostMapping("/verify-invitation")
    public ResponseEntity<JoinResponse> verifyInvitation(
            @Parameter(description = "Invitation token", required = true)
            @RequestParam String token) {
        JoinResponse response = groupService.verifyInvitation(token);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "Get public groups for discovery",
        description = "Retrieves a list of public groups that users can discover and join"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Public groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        )
    })
    @GetMapping("/public")
    public ResponseEntity<List<GroupResponse>> getPublicGroups(
            @Parameter(description = "Search term for filtering groups")
            @RequestParam(required = false) String search) {
        List<GroupResponse> groups = groupService.getPublicGroups(search);
        return ResponseEntity.ok(groups);
    }

    @Operation(
        summary = "Generate QR code for group",
        description = "Generates a QR code containing the group's invitation link"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "QR code generated successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "Not authorized to generate QR code")
    })
    @PostMapping("/{groupId}/qr-code")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> generateQRCode(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.generateQRCode(groupId, user));
    }

    @Operation(
        summary = "Check if user has any groups",
        description = "Returns whether the current user is a member of any groups. " +
                     "Useful for determining if user should see onboarding or regular content."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User group status retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/has-groups")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> hasGroups(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        boolean hasGroups = groupService.userHasGroups(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message(hasGroups ? "User has groups" : "User has no groups")
                .data(hasGroups)
                .build());
    }

    // ========== ENHANCED GROUP INVITATION ENDPOINTS ==========

    @Operation(
        summary = "Create group invitation",
        description = "Creates a new group invitation via email, SMS, or generates a unique invitation code. " +
                     "Only admins and moderators can create invitations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitation created successfully",
            content = @Content(schema = @Schema(implementation = GroupInvitationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid invitation request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create invitations"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/invitations")
    public ResponseEntity<GroupInvitationResponse> createGroupInvitation(
            @Parameter(description = "Invitation details", required = true)
            @Valid @RequestBody GroupInvitationRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupInvitationResponse invitation = groupService.createGroupInvitation(request, user);
        return ResponseEntity.ok(invitation);
    }

    @Operation(
        summary = "Get group invitations",
        description = "Retrieves all invitations for a specific group. " +
                     "Only admins and moderators can view invitations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitations retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupInvitationResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to view invitations"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/invitations")
    public ResponseEntity<List<GroupInvitationResponse>> getGroupInvitations(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupInvitationResponse> invitations = groupService.getGroupInvitations(groupId, user);
        return ResponseEntity.ok(invitations);
    }

    @Operation(
        summary = "Revoke group invitation",
        description = "Revokes a pending group invitation. " +
                     "Only admins and moderators can revoke invitations."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invitation revoked successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot revoke non-pending invitation"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to revoke invitations"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> revokeInvitation(
            @Parameter(description = "ID of the invitation to revoke", required = true)
            @PathVariable String invitationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        groupService.revokeInvitation(invitationId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Join group with invitation code",
        description = "Joins a group using a valid invitation code. " +
                     "The invitation code must be valid and not expired."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully joined the group",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid or expired invitation code"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Invitation not found"),
        @ApiResponse(responseCode = "409", description = "Already a member of the group")
    })
    @PostMapping("/join-with-code")
    public ResponseEntity<GroupResponse> joinWithInvitationCode(
            @Parameter(description = "Invitation code", required = true)
            @RequestParam String code,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupResponse group = groupService.joinWithInvitationCode(code, user);
        return ResponseEntity.ok(group);
    }

    // ========== GROUP-SPECIFIC ACTIVITIES ENDPOINTS ==========

    @Operation(
        summary = "Get group events",
        description = "Retrieves all events for a specific group. Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/events")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getGroupEvents(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.event.EventResponse> events = groupService.getGroupEvents(groupId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group events retrieved successfully")
                .data(events)
                .build());
    }

    @Operation(
        summary = "Create group event",
        description = "Creates a new event for a specific group. Only admins and moderators can create events."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid event details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create events"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/events")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> createGroupEvent(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Event creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.event.EventRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.event.EventResponse event = groupService.createGroupEvent(groupId, request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Event created successfully")
                .data(event)
                .build());
    }

    @Operation(
        summary = "Get group posts",
        description = "Retrieves all posts/messages for a specific group. Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/posts")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getGroupPosts(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.message.MessageResponse> posts = groupService.getGroupPosts(groupId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group posts retrieved successfully")
                .data(posts)
                .build());
    }

    @Operation(
        summary = "Create group post",
        description = "Creates a new post/message for a specific group. Only group members can create posts."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid post details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/posts")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> createGroupPost(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Post creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.message.MessageRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.message.MessageResponse post = groupService.createGroupPost(groupId, request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Post created successfully")
                .data(post)
                .build());
    }

    @Operation(
        summary = "Get group payments",
        description = "Retrieves all payments for a specific group. Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/payments")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getGroupPayments(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.payment.PaymentResponse> payments = groupService.getGroupPayments(groupId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group payments retrieved successfully")
                .data(payments)
                .build());
    }

    @Operation(
        summary = "Create group payment",
        description = "Creates a new payment for a specific group. Only admins and moderators can create payments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create payments"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/payments")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> createGroupPayment(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Payment creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.payment.PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.payment.PaymentResponse payment = groupService.createGroupPayment(groupId, request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Payment created successfully")
                .data(payment)
                .build());
    }

    // ========== ENHANCED MEMBER MANAGEMENT ==========

    @Operation(
        summary = "Add member to group",
        description = "Adds a new member to a group. Only admins and moderators can add members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member added successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid member details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to add members"),
        @ApiResponse(responseCode = "404", description = "Group or user not found"),
        @ApiResponse(responseCode = "409", description = "User already a member")
    })
    @PostMapping("/{groupId}/members")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> addGroupMember(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Member details", required = true)
            @Valid @RequestBody GroupMemberRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupMemberResponse member = groupService.addGroupMember(groupId, request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Member added successfully")
                .data(member)
                .build());
    }

    @Operation(
        summary = "Remove member from group",
        description = "Removes a member from a group. Only admins can remove members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member removed successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to remove members"),
        @ApiResponse(responseCode = "404", description = "Group or member not found")
    })
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> removeGroupMember(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "ID of the member to remove", required = true)
            @PathVariable String memberId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupMemberResponse removedMember = groupService.removeGroupMember(groupId, memberId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Member removed successfully")
                .data(removedMember)
                .build());
    }

    @Operation(
        summary = "Update member role",
        description = "Updates the role of a group member. Only admins can update roles."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Member role updated successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid role update"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update roles"),
        @ApiResponse(responseCode = "404", description = "Group or member not found")
    })
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> updateMemberRole(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "ID of the member", required = true)
            @PathVariable String memberId,
            @Parameter(description = "Role update details", required = true)
            @Valid @RequestBody UpdateRoleRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupMemberResponse updatedMember = groupService.updateMemberRole(groupId, memberId, request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Member role updated successfully")
                .data(updatedMember)
                .build());
    }

    // ========== USER AGGREGATED ENDPOINTS (HOME SCREEN) ==========

    @Operation(
        summary = "Get user's group events",
        description = "Retrieves all events from groups the user is a member of for the home screen."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User's group events retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/events")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getUserGroupEvents(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.event.EventResponse> events = groupService.getUserGroupEvents(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("User's group events retrieved successfully")
                .data(events)
                .build());
    }

    @Operation(
        summary = "Get user's group posts",
        description = "Retrieves all posts from groups the user is a member of for the home screen."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User's group posts retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/posts")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getUserGroupPosts(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.message.MessageResponse> posts = groupService.getUserGroupPosts(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("User's group posts retrieved successfully")
                .data(posts)
                .build());
    }

    @Operation(
        summary = "Get user's group payments",
        description = "Retrieves all payments from groups the user is a member of for the home screen."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User's group payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/payments")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getUserGroupPayments(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<com.mahiberawi.dto.payment.PaymentResponse> payments = groupService.getUserGroupPayments(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("User's group payments retrieved successfully")
                .data(payments)
                .build());
    }

    // ========== ALTERNATIVE USER GROUPS ENDPOINT ==========

    @Operation(
        summary = "Get user's groups (user endpoint)",
        description = "Alternative endpoint to retrieve all groups the current user is a member of. " +
                     "This endpoint is mapped under /user/groups for frontend compatibility."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user/groups")
    public ResponseEntity<List<GroupResponse>> getUserGroupsAlternative(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupResponse> groups = groupService.getGroupsByUser(user);
        return ResponseEntity.ok(groups);
    }

    // ========== PERMISSIONS ENDPOINT ==========

    @Operation(
        summary = "Get user permissions for group",
        description = "Returns the current user's permissions and role for a specific group. " +
                     "This allows the frontend to dynamically show/hide features based on permissions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupPermissionsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/permissions")
    public ResponseEntity<GroupPermissionsResponse> getUserPermissions(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupPermissionsResponse permissions = groupService.getUserPermissions(groupId, user);
        return ResponseEntity.ok(permissions);
    }

    // ========== ENHANCED GROUP-SCOPED ENDPOINTS ==========

    @Operation(
        summary = "Get group events with permissions",
        description = "Retrieves all events for a specific group with user role and permissions. " +
                     "Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupEventsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/events/with-permissions")
    public ResponseEntity<GroupEventsResponse> getGroupEventsWithPermissions(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupEventsResponse response = groupService.getGroupEventsWithPermissions(groupId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get group posts with permissions",
        description = "Retrieves all posts for a specific group with user role and permissions. " +
                     "Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Posts retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupPostsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/posts/with-permissions")
    public ResponseEntity<GroupPostsResponse> getGroupPostsWithPermissions(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupPostsResponse response = groupService.getGroupPostsWithPermissions(groupId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get group payments with permissions",
        description = "Retrieves all payments for a specific group with user role and permissions. " +
                     "Only accessible by group members."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupPaymentsResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not a member of the group"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/payments/with-permissions")
    public ResponseEntity<GroupPaymentsResponse> getGroupPaymentsWithPermissions(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupPaymentsResponse response = groupService.getGroupPaymentsWithPermissions(groupId, user);
        return ResponseEntity.ok(response);
    }

    // ========== ENHANCED PERMISSION-BASED ENDPOINTS ==========

    @Operation(
        summary = "Create group event with permission check",
        description = "Creates a new event for a specific group with enhanced permission checking. " +
                     "Only admins and moderators can create events if group settings allow it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.event.EventResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid event details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create events"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/events/with-permission")
    public ResponseEntity<com.mahiberawi.dto.event.EventResponse> createGroupEventWithPermission(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Event creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.event.EventRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.event.EventResponse event = groupService.createGroupEventWithPermission(groupId, request, user);
        return ResponseEntity.ok(event);
    }

    @Operation(
        summary = "Create group post with permission check",
        description = "Creates a new post for a specific group with enhanced permission checking. " +
                     "Only active members can create posts if group settings allow it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Post created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.message.MessageResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid post details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create posts"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/posts/with-permission")
    public ResponseEntity<com.mahiberawi.dto.message.MessageResponse> createGroupPostWithPermission(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Post creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.message.MessageRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.message.MessageResponse post = groupService.createGroupPostWithPermission(groupId, request, user);
        return ResponseEntity.ok(post);
    }

    @Operation(
        summary = "Create group payment with permission check",
        description = "Creates a new payment for a specific group with enhanced permission checking. " +
                     "Only admins and moderators can create payments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.payment.PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create payments"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/payments/with-permission")
    public ResponseEntity<com.mahiberawi.dto.payment.PaymentResponse> createGroupPaymentWithPermission(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(description = "Payment creation details", required = true)
            @Valid @RequestBody com.mahiberawi.dto.payment.PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        com.mahiberawi.dto.payment.PaymentResponse payment = groupService.createGroupPaymentWithPermission(groupId, request, user);
        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Create group invitation with permission check",
        description = "Creates a new group invitation with enhanced permission checking. " +
                     "Only admins and moderators can send invitations if group settings allow it."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitation created successfully",
            content = @Content(schema = @Schema(implementation = GroupInvitationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid invitation request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to send invitations"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/invitations/with-permission")
    public ResponseEntity<GroupInvitationResponse> createGroupInvitationWithPermission(
            @Parameter(description = "Invitation details", required = true)
            @Valid @RequestBody GroupInvitationRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        GroupInvitationResponse invitation = groupService.createGroupInvitationWithPermission(request, user);
        return ResponseEntity.ok(invitation);
    }

    @Operation(
        summary = "Revoke group invitation with permission check",
        description = "Revokes a pending group invitation with enhanced permission checking. " +
                     "Only admins and moderators can revoke invitations."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invitation revoked successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot revoke non-pending invitation"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to revoke invitations"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @DeleteMapping("/invitations/{invitationId}/with-permission")
    public ResponseEntity<Void> revokeInvitationWithPermission(
            @Parameter(description = "ID of the invitation to revoke", required = true)
            @PathVariable String invitationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        groupService.revokeInvitationWithPermission(invitationId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get group invitations with permission check",
        description = "Retrieves all invitations for a specific group with enhanced permission checking. " +
                     "Only admins and moderators can view invitations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Invitations retrieved successfully",
            content = @Content(schema = @Schema(implementation = GroupInvitationResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to view invitations"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/invitations/with-permissions")
    public ResponseEntity<List<GroupInvitationResponse>> getGroupInvitationsWithPermissions(
            @Parameter(description = "ID of the group", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<GroupInvitationResponse> invitations = groupService.getGroupInvitationsWithPermissions(groupId, user);
        return ResponseEntity.ok(invitations);
    }
} 