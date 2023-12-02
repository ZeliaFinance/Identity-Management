package com.zeliafinance.identitymanagement.thirdpartyapis.paystack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundData {
    private Transaction transaction;
    private Long integration;
    private int deducted_amount;
    private Object channel;
    private String merchant_note;
    private String customer_note;
    private String status;
    private String refunded_by;
    private String expected_at;
    private String currency;
    private String domain;
    private double amount;
    private boolean fully_deducted;
    private Long id;
    private String createdAt;
    private String updatedAt;
}
