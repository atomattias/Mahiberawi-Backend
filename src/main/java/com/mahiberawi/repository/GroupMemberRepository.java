package com.mahiberawi.repository;

import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.GroupMember;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {
    List<GroupMember> findByGroup(Group group);
    List<GroupMember> findByUser(User user);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    boolean existsByGroupAndUser(Group group, User user);
    boolean existsByUserAndStatus(User user, GroupMemberStatus status);
    
    // Count methods
    int countByUserId(String userId);
} 