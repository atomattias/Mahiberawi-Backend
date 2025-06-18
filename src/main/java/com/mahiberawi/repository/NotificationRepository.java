package com.mahiberawi.repository;

import com.mahiberawi.entity.Notification;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndType(User user, NotificationType type);
    List<Notification> findByUserAndReadFalse(User user);
} 