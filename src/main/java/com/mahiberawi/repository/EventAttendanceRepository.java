package com.mahiberawi.repository;

import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.EventAttendance;
import com.mahiberawi.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, String> {
    List<EventAttendance> findByEvent(Event event);
    Optional<EventAttendance> findByEventAndUserId(Event event, String userId);
    int countByEventAndStatus(Event event, AttendanceStatus status);
} 