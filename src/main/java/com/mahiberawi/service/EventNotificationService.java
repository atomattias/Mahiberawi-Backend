package com.mahiberawi.service;

import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.EventAttendance;
import com.mahiberawi.entity.Notification;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.NotificationType;
import com.mahiberawi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventNotificationService {
    private final NotificationRepository notificationRepository;
    private final EventService eventService;

    @Scheduled(cron = "0 0 9 * * *") // Run at 9 AM every day
    @Transactional
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcomingEvents = eventService.getUpcomingEventsForReminders(now);

        for (Event event : upcomingEvents) {
            long daysUntilEvent = ChronoUnit.DAYS.between(now, event.getStartTime());
            
            if (daysUntilEvent == 1) {
                sendDayBeforeReminder(event);
            } else if (daysUntilEvent == 7) {
                sendWeekBeforeReminder(event);
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void sendEventUpdates() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> activeEvents = eventService.getActiveEvents(now);

        for (Event event : activeEvents) {
            checkAndNotifyCapacity(event);
            checkAndNotifyTimeChanges(event);
        }
    }

    private void sendDayBeforeReminder(Event event) {
        String message = String.format("Event '%s' is starting in 1 day at %s",
            event.getTitle(), event.getStartTime().toString());
        
        for (EventAttendance attendee : event.getAttendees()) {
            createNotification(
                attendee.getUser(),
                event,
                message,
                NotificationType.EVENT_REMINDER
            );
        }
    }

    private void sendWeekBeforeReminder(Event event) {
        String message = String.format("Event '%s' is starting in 1 week at %s",
            event.getTitle(), event.getStartTime().toString());
        
        for (EventAttendance attendee : event.getAttendees()) {
            createNotification(
                attendee.getUser(),
                event,
                message,
                NotificationType.EVENT_REMINDER
            );
        }
    }

    private void checkAndNotifyCapacity(Event event) {
        int maxParticipants = event.getMaxParticipants();
        if (maxParticipants > 0) {
            int currentParticipants = event.getAttendees().size();
            
            if (currentParticipants >= maxParticipants * 0.9) { // 90% capacity
                String message = String.format("'%s' is almost full! Only %d spots remaining.", 
                    event.getTitle(), maxParticipants - currentParticipants);
                
                createNotification(
                    event.getCreator(),
                    event,
                    message,
                    NotificationType.EVENT_CAPACITY
                );
            }
        }
    }

    private void checkAndNotifyTimeChanges(Event event) {
        // This would be called when event time is updated
        String message = String.format("The time for '%s' has been updated to %s", 
            event.getTitle(), event.getStartTime().toString());
        
        for (EventAttendance attendee : event.getAttendees()) {
            createNotification(
                attendee.getUser(),
                event,
                message,
                NotificationType.EVENT_UPDATE
            );
        }
    }

    private void createNotification(User user, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setContent(message);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private void createNotification(User user, Event event, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setType(type);
        notification.setContent(message);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
} 