package com.mahiberawi.repository;

import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    List<Group> findByCreator(User creator);
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = ?1")
    List<Group> findByMemberId(String userId);
    
    @Query("SELECT g FROM Group g WHERE g.name LIKE %?1% OR g.description LIKE %?1%")
    List<Group> searchByNameOrDescription(String searchTerm);
} 