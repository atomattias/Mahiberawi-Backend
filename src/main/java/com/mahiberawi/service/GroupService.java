package com.mahiberawi.service;

import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupMemberResponse;
import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.dto.UserResponse;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.GroupMember;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.UserRole;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public GroupResponse createGroup(GroupRequest request, User creator) {
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .profilePicture(request.getProfilePicture())
                .isPublic(request.isPublic())
                .creator(creator)
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
        group.setProfilePicture(request.getProfilePicture());
        group.setPublic(request.isPublic());
        group.setJoinCode(request.getJoinCode());
        group.setInvitationLink(request.getInvitationLink());
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

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(GroupMemberRole.MEMBER);
        member.setStatus(GroupMemberStatus.ACTIVE);
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
            groups = groupRepository.findByIsPublicTrueAndNameContainingOrDescriptionContaining(
                    search, search);
        } else {
            groups = groupRepository.findByIsPublicTrue();
        }
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupResponse joinGroupWithCode(String code, User user) {
        Group group = groupRepository.findByJoinCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with code: " + code));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
                .build();
        groupMemberRepository.save(member);

        return mapToResponse(group);
    }

    @Transactional
    public GroupResponse joinGroupWithLink(String link, User user) {
        Group group = groupRepository.findByInvitationLink(link)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with link: " + link));

        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(GroupMemberRole.MEMBER)
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
                .profilePicture(group.getProfilePicture())
                .isPublic(group.isPublic())
                .joinCode(group.getJoinCode())
                .invitationLink(group.getInvitationLink())
                .creatorId(group.getCreator().getId())
                .creatorName(group.getCreator().getFullName())
                .memberCount(group.getMembers().size())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
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
                .userEmail(member.getUser().getEmail())
                .userName(member.getUser().getFullName())
                .userProfilePicture(member.getUser().getProfilePicture())
                .role(member.getRole())
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
                member.getUser().getFullName(), member.getGroup().getName());
        notificationService.createGroupNotification(member.getGroup().getCreator(), member.getGroup(), message);
    }

    private void notifyMemberRejected(GroupMember member) {
        String message = String.format("%s has declined the invitation to join the group '%s'",
                member.getUser().getFullName(), member.getGroup().getName());
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
                .members(group.getMembers().stream()
                        .map(member -> GroupMemberResponse.builder()
                                .id(member.getId())
                                .userId(member.getUser().getId())
                                .userEmail(member.getUser().getEmail())
                                .userName(member.getUser().getFullName())
                                .userProfilePicture(member.getUser().getProfilePicture())
                                .role(member.getRole())
                                .joinedAt(member.getJoinedAt())
                                .lastActiveAt(member.getUpdatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
} 