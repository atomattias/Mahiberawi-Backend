package com.mahiberawi.service;

import com.mahiberawi.dto.group.*;
import com.mahiberawi.entity.*;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.entity.enums.GroupPrivacy;
import com.mahiberawi.entity.enums.GroupType;
import com.mahiberawi.entity.enums.InvitationStatus;
import com.mahiberawi.entity.MessageType;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import com.mahiberawi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

import com.mahiberawi.dto.group.JoinGroupRequest;
import com.mahiberawi.dto.ApiResponse;

import java.util.Random;
import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.Payment;
import com.mahiberawi.repository.EventRepository;
import com.mahiberawi.repository.MessageRepository;
import com.mahiberawi.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final MessageRepository messageRepository;
    private final PaymentRepository paymentRepository;

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
                .memberCount(0) // Initialize member count to 0
                .build();

        // Set group settings if provided
        if (request.getSettings() != null) {
            GroupRequest.GroupSettings settings = request.getSettings();
            group.setAllowEventCreation(settings.getAllowEventCreation());
            group.setAllowMemberInvites(settings.getAllowMemberInvites());
            group.setAllowMessagePosting(settings.getAllowMessagePosting());
            group.setPaymentRequired(settings.getPaymentRequired());
            group.setRequireApproval(settings.getRequireApproval());
            group.setMonthlyDues(settings.getMonthlyDues());
        }

        group = groupRepository.save(group);

        // Add creator as admin
        GroupMember creatorMember = GroupMember.builder()
                .groupId(group.getId()) // Set the groupId string field
                .userId(creator.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(creator) // Set the user object
                .role(GroupMemberRole.ADMIN)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(creatorMember);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        group = groupRepository.save(group);

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
        
        // Update group settings if provided
        if (request.getSettings() != null) {
            GroupRequest.GroupSettings settings = request.getSettings();
            group.setAllowEventCreation(settings.getAllowEventCreation());
            group.setAllowMemberInvites(settings.getAllowMemberInvites());
            group.setAllowMessagePosting(settings.getAllowMessagePosting());
            group.setPaymentRequired(settings.getPaymentRequired());
            group.setRequireApproval(settings.getRequireApproval());
            group.setMonthlyDues(settings.getMonthlyDues());
        }
        
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
                .groupId(groupId) // Set the groupId string field
                .userId(userId) // Set the userId string field
                .group(group) // Set the group object
                .user(user) // Set the user object
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        group = groupRepository.save(group);

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
                .groupId(group.getId()) // Set the groupId string field
                .userId(user.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(user) // Set the user object
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        group = groupRepository.save(group);

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
                .groupId(group.getId()) // Set the groupId string field
                .userId(user.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(user) // Set the user object
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        group = groupRepository.save(group);

        return mapToResponse(group);
    }

    public List<GroupResponse> getGroupsByUser(User user) {
        List<Group> groups = groupRepository.findByMembersUser(user);
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GroupMemberResponse> getUserMemberships(User user) {
        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        return memberships.stream()
                .map(this::mapToMemberResponse)
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
                .allowEventCreation(group.getAllowEventCreation())
                .allowMemberInvites(group.getAllowMemberInvites())
                .allowMessagePosting(group.getAllowMessagePosting())
                .paymentRequired(group.getPaymentRequired())
                .requireApproval(group.getRequireApproval())
                .monthlyDues(group.getMonthlyDues())
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
                .groupId(group.getId()) // Set the groupId string field
                .userId(userToInvite.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(userToInvite) // Set the user object
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

        // Update group member count
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        groupRepository.save(group);

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

        // Update group member count (only if group is not being deleted)
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        groupRepository.save(group);

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
                .allowEventCreation(group.getAllowEventCreation())
                .allowMemberInvites(group.getAllowMemberInvites())
                .allowMessagePosting(group.getAllowMessagePosting())
                .paymentRequired(group.getPaymentRequired())
                .requireApproval(group.getRequireApproval())
                .monthlyDues(group.getMonthlyDues())
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
                .groupId(group.getId()) // Set the groupId string field
                .userId(user.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(user) // Set the user object
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
                            .groupId(group.getId()) // Set the groupId string field
                            .userId(currentUser.getId()) // Set the userId string field
                            .group(group) // Set the group object
                            .user(currentUser) // Set the user object
                            .role(GroupMemberRole.MEMBER)
                            .status(GroupMemberStatus.ACTIVE)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    groupMemberRepository.save(member);
                    
                    // Update group member count
                    group.setMemberCount(group.getMemberCount() + 1);
                    group = groupRepository.save(group);
                    
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
                    .groupId(group.getId()) // Set the groupId string field
                    .userId(user.getId()) // Set the userId string field
                    .group(group) // Set the group object
                    .user(user) // Set the user object
                    .role(GroupMemberRole.MEMBER)
                    .status(GroupMemberStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build();
            groupMemberRepository.save(member);
            
            // Update group member count
            group.setMemberCount(group.getMemberCount() + 1);
            group = groupRepository.save(group);
            
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
                .groupId(group.getId()) // Set the groupId string field
                .userId(user.getId()) // Set the userId string field
                .group(group) // Set the group object
                .user(user) // Set the user object
                .role(GroupMemberRole.MEMBER)
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        group = groupRepository.save(group);

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

    // ========== GROUP-SPECIFIC ACTIVITIES METHODS ==========

    public List<com.mahiberawi.dto.event.EventResponse> getGroupEvents(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return group.getEvents().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public com.mahiberawi.dto.event.EventResponse createGroupEvent(String groupId, 
            com.mahiberawi.dto.event.EventRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Check if user has permission to create events
        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can create events");
        }

        // Check if group allows event creation
        if (!group.getAllowEventCreation()) {
            throw new UnauthorizedException("Event creation is not allowed in this group");
        }

        // Create event
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setGroup(group);
        event.setCreator(currentUser);

        Event savedEvent = eventRepository.save(event);
        return mapToEventResponse(savedEvent);
    }

    public List<com.mahiberawi.dto.message.MessageResponse> getGroupPosts(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return group.getMessages().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public com.mahiberawi.dto.message.MessageResponse createGroupPost(String groupId, 
            com.mahiberawi.dto.message.MessageRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Check if group allows message posting
        if (!group.getAllowMessagePosting()) {
            throw new UnauthorizedException("Message posting is not allowed in this group");
        }

        // Create message
        Message message = new Message();
        message.setContent(request.getContent());
        message.setType(MessageType.GROUP);
        message.setSender(currentUser);
        message.setGroup(group);

        Message savedMessage = messageRepository.save(message);
        return mapToMessageResponse(savedMessage);
    }

    public List<com.mahiberawi.dto.payment.PaymentResponse> getGroupPayments(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return paymentRepository.findByGroupId(groupId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public com.mahiberawi.dto.payment.PaymentResponse createGroupPayment(String groupId, 
            com.mahiberawi.dto.payment.PaymentRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        // Check if user has permission to create payments
        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can create payments");
        }

        // Create payment
        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPayer(currentUser);
        payment.setGroup(group);
        payment.setDescription(request.getDescription());
        payment.setTransactionId(generateTransactionId());

        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    // ========== ENHANCED MEMBER MANAGEMENT METHODS ==========

    @Transactional
    public GroupMemberResponse addGroupMember(String groupId, GroupMemberRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if current user has permission to add members
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN && currentMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can add members");
        }

        // Find user to add
        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupAndUser(group, userToAdd)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        // Create member record
        GroupMember member = GroupMember.builder()
                .groupId(group.getId())
                .userId(userToAdd.getId())
                .group(group)
                .user(userToAdd)
                .role(request.getRole())
                .status(GroupMemberStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();

        member = groupMemberRepository.save(member);

        // Update group member count
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);

        return mapToMemberResponse(member);
    }

    @Transactional
    public GroupMemberResponse removeGroupMember(String groupId, String memberId, User currentUser) {
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

        // Update group member count
        group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
        groupRepository.save(group);

        return mapToMemberResponse(memberToRemove);
    }

    @Transactional
    public GroupMemberResponse updateMemberRole(String groupId, String memberId, 
            UpdateRoleRequest request, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if current user has permission to update roles
        GroupMember currentMember = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (currentMember.getRole() != GroupMemberRole.ADMIN) {
            throw new UnauthorizedException("Only admins can update member roles");
        }

        // Find member to update
        GroupMember memberToUpdate = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        // Check if member belongs to the group
        if (!memberToUpdate.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Member does not belong to this group");
        }

        // Prevent demoting the last admin
        if (memberToUpdate.getRole() == GroupMemberRole.ADMIN && request.getRole() != GroupMemberRole.ADMIN) {
            long adminCount = group.getMembers().stream()
                    .filter(m -> m.getRole() == GroupMemberRole.ADMIN)
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last admin");
            }
        }

        // Update role
        memberToUpdate.setRole(request.getRole());
        GroupMember updatedMember = groupMemberRepository.save(memberToUpdate);

        return mapToMemberResponse(updatedMember);
    }

    // ========== USER AGGREGATED METHODS (HOME SCREEN) ==========

    public List<com.mahiberawi.dto.event.EventResponse> getUserGroupEvents(User currentUser) {
        List<Group> userGroups = groupRepository.findByMemberId(currentUser.getId());
        List<Event> allEvents = new ArrayList<>();
        
        for (Group group : userGroups) {
            allEvents.addAll(group.getEvents());
        }
        
        return allEvents.stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    public List<com.mahiberawi.dto.message.MessageResponse> getUserGroupPosts(User currentUser) {
        List<Group> userGroups = groupRepository.findByMemberId(currentUser.getId());
        List<Message> allMessages = new ArrayList<>();
        
        for (Group group : userGroups) {
            allMessages.addAll(group.getMessages());
        }
        
        return allMessages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    public List<com.mahiberawi.dto.payment.PaymentResponse> getUserGroupPayments(User currentUser) {
        List<Group> userGroups = groupRepository.findByMemberId(currentUser.getId());
        List<Payment> allPayments = new ArrayList<>();
        
        for (Group group : userGroups) {
            allPayments.addAll(paymentRepository.findByGroupId(group.getId()));
        }
        
        return allPayments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private com.mahiberawi.dto.event.EventResponse mapToEventResponse(Event event) {
        return com.mahiberawi.dto.event.EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .maxParticipants(event.getMaxParticipants())
                .groupId(event.getGroup().getId())
                .groupName(event.getGroup().getName())
                .creatorId(event.getCreator().getId())
                .creatorName(event.getCreator().getName())
                .createdAt(event.getCreatedAt())
                .status(event.getStatus())
                .build();
    }

    private com.mahiberawi.dto.message.MessageResponse mapToMessageResponse(Message message) {
        return com.mahiberawi.dto.message.MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderName(message.getSender().getName())
                .senderId(message.getSender().getId())
                .createdAt(message.getCreatedAt())
                .groupId(message.getGroup() != null ? message.getGroup().getId() : null)
                .build();
    }

    private com.mahiberawi.dto.payment.PaymentResponse mapToPaymentResponse(Payment payment) {
        return com.mahiberawi.dto.payment.PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .description(payment.getDescription())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .userId(payment.getPayer().getId())
                .build();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
} 