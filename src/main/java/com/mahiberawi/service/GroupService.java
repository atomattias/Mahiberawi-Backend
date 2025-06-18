package com.mahiberawi.service;

import com.mahiberawi.dto.group.GroupMemberRequest;
import com.mahiberawi.dto.group.GroupMemberResponse;
import com.mahiberawi.dto.group.GroupRequest;
import com.mahiberawi.dto.group.GroupResponse;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.GroupMember;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreator(creator);

        Group savedGroup = groupRepository.save(group);

        // Add creator as admin
        GroupMember adminMember = new GroupMember();
        adminMember.setGroup(savedGroup);
        adminMember.setUser(creator);
        adminMember.setRole(GroupMemberRole.ADMIN);
        adminMember.setStatus(GroupMemberStatus.ACTIVE);
        groupMemberRepository.save(adminMember);

        return mapToGroupResponse(savedGroup);
    }

    @Transactional
    public GroupResponse inviteMember(String groupId, GroupMemberRequest request, User inviter) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Check if inviter has permission
        GroupMember inviterMember = groupMemberRepository.findByGroupAndUser(group, inviter)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", inviter.getId()));

        if (inviterMember.getRole() != GroupMemberRole.ADMIN) {
            throw new IllegalStateException("Only group admins can invite members");
        }

        User invitee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupAndUser(group, invitee)) {
            throw new IllegalStateException("User is already a member of this group");
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(invitee);
        member.setRole(request.getRole());
        member.setStatus(GroupMemberStatus.PENDING);
        member.setInvitedBy(inviter);
        member.setInvitationMessage(request.getInvitationMessage());

        GroupMember savedMember = groupMemberRepository.save(member);
        notifyMemberInvited(savedMember);
        return mapToGroupResponse(group);
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
        return mapToGroupResponse(group);
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
        return mapToGroupResponse(group);
    }

    @Transactional
    public GroupResponse updateMemberRole(String groupId, String userId, GroupMemberRole newRole, User updater) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Check if updater has permission
        GroupMember updaterMember = groupMemberRepository.findByGroupAndUser(group, updater)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", updater.getId()));

        if (updaterMember.getRole() != GroupMemberRole.ADMIN) {
            throw new IllegalStateException("Only group admins can update member roles");
        }

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, 
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)))
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", userId));

        member.setRole(newRole);
        GroupMember updatedMember = groupMemberRepository.save(member);
        notifyMemberRoleUpdated(updatedMember);
        return mapToGroupResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(String groupId, String userId, User remover) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        // Check if remover has permission
        GroupMember removerMember = groupMemberRepository.findByGroupAndUser(group, remover)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", remover.getId()));

        if (removerMember.getRole() != GroupMemberRole.ADMIN) {
            throw new IllegalStateException("Only group admins can remove members");
        }

        GroupMember member = groupMemberRepository.findByGroupAndUser(group,
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)))
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "userId", userId));

        groupMemberRepository.delete(member);
        notifyMemberRemoved(member);
        return mapToGroupResponse(group);
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
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        return mapToGroupResponse(group);
    }

    @Transactional
    public GroupResponse updateGroup(String groupId, GroupRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group = groupRepository.save(group);

        return mapToGroupResponse(group);
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

        return mapToGroupResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", "id", groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new ResourceNotFoundException("Group member", "id", userId));

        groupMemberRepository.delete(member);
        return mapToGroupResponse(group);
    }

    public List<GroupResponse> getUserGroups(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return groupRepository.findByMemberId(userId).stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    private GroupResponse mapToGroupResponse(Group group) {
        List<GroupMemberResponse> members = group.getMembers().stream()
                .map(this::mapToGroupMemberResponse)
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .creatorId(group.getCreator().getId())
                .creatorName(group.getCreator().getFullName())
                .status(group.getStatus())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .members(members)
                .memberCount(members.size())
                .eventCount(group.getEvents().size())
                .build();
    }

    private GroupMemberResponse mapToGroupMemberResponse(GroupMember member) {
        return GroupMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .userEmail(member.getUser().getEmail())
                .userName(member.getUser().getFullName())
                .userProfilePicture(member.getUser().getProfilePicture())
                .role(member.getRole())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .build();
    }
} 