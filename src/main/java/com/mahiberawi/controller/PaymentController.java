package com.mahiberawi.controller;

import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.dto.payment.GroupPaymentRequest;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.Payment;
import com.mahiberawi.service.PaymentService;
import com.mahiberawi.service.TeleBirrService;
import com.mahiberawi.service.VippsService;
import com.mahiberawi.service.GroupPaymentService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;
    private final TeleBirrService teleBirrService;
    private final VippsService vippsService;
    private final GroupPaymentService groupPaymentService;

    @Operation(
        summary = "Create a new payment",
        description = "Creates a new payment record for an event or membership"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment created successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Related event or membership not found")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Parameter(description = "Payment creation details", required = true)
            @Valid @RequestBody PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        PaymentResponse payment = paymentService.createPayment(request, user);
        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Process a payment",
        description = "Processes a pending payment and updates its status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment processed successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Payment cannot be processed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "ID of the payment to process", required = true)
            @PathVariable String id) {
        PaymentResponse payment = paymentService.processPayment(id);
        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Cancel a payment",
        description = "Cancels a pending payment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment cancelled successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "ID of the payment to cancel", required = true)
            @PathVariable String id) {
        PaymentResponse payment = paymentService.cancelPayment(id);
        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Get payment details",
        description = "Retrieves detailed information about a specific payment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment found",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "ID of the payment to retrieve", required = true)
            @PathVariable String id) {
        PaymentResponse payment = paymentService.getPayment(id);
        return ResponseEntity.ok(payment);
    }

    @Operation(
        summary = "Get user's payments",
        description = "Retrieves all payments made by the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/user")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUser(user);
        return ResponseEntity.ok(payments);
    }

    @Operation(
        summary = "Get event's payments",
        description = "Retrieves all payments associated with a specific event"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = PaymentResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<PaymentResponse>> getEventPayments(
            @Parameter(description = "ID of the event", required = true)
            @PathVariable String eventId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByEvent(eventId);
        return ResponseEntity.ok(payments);
    }

    // ========== TELEBIRR PAYMENT ENDPOINTS ==========

    @Operation(
        summary = "Initiate Telebirr payment",
        description = "Initiates a payment using Telebirr payment gateway"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment initiated successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.payment.TeleBirrPaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Payment gateway error")
    })
    @PostMapping("/telebirr/initiate")
    public ResponseEntity<com.mahiberawi.dto.payment.TeleBirrPaymentResponse> initiateTeleBirrPayment(
            @Parameter(description = "Payment details", required = true)
            @Valid @RequestBody PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        Payment payment = paymentService.createPaymentEntity(request, user);
        com.mahiberawi.dto.payment.TeleBirrPaymentResponse response = teleBirrService.initiatePayment(payment);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Check Telebirr payment status",
        description = "Checks the status of a Telebirr payment transaction"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/telebirr/status/{transactionId}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> checkTeleBirrPaymentStatus(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable String transactionId) {
        boolean isSuccessful = teleBirrService.verifyPayment(transactionId);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Payment status checked successfully")
                .data(Map.of("transactionId", transactionId, "status", isSuccessful ? "SUCCESS" : "FAILED"))
                .build());
    }

    @Operation(
        summary = "Verify phone number for Telebirr",
        description = "Verifies a phone number for Telebirr payment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Phone verification successful",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid phone number"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/telebirr/verify-phone")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> verifyPhoneForTeleBirr(
            @Parameter(description = "Phone number to verify", required = true)
            @RequestParam String phoneNumber) {
        // This would integrate with Telebirr's phone verification service
        // For now, we'll return a mock response
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Phone number verified successfully")
                .data(Map.of("phoneNumber", phoneNumber, "verified", true))
                .build());
    }

    @Operation(
        summary = "Get Telebirr payment history",
        description = "Retrieves payment history for a specific phone number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment history retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No payments found")
    })
    @GetMapping("/telebirr/payment/history/{phoneNumber}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getTeleBirrPaymentHistory(
            @Parameter(description = "Phone number", required = true)
            @PathVariable String phoneNumber) {
        List<PaymentResponse> payments = paymentService.getPaymentsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Payment history retrieved successfully")
                .data(payments)
                .build());
    }

    // ========== VIPPS PAYMENT ENDPOINTS ==========

    @Operation(
        summary = "Initiate Vipps payment",
        description = "Initiates a payment using Vipps payment gateway"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment initiated successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.payment.VippsPaymentResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Payment gateway error")
    })
    @PostMapping("/vipps/initiate")
    public ResponseEntity<com.mahiberawi.dto.payment.VippsPaymentResponse> initiateVippsPayment(
            @Parameter(description = "Payment details", required = true)
            @Valid @RequestBody PaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        Payment payment = paymentService.createPaymentEntity(request, user);
        com.mahiberawi.dto.payment.VippsPaymentResponse response = vippsService.initiatePayment(payment);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Check Vipps payment status",
        description = "Checks the status of a Vipps payment transaction"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment status retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/vipps/status/{transactionId}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> checkVippsPaymentStatus(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable String transactionId) {
        boolean isSuccessful = vippsService.verifyPayment(transactionId);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Payment status checked successfully")
                .data(Map.of("transactionId", transactionId, "status", isSuccessful ? "SUCCESS" : "FAILED"))
                .build());
    }

    @Operation(
        summary = "Get Vipps payment history",
        description = "Retrieves payment history for a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment history retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No payments found")
    })
    @GetMapping("/vipps/payment/history/{userId}")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getVippsPaymentHistory(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId).stream()
                .filter(payment -> payment.getMethod() == com.mahiberawi.entity.PaymentMethod.VIPPS)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Payment history retrieved successfully")
                .data(payments)
                .build());
    }

    // ========== GROUP PAYMENT ENDPOINTS ==========

    @Operation(
        summary = "Create group payment request",
        description = "Group administrators can create payment requests for group members"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group payment request created successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to create group payments"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/group/create")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> createGroupPaymentRequest(
            @Parameter(description = "Group payment request details", required = true)
            @Valid @RequestBody GroupPaymentRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> createdPayments = groupPaymentService.createGroupPaymentRequest(request, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group payment request created successfully")
                .data(Map.of(
                    "groupId", request.getGroupId(),
                    "amount", request.getAmount(),
                    "method", request.getMethod(),
                    "description", request.getDescription(),
                    "createdBy", user.getId(),
                    "createdPayments", createdPayments.size(),
                    "payments", createdPayments
                ))
                .build());
    }

    @Operation(
        summary = "Get group payment requests",
        description = "Retrieves all payment requests for a specific group"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group payment requests retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/group/{groupId}/requests")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getGroupPaymentRequests(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = groupPaymentService.getGroupPaymentRequests(groupId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group payment requests retrieved successfully")
                .data(payments)
                .build());
    }

    @Operation(
        summary = "Get user's pending payments",
        description = "Retrieves all pending payments for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pending payments retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/pending")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getPendingPayments(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = groupPaymentService.getUserPendingPayments(user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Pending payments retrieved successfully")
                .data(payments)
                .build());
    }

    @Operation(
        summary = "Get group payment statistics",
        description = "Retrieves payment statistics for a specific group. Only admins and moderators can view statistics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = com.mahiberawi.dto.ApiResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not authorized to view statistics"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/group/{groupId}/statistics")
    public ResponseEntity<com.mahiberawi.dto.ApiResponse> getGroupPaymentStatistics(
            @Parameter(description = "Group ID", required = true)
            @PathVariable String groupId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        List<PaymentResponse> payments = groupPaymentService.getGroupPaymentStatistics(groupId, user);
        return ResponseEntity.ok(com.mahiberawi.dto.ApiResponse.builder()
                .success(true)
                .message("Group payment statistics retrieved successfully")
                .data(payments)
                .build());
    }
} 