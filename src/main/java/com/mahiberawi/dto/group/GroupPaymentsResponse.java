package com.mahiberawi.dto.group;

import com.mahiberawi.dto.payment.PaymentResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupPaymentsResponse {
    private String groupId;
    private String groupName;
    private String userRole;
    private List<PaymentResponse> payments;
    private Boolean canCreatePayments;
    private Integer totalPayments;
} 