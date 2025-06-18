package com.mahiberawi.service;

import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Membership;
import com.mahiberawi.entity.Payment;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.PaymentMethod;
import com.mahiberawi.entity.PaymentStatus;
import com.mahiberawi.entity.MembershipStatus;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.EventRepository;
import com.mahiberawi.repository.MembershipRepository;
import com.mahiberawi.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, User payer) {
        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPayer(payer);
        payment.setDescription(request.getDescription());
        payment.setTransactionId(generateTransactionId());

        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));
            payment.setEvent(event);
        }

        if (request.getMembershipId() != null) {
            Membership membership = membershipRepository.findById(request.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", request.getMembershipId()));
            payment.setMembership(membership);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Here you would integrate with a payment gateway
        // For now, we'll simulate a successful payment
        payment.setStatus(PaymentStatus.COMPLETED);
        Payment processedPayment = paymentRepository.save(payment);

        return mapToPaymentResponse(processedPayment);
    }

    @Transactional
    public PaymentResponse cancelPayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment cancelledPayment = paymentRepository.save(payment);

        return mapToPaymentResponse(cancelledPayment);
    }

    public PaymentResponse getPayment(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByUser(User user) {
        List<Payment> payments = paymentRepository.findByPayer(user);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByEvent(String eventId) {
        List<Payment> payments = paymentRepository.findByEventId(eventId);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getPayer().getId())
                .userFullName(payment.getPayer().getFullName())
                .userEmail(payment.getPayer().getEmail())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .eventId(payment.getEvent() != null ? payment.getEvent().getId() : null)
                .eventName(payment.getEvent() != null ? payment.getEvent().getTitle() : null)
                .description(payment.getDescription())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
} 