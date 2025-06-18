package com.mahiberawi.repository;

import com.mahiberawi.entity.Membership;
import com.mahiberawi.entity.MembershipStatus;
import com.mahiberawi.entity.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, String> {
    List<Membership> findByUserId(String userId);
    List<Membership> findByGroupId(String groupId);
    Optional<Membership> findByUserIdAndGroupId(String userId, String groupId);
    List<Membership> findByType(MembershipType type);
    List<Membership> findByUserIdAndType(String userId, MembershipType type);
    List<Membership> findByStatus(MembershipStatus status);
    List<Membership> findByEndDateBeforeAndStatus(LocalDateTime date, MembershipStatus status);
} 