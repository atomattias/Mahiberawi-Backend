package com.mahiberawi.service;

import com.mahiberawi.entity.*;
import com.mahiberawi.entity.enums.EqubRoundStatus;
import com.mahiberawi.entity.enums.EqubSelectionMethod;
import com.mahiberawi.entity.enums.GroupMemberStatus;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.exception.UnauthorizedException;
import com.mahiberawi.repository.EqubRoundRepository;
import com.mahiberawi.repository.GroupMemberRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EqubService {
    private final GroupRepository groupRepository;
    private final EqubRoundRepository equbRoundRepository;
    private final PaymentRepository paymentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final NotificationService notificationService;

    @Transactional
    public EqubRound startNewRound(String groupId, User admin) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!group.getIsEqubGroup()) {
            throw new IllegalStateException("This group is not configured for Equb");
        }

        // Check if admin has permission
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(group, admin)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (adminMember.getRole() != com.mahiberawi.entity.enums.GroupMemberRole.ADMIN) {
            throw new UnauthorizedException("Only admins can start Equb rounds");
        }

        // Get next round number
        Integer nextRoundNumber = group.getCurrentEqubRound() + 1;

        // Calculate expected amount (number of members * equb amount)
        List<GroupMember> activeMembers = groupMemberRepository.findByGroupAndStatus(group, GroupMemberStatus.ACTIVE);
        BigDecimal expectedAmount = group.getEqubAmount().multiply(new BigDecimal(activeMembers.size()));

        // Create new round
        EqubRound newRound = EqubRound.builder()
                .group(group)
                .roundNumber(nextRoundNumber)
                .expectedAmount(expectedAmount)
                .status(EqubRoundStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(group.getEqubPaymentDeadlineDays()))
                .paymentDeadline(LocalDateTime.now().plusDays(group.getEqubPaymentDeadlineDays()))
                .gracePeriodDays(group.getEqubGracePeriodDays())
                .penaltyAmount(group.getEqubPenaltyAmount())
                .build();

        EqubRound savedRound = equbRoundRepository.save(newRound);

        // Update group current round
        group.setCurrentEqubRound(nextRoundNumber);
        groupRepository.save(group);

        // Create payment requests for all members
        createEqubPaymentsForRound(savedRound, activeMembers);

        log.info("Started Equb round {} for group {}", nextRoundNumber, groupId);
        return savedRound;
    }

    @Transactional
    public EqubRound selectWinner(String groupId, User admin) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!group.getIsEqubGroup()) {
            throw new IllegalStateException("This group is not configured for Equb");
        }

        // Check if admin has permission
        GroupMember adminMember = groupMemberRepository.findByGroupAndUser(group, admin)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this group"));

        if (adminMember.getRole() != com.mahiberawi.entity.enums.GroupMemberRole.ADMIN) {
            throw new UnauthorizedException("Only admins can select Equb winners");
        }

        // Get current active round
        List<EqubRound> activeRounds = equbRoundRepository.findByGroupAndStatus(group, EqubRoundStatus.ACTIVE);
        if (activeRounds.isEmpty()) {
            throw new ResourceNotFoundException("No active Equb round found");
        }
        EqubRound currentRound = activeRounds.get(0);

        // Verify minimum payment threshold
        List<Payment> roundPayments = paymentRepository.findByEqubRoundEntity(currentRound);
        long paidMembers = roundPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();

        List<GroupMember> activeMembers = groupMemberRepository.findByGroupAndStatus(group, GroupMemberStatus.ACTIVE);
        
        if (paidMembers < activeMembers.size()) {
            throw new IllegalStateException("Not all members have paid. Cannot select winner yet.");
        }

        // Select winner based on method
        User winner = selectWinnerByMethod(group, currentRound, activeMembers);
        
        // Update round with winner
        currentRound.setWinner(winner);
        currentRound.setWinnerSelectedAt(LocalDateTime.now());
        currentRound.setStatus(EqubRoundStatus.COMPLETED);
        currentRound.setTotalAmount(currentRound.getExpectedAmount());

        EqubRound savedRound = equbRoundRepository.save(currentRound);

        // Update group
        group.setCurrentWinner(winner);
        group.setLastEqubDraw(LocalDateTime.now());
        groupRepository.save(group);

        // Notify winner
        notificationService.createGroupNotification(winner, group, 
            "Congratulations! You have won the Equb round " + currentRound.getRoundNumber());

        log.info("Selected winner {} for Equb round {} in group {}", winner.getId(), currentRound.getRoundNumber(), groupId);
        return savedRound;
    }

    private User selectWinnerByMethod(Group group, EqubRound round, List<GroupMember> members) {
        if (group.getEqubSelectionMethod() == EqubSelectionMethod.LOTTERY) {
            return selectWinnerByLottery(members);
        } else {
            return selectWinnerByFixedTurn(group, round, members);
        }
    }

    private User selectWinnerByLottery(List<GroupMember> members) {
        Random random = new Random();
        int randomIndex = random.nextInt(members.size());
        return members.get(randomIndex).getUser();
    }

    private User selectWinnerByFixedTurn(Group group, EqubRound round, List<GroupMember> members) {
        // For fixed turn, winner is based on round number and member count
        int memberCount = members.size();
        int winnerIndex = (round.getRoundNumber() - 1) % memberCount;
        return members.get(winnerIndex).getUser();
    }

    private void createEqubPaymentsForRound(EqubRound round, List<GroupMember> members) {
        for (GroupMember member : members) {
            Payment payment = new Payment();
            payment.setAmount(round.getGroup().getEqubAmount());
            payment.setMethod(com.mahiberawi.entity.PaymentMethod.TELEBIRR); // Default method
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPayer(member.getUser());
            payment.setGroup(round.getGroup());
            payment.setDescription("Equb Round " + round.getRoundNumber() + " Payment");
            payment.setTransactionId(generateTransactionId());
            payment.setEqubRound(round.getRoundNumber());
            payment.setIsEqubPayment(true);
            payment.setEqubRoundEntity(round);

            paymentRepository.save(payment);

            // Send notification
            notificationService.createGroupNotification(member.getUser(), round.getGroup(),
                "New Equb payment request for Round " + round.getRoundNumber() + ". Amount: " + round.getGroup().getEqubAmount());
        }
    }

    @Transactional(readOnly = true)
    public List<EqubRound> getGroupRounds(String groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        return equbRoundRepository.findByGroupOrderByRoundNumberDesc(group);
    }

    @Transactional(readOnly = true)
    public EqubRound getCurrentRound(String groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new UnauthorizedException("You are not a member of this group");
        }

        List<EqubRound> activeRounds = equbRoundRepository.findByGroupAndStatus(group, EqubRoundStatus.ACTIVE);
        return activeRounds.isEmpty() ? null : activeRounds.get(0);
    }

    @Transactional
    public void processLatePayments() {
        // Find all active rounds that have passed their deadline
        // Note: This method needs to be implemented in the repository
        // For now, we'll skip this functionality
        log.info("Late payment processing would be implemented here");
    }

    private String generateTransactionId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
} 