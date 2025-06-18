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

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createMessageNotification(User user, Message message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.MESSAGE);
        notification.setContent(String.format("New message from %s", message.getSender().getFullName()));
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
} 