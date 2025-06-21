package com.mahiberawi.service;

import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.Notification;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.NotificationType;
import com.mahiberawi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mahiberawi.entity.GroupMember;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createMessageNotification(User user, Message message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.MESSAGE);
        notification.setContent(String.format("New message from %s", message.getSender().getName()));
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createGroupNotification(User user, Group group, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.GROUP);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createEventNotification(User user, Event event, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setType(NotificationType.EVENT);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createSystemNotification(User user, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createMembershipNotification(User user, String content) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public void sendGroupInvitationNotification(GroupMember member) {
        // Implementation of sendGroupInvitationNotification method
    }

    public void sendRoleUpdateNotification(GroupMember member) {
        // Implementation of sendRoleUpdateNotification method
    }

    public void sendMemberRemovedNotification(GroupMember member) {
        // Implementation of sendMemberRemovedNotification method
    }

    public void sendMemberLeftNotification(GroupMember member) {
        // Implementation of sendMemberLeftNotification method
    }
} 