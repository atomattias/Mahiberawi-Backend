package com.mahiberawi.repository;

import com.mahiberawi.entity.GroupInvitation;
import com.mahiberawi.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, String> {
    
    // Find invitations by group
    List<GroupInvitation> findByGroupId(String groupId);
    
    // Find invitations by inviter
    List<GroupInvitation> findByInvitedBy(String invitedBy);
    
    // Find invitations by email
    List<GroupInvitation> findByEmail(String email);
    
    // Find invitations by phone
    List<GroupInvitation> findByPhone(String phone);
    
    // Find pending invitations by group
    List<GroupInvitation> findByGroupIdAndStatus(String groupId, InvitationStatus status);
    
    // Find pending invitations by email
    List<GroupInvitation> findByEmailAndStatus(String email, InvitationStatus status);
    
    // Find pending invitations by phone
    List<GroupInvitation> findByPhoneAndStatus(String phone, InvitationStatus status);
    
    // Find expired invitations
    @Query("SELECT gi FROM GroupInvitation gi WHERE gi.expiresAt < :now AND gi.status = 'PENDING'")
    List<GroupInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);
    
    // Find invitation by email and group
    Optional<GroupInvitation> findByEmailAndGroupId(String email, String groupId);
    
    // Find invitation by phone and group
    Optional<GroupInvitation> findByPhoneAndGroupId(String phone, String groupId);
    
    // Find invitation by invitation code
    Optional<GroupInvitation> findByInvitationCode(String invitationCode);
    
    // Find pending invitation by invitation code
    Optional<GroupInvitation> findByInvitationCodeAndStatus(String invitationCode, InvitationStatus status);
    
    // Count pending invitations by group
    int countByGroupIdAndStatus(String groupId, InvitationStatus status);
    
    // Delete expired invitations
    @Modifying
    @Query("DELETE FROM GroupInvitation gi WHERE gi.expiresAt < :now AND gi.status = 'PENDING'")
    int deleteExpiredInvitations(@Param("now") LocalDateTime now);
    
    // Delete invitations by group
    @Modifying
    @Query("DELETE FROM GroupInvitation gi WHERE gi.groupId = :groupId")
    int deleteByGroupId(@Param("groupId") String groupId);
} 