package com.mahiberawi.entity;

import com.mahiberawi.entity.enums.GroupType;
import com.mahiberawi.entity.enums.GroupPrivacy;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private GroupPrivacy privacy = GroupPrivacy.PRIVATE;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "invite_link")
    private String inviteLink;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "member_count")
    @Builder.Default
    private int memberCount = 0;

    @Column(name = "profile_picture")
    private String profilePicture;

    // Group settings
    @Column(name = "allow_event_creation")
    @Builder.Default
    private Boolean allowEventCreation = true;

    @Column(name = "allow_member_invites")
    @Builder.Default
    private Boolean allowMemberInvites = true;

    @Column(name = "allow_message_posting")
    @Builder.Default
    private Boolean allowMessagePosting = true;

    @Column(name = "payment_required")
    @Builder.Default
    private Boolean paymentRequired = false;

    @Column(name = "require_approval")
    @Builder.Default
    private Boolean requireApproval = false;

    @Column(name = "monthly_dues", precision = 10, scale = 2)
    private BigDecimal monthlyDues;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Payment> payments = new HashSet<>();

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