package com.mahiberawi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahiberawi.config.TeleBirrConfig;
import com.mahiberawi.dto.payment.TeleBirrPaymentRequest;
import com.mahiberawi.dto.payment.TeleBirrPaymentResponse;
import com.mahiberawi.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeleBirrService {
    private final TeleBirrConfig teleBirrConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TeleBirrPaymentResponse initiatePayment(Payment payment) {
        try {
            String accessToken = getAccessToken();
            
            TeleBirrPaymentRequest request = TeleBirrPaymentRequest.builder()
                    .outTradeNo(payment.getTransactionId())
                    .totalAmount(payment.getAmount().toString())
                    .subject(payment.getDescription())
                    .notifyUrl(teleBirrConfig.getNotifyUrl())
                    .returnUrl(teleBirrConfig.getReturnUrl())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<TeleBirrPaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<TeleBirrPaymentResponse> response = restTemplate.exchange(
                    teleBirrConfig.getPaymentUrl(),
                    HttpMethod.POST,
                    entity,
                    TeleBirrPaymentResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Failed to initiate TeleBirr payment");
        } catch (Exception e) {
            log.error("Error initiating TeleBirr payment: {}", e.getMessage());
            throw new RuntimeException("Failed to initiate TeleBirr payment", e);
        }
    }

    public boolean verifyPayment(String transactionId) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, String> request = new HashMap<>();
            request.put("outTradeNo", transactionId);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    teleBirrConfig.getVerifyUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return "SUCCESS".equals(responseBody.get("status"));
            }

            return false;
        } catch (Exception e) {
            log.error("Error verifying TeleBirr payment: {}", e.getMessage());
            return false;
        }
    }

    private String getAccessToken() {
        try {
            String credentials = teleBirrConfig.getAppKey() + ":" + teleBirrConfig.getAppSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(encodedCredentials);

            Map<String, String> request = new HashMap<>();
            request.put("grant_type", "client_credentials");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    teleBirrConfig.getTokenUrl(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }

            throw new RuntimeException("Failed to get TeleBirr access token");
        } catch (Exception e) {
            log.error("Error getting TeleBirr access token: {}", e.getMessage());
            throw new RuntimeException("Failed to get TeleBirr access token", e);
        }
    }
} 