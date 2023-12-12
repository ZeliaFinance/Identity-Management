package com.zeliafinance.identitymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsResponse {
    private Long id;
    private String transactionRef;
    private String walletId;
    private String transactionType;
    private double transactionAmount;
    private String createdAt;
    private String transactionStatus;
    private String externalRefNumber;
}
