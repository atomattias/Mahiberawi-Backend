package com.mahiberawi.service;

import com.mahiberawi.dto.membership.MembershipRequest;
import com.mahiberawi.dto.membership.MembershipResponse;
import com.mahiberawi.entity.Membership;
import com.mahiberawi.entity.MembershipStatus;
import com.mahiberawi.entity.MembershipType;
import com.mahiberawi.entity.User;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.MembershipRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public MembershipResponse createMembership(MembershipRequest request, User creator) {
        User member = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Membership membership = new Membership();
        membership.setUser(member);
        membership.setType(request.getType());
        membership.setStartDate(request.getStartDate());
        membership.setEndDate(request.getEndDate());
        membership.setFee(request.getFee());
        membership.setDescription(request.getDescription());
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setCreatedBy(creator);

        Membership savedMembership = membershipRepository.save(membership);
        notifyMembershipCreated(savedMembership);
        return mapToMembershipResponse(savedMembership);
    }

    public MembershipResponse getMembership(String id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));
        return mapToMembershipResponse(membership);
    }

    public List<MembershipResponse> getMembershipsByUser(String userId) {
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(this::mapToMembershipResponse)
                .collect(Collectors.toList());
    }

    public List<MembershipResponse> getActiveMemberships() {
        List<Membership> memberships = membershipRepository.findByStatus(MembershipStatus.ACTIVE);
        return memberships.stream()
                .map(this::mapToMembershipResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MembershipResponse updateMembership(String id, MembershipRequest request) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));

        membership.setType(request.getType());
        membership.setStartDate(request.getStartDate());
        membership.setEndDate(request.getEndDate());
        membership.setFee(request.getFee());
        membership.setDescription(request.getDescription());

        Membership updatedMembership = membershipRepository.save(membership);
        notifyMembershipUpdated(updatedMembership);
        return mapToMembershipResponse(updatedMembership);
    }

    @Transactional
    public MembershipResponse renewMembership(String id, LocalDateTime newEndDate) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));

        membership.setEndDate(newEndDate);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setLastPaymentDate(LocalDateTime.now());
        membership.setNextPaymentDate(newEndDate);

        Membership renewedMembership = membershipRepository.save(membership);
        notifyMembershipRenewed(renewedMembership);
        return mapToMembershipResponse(renewedMembership);
    }

    @Transactional
    public MembershipResponse cancelMembership(String id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", id));

        membership.setStatus(MembershipStatus.CANCELLED);
        Membership cancelledMembership = membershipRepository.save(membership);
        notifyMembershipCancelled(cancelledMembership);
        return mapToMembershipResponse(cancelledMembership);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void checkExpiringMemberships() {
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        List<Membership> expiringMemberships = membershipRepository.findByEndDateBeforeAndStatus(
                thirtyDaysFromNow, MembershipStatus.ACTIVE);

        for (Membership membership : expiringMemberships) {
            notifyMembershipExpiring(membership);
        }
    }

    private void notifyMembershipCreated(Membership membership) {
        String message = String.format("Your membership has been created. Type: %s, Valid until: %s",
                membership.getType(), membership.getEndDate());
        notificationService.createMembershipNotification(membership.getUser(), message);
    }

    private void notifyMembershipUpdated(Membership membership) {
        String message = String.format("Your membership has been updated. New end date: %s",
                membership.getEndDate());
        notificationService.createMembershipNotification(membership.getUser(), message);
    }

    private void notifyMembershipRenewed(Membership membership) {
        String message = String.format("Your membership has been renewed until %s",
                membership.getEndDate());
        notificationService.createMembershipNotification(membership.getUser(), message);
    }

    private void notifyMembershipCancelled(Membership membership) {
        String message = "Your membership has been cancelled";
        notificationService.createMembershipNotification(membership.getUser(), message);
    }

    private void notifyMembershipExpiring(Membership membership) {
        String message = String.format("Your membership will expire on %s. Please renew to maintain access.",
                membership.getEndDate());
        notificationService.createMembershipNotification(membership.getUser(), message);
    }

    private MembershipResponse mapToMembershipResponse(Membership membership) {
        return MembershipResponse.builder()
                .id(membership.getId())
                .userId(membership.getUser().getId())
                .userEmail(membership.getUser().getEmail())
                .userFullName(membership.getUser().getFullName())
                .userProfilePicture(membership.getUser().getProfilePicture())
                .type(membership.getType())
                .status(membership.getStatus())
                .startDate(membership.getStartDate())
                .endDate(membership.getEndDate())
                .fee(membership.getFee())
                .description(membership.getDescription())
                .createdBy(membership.getCreatedBy().getFullName())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .lastPaymentDate(membership.getLastPaymentDate())
                .nextPaymentDate(membership.getNextPaymentDate())
                .build();
    }
} 