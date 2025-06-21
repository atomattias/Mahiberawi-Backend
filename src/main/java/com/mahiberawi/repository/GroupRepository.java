package com.mahiberawi.repository;

import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.enums.GroupPrivacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findByCreator(User creator);
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = ?1")
    List<Group> findByMemberId(String userId);
    
    @Query("SELECT g FROM Group g WHERE g.name LIKE %?1% OR g.description LIKE %?1%")
    List<Group> searchByNameOrDescription(String searchTerm);

    List<Group> findByMembersUser(User user);
    Optional<Group> findByCode(String code);
    Optional<Group> findByInviteLink(String inviteLink);
    List<Group> findByPrivacy(GroupPrivacy privacy);
    List<Group> findByPrivacyAndNameContainingOrPrivacyAndDescriptionContaining(GroupPrivacy privacy1, String name, GroupPrivacy privacy2, String description);
} 