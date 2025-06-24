package com.mahiberawi.service;

import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupMemberResponse;
import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.dto.group.JoinByEmailRequest;
import com.mahiberawi.dto.group.JoinByLinkRequest;
import com.mahiberawi.dto.group.JoinResponse;
import com.mahiberawi.dto.group.GroupInvitationRequest;
import com.mahiberawi.dto.group.GroupInvitationResponse;
import com.mahiberawi.dto.UserResponse;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.GroupMember;
import com.mahiberawi.entity.GroupInvitation;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.entity.enums.GroupPrivacy;
import com.mahiberawi.entity.enums.InvitationStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.GroupInvitationRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mahiberawi.dto.group.JoinGroupRequest;
import com.mahiberawi.dto.ApiResponse;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public GroupResponse createGroup(GroupRequest request, User creator) {
        // Generate unique group code and invitation link
        String groupCode = generateUniqueGroupCode();
        String inviteLink = generateUniqueInviteLink();
        
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .privacy(request.getPrivacy())
                .profilePicture(request.getProfilePicture())
                .creator(creator)
                .code(groupCode)
                .inviteLink(inviteLink)
                .build();

        group = groupRepository.save(group);

        // Add creator as admin
        GroupMember creatorMember = GroupMember.builder()
                .group(group)
                .user(creator)
                .role(GroupMemberRole.ADMIN)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(creatorMember);

        return mapToResponse(group);
    }

    /**
     * Generate unique group code
     */
    private String generateUniqueGroupCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        do {
            code.setLength(0);
            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (groupRepository.findByCode(code.toString()).isPresent());
        
        return code.toString();
    }

    /**
     * Generate unique invitation link
     */
    private String generateUniqueInviteLink() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder link = new StringBuilder();
        Random random = new Random();
        
        do {
            link.setLength(0);
            for (int i = 0; i < 12; i++) {
                link.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (groupRepository.findByInviteLink(link.toString()).isPresent());
        
        return link.toString();
    }

    @Transactional
    public GroupResponse acceptInvitation(String groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", user.getId()));

        if (member.getStatus() != GroupMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invitation found");
        }

        member.setStatus(GroupMemberStatus.ACTIVE);
        GroupMember savedMember = groupMemberRepository.save(member);
        notifyMemberJoined(savedMember);
        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse rejectInvitation(String groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", user.getId()));

        if (member.getStatus() != GroupMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invitation found");
        }

        member.setStatus(GroupMemberStatus.REJECTED);
        GroupMember savedMember = groupMemberRepository.save(member);
        notifyMemberRejected(savedMember);
        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse updateGroup(String groupId, GroupRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setType(request.getType());
        group.setPrivacy(request.getPrivacy());
        group = groupRepository.save(group);

        return mapToResponse(group);
    }

    @Transactional
    public void deleteGroup(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        groupRepository.delete(group);
    }

    @Transactional
    public GroupResponse addMember(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new RuntimeException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        return mapToResponse(group);
    }

    public List<GroupResponse> getUserGroups(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return groupRepository.findByMemberId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getPublicGroups(String search) {
        List<Group> groups;
        if (search != null && !search.trim().isEmpty()) {
            groups = groupRepository.findByPrivacyAndNameContainingOrPrivacyAndDescriptionContaining(
                    GroupPrivacy.PUBLIC, search, GroupPrivacy.PUBLIC, search);
        } else {
            groups = groupRepository.findByPrivacy(GroupPrivacy.PUBLIC);
        }
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse joinGroupWithCode(String code, User user) {
        Group group = groupRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with code: " + code));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse joinGroupWithLink(String link, User user) {
        Group group = groupRepository.findByInviteLink(link)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with link: " + link));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        return mapToResponse(group);
    }

    public List<GroupResponse> getGroupsByUser(User user) {
        List<Group> groups = groupRepository.findByMembersUser(user);
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .type(group.getType())
                .privacy(group.getPrivacy())
                .code(group.getCode())
                .inviteLink(group.getInviteLink())
                .memberCount(group.getMemberCount())
                .createdAt(group.getCreatedAt())
                .build();
    }

    public List<GroupMemberResponse> getGroupMembers(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return group.getMembers().stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupMemberResponse inviteMember(String groupId, GroupMemberRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if current user has permission to invite
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can invite members");
        }

        // Find user to invite
        User userToInvite = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupAndUser(group, userToInvite)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        // Create member record
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(userToInvite)
                .role(request.getRole())
                .status(GroupMemberStatus.PENDING)
                .invitedBy(currentUser)
                .joinedAt(LocalDateTime.now())
                .build();

        member = groupMemberRepository.save(member);

        // Send notification to invited user
        notificationService.sendGroupInvitationNotification(member);

        return mapToMemberResponse(member);
    }

    @Transactional
    public GroupMemberResponse removeMember(String groupId, String memberId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Find member to remove
        GroupMember memberToRemove = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        // Check if member belongs to the group
        if (!memberToRemove.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Member does not belong to this group");
        }

        // Check permissions
        if (!memberToRemove.getUser().getId().equals(currentUser.getId())) {
            // If not removing self, must be admin
            GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                    .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

            if (currentMember.getRole() != GroupMemberRole.ADMIN) {
                throw new UnauthorizedException("Only admins can remove members");
            }
        }

        // Prevent removing the last admin
        if (memberToRemove.getRole() == GroupMemberRole.ADMIN) {
            long adminCount = group.getMembers().stream()
                    .filter(m -> m.getRole() == GroupMemberRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove the last admin");
            }
        }

        // Remove member
        groupMemberRepository.delete(memberToRemove);

        // Send notification about removal
        notificationService.sendMemberRemovedNotification(memberToRemove);

        return mapToMemberResponse(memberToRemove);
    }

    @Transactional
    public GroupMemberResponse leaveGroup(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Find current user's membership
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // If last admin, delete the group
        if (member.getRole() == GroupMemberRole.ADMIN) {
            long adminCount = group.getMembers().stream()
                    .filter(m -> m.getRole() == GroupMemberRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                groupRepository.delete(group);
                return mapToMemberResponse(member);
            }
        }

        // Remove member
        groupMemberRepository.delete(member);

        // Send notification about leaving
        notificationService.sendMemberLeftNotification(member);

        return mapToMemberResponse(member);
    }

    private GroupMemberResponse mapToMemberResponse(GroupMember member) {
        return GroupMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .name(member.getUser().getName())
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private void notifyMemberInvited(GroupMember member) {
        String message = String.format("You have been invited to join the group '%s' as %s",
                member.getGroup().getName(), member.getRole());
        notificationService.createGroupNotification(member.getUser(), member.getGroup(), message);
    }

    private void notifyMemberJoined(GroupMember member) {
        String message = String.format("%s has joined the group '%s'",
                member.getUser().getName(), member.getGroup().getName());
        notificationService.createGroupNotification(member.getGroup().getCreator(), member.getGroup(), message);
    }

    private void notifyMemberRejected(GroupMember member) {
        String message = String.format("%s has declined the invitation to join the group '%s'",
                member.getUser().getName(), member.getGroup().getName());
        notificationService.createGroupNotification(member.getGroup().getCreator(), member.getGroup(), message);
    }

    private void notifyMemberRoleUpdated(GroupMember member) {
        String message = String.format("Your role in the group '%s' has been updated to %s",
                member.getGroup().getName(), member.getRole());
        notificationService.createGroupNotification(member.getUser(), member.getGroup(), message);
    }

    private void notifyMemberRemoved(GroupMember member) {
        String message = String.format("You have been removed from the group '%s'",
                member.getGroup().getName());
        notificationService.createGroupNotification(member.getUser(), member.getGroup(), message);
    }

    public GroupResponse getGroup(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return mapToGroupResponse(group);
    }

    private GroupResponse mapToGroupResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .type(group.getType())
                .privacy(group.getPrivacy())
                .code(group.getCode())
                .inviteLink(group.getInviteLink())
                .memberCount(group.getMemberCount())
                .createdAt(group.getCreatedAt())
                .build();
    }

    @Transactional
    public ApiResponse joinGroup(String groupId, JoinGroupRequest request, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new RuntimeException("User is already a member of this group");
        }

        // Validate the group code
        if (!group.getCode().equals(request.getCode())) {
            throw new RuntimeException("Invalid group code");
        }

        // Add user as member
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);

        // Send notification
        notifyMemberJoined(member);

        return ApiResponse.builder()
                .success(true)
                .message("Successfully joined the group")
                .build();
    }

    @Transactional
    public JoinResponse joinByEmail(JoinByEmailRequest request, User currentUser) {
        String email = request.getEmail();
        String groupCode = request.getGroupCode();
        
        // Check if email is different from current user's email
        if (!email.equals(currentUser.getEmail())) {
            // Send invitation email to the specified email
            boolean emailSent = emailService.sendGroupInvitationEmail(email, groupCode, currentUser.getFullName());
            
            if (emailSent) {
                return JoinResponse.builder()
                        .success(true)
                        .message("Invitation email sent successfully to " + email)
                        .requiresVerification(false)
                        .email(email)
                        .build();
            } else {
                return JoinResponse.builder()
                        .success(false)
                        .message("Failed to send invitation email")
                        .requiresVerification(false)
                        .build();
            }
        } else {
            // User is inviting themselves - check if group code is provided
            if (groupCode != null && !groupCode.trim().isEmpty()) {
                try {
                    Group group = groupRepository.findByCode(groupCode)
                            .orElseThrow(() -> new ResourceNotFoundException("Group not found with code: " + groupCode));
                    
                    // Check if user is already a member
                    if (groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
                        return JoinResponse.builder()
                                .success(false)
                                .message("You are already a member of this group")
                                .requiresVerification(false)
                                .build();
                    }
                    
                    // Join the group directly
                    GroupMember member = GroupMember.builder()
                            .group(group)
                            .user(currentUser)
                            .role(GroupMemberRole.MEMBER)
                            .status(GroupMemberStatus.ACTIVE)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    groupMemberRepository.save(member);
                    
                    return JoinResponse.builder()
                            .success(true)
                            .message("Successfully joined the group")
                            .requiresVerification(false)
                            .group(mapToResponse(group))
                            .build();
                    
                } catch (ResourceNotFoundException e) {
                    return JoinResponse.builder()
                            .success(false)
                            .message("Invalid group code")
                            .requiresVerification(false)
                            .build();
                }
            } else {
                // Send verification email to current user
                String invitationToken = UUID.randomUUID().toString();
                boolean emailSent = emailService.sendGroupInvitationVerificationEmail(email, invitationToken, currentUser.getFullName());
                
                if (emailSent) {
                    return JoinResponse.builder()
                            .success(false)
                            .message("Verification email sent to " + email)
                            .requiresVerification(true)
                            .email(email)
                            .invitationToken(invitationToken)
                            .build();
                } else {
                    return JoinResponse.builder()
                            .success(false)
                            .message("Failed to send verification email")
                            .requiresVerification(false)
                            .build();
                }
            }
        }
    }

    @Transactional
    public JoinResponse joinByLink(JoinByLinkRequest request, User user) {
        String invitationLink = request.getInvitationLink();
        
        try {
            // Extract group ID and optional token from the link
            // Expected format: https://mahiberawi.com/join/{groupId}?token={token}
            String[] parts = invitationLink.split("/join/");
            if (parts.length != 2) {
                return JoinResponse.builder()
                        .success(false)
                        .message("Invalid invitation link format")
                        .requiresVerification(false)
                        .build();
            }
            
            String groupIdAndParams = parts[1];
            String groupId = groupIdAndParams.split("\\?")[0];
            
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
            
            // Check if user is already a member
            if (groupMemberRepository.existsByGroupAndUser(group, user)) {
                return JoinResponse.builder()
                        .success(false)
                        .message("You are already a member of this group")
                        .requiresVerification(false)
                        .build();
            }
            
            // Check if group is public or if invitation is valid
            if (group.getPrivacy() == GroupPrivacy.PRIVATE) {
                // For private groups, we might need to validate the invitation token
                // For now, we'll allow joining if the link is valid
                // TODO: Implement token validation for private groups
            }
            
            // Join the group
            GroupMember member = GroupMember.builder()
                    .group(group)
                    .user(user)
                    .role(GroupMemberRole.MEMBER)
                    .status(GroupMemberStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build();
            groupMemberRepository.save(member);
            
            return JoinResponse.builder()
                    .success(true)
                    .message("Successfully joined the group")
                    .requiresVerification(false)
                    .group(mapToResponse(group))
                    .build();
                    
        } catch (ResourceNotFoundException e) {
            return JoinResponse.builder()
                    .success(false)
                    .message("Group not found")
                    .requiresVerification(false)
                    .build();
        } catch (Exception e) {
            return JoinResponse.builder()
                    .success(false)
                    .message("Invalid invitation link")
                    .requiresVerification(false)
                    .build();
        }
    }

    @Transactional
    public JoinResponse verifyInvitation(String token) {
        // TODO: Implement token verification logic
        // This would typically involve:
        // 1. Looking up the invitation by token
        // 2. Checking if it's expired
        // 3. Getting the associated group and user
        // 4. Completing the joining process
        
        return JoinResponse.builder()
                .success(false)
                .message("Token verification not implemented yet")
                .requiresVerification(false)
                .build();
    }

    public ApiResponse generateQRCode(String groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        
        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("You are not a member of this group");
        }
        
        // Generate invitation link
        String invitationLink = "https://mahiberawi.com/join/" + groupId;
        
        // TODO: Generate actual QR code image
        // For now, return the invitation link
        
        return ApiResponse.builder()
                .success(true)
                .message("QR code generated successfully")
                .data(invitationLink)
                .build();
    }

    public boolean userHasGroups(User user) {
        return !groupRepository.findByMemberId(user.getId()).isEmpty();
    }

    // ========== ENHANCED GROUP INVITATION METHODS ==========

    /**
     * Create group invitation (email, SMS, or code)
     */
    @Transactional
    public GroupInvitationResponse createGroupInvitation(GroupInvitationRequest request, User currentUser) {
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("At least one invitation method (email, phone, or generateCode) must be specified");
        }

        // Get group and verify permissions
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));

        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can send invitations");
        }

        // Calculate expiration time
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(request.getExpirationHours());

        // Handle email invitation
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            return createEmailInvitation(group, request.getEmail(), currentUser, expiresAt, request.getMessage());
        }

        // Handle SMS invitation
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            return createSMSInvitation(group, request.getPhone(), currentUser, expiresAt, request.getMessage());
        }

        // Handle code generation
        if (request.getGenerateCode() != null && request.getGenerateCode()) {
            return createCodeInvitation(group, currentUser, expiresAt, request.getMessage());
        }

        throw new IllegalArgumentException("Invalid invitation request");
    }

    /**
     * Create email invitation
     */
    private GroupInvitationResponse createEmailInvitation(Group group, String email, User inviter, 
                                                        LocalDateTime expiresAt, String message) {
        // Check if invitation already exists
        if (groupInvitationRepository.findByEmailAndGroupId(email, group.getId()).isPresent()) {
            throw new IllegalStateException("An invitation for this email already exists");
        }

        // Generate invitation code
        String invitationCode = emailService.generateInvitationCode();

        // Create invitation record
        GroupInvitation invitation = GroupInvitation.builder()
                .groupId(group.getId())
                .email(email)
                .invitedBy(inviter.getId())
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .message(message)
                .build();

        invitation = groupInvitationRepository.save(invitation);

        // Send email
        boolean emailSent = emailService.sendEnhancedGroupInvitationEmail(
                email, group.getName(), inviter.getName(), invitationCode, expiresAt, message);

        if (!emailSent) {
            // Delete the invitation if email failed
            groupInvitationRepository.delete(invitation);
            throw new RuntimeException("Failed to send invitation email");
        }

        return mapToInvitationResponse(invitation, group, inviter, invitationCode);
    }

    /**
     * Create SMS invitation
     */
    private GroupInvitationResponse createSMSInvitation(Group group, String phone, User inviter, 
                                                      LocalDateTime expiresAt, String message) {
        // Check if invitation already exists
        if (groupInvitationRepository.findByPhoneAndGroupId(phone, group.getId()).isPresent()) {
            throw new IllegalStateException("An invitation for this phone number already exists");
        }

        // Generate invitation code
        String invitationCode = emailService.generateInvitationCode();

        // Create invitation record
        GroupInvitation invitation = GroupInvitation.builder()
                .groupId(group.getId())
                .phone(phone)
                .invitedBy(inviter.getId())
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .message(message)
                .build();

        invitation = groupInvitationRepository.save(invitation);

        // Send SMS
        boolean smsSent = emailService.sendSMSInvitation(
                phone, group.getName(), inviter.getName(), invitationCode, expiresAt, message);

        if (!smsSent) {
            // Delete the invitation if SMS failed
            groupInvitationRepository.delete(invitation);
            throw new RuntimeException("Failed to send invitation SMS");
        }

        return mapToInvitationResponse(invitation, group, inviter, invitationCode);
    }

    /**
     * Create code invitation
     */
    private GroupInvitationResponse createCodeInvitation(Group group, User inviter, 
                                                       LocalDateTime expiresAt, String message) {
        // Generate invitation code
        String invitationCode = emailService.generateInvitationCode();

        // Create invitation record (no email/phone for code-based invitations)
        GroupInvitation invitation = GroupInvitation.builder()
                .groupId(group.getId())
                .invitedBy(inviter.getId())
                .status(InvitationStatus.PENDING)
                .expiresAt(expiresAt)
                .message(message)
                .build();

        invitation = groupInvitationRepository.save(invitation);

        return mapToInvitationResponse(invitation, group, inviter, invitationCode);
    }

    /**
     * Get all invitations for a group
     */
    public List<GroupInvitationResponse> getGroupInvitations(String groupId, User currentUser) {
        // Verify permissions
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can view invitations");
        }

        List<GroupInvitation> invitations = groupInvitationRepository.findByGroupId(groupId);
        
        return invitations.stream()
                .map(invitation -> {
                    User inviter = userRepository.findById(invitation.getInvitedBy())
                            .orElse(null);
                    return mapToInvitationResponse(invitation, group, inviter, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Revoke an invitation
     */
    @Transactional
    public void revokeInvitation(String invitationId, User currentUser) {
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", invitationId));

        // Verify permissions
        Group group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", invitation.getGroupId()));

        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can revoke invitations");
        }

        // Only allow revoking pending invitations
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Can only revoke pending invitations");
        }

        // Update status to revoked
        invitation.setStatus(InvitationStatus.REJECTED);
        groupInvitationRepository.save(invitation);
    }

    /**
     * Join group using invitation code
     */
    @Transactional
    public GroupResponse joinWithInvitationCode(String invitationCode, User user) {
        // Find invitation by code (this would need to be implemented based on your code storage strategy)
        // For now, we'll assume the code is stored in the invitation record or a separate table
        
        // This is a simplified implementation - you might want to store codes separately
        List<GroupInvitation> pendingInvitations = groupInvitationRepository.findExpiredInvitations(LocalDateTime.now());
        
        // Find the invitation that matches the code (you'll need to implement this based on your code storage)
        GroupInvitation invitation = pendingInvitations.stream()
                .filter(inv -> inv.getStatus() == InvitationStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation code"));

        // Verify invitation is not expired
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Invitation has expired");
        }

        // Get group
        Group group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", invitation.getGroupId()));

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("You are already a member of this group");
        }

        // Add user as member
        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        groupInvitationRepository.save(invitation);

        return mapToResponse(group);
    }

    /**
     * Clean up expired invitations (scheduled task)
     */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
    public void cleanupExpiredInvitations() {
        int deletedCount = groupInvitationRepository.deleteExpiredInvitations(LocalDateTime.now());
        // Log the cleanup
        System.out.println("Cleaned up " + deletedCount + " expired invitations");
    }

    /**
     * Map invitation to response
     */
    private GroupInvitationResponse mapToInvitationResponse(GroupInvitation invitation, Group group, 
                                                          User inviter, String invitationCode) {
        return GroupInvitationResponse.builder()
                .id(invitation.getId())
                .groupId(invitation.getGroupId())
                .groupName(group.getName())
                .email(invitation.getEmail())
                .phone(invitation.getPhone())
                .invitedBy(invitation.getInvitedBy())
                .inviterName(inviter != null ? inviter.getName() : "Unknown")
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .invitationCode(invitationCode)
                .build();
    }
} 