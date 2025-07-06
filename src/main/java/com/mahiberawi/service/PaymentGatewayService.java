package com.mahiberawi.service;

import com.mahiberawi.dto.payment.PaymentRequest;
import com.mahiberawi.dto.payment.PaymentResponse;
import com.mahiberawi.dto.payment.TeleBirrPaymentResponse;
import com.mahiberawi.dto.payment.VippsPaymentResponse;
import com.mahiberawi.entity.Payment;
import com.mahiberawi.entity.PaymentMethod;
import com.mahiberawi.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {
    private final PaymentService paymentService;
    private final TeleBirrService teleBirrService;
    private final VippsService vippsService;

    public Object initiatePayment(PaymentRequest request, User user) {
        Payment payment = paymentService.createPaymentEntity(request, user);
        
        switch (request.getMethod()) {
            case TELEBIRR:
                log.info("Initiating TeleBirr payment for transaction: {}", payment.getTransactionId());
                return teleBirrService.initiatePayment(payment);
                
            case VIPPS:
                log.info("Initiating Vipps payment for transaction: {}", payment.getTransactionId());
                return vippsService.initiatePayment(payment);
                
            case CASH:
            case BANK_TRANSFER:
            case MOBILE_PAYMENT:
            case CREDIT_CARD:
                log.info("Processing manual payment for transaction: {}", payment.getTransactionId());
                return paymentService.createPayment(request, user);
                
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + request.getMethod());
        }
    }

    public boolean verifyPayment(String transactionId, PaymentMethod method) {
        switch (method) {
            case TELEBIRR:
                return teleBirrService.verifyPayment(transactionId);
                
            case VIPPS:
                return vippsService.verifyPayment(transactionId);
                
            default:
                log.warn("Verification not supported for payment method: {}", method);
                return false;
        }
    }

    public PaymentResponse getPaymentStatus(String paymentId) {
        return paymentService.getPayment(paymentId);
    }
} 