package com.mahiberawi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahiberawi.config.VippsConfig;
import com.mahiberawi.dto.payment.VippsPaymentRequest;
import com.mahiberawi.dto.payment.VippsPaymentResponse;
import com.mahiberawi.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VippsService {
    private final VippsConfig vippsConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public VippsPaymentResponse initiatePayment(Payment payment) {
        try {
            String accessToken = getAccessToken();
            
            // Build Vipps payment request
            Map<String, Object> merchantInfo = new HashMap<>();
            merchantInfo.put("merchantId", vippsConfig.getMerchantId());
            merchantInfo.put("callbackPrefix", vippsConfig.getNotifyUrl());
            merchantInfo.put("fallBack", vippsConfig.getFallbackUrl());
            merchantInfo.put("consentRemovalPrefix", vippsConfig.getCancelUrl());
            merchantInfo.put("isApp", false);
            merchantInfo.put("paymentType", "eComm Regular Payment");
            merchantInfo.put("subMerchantId", "");
            merchantInfo.put("orderId", payment.getTransactionId());
            merchantInfo.put("shippingDetails", new HashMap<>());
            merchantInfo.put("taxDetails", new HashMap<>());
            merchantInfo.put("transactionText", payment.getDescription());

            Map<String, Object> customerInfo = new HashMap<>();
            customerInfo.put("mobileNumber", payment.getPayer().getPhone());

            Map<String, Object> transaction = new HashMap<>();
            transaction.put("orderId", payment.getTransactionId());
            transaction.put("amount", payment.getAmount().multiply(new java.math.BigDecimal("100")).intValue()); // Convert to øre
            transaction.put("transactionText", payment.getDescription());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantInfo", merchantInfo);
            requestBody.put("customerInfo", customerInfo);
            requestBody.put("transaction", transaction);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("X-Request-Id", payment.getTransactionId());
            headers.set("Ocp-Apim-Subscription-Key", vippsConfig.getSubscriptionKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    vippsConfig.getPaymentUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                VippsPaymentResponse vippsResponse = new VippsPaymentResponse();
                vippsResponse.setOrderId((String) responseBody.get("orderId"));
                vippsResponse.setRedirectUrl((String) responseBody.get("redirectUrl"));
                vippsResponse.setReference((String) responseBody.get("reference"));
                vippsResponse.setStatus("INITIATED");
                vippsResponse.setTransactionId(payment.getTransactionId());
                
                return vippsResponse;
            }

            throw new RuntimeException("Failed to initiate Vipps payment");
        } catch (Exception e) {
            log.error("Error initiating Vipps payment: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate Vipps payment", e);
        }
    }

    public boolean verifyPayment(String transactionId) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("X-Request-Id", transactionId);
            headers.set("Ocp-Apim-Subscription-Key", vippsConfig.getSubscriptionKey());

            ResponseEntity<Map> response = restTemplate.exchange(
                    vippsConfig.getPaymentUrl() + "/" + transactionId + "/status",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return "RESERVED".equals(responseBody.get("transactionInfo")) || 
                       "SALE".equals(responseBody.get("transactionInfo"));
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying Vipps payment: {}", e.getMessage());
            return false;
        }
    }

    private String getAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Ocp-Apim-Subscription-Key", vippsConfig.getSubscriptionKey());

            Map<String, String> request = new HashMap<>();
            request.put("grant_type", "client_credentials");
            request.put("client_id", vippsConfig.getClientId());
            request.put("client_secret", vippsConfig.getClientSecret());

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    vippsConfig.getBaseUrl() + "/accessToken/get",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }

            throw new RuntimeException("Failed to get Vipps access token");
        } catch (Exception e) {
            log.error("Error getting Vipps access token: {}", e.getMessage());
            throw new RuntimeException("Failed to get Vipps access token", e);
        }
    }
} 