package com.mahiberawi.controller;

import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        PaymentResponse payment = paymentService.createPayment(request, user);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String id) {
        PaymentResponse payment = paymentService.processPayment(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable String id) {
        PaymentResponse payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        PaymentResponse payment = paymentService.getPayment(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(@AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUser(user);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<PaymentResponse>> getEventPayments(@PathVariable String eventId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByEvent(eventId);
        return ResponseEntity.ok(payments);
    }
} 