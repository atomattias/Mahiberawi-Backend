package com.mahiberawi.service;

import com.mahiberawi.dto.payment.GroupPaymentRequest;
import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.entity.*;
import com.mahiberawi.entity.enums.GroupMemberRole;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupPaymentService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional
    public List<PaymentResponse> createGroupPaymentRequest(GroupPaymentRequest request, User admin) {
        // Validate group exists
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.getGroupId()));

        // Check if admin is actually an admin of the group
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(group, admin)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (adminMember.getRole() != GroupMemberRole.ADMIN && adminMember.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can create group payment requests");
        }

        // Get all active members of the group
        List<GroupMember> activeMembers = groupMemberRepository.findByGroupAndStatus(group, GroupMemberStatus.ACTIVE);

        // Filter members if specific targetUserIds are provided
        List<GroupMember> targetMembers = activeMembers;
        if (request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {
            targetMembers = activeMembers.stream()
                    .filter(member -> request.getTargetUserIds().contains(member.getUser().getId()))
                    .collect(Collectors.toList());
        }

        List<PaymentResponse> createdPayments = new ArrayList<>();

        // Create individual payment records for each member
        for (GroupMember member : targetMembers) {
            try {
                Payment payment = new Payment();
                payment.setAmount(request.getAmount());
                payment.setMethod(request.getMethod());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setPayer(member.getUser());
                payment.setGroup(group);
                payment.setDescription(request.getDescription());
                payment.setTransactionId(generateTransactionId());
                
                // Set due date if provided
                if (request.getDueDate() != null) {
                    // You might want to add a dueDate field to Payment entity
                    // For now, we'll store it in description
                    payment.setDescription(payment.getDescription() + " (Due: " + request.getDueDate() + ")");
                }

                Payment savedPayment = paymentRepository.save(payment);
                PaymentResponse paymentResponse = paymentService.mapToPaymentResponse(savedPayment);
                createdPayments.add(paymentResponse);

                // Send notification to member about the payment request
                sendPaymentRequestNotification(member.getUser(), group, request);

                log.info("Created payment request for user {} in group {}: {}", 
                    member.getUser().getId(), group.getId(), payment.getTransactionId());

            } catch (Exception e) {
                log.error("Failed to create payment for user {} in group {}: {}", 
                    member.getUser().getId(), group.getId(), e.getMessage());
            }
        }

        log.info("Created {} payment requests for group {}", createdPayments.size(), group.getId());
        return createdPayments;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getGroupPaymentRequests(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        List<Payment> payments = paymentRepository.findByGroupId(groupId);
        return payments.stream()
                .map(paymentService::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPendingPayments(User user) {
        List<Payment> pendingPayments = paymentRepository.findByPayerAndStatus(user, PaymentStatus.PENDING);
        return pendingPayments.stream()
                .map(paymentService::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getGroupPaymentStatistics(String groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if user is an admin or moderator
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, currentUser)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (member.getRole() != GroupMemberRole.ADMIN && member.getRole() != GroupMemberRole.MODERATOR) {
            throw new UnauthorizedException("Only admins and moderators can view payment statistics");
        }

        List<Payment> groupPayments = paymentRepository.findByGroupId(groupId);
        
        // Group payments by status
        long pendingCount = groupPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
        
        long completedCount = groupPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
        
        long cancelledCount = groupPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CANCELLED)
                .count();

        log.info("Group {} payment statistics - Pending: {}, Completed: {}, Cancelled: {}", 
            groupId, pendingCount, completedCount, cancelledCount);

        return groupPayments.stream()
                .map(paymentService::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    private void sendPaymentRequestNotification(User user, Group group, GroupPaymentRequest request) {
        try {
            String message = String.format(
                "New payment request in group '%s': %s - Amount: %s %s. Due: %s",
                group.getName(),
                request.getDescription(),
                request.getAmount(),
                request.getMethod(),
                request.getDueDate() != null ? request.getDueDate() : "No due date"
            );

            notificationService.createGroupNotification(user, group, message);
            
            log.info("Sent payment request notification to user {} for group {}", user.getId(), group.getId());
        } catch (Exception e) {
            log.error("Failed to send payment request notification to user {}: {}", user.getId(), e.getMessage());
        }
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
} 