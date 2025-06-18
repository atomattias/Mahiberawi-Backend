package com.mahiberawi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status = GroupStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<GroupMember> members = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Payment> payments = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Message> messages = new HashSet<>();
} 