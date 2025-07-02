package com.mahiberawi.service;

import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.Notification;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.NotificationType;
import com.mahiberawi.repository.NotificationRepository;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mahiberawi.entity.GroupMember;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createMessageNotification(User user, Message message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.MESSAGE);
        notification.setContent(String.format("New message from %s", 
            message.getSender() != null ? message.getSender().getName() : "Unknown"));
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

    // ========== NOTIFICATION MANAGEMENT METHODS ==========

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUser(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(String notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only mark your own notifications as read");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadFalse(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(String notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only delete your own notifications");
        }
        
        notificationRepository.delete(notification);
    }

    public Map<String, Boolean> getNotificationSettings(User user) {
        // For now, return default settings
        // In a real implementation, this would be stored in a separate table
        return Map.of(
            "emailNotifications", true,
            "pushNotifications", true,
            "smsNotifications", false,
            "eventReminders", true,
            "groupUpdates", true,
            "paymentNotifications", true
        );
    }

    @Transactional
    public void updateNotificationSettings(User user, Map<String, Boolean> settings) {
        // For now, just validate the settings
        // In a real implementation, this would be stored in a separate table
        if (settings == null || settings.isEmpty()) {
            throw new IllegalArgumentException("Settings cannot be null or empty");
        }
        
        // Validate setting keys
        for (String key : settings.keySet()) {
            if (!isValidSettingKey(key)) {
                throw new IllegalArgumentException("Invalid setting key: " + key);
            }
        }
    }

    private boolean isValidSettingKey(String key) {
        return key.equals("emailNotifications") ||
               key.equals("pushNotifications") ||
               key.equals("smsNotifications") ||
               key.equals("eventReminders") ||
               key.equals("groupUpdates") ||
               key.equals("paymentNotifications");
    }
} 