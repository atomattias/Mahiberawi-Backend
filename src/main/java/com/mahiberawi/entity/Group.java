package com.mahiberawi.entity;

import com.mahiberawi.entity.enums.GroupType;
import com.mahiberawi.entity.enums.GroupPrivacy;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    private GroupPrivacy privacy = GroupPrivacy.PRIVATE;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "invite_link")
    private String inviteLink;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "member_count")
    private int memberCount = 0;

    @Column(name = "profile_picture")
    private String profilePicture;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Message> messages = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (code == null) {
            code = generateCode();
        }
        if (inviteLink == null) {
            inviteLink = generateInviteLink();
        }
        if (createdBy == null && creator != null) {
            createdBy = creator.getId();
        }
    }

    private String generateCode() {
        // Generate a random 6-character alphanumeric code
        return java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateInviteLink() {
        // Generate a unique invitation link
        return java.util.UUID.randomUUID().toString();
    }
} 