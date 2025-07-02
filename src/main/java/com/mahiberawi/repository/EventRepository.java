package com.mahiberawi.repository;

import com.mahiberawi.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByGroupId(String groupId);
    List<Event> findByStartTimeAfter(LocalDateTime startTime);
    List<Event> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Admin methods
    List<Event> findTop10ByOrderByCreatedAtDesc();
} 