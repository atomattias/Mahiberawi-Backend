package com.mahiberawi.repository;

import com.mahiberawi.entity.Payment;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.EqubRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByPayer(User payer);
    List<Payment> findByPayer_Id(String userId);
    List<Payment> findByEventId(String eventId);
    List<Payment> findByMembershipId(String membershipId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByPayerAndStatus(User payer, PaymentStatus status);
    List<Payment> findByGroupId(String groupId);
    List<Payment> findByPayer_Phone(String phoneNumber);
    
    // Equb-specific methods
    List<Payment> findByGroupIdAndIsEqubPaymentTrue(String groupId);
    List<Payment> findByGroupIdAndEqubRound(String groupId, Integer equbRound);
    List<Payment> findByGroupIdAndEqubRoundAndStatus(String groupId, Integer equbRound, PaymentStatus status);
    List<Payment> findByEqubRoundEntity(EqubRound equbRound);
    List<Payment> findByGroupIdAndIsLatePaymentTrue(String groupId);
} 