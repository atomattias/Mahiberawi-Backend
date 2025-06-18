package com.mahiberawi.repository;

import com.mahiberawi.entity.EventParticipant;
import com.mahiberawi.entity.EventParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, String> {
    List<EventParticipant> findByEventId(String eventId);
    List<EventParticipant> findByUserId(String userId);
    Optional<EventParticipant> findByEventIdAndUserId(String eventId, String userId);
    List<EventParticipant> findByEventIdAndStatus(String eventId, EventParticipantStatus status);
    long countByEventIdAndStatus(String eventId, EventParticipantStatus status);
} 