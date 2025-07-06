package com.mahiberawi.repository;

import com.mahiberawi.entity.EqubRound;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.enums.EqubRoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EqubRoundRepository extends JpaRepository<EqubRound, String> {
    List<EqubRound> findByGroupOrderByRoundNumberDesc(Group group);
    List<EqubRound> findByGroupAndStatus(Group group, EqubRoundStatus status);
    Optional<EqubRound> findByGroupAndRoundNumber(Group group, Integer roundNumber);
    Optional<EqubRound> findByGroupAndStatusAndStartDateBefore(Group group, EqubRoundStatus status, LocalDateTime date);
    List<EqubRound> findByGroupAndEndDateBefore(Group group, LocalDateTime date);
    Optional<EqubRound> findFirstByGroupOrderByRoundNumberDesc(Group group);
} 