package com.mahiberawi.controller;

import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.entity.User;
import com.mahiberawi.service.PaymentService;
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

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    private final PaymentService paymentService;

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
} 