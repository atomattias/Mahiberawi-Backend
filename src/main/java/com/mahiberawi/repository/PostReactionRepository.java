package com.mahiberawi.repository;

import com.mahiberawi.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, String> {
    
    // Find reactions by post
    List<PostReaction> findByPostId(String postId);
    
    // Find reactions by user
    List<PostReaction> findByUserId(String userId);
    
    // Find reaction by post and user
    Optional<PostReaction> findByPostIdAndUserId(String postId, String userId);
    
    // Find reaction by post, user and type
    Optional<PostReaction> findByPostIdAndUserIdAndReactionType(String postId, String userId, String reactionType);
    
    // Count reactions by post and type
    long countByPostIdAndReactionType(String postId, String reactionType);
    
    // Delete reaction by post, user and type
    void deleteByPostIdAndUserIdAndReactionType(String postId, String userId, String reactionType);
    
    // Delete all reactions for a post
    void deleteByPostId(String postId);
    
    // Get reaction counts by post
    @Query("SELECT pr.reactionType, COUNT(pr) FROM PostReaction pr WHERE pr.postId = :postId GROUP BY pr.reactionType")
    List<Object[]> getReactionCountsByPost(@Param("postId") String postId);
    
    // Get user's reaction to a post
    @Query("SELECT pr.reactionType FROM PostReaction pr WHERE pr.postId = :postId AND pr.userId = :userId")
    Optional<String> getUserReaction(@Param("postId") String postId, @Param("userId") String userId);
} 